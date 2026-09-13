package com.alessio89g.mysearchwidget

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.Renderer
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.Base64

class IconColorsTest {
 private val context=InstrumentationRegistry.getInstrumentation().targetContext
 private fun asset():Asset {
  val b=Bitmap.createBitmap(12,12,Bitmap.Config.ARGB_8888)
  for(y in 0..11)for(x in 0..11)b.setPixel(x,y,if(y<4)Color.TRANSPARENT else if(x<6)Color.RED else Color.GREEN)
  val bytes=ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,bytes);b.recycle()
  return Asset("image",Base64.getEncoder().encodeToString(bytes.toByteArray()))
 }
 private fun pixel(b:Bitmap,x:Float,y:Float)=b.getPixel((x*b.width/356f).toInt(),(y*b.height/64f).toInt())
 @Test fun importedColorsAndTransparencySurviveThemesMaterialYouAndStoredGradient() {
  for(dynamic in listOf(false,true))for(theme in listOf("light","dark")) {
   val base=WidgetConfig(dynamic=dynamic,theme=theme,assets=mapOf("image" to asset()))
   val icon=IconSpec(asset="image",monochrome=false,light="#000000FF",dark="#000000FF",lightGradient=Gradient(true),darkGradient=Gradient(true))
   val c=base.copy(logo=base.logo.copy(icon=icon),buttons=base.buttons.map {it.copy(icon=icon)})
   val b=Renderer.render(context,c,356)
   assertEquals(Color.RED,pixel(b,25f,40f));assertEquals(Color.GREEN,pixel(b,41f,40f))
   // Transparent top of the source reveals the field, not a tinted rectangle.
   assertEquals(pixel(b,52f,19f),pixel(b,25f,19f))
   assertEquals(Color.RED,pixel(b,263f,38f));assertEquals(Color.GREEN,pixel(b,277f,38f))
   b.recycle()
  }
 }
 @Test fun monochromeTintReturnsWithoutMutatingCachedOriginalBitmap() {
  val base=WidgetConfig(dynamic=false,assets=mapOf("image" to asset()))
  val icon=IconSpec(asset="image",light="#0000FF",dark="#0000FF")
  fun render(mono:Boolean)=Renderer.render(context,base.copy(logo=base.logo.copy(icon=icon.copy(monochrome=mono))),356)
  render(true).let {assertEquals(Color.BLUE,pixel(it,25f,40f));assertEquals(Color.BLUE,pixel(it,41f,40f));it.recycle()}
  render(false).let {assertEquals(Color.RED,pixel(it,25f,40f));assertEquals(Color.GREEN,pixel(it,41f,40f));it.recycle()}
  render(true).let {assertEquals(Color.BLUE,pixel(it,25f,40f));it.recycle()}
 }
 @Test fun originalBrandSymbolsHaveMultipleColorsAndDefaultRemainsMonochrome() {
  for(name in listOf("Google","Chrome")) {
   fun count(mono:Boolean):Int {
    val base=WidgetConfig(dynamic=false,theme="light")
    val b=Renderer.render(context,base.copy(logo=base.logo.copy(icon=IconSpec(name=name,monochrome=mono))),356)
    val pixels=(18..48).flatMap {x->(17..47).map {y->pixel(b,x.toFloat(),y.toFloat())}}
    val colors=listOf(Color.rgb(234,67,53),Color.rgb(251,188,5),Color.rgb(52,168,83),Color.rgb(66,133,244)).count {it in pixels}
    b.recycle();return colors
   }
   assertEquals(4,count(false));assertEquals(0,count(true))
  }
 }
 @Test fun nativeMaterialIconAndExamplePreviewsRender() {
  for(theme in listOf("light","dark")) {
   val base=WidgetConfig(dynamic=true,theme=theme)
   val c=base.copy(logo=base.logo.copy(icon=base.logo.icon.copy(monochrome=false)),buttons=base.buttons.map {it.copy(icon=it.icon.copy(monochrome=false))})
   val b=Renderer.render(context,c,356)
   java.io.File(context.filesDir,"original-icons-$theme.png").outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)}
   assertTrue(b.width>0);b.recycle()
  }
 }
}
