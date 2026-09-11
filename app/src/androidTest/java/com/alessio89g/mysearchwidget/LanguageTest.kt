package com.alessio89g.mysearchwidget

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.config.ConfigActivity
import com.alessio89g.mysearchwidget.config.ConfigState
import com.alessio89g.mysearchwidget.data.WidgetConfig
import com.alessio89g.mysearchwidget.i18n.*
import com.alessio89g.mysearchwidget.widget.Renderer
import org.junit.Assert.*
import org.junit.Test

class LanguageTest {
 private val instrumentation=InstrumentationRegistry.getInstrumentation()
 private val context=instrumentation.targetContext
 @Test fun englishIsDefaultAndLanguagePersistsIndependentlyOfSystemLocale() {
  val prefs=context.getSharedPreferences("interface",Context.MODE_PRIVATE)
  val old=prefs.getString("language",null)
  try {
   assertTrue(prefs.edit().remove("language").commit())
   AppLanguage.initialize(context)
   assertEquals("en",AppLanguage.code)
   assertEquals("Search the web",tr(R.string.placeholder))
   AppLanguage.select("it")
   assertEquals("it",AppLanguage.read(context))
   AppLanguage.initialize(context)
   assertEquals("Cerca sul web",tr(R.string.placeholder))
   AppLanguage.select("en")
   assertEquals("Search the web",tr(R.string.placeholder))
  } finally {prefs.edit().putString("language",old).commit();AppLanguage.initialize(context)}
 }
 @Test fun automaticPlaceholderChangesButCustomTextAndColorsDoNot() {
  val previous=AppLanguage.code
  try {
   val config=WidgetConfig(dynamic=false)
   AppLanguage.select("en")
   val defaultEn=Renderer.render(context,config,356)
   val customEn=Renderer.render(context,config.copy(placeholder="My custom search"),356)
   AppLanguage.select("it")
   val defaultIt=Renderer.render(context,config,356)
   val customIt=Renderer.render(context,config.copy(placeholder="My custom search"),356)
   assertFalse(defaultEn.sameAs(defaultIt))
   assertTrue(customEn.sameAs(customIt))
   assertEquals("",config.placeholder)
   listOf(defaultEn,customEn,defaultIt,customIt).forEach {it.recycle()}
  } finally {AppLanguage.select(previous)}
 }
 @Test fun visibleSwitchDoesNotRecreateActivityOrLoseDraftOrTab() {
  val previous=AppLanguage.code
  AppLanguage.select("en")
  try {
   ActivityScenario.launch<ConfigActivity>(Intent(context,ConfigActivity::class.java)).use {scenario->
    var original:ConfigActivity?=null
    waitFor("Switch to Italian")
    scenario.onActivity {activity->
     original=activity
     val state=ViewModelProvider(activity)[ConfigState::class.java]
     state.configs=mapOf(987654 to WidgetConfig())
     state.selected=987654
     state.config=WidgetConfig(placeholder="Draft preserved")
     state.ready=true
    }
    click("Search")
    waitFor("Placeholder text")
    click("Switch to Italian")
    waitFor("Testo a riposo")
    waitFor("IT")
    scenario.onActivity {activity->
     assertSame(original,activity)
     assertEquals("Draft preserved",ViewModelProvider(activity)[ConfigState::class.java].config.placeholder)
    }
    click("Passa all’inglese")
    waitFor("Placeholder text")
    waitFor("EN")
    scenario.onActivity {assertSame(original,it)}
   }
  } finally {AppLanguage.select(previous)}
 }
 private fun find(node:AccessibilityNodeInfo?,text:String):AccessibilityNodeInfo? {
  if(node==null)return null
  if(node.text?.toString()==text || node.contentDescription?.toString()==text)return node
  for(i in 0 until node.childCount)find(node.getChild(i),text)?.let{return it}
  return null
 }
 private fun waitFor(text:String):AccessibilityNodeInfo {
  val until=SystemClock.uptimeMillis()+10000
  do {
   find(instrumentation.uiAutomation.rootInActiveWindow,text)?.let{return it}
   SystemClock.sleep(100)
  } while(SystemClock.uptimeMillis()<until)
  throw AssertionError("UI text not found: $text")
 }
 private fun click(text:String) {
  var node=waitFor(text)
  while(!node.isClickable)node=node.parent ?: throw AssertionError("Not clickable: $text")
  assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
 }
}
