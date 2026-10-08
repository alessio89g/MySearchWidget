package com.alessio89g.mysearchwidget.data

/** A stable design canvas, scaled as one unit into the launcher allocation. */
class WidgetPresentation(requested:WidgetConfig,availableWidth:Int,availableHeight:Float?=null) {
 val designWidth=requested.referenceWidthDp ?: availableWidth.coerceIn(180,1000)
 val config=requested.fitOuter(designWidth.toFloat())
 val scale=minOf(availableWidth.toFloat()/designWidth,availableHeight?.div(config.heightDp) ?: Float.POSITIVE_INFINITY)
 val width=designWidth*scale
 val height=config.heightDp*scale
 fun transform(bounds:Bounds)=Bounds(bounds.left*scale,bounds.top*scale,bounds.right*scale,bounds.bottom*scale)
}
