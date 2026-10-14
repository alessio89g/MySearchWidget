package com.alessio89g.mysearchwidget.data

/** Adopt count-responsive slots on an explicit count edit, never while loading a widget. */
fun WidgetConfig.withButtonCount(value:Int,defaults:WidgetConfig):WidgetConfig {
 require(value in 0..3)
 if(value==count)return this
 if(adaptiveButtons)return copy(count=value)
 val preset=defaults.initialWidth(referenceWidthDp ?: defaults.referenceWidthDp ?: 374)
 val automaticWidth=sizing.fieldWidthDp==preset.sizing.fieldWidthDp
 return copy(count=value,adaptiveButtons=true,
  sizing=sizing.copy(fieldWidthDp=sizing.fieldWidthDp.takeUnless {automaticWidth},fieldRatio=sizing.fieldRatio.takeUnless {automaticWidth}),
  placement=placement.copy(field=placement.field-defaults.placement.field),
  buttons=buttons.mapIndexed {i,s->s.copy(position=s.position-defaults.buttons[i].position)})
}
