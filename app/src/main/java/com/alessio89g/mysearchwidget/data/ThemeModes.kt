package com.alessio89g.mysearchwidget.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/** Light is the base. Only explicitly separated properties receive a dark override.
 * Color pairs retain their original representation, including old backups. */
@Serializable data class ThemeModes(
 val separate:Set<String> = COLOR_GROUPS.toSet(),
 val dark:Map<String,JsonElement> = emptyMap(),
 val sharedRuns:Map<String,JsonElement> = emptyMap()
)
val COLOR_GROUPS=listOf("color.outer","color.field","color.logo","color.hint","color.query")+
 (0..2).flatMap {listOf("color.button$it","color.icon$it")}
object ModeSettings {
 private val runPaths=setOf("hintRuns","hintRunSizes","hintRunFonts","hintRunColors")
 private fun runParts(runs:List<TextRun>,part:String)=runs.map {r->
  val s=r.style
  r.copy(style=when(part){
   "size"->TextStyle(size=s.size)
   "font"->TextStyle(font=s.font)
   "color"->TextStyle(light=s.light,dark=s.dark,lightGradient=s.lightGradient,darkGradient=s.darkGradient)
   else->s.copy(font="",size=15f,light="",dark="",lightGradient=Gradient(),darkGradient=Gradient())
  })
 }
 private fun combineRuns(c:WidgetConfig,format:List<TextRun>,sizes:List<TextRun>,colors:List<TextRun>,fonts:List<TextRun>):List<TextRun> {
  val all=format+sizes+colors+fonts
  val points=(all.flatMap {listOf(it.start,it.end)}+listOf(0,c.placeholder.length)).map {it.coerceIn(0,c.placeholder.length)}.distinct().sorted()
  var result=emptyList<TextRun>()
  for((a,b) in points.zipWithNext())if(a<b && all.any {it.start<=a && it.end>a}) {
   val f=RichText.styleAt(format,a,c.hint);val z=RichText.styleAt(sizes,a,c.hint);val p=RichText.styleAt(colors,a,c.hint)
   result=RichText.apply(result,a,b,f.copy(font=RichText.styleAt(fonts,a,c.hint).font,size=z.size,light=p.light,dark=p.dark,lightGradient=p.lightGradient,darkGradient=p.darkGradient))
  }
  return result
 }

