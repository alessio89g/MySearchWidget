package com.alessio89g.mysearchwidget

import android.graphics.*
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.Renderer
import org.junit.Assert.*
import org.junit.Test

class ButtonShapesTest {
 private val context=InstrumentationRegistry.getInstrumentation().targetContext
 @Test fun referenceContoursAreSolidBoundedAndScaleIndependently() {
  for(name in Catalog.shapes) {
   val s=Surface(shape=name)
   val a=Renderer.shape(RectF(0f,0f,200f,200f),s)
   val b=Renderer.shape(RectF(300f,20f,360f,100f),s)
   val bounds=RectF();a.computeBounds(bounds,true)
   // computeBounds includes Bezier control handles; check the filled contour instead.
   val filled=Region();filled.setPath(a,Region(-10,-10,210,210))
   assertTrue(name,Rect(0,0,200,200).contains(filled.bounds))
   val region=Region();region.setPath(a,Region(-1,-1,202,202))
   assertTrue("solid center: $name",region.contains(100,100))
   assertFalse(region.contains(201,201))
   val filled2=Region();filled2.setPath(b,Region(290,10,370,110))
   assertTrue(name,Rect(300,20,360,100).contains(filled2.bounds))
   // Requesting another size must leave the original/cached outline unchanged.
   val again=RectF();a.computeBounds(again,true);assertEquals(bounds,again)
  }
 }
 @Test fun shapesHaveDistinctSilhouettesWithoutReferenceImageContents() {
  val fingerprints=mutableSetOf<Int>()
  for(name in Catalog.shapes) {
   val bitmap=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888)
   Canvas(bitmap).drawPath(Renderer.shape(RectF(2f,2f,126f,126f),Surface(shape=name)),Paint().apply {color=Color.WHITE})
   val pixels=IntArray(128*128);bitmap.getPixels(pixels,0,128,0,0,128,128)
   assertTrue("distinct shape $name",fingerprints.add(pixels.contentHashCode()))
   for(y in 48..80)for(x in 48..80)assertEquals("no internal cutout $name",Color.WHITE,bitmap.getPixel(x,y))
   bitmap.recycle()
  }
 }
 @Test fun everyShapeRendersInWidgetWithThemesAndGradients() {
  for(name in Catalog.shapes)for(theme in listOf("light","dark"))for(width in listOf(180,356,600)) {
   val base=WidgetConfig(count=3,theme=theme,dynamic=false)
   val c=base.copy(buttons=base.buttons.map {it.copy(surface=it.surface.copy(shape=name,light=Tone(gradient=Gradient(true)),dark=Tone(gradient=Gradient(true))))})
   Renderer.render(context,c,width).let {assertTrue(it.width>0);it.recycle()}
  }
 }
 @Test fun exportShapeGalleryFromActualAndroidRenderer() {
  val names=listOf("flower","clover","leaf","pebble","scallop","teardrop")
  val b=Bitmap.createBitmap(960,220,Bitmap.Config.ARGB_8888);val canvas=Canvas(b)
  canvas.drawColor(Color.rgb(250,248,245));val paint=Paint(3).apply {color=Color.rgb(98,93,80)}
  for((i,name) in names.withIndex()) {
   canvas.drawPath(Renderer.shape(RectF(i*160f+20f,20f,i*160f+140f,140f),Surface(shape=name)),paint)
   paint.textSize=18f;paint.textAlign=Paint.Align.CENTER;canvas.drawText(name,i*160f+80f,180f,paint)
  }
  java.io.File(context.filesDir,"button-shapes.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
 }
}
