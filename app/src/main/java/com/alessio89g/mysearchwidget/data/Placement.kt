package com.alessio89g.mysearchwidget.data

import kotlinx.serialization.Serializable

/** Translation from the automatic anchor, in dp. Positive axes point right/down. */
@Serializable data class OffsetDp(val x:Float=0f,val y:Float=0f) {
 operator fun plus(other:OffsetDp)=OffsetDp(x+other.x,y+other.y)
 operator fun minus(other:OffsetDp)=OffsetDp(x-other.x,y-other.y)
}
val DEFAULT_LAYERS=listOf("outer","field","logo","button0","icon0","button1","icon1","button2","icon2","text")
@Serializable data class Placement(
 val outer:OffsetDp=OffsetDp(),val field:OffsetDp=OffsetDp(),
 val logo:OffsetDp=OffsetDp(),val text:OffsetDp=OffsetDp(),val fieldLinked:Boolean=true,val layers:List<String> = DEFAULT_LAYERS,
 val detachedFieldWidth:Float?=null,val detachedFieldHeight:Float?=null
)
fun Bounds.shift(offset:OffsetDp)=Bounds(left+offset.x,top+offset.y,right+offset.x,bottom+offset.y)
/** Change membership without making existing artwork jump. */
fun WidgetConfig.linkField(linked:Boolean,width:Float):WidgetConfig {
 val before=ElementLayout(width,this)
 val changed=copy(placement=placement.copy(fieldLinked=linked,
  detachedFieldWidth=if(linked)placement.detachedFieldWidth else before.field.width,
  detachedFieldHeight=if(linked)placement.detachedFieldHeight else before.field.height))
 val after=ElementLayout(width,changed)
 return changed.copy(placement=changed.placement.copy(
  logo=placement.logo+OffsetDp(before.logo.centerX-after.logo.centerX,before.logo.top-after.logo.top),
  text=placement.text+OffsetDp(before.textLeft-after.textLeft,before.textCenterY-after.textCenterY)
 ))
}
fun Slot.linkIcon(linked:Boolean):Slot = if(linked==iconLinked)this else copy(iconLinked=linked,
 iconPosition=iconPosition+if(linked)OffsetDp()-position else position)

/** Only the intersection can be touched; artwork itself may be outside the canvas. */
fun Bounds.visibleWithin(width:Float,height:Float):Bounds? {
 val l=maxOf(0f,left);val t=maxOf(0f,top);val r=minOf(width,right);val b=minOf(height,bottom)
 return if(r>l && b>t)Bounds(l,t,r,b) else null
}

fun WidgetConfig.visibleLayers():List<String> = placement.layers.filter {id->
 !id.startsWith("button") && !id.startsWith("icon") || id.last().digitToInt()<count
}
fun WidgetConfig.moveLayer(id:String,forward:Boolean):WidgetConfig {
 val visible=visibleLayers();val index=visible.indexOf(id);val next=index+if(forward)1 else -1
 if(index<0 || next !in visible.indices)return this
 val order=placement.layers.toMutableList();val a=order.indexOf(id);val b=order.indexOf(visible[next])
 order[a]=order[b].also {order[b]=order[a]}
 return copy(placement=placement.copy(layers=order))
}
