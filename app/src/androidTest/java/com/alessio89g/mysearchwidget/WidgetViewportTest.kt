package com.alessio89g.mysearchwidget

import android.appwidget.AppWidgetManager
import android.content.res.Configuration
import android.os.Bundle
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
class WidgetViewportTest {
 @Test fun previewAndHomeUseSameAllocationAcrossOrientations() {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val options=Bundle().apply {
   putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,384)
   putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,600)
   putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,48)
   putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,100)
  }
  val config=WidgetConfig(heightDp=69f,count=3,sizing=ElementSizing(fieldWidthDp=193f,fieldHeightDp=47f))
  for(orientation in listOf(Configuration.ORIENTATION_PORTRAIT,Configuration.ORIENTATION_LANDSCAPE)) {
   val host=context.createConfigurationContext(Configuration(context.resources.configuration).apply {this.orientation=orientation})
   val viewport=WidgetViewport.from(host,options)
   assertEquals(if(orientation==Configuration.ORIENTATION_PORTRAIT)384 else 600,viewport.width)
   assertEquals(if(orientation==Configuration.ORIENTATION_PORTRAIT)69f else 48f,viewport.fit(config).heightDp,0f)
   assertEquals(69f,config.heightDp,0f)
  }
 }
 @Test fun paddingChangesDoNotStretchArtworkAwayFromTouchTargets() {
  val ins=InstrumentationRegistry.getInstrumentation();val context=ins.targetContext
  ins.runOnMainSync {
   val config=WidgetConfig(heightDp=69f,count=3,sizing=ElementSizing(fieldWidthDp=193f,fieldHeightDp=47f))
   val density=context.resources.displayMetrics.density
   val width=384
   val dimensions=ElementLayout(width.toFloat(),config)
   for(hostWidth in listOf(368,384,400)) {
    val root=SearchWidget.views(context,987659,config,width).apply(context,FrameLayout(context))
    root.measure(View.MeasureSpec.makeMeasureSpec((hostWidth*density).roundToInt(),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec((100*density).roundToInt(),View.MeasureSpec.EXACTLY))
    root.layout(0,0,root.measuredWidth,root.measuredHeight)
    val frame=root.findViewById<View>(R.id.widget_frame)
    val art=root.findViewById<View>(R.id.art)
    assertEquals((width*density).roundToInt(),frame.width)
    assertEquals(frame.width,art.width)
    assertEquals(frame.height,art.height)
    val button=root.findViewById<View>(R.id.button0)
    assertTrue(kotlin.math.abs(button.left-(dimensions.buttonHits[0].left*density).roundToInt())<=1)
    assertEquals((dimensions.buttonHits[0].width*density).roundToInt(),button.width)
   }
  }
 }
}
