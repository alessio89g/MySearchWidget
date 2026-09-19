package com.alessio89g.mysearchwidget

import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.os.Parcel
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class LibraryAndThemeTest {
 private val ins=InstrumentationRegistry.getInstrumentation()
 private val context=ins.targetContext
 @Test fun onlySavedBoundWidgetsAppearAndReadsDoNotCreateDefaults()=runBlocking {
  val repo=Repository(context);val saved=80001;val unsaved=80002;val stale=80003
  try {
   repo.save(saved,WidgetConfig()) // A deliberately saved default is legitimate.
   repo.save(stale,WidgetConfig(placeholder="No longer bound"))
   assertEquals(setOf(saved),repo.library(setOf(saved,unsaved)).keys)
   repeat(3){assertEquals(WidgetConfig(),repo.config(unsaved))}
   assertFalse(repo.hasConfig(unsaved))
   assertEquals(setOf(saved),repo.library(setOf(saved,unsaved)).keys)
  }finally {listOf(saved,unsaved,stale).forEach {repo.remove(it)}}
 }
 @Test fun bulkRemovalPreservesActiveWidgetSettingsAndUnselectedTemplates()=runBlocking {
  val repo=Repository(context);val ids=setOf(80004,80005)
  val c=WidgetConfig(placeholder="Keep Home settings",count=3)
  val models=(1..3).map {repo.importBackup(Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first())))}
  try {
   ids.forEach {repo.save(it,c)}
   repo.removeLibraryItems(setOf(80004),models.take(2).map {it.id}.toSet())
   assertEquals(setOf(80005),repo.library(ids).keys)
   assertEquals(c,repo.config(80004));assertEquals(c,repo.config(80005))
   assertTrue(repo.templates().any {it.id==models.last().id})
   assertFalse(repo.templates().any {it.id in models.take(2).map {m->m.id}})
   assertEquals(setOf(80005),Repository(context).library(ids).keys)
   repo.save(80004,c)
   assertEquals(ids,repo.library(ids).keys)
  }finally {ids.forEach {repo.remove(it)};models.forEach {repo.deleteTemplate(it.id)}}
 }
 @Test fun pinConfirmationAddsDefaultsOnlyOnceAndNeverOverwritesSavedSettings()=runBlocking {
  val repo=Repository(context);val id=80007
  try {
   assertFalse(repo.hasConfig(id));repo.confirmPinned(id)
   assertEquals(setOf(id),repo.library(setOf(id)).keys)
   val c=WidgetConfig(placeholder="Saved before late pin callback")
   repo.save(id,c);repo.confirmPinned(id);assertEquals(c,repo.config(id))
  }finally {repo.remove(id)}
 }
 @Test fun serializedWidgetSwitchesWithHostThemeWithoutProviderUpdate() {
  ins.runOnMainSync {
   for(theme in listOf("system","light","dark"))for(dynamic in listOf(false,true)) {
    val config=WidgetConfig(theme=theme,dynamic=dynamic)
    val original=SearchWidget.views(context,80006,config,356)
    // A host gets a parcel, not a callback into the renderer or app process.
    val parcel=Parcel.obtain()
    val rv=try {original.writeToParcel(parcel,0);parcel.setDataPosition(0);RemoteViews.CREATOR.createFromParcel(parcel)}finally {parcel.recycle()}
    var lightBitmap:android.graphics.Bitmap?=null
    for(night in listOf(false,true,false)) {
     val cfg=Configuration(context.resources.configuration).apply {
      uiMode=(uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or if(night)Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
     }
     val host=context.createConfigurationContext(cfg)
     val root=rv.apply(host,FrameLayout(host))
     val actual=(root.findViewById<ImageView>(R.id.art).drawable as BitmapDrawable).bitmap
     val expected=Renderer.render(host,config,356)
     assertTrue("theme=$theme dynamic=$dynamic night=$night",expected.sameAs(actual))
     if(!night)lightBitmap=actual else if(theme=="system")assertFalse(lightBitmap!!.sameAs(actual)) else assertTrue(lightBitmap!!.sameAs(actual))
     expected.recycle()
    }
   }
  }
 }
}
