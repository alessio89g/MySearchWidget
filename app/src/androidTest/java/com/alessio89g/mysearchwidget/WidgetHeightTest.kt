package com.alessio89g.mysearchwidget

import android.view.View
import android.widget.FrameLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class WidgetHeightTest {
 @Test fun bitmapsAndTouchTargetsFollowHeightForBothThemesAndQuery() {
  val instrumentation=InstrumentationRegistry.getInstrumentation()
  val context=instrumentation.targetContext
  instrumentation.runOnMainSync {
   val density=context.resources.displayMetrics.density
   for(height in listOf(16f,48.5f,64f,96f,256f))for(count in 0..3)for(theme in listOf("light","dark","system")) {
    val c=WidgetConfig(heightDp=height,count=count,theme=theme)
    val width=356
    val dimensions=WidgetDimensions(width.toFloat(),height,count)
    for(session in listOf(null,SessionText("Search query",4))) {
     val bitmap=Renderer.render(context,c,width,session)
     assertEquals((height*density.coerceAtMost(3f)).roundToInt(),bitmap.height)
     bitmap.recycle()
    }
    // Parcel like a real launcher before applying the RemoteViews actions.
    val parcel=android.os.Parcel.obtain()
    val rv=try {
     SearchWidget.views(context,987659,c,width).writeToParcel(parcel,0)
     parcel.setDataPosition(0)
     android.widget.RemoteViews.CREATOR.createFromParcel(parcel)
    } finally {parcel.recycle()}
    val root=rv.apply(context,FrameLayout(context))
    root.measure(View.MeasureSpec.makeMeasureSpec((width*density).roundToInt(),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec((height*density).roundToInt(),View.MeasureSpec.EXACTLY))
    root.layout(0,0,root.measuredWidth,root.measuredHeight)
    assertEquals((height*density).roundToInt(),root.findViewById<View>(R.id.widget_frame).height)
    assertEquals((52*dimensions.scale*density).roundToInt(),root.findViewById<View>(R.id.hit_row).height)
    assertEquals((44*dimensions.scale*density).roundToInt(),root.findViewById<View>(R.id.logo).width)
    assertTrue(root.findViewById<View>(R.id.input).width>0)
    if(count>0)assertEquals((52*dimensions.scale*density).roundToInt(),root.findViewById<View>(R.id.button0).width)
   }
  }
 }
}
