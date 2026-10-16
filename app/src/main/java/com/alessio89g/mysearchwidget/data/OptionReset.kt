package com.alessio89g.mysearchwidget.data

import kotlinx.serialization.json.*

/** Reset explicit properties on the editor draft; persistence and mode sharing stay with the editor. */
object OptionReset {
 private fun read(root:JsonElement,path:List<String>):JsonElement = if(path.isEmpty())root else
  read(if(root is JsonArray)root[path.first().toInt()] else root.jsonObject.getValue(path.first()),path.drop(1))
 private fun write(root:JsonElement,path:List<String>,value:JsonElement):JsonElement {
  if(path.isEmpty())return value
  val head=path.first();val tail=path.drop(1)
  return if(root is JsonArray)JsonArray(root.mapIndexed {i,e->if(i==head.toInt())write(e,tail,value) else e})
  else JsonObject(root.jsonObject.toMutableMap().apply {put(head,write(get(head) ?: JsonNull,tail,value))})
 }
 fun apply(config:WidgetConfig,defaults:WidgetConfig,paths:List<String>):WidgetConfig {
  val preset=if(config.adaptiveButtons)defaults.copy(sizing=defaults.sizing.copy(fieldWidthDp=null,fieldRatio=null),placement=defaults.placement.copy(field=OffsetDp()),buttons=defaults.buttons.map {it.copy(position=OffsetDp())}) else defaults
  var result=config
  for(path in paths) {
   when {
    path=="placement.fieldLinked" -> result=result.linkField(defaults.placement.fieldLinked,(config.referenceWidthDp ?: 374).toFloat())
    path.endsWith(".iconLinked") -> {val i=path.split('.')[1].toInt();result=result.copy(buttons=result.buttons.mapIndexed {n,s->if(n==i)s.linkIcon(defaults.buttons[i].iconLinked) else s})}
    path=="count" -> result=result.withButtonCount(defaults.count,defaults)
    path.startsWith("haptics.") -> {val key=path.substringAfter('.');result=result.copy(haptics=result.haptics+(key to defaults.hapticEnabled(key)))}
    path.startsWith("layer:") -> {
     val element=path.substringAfter(':');val layers=result.placement.layers.toMutableList()
     layers.remove(element);layers.add(defaults.placement.layers.indexOf(element).coerceIn(0,layers.size),element)
     result=result.copy(placement=result.placement.copy(layers=layers))
    }
    else -> {
     val parts=path.split('.')
     val value=read(Catalog.json.encodeToJsonElement(preset),parts)
     result=Catalog.json.decodeFromJsonElement(write(Catalog.json.encodeToJsonElement(result),parts,value))
     if(path.startsWith("hint."))result=result.copy(hintRuns=result.hintRuns.map {run->
      run.copy(style=Catalog.json.decodeFromJsonElement(write(Catalog.json.encodeToJsonElement(run.style),parts.drop(1),value)))
     })
     if(path=="placeholder")result=result.copy(hintRuns=RichText.edit(config.placeholder,result.placeholder,result.hintRuns,result.hint))
     if(path.endsWith(".asset") && value.jsonPrimitive.content.isNotEmpty()) {
      val id=value.jsonPrimitive.content
      result=result.copy(assets=result.assets+(id to defaults.assets.getValue(id)))
     }
    }
   }
  }
  // A one-axis reset must not retain a stale aspect ratio that undoes the reset at render time.
  if(paths.any {it in listOf("sizing.outerWidthDp","heightDp","sizing.outerLocked")})result=result.copy(sizing=result.sizing.copy(outerRatio=if(result.sizing.outerLocked)result.sizing.outerWidthDp?.div(result.heightDp) else null))
  if(paths.any {it in listOf("sizing.fieldWidthDp","sizing.fieldHeightDp","sizing.fieldLocked")})result=result.copy(sizing=result.sizing.copy(fieldRatio=if(result.sizing.fieldLocked && result.sizing.fieldWidthDp!=null && result.sizing.fieldHeightDp!=null)result.sizing.fieldWidthDp!!/result.sizing.fieldHeightDp!! else null))
  result=result.copy(buttons=result.buttons.mapIndexed {i,s->if(paths.any {it in listOf("buttons.$i.widthDp","buttons.$i.heightDp","buttons.$i.proportionsLocked")})s.copy(aspectRatio=if(s.proportionsLocked && s.widthDp!=null && s.heightDp!=null)s.widthDp/s.heightDp else null) else s})
  return result
 }
}
