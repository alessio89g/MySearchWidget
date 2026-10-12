package com.alessio89g.mysearchwidget.data

/** Explicit resets use the creation preset; loading existing widgets never calls these. */
fun WidgetConfig.resetWidgetHeight(defaults:WidgetConfig):WidgetConfig = copy(
 heightDp=defaults.heightDp,
 sizing=sizing.copy(outerWidthDp=defaults.sizing.outerWidthDp,outerRatio=defaults.sizing.outerRatio.takeIf {sizing.outerLocked},layoutHeightDp=defaults.sizing.layoutHeightDp)
)

/** Reset only the selected dimensions; retain positions, links, styling and actions. */
fun WidgetConfig.resetElementDimensions(element:String,defaults:WidgetConfig):WidgetConfig = when(element) {
 "outer"->copy(heightDp=defaults.heightDp,sizing=sizing.copy(outerWidthDp=defaults.sizing.outerWidthDp,outerRatio=defaults.sizing.outerRatio.takeIf {sizing.outerLocked},sidePaddingDp=defaults.sizing.sidePaddingDp,layoutHeightDp=sizing.layoutHeightDp ?: heightDp))
 "field"->copy(sizing=sizing.copy(fieldWidthDp=defaults.sizing.fieldWidthDp,fieldHeightDp=defaults.sizing.fieldHeightDp,fieldRatio=defaults.sizing.fieldRatio.takeIf {sizing.fieldLocked}))
 "logo"->copy(logo=logo.copy(icon=logo.icon.copy(sizeDp=defaults.logo.icon.sizeDp)),sizing=sizing.copy(logoInsetDp=defaults.sizing.logoInsetDp))
 "text"->copy(hint=hint.copy(size=defaults.hint.size),query=query.copy(size=defaults.query.size),hintRuns=hintRuns.map {it.copy(style=it.style.copy(size=defaults.hint.size))},sizing=sizing.copy(textGapDp=defaults.sizing.textGapDp))
 else->{
  require(element in DEFAULT_LAYERS)
  val index=element.last().digitToInt();val target=defaults.buttons[index]
  copy(buttons=buttons.mapIndexed {i,s->if(i!=index)s else if(element.startsWith("button"))s.copy(widthDp=target.widthDp,heightDp=target.heightDp,aspectRatio=target.aspectRatio.takeIf {s.proportionsLocked},gapDp=target.gapDp) else s.copy(icon=s.icon.copy(sizeDp=target.icon.sizeDp))})
 }
}
/** Restore this element's preset offset, retaining the current movement links. */
fun WidgetConfig.resetElementPosition(element:String,defaults:WidgetConfig):WidgetConfig = when(element) {
 "outer"->copy(placement=placement.copy(outer=defaults.placement.outer))
 "field"->copy(placement=placement.copy(field=defaults.placement.field))
 "logo"->copy(placement=placement.copy(logo=defaults.placement.logo))
 "text"->copy(placement=placement.copy(text=defaults.placement.text))
 else->{
  require(element in DEFAULT_LAYERS)
  val index=element.last().digitToInt();val target=defaults.buttons[index]
  copy(buttons=buttons.mapIndexed {i,s->if(i!=index)s else if(element.startsWith("button"))s.copy(position=target.position) else s.copy(iconPosition=target.iconPosition)})
 }
}
