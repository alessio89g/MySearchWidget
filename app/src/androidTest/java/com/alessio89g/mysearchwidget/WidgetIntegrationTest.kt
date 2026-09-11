package com.alessio89g.mysearchwidget

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.view.View
import android.widget.FrameLayout
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetIntegrationTest {
 private val instrumentation=InstrumentationRegistry.getInstrumentation()
 private val context=instrumentation.targetContext
 @Test fun exportPreviewForVisualReview(){
  instrumentation.runOnMainSync {
   for(theme in listOf("light","dark")) {
    val bitmap=Renderer.render(context,WidgetConfig(theme=theme,dynamic=false),356)
    assertTrue(bitmap.width>0 && bitmap.height>0)
    java.io.File(context.filesDir,"preview-$theme.png").outputStream().use {bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
   }
  }
 }
 @Test fun remoteViewsInflateEveryButtonCountAndShape(){
  instrumentation.runOnMainSync {
   for(count in 0..3)for(shape in listOf("circle","squircle","flower"))for(theme in listOf("light","dark")) {
    val c=WidgetConfig(count=count,theme=theme,buttons=WidgetConfig().buttons.map {it.copy(surface=it.surface.copy(shape=shape))})
    for(width in listOf(220,356,600)) {
     val rv=SearchWidget.views(context,51,c,width,SessionText("Questa query lunga verifica lo scorrimento e il font personalizzato",30))
     val root=rv.apply(context,FrameLayout(context))
     root.measure(View.MeasureSpec.makeMeasureSpec(width*3,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(174,View.MeasureSpec.EXACTLY));root.layout(0,0,width*3,174)
     assertNotNull(root.findViewById<View>(R.id.input))
     assertEquals(if(count==3)View.VISIBLE else View.GONE,root.findViewById<View>(R.id.button2).visibility)
    }
   }
  }
 }
 @Test fun importIsTransactionalAndCannotOverwriteInstances()=runBlocking {
  val repo=Repository(context)
  val first=WidgetConfig(placeholder="Prima istanza",count=0)
  val second=WidgetConfig(placeholder="Seconda istanza",count=3)
  repo.save(70001,first);repo.save(70002,second)
  val modelsBefore=repo.templates().size
  val model=repo.importBackup(Catalog.json.encodeToString(Backup(1,WidgetConfig(),Catalog.engines.first())))
  assertEquals(modelsBefore+1,repo.templates().size)
  assertEquals(first,repo.config(70001));assertEquals(second,repo.config(70002))
  assertTrue(runCatching {repo.importBackup("{\"schemaVersion\":99}")}.isFailure)
  assertEquals(modelsBefore+1,repo.templates().size)
  assertEquals(first,repo.config(70001));assertEquals(second,repo.config(70002))
  repo.deleteTemplate(model.id);repo.remove(70001);repo.remove(70002)
 }
 @Test fun corruptEmbeddedImageRejectsEntireImport()=runBlocking {
  val repo=Repository(context);val count=repo.templates().size
  val broken=WidgetConfig(assets=mapOf("image" to Asset("image","YWJjZA==")))
  assertTrue(runCatching {repo.importBackup(Catalog.json.encodeToString(Backup(1,broken,Catalog.engines.first())))}.isFailure)
  assertEquals(count,repo.templates().size)
 }
 @Test fun applyingCustomEngineAvoidsCrossWidgetMutation()=runBlocking {
  val repo=Repository(context)
  val imported=Engine("google","Different","https://example.com/?q=%s")
  val t=Template("test","Test",Backup(1,WidgetConfig(),imported))
  val prepared=repo.prepareTemplate(t)
  assertNotEquals("google",prepared.engineId)
  assertEquals(Catalog.engines.first(),repo.engine("google"))
  assertEquals(imported.template,repo.engine(prepared.engineId).template)
  repo.deleteEngine(prepared.engineId)
 }
 @Test fun portableBackupRestoresFontAndImageWithoutSourceFiles()=runBlocking {
  val repo=Repository(context)
  val fontFile=java.io.File.createTempFile("testfont", ".ttf",context.cacheDir)
  val imageFile=java.io.File.createTempFile("testimage", ".png",context.cacheDir)
  try {
   context.resources.openRawResource(R.font.google_sans).use {source->fontFile.outputStream().use {source.copyTo(it)}}
   val image=android.graphics.Bitmap.createBitmap(24,24,android.graphics.Bitmap.Config.ARGB_8888)
   image.eraseColor(android.graphics.Color.BLUE)
   imageFile.outputStream().use {image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
   val font=Assets.read(context,android.net.Uri.fromFile(fontFile),"font")
   val icon=Assets.read(context,android.net.Uri.fromFile(imageFile),"image")
   val original=WidgetConfig(hint=TextStyle(font=font.first),query=TextStyle(font=font.first,size=20f),logo=WidgetConfig().logo.copy(icon=IconSpec(asset=icon.first)),assets=mapOf(font,icon))
   val raw=Catalog.json.encodeToString(Backup(1,original,Catalog.engines.first()))
   fontFile.delete();imageFile.delete()
   val imported=repo.importBackup(raw)
   try {
    assertEquals(original,imported.backup.config)
    assertNotNull(Assets.font(context,imported.backup.config.assets.getValue(font.first)))
    assertEquals(24,Assets.bitmap(imported.backup.config.assets.getValue(icon.first))!!.width)
    instrumentation.runOnMainSync {assertTrue(Renderer.render(context,imported.backup.config,356,SessionText("Font importato")).width>0)}
   } finally {repo.deleteTemplate(imported.id)}
  } finally {fontFile.delete();imageFile.delete()}
 }
 @Test fun multipleCustomEnginesCanBeEditedAndRemovedIndependently()=runBlocking {
  val repo=Repository(context)
  val first=Engine(java.util.UUID.randomUUID().toString(),"Primo test","https://duckduckgo.com/?q=%s")
  val second=Engine(java.util.UUID.randomUUID().toString(),"Secondo test","https://example.com/search?q=%s")
  try {
   repo.saveEngine(first);repo.saveEngine(second)
   repo.save(70003,WidgetConfig(engineId=first.id))
   assertTrue(runCatching {repo.deleteEngine(first.id)}.isFailure)
   val edited=second.copy(name="Secondo modificato",template="https://example.org/?q=%s")
   repo.saveEngine(edited)
   assertEquals(first,repo.engine(first.id));assertEquals(edited,repo.engine(second.id))
   repo.deleteEngine(second.id)
   assertFalse(repo.engines().any {it.id==second.id})
   assertEquals(first,repo.engine(first.id))
  } finally {repo.remove(70003);repo.deleteEngine(first.id);repo.deleteEngine(second.id)}
 }
 @Test fun corruptFontDoesNotPartiallyImportOtherValidAssets()=runBlocking {
  val repo=Repository(context);val before=repo.templates()
  val c=WidgetConfig(assets=mapOf("font" to Asset("font","bm90IGEgcmVhbCBmb250")),hint=TextStyle(font="font"))
  assertTrue(runCatching {repo.importBackup(Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first())))}.isFailure)
  assertEquals(before,repo.templates())
 }

}