 val elements=DEFAULT_LAYERS
 val groups=(elements.map {"size.$it"}+elements.map {"position.$it"}+elements.map {"layer.$it"}+COLOR_GROUPS+
 listOf("search.engine","search.google","logo","count","text.hint","text.query","font.hint","font.query","placeholder","size.query","shape.outer","shape.field")+
 (0..2).flatMap {listOf("shape.button$it","icon.$it")}+
 listOf("action.field","action.logo")+(0..2).map {"action.button$it"}).toSet()
 private fun flat(c:WidgetConfig):Map<String,JsonElement> {
  val out=linkedMapOf<String,JsonElement>()
  fun visit(e:JsonElement,path:String) {
   if(path=="modes")return
   when {
    path=="placement.layers" -> e.jsonArray.forEachIndexed {i,v->out["layer:${v.jsonPrimitive.content}"]=JsonPrimitive(i)}
    path=="hintRuns" -> {
     out[path]=Catalog.json.encodeToJsonElement(runParts(c.hintRuns,"format"))
     out["hintRunFonts"]=Catalog.json.encodeToJsonElement(runParts(c.hintRuns,"font"))
     out["hintRunSizes"]=Catalog.json.encodeToJsonElement(runParts(c.hintRuns,"size"))
     out["hintRunColors"]=Catalog.json.encodeToJsonElement(runParts(c.hintRuns,"color"))
    }
    path=="assets" -> out[path]=e
    e is JsonObject -> e.forEach {(k,v)->visit(v,if(path.isEmpty())k else "$path.$k")}
    e is JsonArray -> e.forEachIndexed {i,v->visit(v,"$path.$i")}
    else -> out[path]=e
   }
  }
  visit(Catalog.json.encodeToJsonElement(c.copy(modes=ThemeModes(),haptics=listOf("field","logo","button0","button1","button2").associateWith {c.hapticEnabled(it)})),"")
  return out
 }
 private fun rebuild(c:WidgetConfig,values:Map<String,JsonElement>):WidgetConfig {
  if(values.isEmpty())return c
  fun visit(e:JsonElement,path:String):JsonElement {
   values[path]?.let {return it}
   if(path=="placement.layers")return JsonArray(c.placement.layers.sortedWith(compareBy<String> {values["layer:$it"]?.jsonPrimitive?.int ?: c.placement.layers.indexOf(it)}.thenBy {val old=c.placement.layers.indexOf(it);val next=values["layer:$it"]?.jsonPrimitive?.int ?: old;(next-old).coerceIn(-1,1)}).map(::JsonPrimitive))
   return when(e) {
    is JsonObject->{
     val extra=if(path=="haptics")values.filterKeys {it.startsWith("haptics.")}.mapKeys {it.key.substringAfter('.')} else emptyMap()
     JsonObject((e+extra).mapValues {(k,v)->visit(v,if(path.isEmpty())k else "$path.$k")})
    }
    is JsonArray->JsonArray(e.mapIndexed {i,v->visit(v,"$path.$i")})
    else->e
   }
  }
  val result=Catalog.json.decodeFromJsonElement<WidgetConfig>(visit(Catalog.json.encodeToJsonElement(c),""))
  fun runs(key:String,part:String):List<TextRun> = values[key]?.let {Catalog.json.decodeFromJsonElement<List<TextRun>>(it)} ?: runParts(c.hintRuns,part)
  return result.copy(hintRuns=combineRuns(result,runs("hintRuns","format"),runs("hintRunSizes","size"),runs("hintRunColors","color"),runs("hintRunFonts","font")))
 }
 fun group(path:String):String? {
  if(path=="hintRunFonts")return "font.hint"
  if(path=="hintRunSizes")return "size.text"
  if(path=="hintRunColors")return "color.hint"
  if(path.startsWith("layer:"))return "layer."+path.substringAfter(':')
  if(path=="engineId")return "search.engine"
  if(path=="googleInput")return "search.google"
  if(path=="count")return "count"
  if(path=="placeholder")return "placeholder"
  if(path=="hintRuns")return "text.hint"
  if(path.startsWith("haptics."))return "action."+path.substringAfter('.')
  if(path=="heightDp" || path.startsWith("sizing.outer") || path in listOf("sizing.sidePaddingDp","sizing.layoutHeightDp"))return "size.outer"
  if(path.startsWith("sizing.field") || path.startsWith("placement.detachedField"))return "size.field"
  if(path=="sizing.logoInsetDp")return "size.logo"
  if(path=="sizing.textGapDp")return "size.text"
  if(path.startsWith("placement."))return "position."+when(path.split('.')[1]){"fieldLinked"->"field";else->path.split('.')[1]}
  val parts=path.split('.')
  if(parts[0] in listOf("hint","query")) {
   val element=parts[0]
   return if(parts[1]=="font")"font.$element" else if(parts[1] in listOf("light","dark","lightGradient","darkGradient"))"color.$element" else if(parts[1]=="size")if(element=="hint")"size.text" else "size.query" else "text.$element"
  }
  if(parts[0] in listOf("outer","field"))return if(parts[1] in listOf("light","dark"))"color.${parts[0]}" else "shape.${parts[0]}"
  if(parts[0]=="logo" || parts[0]=="buttons") {
   val logo=parts[0]=="logo";val n=if(logo)"" else parts[1]
   val rest=parts.drop(if(logo)1 else 2);val element=if(logo)"logo" else "button$n"
   return when(rest[0]) {
    "tap","up","down"->"action.$element"
    "position","iconLinked"->"position.$element"
    "iconPosition"->"position."+if(logo)"logo" else "icon$n"
    "icon"->when(rest[1]) {
     "sizeDp"->"size."+if(logo)"logo" else "icon$n"
     "light","dark","lightGradient","darkGradient"->"color."+if(logo)"logo" else "icon$n"
     else->if(logo)"logo" else "icon.$n"
    }
    "surface"->if(rest[1] in listOf("light","dark"))"color.$element" else if(logo)"logo" else "shape.$element"
    else->"size.$element"
   }
  }
  return null
 }
 fun resolve(c:WidgetConfig,dark:Boolean):WidgetConfig = rebuild(c,c.modes.sharedRuns+
  if(dark)c.modes.dark.filterKeys {group(it) in c.modes.separate} else emptyMap()).copy(modes=ThemeModes())
 private fun mirrorColors(c:WidgetConfig,groups:Set<String>,dark:Boolean):WidgetConfig {
  val values=flat(c);val changes=mutableMapOf<String,JsonElement>()
  values.forEach {(path,value)->
   if(group(path) in groups && group(path)?.startsWith("color.")==true) {
    val source=if(dark)"dark" else "light";val dest=if(dark)"light" else "dark"
    val parts=path.split('.').map {if(it==source)dest else if(it==source+"Gradient")dest+"Gradient" else it}
    if(parts.joinToString(".")!=path) {
     val emptyColor=value is JsonPrimitive && value.isString && value.content.isEmpty() && (path.endsWith(".color") || path.substringAfterLast('.')==source)
     val fallback=when {
      group(path)=="color.outer"->if(dark)"#474645" else "#E5E2DA"
      path.endsWith(".color")->if(dark)"#000000" else "#FFFFFF"
      else->if(dark)"#B8B3A1" else "#48463B"
     }
     val shared=if(emptyColor)JsonPrimitive(fallback) else value
     changes[parts.joinToString(".")]=shared
     if(emptyColor)changes[path]=shared
    }
   }
  }
  val base=rebuild(c,changes)
  if("color.hint" !in groups)return base
  return base.copy(hintRuns=base.hintRuns.map {run->val s=run.style;val color=(if(dark)s.dark else s.light).ifEmpty {if(dark)"#B8B3A1" else "#48463B"};run.copy(style=if(dark)s.copy(light=color,dark=color,lightGradient=s.darkGradient) else s.copy(light=color,dark=color,darkGradient=s.lightGradient))})
 }
 fun edit(c:WidgetConfig,edited:WidgetConfig,dark:Boolean):WidgetConfig {
  val before=flat(resolve(c,dark));val next=flat(edited)
  val oldDark=flat(resolve(c,true))
  val common=mutableMapOf<String,JsonElement>();val overrides=c.modes.dark.toMutableMap();val shared=c.modes.sharedRuns.toMutableMap()
  next.forEach {(path,value)->if(before[path]!=value) {
   val group=group(path)
   val separate=group in c.modes.separate && (path in runPaths || group?.startsWith("color.")!=true)
   if(separate && dark)overrides[path]=value
   else {
    common[path]=value
    if(path in runPaths) {
     if(separate){if(path !in overrides)oldDark[path]?.let {overrides[path]=it};shared.remove(path)}
     else {shared[path]=value;overrides.remove(path)}
    } else if(group !in c.modes.separate)overrides.remove(path)
   }
  }}
  var result=rebuild(c,common).copy(modes=c.modes.copy(dark=overrides,sharedRuns=shared))
  result=shareColors(result,COLOR_GROUPS.toSet()-c.modes.separate,dark)
  return result
 }
 private fun shareColors(c:WidgetConfig,groups:Set<String>,dark:Boolean):WidgetConfig {
  var result=mirrorColors(c,groups,dark)
  if("color.hint" in groups) {
   val active=mirrorColors(resolve(c,dark),setOf("color.hint"),dark)
   val paint=flat(active).getValue("hintRunColors")
   result=rebuild(result,mapOf("hintRunColors" to paint)).copy(modes=result.modes.copy(
    dark=result.modes.dark-"hintRunColors",sharedRuns=result.modes.sharedRuns+("hintRunColors" to paint)))
  }
  return result
 }
 fun toggle(c:WidgetConfig,key:String,enabled:Boolean,dark:Boolean):WidgetConfig {
  require(key in groups)
  if(enabled==(key in c.modes.separate))return c
  val lightValues=flat(resolve(c,false));val darkValues=flat(resolve(c,true));val current=if(dark)darkValues else lightValues
  var result=c
  val overrides=c.modes.dark.toMutableMap();val shared=c.modes.sharedRuns.toMutableMap()
  if(enabled) {
   val light=lightValues.filterKeys {group(it)==key && (it in runPaths || !key.startsWith("color."))}
   result=rebuild(c,light)
   darkValues.filterKeys {group(it)==key && (it in runPaths || !key.startsWith("color."))}.forEach {(k,v)->overrides[k]=v}
   shared.keys.removeAll {group(it)==key}
  } else {
   val values=current.filterKeys {group(it)==key && (it in runPaths || !key.startsWith("color."))}
   result=rebuild(c,values)
   values.filterKeys {it in runPaths}.forEach {(k,v)->shared[k]=v}
   overrides.keys.removeAll {group(it)==key}
  }
  result=result.copy(modes=ThemeModes(if(enabled)c.modes.separate+key else c.modes.separate-key,overrides,shared))
  return if(!enabled && key.startsWith("color."))shareColors(result,setOf(key),dark) else result
 }
 /** Compare the resulting shared configuration, including automatic theme colors. */
 fun hasDifferentValues(c:WidgetConfig,key:String):Boolean =
  key in c.modes.separate && toggle(c,key,false,false)!=toggle(c,key,false,true)
 fun validate(c:WidgetConfig) {
  require(c.modes.separate.all {it in groups})
  val paths=flat(c).keys
  require(c.modes.dark.keys.all {it in paths && group(it) in c.modes.separate && (it in runPaths || group(it)?.startsWith("color.")!=true)})
  require(c.modes.dark.size<=400)
  require(c.modes.sharedRuns.keys.all {it in runPaths && group(it) !in c.modes.separate})
  for((path,value) in c.modes.dark+c.modes.sharedRuns)if(path in runPaths) {
   val runs=Catalog.json.decodeFromJsonElement<List<TextRun>>(value)
   require(runs.size<=500 && runs.all {it.start>=0 && it.end>it.start && it.end<=500})
   Validation.config(c.copy(placeholder=" ".repeat(500),hintRuns=runs,modes=ThemeModes()),false)
  }
  c.modes.dark.filterKeys {it.startsWith("layer:")}.values.forEach {require(it is JsonPrimitive && !it.isString && it.intOrNull in 0 until DEFAULT_LAYERS.size)}
  Validation.config(resolve(c,true),false)
 }
}
