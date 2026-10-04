package com.alessio89g.mysearchwidget.data

object AspectRatio {
 /** Clamp the pair together so reaching a limit never changes its ratio. */
 fun resize(value:Float,widthChanged:Boolean,ratio:Float,minWidth:Float,maxWidth:Float,minHeight:Float,maxHeight:Float):Pair<Float,Float> {
  require(ratio.isFinite() && ratio>0f)
  val low=maxOf(minWidth,minHeight*ratio)
  val high=minOf(maxWidth,maxHeight*ratio)
  require(low<=high+.001f)
  val width=(if(widthChanged)value else value*ratio).coerceIn(low,minOf(maxWidth,maxOf(low,high)))
  return width to width/ratio
 }
}

/** Render-only fit: never overwrite the saved dimensions when the launcher is smaller. */
fun WidgetConfig.fitOuter(width:Float,availableHeight:Float=heightDp):WidgetConfig {
 val h=minOf(heightDp,availableHeight)
 if(sizing.outerRatio==null)return copy(heightDp=h)
 val w=sizing.outerWidthDp ?: heightDp*sizing.outerRatio
 val factor=minOf(1f,width/w,h/heightDp)
 return copy(heightDp=heightDp*factor,sizing=sizing.copy(outerWidthDp=w*factor))
}
