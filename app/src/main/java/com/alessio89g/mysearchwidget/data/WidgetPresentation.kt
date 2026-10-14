package com.alessio89g.mysearchwidget.data

/** Reflow horizontally; only a genuinely short host allocation scales the composition. */
class WidgetPresentation(requested:WidgetConfig,availableWidth:Int,availableHeight:Float?=null) {
 val designWidth=availableWidth.coerceIn(180,1000)
 // This is a render-only copy. Saved sizes and manual offsets remain untouched.
 val config=requested.initialWidth(designWidth).copy(referenceWidthDp=requested.referenceWidthDp ?: designWidth).fitOuter(designWidth.toFloat())
 val scale=minOf(1f,availableHeight?.div(config.heightDp) ?: 1f)
 val width=designWidth*scale
 val height=config.heightDp*scale
 fun transform(bounds:Bounds)=Bounds(bounds.left*scale,bounds.top*scale,bounds.right*scale,bounds.bottom*scale)
}
