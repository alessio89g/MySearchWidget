package com.alessio89g.mysearchwidget

import android.app.ActivityOptions
import android.app.PendingIntent
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.config.*
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.SearchWidget
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class WidgetPinningTest {
 private val ins=InstrumentationRegistry.getInstrumentation()
 private val context=ins.targetContext
 private fun boundWidget(block:(Int)->Unit) {
  val host=AppWidgetHost(context,7133)
  val id=host.allocateAppWidgetId()
  ins.uiAutomation.adoptShellPermissionIdentity("android.permission.BIND_APPWIDGET")
  try {assertTrue(AppWidgetManager.getInstance(context).bindAppWidgetIdIfAllowed(id,ComponentName(context,SearchWidget::class.java)))}
  finally {ins.uiAutomation.dropShellPermissionIdentity()}
  try {block(id)} finally {host.deleteAppWidgetId(id);runBlocking {Repository(context).remove(id)}}
 }
 @Test fun appLaunchKeepsLibraryVisibleWithOneSavedWidget()=boundWidget {id->
  runBlocking {Repository(context).save(id,WidgetConfig(placeholder="Existing"))}
  ActivityScenario.launch<ConfigActivity>(Intent(context,ConfigActivity::class.java).setAction(Intent.ACTION_MAIN)).use {scenario->
   var ready=false
   repeat(100) {
    scenario.onActivity {val state=ViewModelProvider(it)[ConfigState::class.java];ready=state.ready}
    if(!ready)SystemClock.sleep(50)
   }
   assertTrue(ready)
   scenario.onActivity {
    val state=ViewModelProvider(it)[ConfigState::class.java]
    assertNull(state.selected);assertTrue(state.configs.containsKey(id))
   }
  }
 }
 @Test fun confirmedPinOpensCorrectDraftAndCallbackIsOneShot()=boundWidget {id->
  val repo=Repository(context)
  assertFalse(runBlocking {repo.hasConfig(id)})
  ActivityScenario.launch<ConfigActivity>(Intent(context,ConfigActivity::class.java)).use {
   val monitor=ins.addMonitor(ConfigActivity::class.java.name,null,false)
   val callback=WidgetPinning.confirmation(context)
   val other=WidgetPinning.confirmation(context)
   assertNotEquals(callback,other);other.cancel()
   try {
    val options=ActivityOptions.makeBasic().apply {
     setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
    }.toBundle()
    callback.send(context,0,Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id),null,null,null,options)
    val activity=ins.waitForMonitorWithTimeout(monitor,10000) as? ConfigActivity
    assertNotNull("Confirmation must open configurator",activity)
    var selected:Int?=null
    repeat(100) {
     ins.runOnMainSync {selected=ViewModelProvider(activity!!)[ConfigState::class.java].selected}
     if(selected!=id)SystemClock.sleep(50)
    }
    assertEquals(id,selected)
    assertEquals(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE,activity!!.intent.action)
    assertFalse("Opening the editor must not save defaults",runBlocking {repo.hasConfig(id)})
    assertThrows(PendingIntent.CanceledException::class.java){callback.send()}
    ins.runOnMainSync {activity.finish()}
   }finally {callback.cancel();ins.removeMonitor(monitor)}
  }
 }
}
