package com.alessio89g.mysearchwidget.data

/** Adapt the creation preset horizontally before fixing the launcher's design width.
 * Existing saved configurations are never passed through this function on load. */
fun WidgetConfig.initialWidth(width:Int):WidgetConfig {
 val previous=referenceWidthDp ?: return copy(referenceWidthDp=width)
 if(previous==width)return this
 val delta=width-previous
 val outer=(sizing.outerWidthDp?.plus(delta))?.coerceIn(16f,1000f)
 val field=(sizing.fieldWidthDp?.plus(delta))?.coerceIn(16f,1000f)
 return copy(referenceWidthDp=width,sizing=sizing.copy(outerWidthDp=outer,
  outerRatio=if(sizing.outerLocked && outer!=null)outer/heightDp else null,
  fieldWidthDp=field,fieldRatio=null),placement=placement.copy(detachedFieldWidth=placement.detachedFieldWidth?.plus(delta)?.coerceAtLeast(16f)))
}

/** Global height changes the height, not a width that would immediately be fitted back down. */
fun WidgetConfig.withWidgetHeight(height:Float,canvasWidth:Float):WidgetConfig {
 val w=minOf(sizing.outerWidthDp ?: canvasWidth,canvasWidth)
 return copy(heightDp=height.coerceIn(16f,256f),sizing=sizing.copy(
  outerWidthDp=sizing.outerWidthDp?.let {w},outerRatio=if(sizing.outerLocked)w/height.coerceIn(16f,256f) else null,layoutHeightDp=null))
}
