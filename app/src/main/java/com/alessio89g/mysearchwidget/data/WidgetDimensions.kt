package com.alessio89g.mysearchwidget.data

/** Shared by bitmap rendering and the launcher hit targets. All artwork uses a 64dp baseline. */
class WidgetDimensions(val width:Float,val height:Float,val count:Int) {
 companion object {
  const val DEFAULT_HEIGHT=64f
  const val MIN_HEIGHT=16f
  const val MAX_HEIGHT=256f
 }
 // Keep a usable search field when a tall widget has little horizontal space.
 val scale=minOf(height/DEFAULT_HEIGHT,width/(104f+count*52f))
 val canvasWidth=width/scale
 val verticalInset=(height/scale-DEFAULT_HEIGHT)/2f
}
