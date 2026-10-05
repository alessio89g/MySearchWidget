package com.alessio89g.mysearchwidget.data

/** Reset only the selected dimensions; retain positions, links, styling and actions. */
fun WidgetConfig.resetElementDimensions(element:String):WidgetConfig = when(element) {
 "outer"->copy(heightDp=WidgetDimensions.DEFAULT_HEIGHT,sizing=sizing.copy(outerWidthDp=null,outerRatio=null,layoutHeightDp=sizing.layoutHeightDp ?: heightDp))
 "field"->copy(sizing=sizing.copy(fieldWidthDp=null,fieldHeightDp=null,fieldRatio=null))
 "logo"->copy(logo=logo.copy(icon=logo.icon.copy(sizeDp=null)))
 "text"->copy(hint=hint.copy(size=TextStyle().size),query=query.copy(size=TextStyle().size),hintRuns=hintRuns.map {it.copy(style=it.style.copy(size=TextStyle().size))})
 else->{
  require(element in DEFAULT_LAYERS)
  val index=element.last().digitToInt()
  copy(buttons=buttons.mapIndexed {i,s->if(i!=index)s else if(element.startsWith("button"))s.copy(widthDp=null,heightDp=null,aspectRatio=null) else s.copy(icon=s.icon.copy(sizeDp=null))})
 }
}
/** Zero is the automatic anchor, relative to the parent while movement is linked. */
fun WidgetConfig.resetElementPosition(element:String):WidgetConfig = when(element) {
 "outer"->copy(placement=placement.copy(outer=OffsetDp()))
 "field"->copy(placement=placement.copy(field=OffsetDp()))
 "logo"->copy(placement=placement.copy(logo=OffsetDp()))
 "text"->copy(placement=placement.copy(text=OffsetDp()))
 else->{
  require(element in DEFAULT_LAYERS)
  val index=element.last().digitToInt()
  copy(buttons=buttons.mapIndexed {i,s->if(i!=index)s else if(element.startsWith("button"))s.copy(position=OffsetDp()) else s.copy(iconPosition=OffsetDp())})
 }
}
