package com.alessio89g.mysearchwidget

import android.content.res.Configuration
import android.os.Parcel
import android.view.View
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class ThemeModesTest {
 private val ins=InstrumentationRegistry.getInstrumentation()
 private val context=ins.targetContext
 @Test fun hostChangesWholeLayoutWithoutAppRunning() {
  var config=DefaultWidget.create(context)
  for(key in listOf("size.outer","position.field","count","search.engine"))config=ModeSettings.toggle(config,key,true,false)
  val d=ModeSettings.resolve(config,true)
  config=ModeSettings.edit(config,d.copy(heightDp=100f,count=1,engineId="bing",placement=d.placement.copy(field=OffsetDp(20f,10f))),true)
  ins.runOnMainSync {
   val rv=SearchWidget.views(context,90140,config,374)
   val parcel=Parcel.obtain()
   val remote=try {rv.writeToParcel(parcel,0);parcel.setDataPosition(0);RemoteViews.CREATOR.createFromParcel(parcel)}finally {parcel.recycle()}
   for(dark in listOf(false,true)) {
    val cfg=Configuration(context.resources.configuration).apply {uiMode=(uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or if(dark)Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO}
    val host=context.createConfigurationContext(cfg)
    val applied=remote.apply(host,FrameLayout(host))
    assertEquals(if(dark)View.GONE else View.VISIBLE,applied.findViewById<View>(R.id.light_variant).visibility)
    assertEquals(if(dark)View.VISIBLE else View.GONE,applied.findViewById<View>(R.id.dark_variant).visibility)
    val active=applied.findViewById<FrameLayout>(if(dark)R.id.dark_variant else R.id.light_variant)
    val hiddenButton=active.findViewById<View>(R.id.button1)
    if(dark)assertNull(hiddenButton) else assertNotNull(hiddenButton)
    assertNotNull(active.findViewById<View>(R.id.input))
   }
  }
 }
 @Test fun backupsKeepBothModesAndBothEngines()=runBlocking {
  val repo=Repository(context)
  var c=DefaultWidget.create(context)
  c=ModeSettings.toggle(c,"search.engine",true,false)
  c=ModeSettings.edit(c,ModeSettings.resolve(c,true).copy(engineId="custom-mode"),true)
  val engine=Engine("custom-mode","Qwant","https://www.qwant.com/?q=%s")
  val b=Backup(1,c,Catalog.engines.first(),additionalEngines=listOf(engine))
  val parsed=Validation.parse(Catalog.json.encodeToString(b))
  assertEquals(b,parsed)
  val template=Template("test-modes","test",parsed)
  val imported=repo.prepareTemplate(template)
  assertEquals(engine,repo.engine(ModeSettings.resolve(imported,true).engineId))
  val id=90141
  try {
   repo.save(id,imported)
   assertEquals(imported,repo.config(id))
   val withGoogle=ModeSettings.toggle(imported,"search.google",true,false)
   val draft=ModeSettings.edit(withGoogle,ModeSettings.resolve(imported,true).copy(googleInput=true),true)
   assertFalse(ModeSettings.resolve(repo.config(id),true).googleInput)
   repo.save(id,draft)
   assertTrue(ModeSettings.resolve(repo.config(id),true).googleInput)
   assertFalse(repo.config(id).googleInput)
  }finally {repo.remove(id);repo.deleteEngine(engine.id)}
 }
}
