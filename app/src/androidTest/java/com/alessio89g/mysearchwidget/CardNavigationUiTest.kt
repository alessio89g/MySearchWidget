package com.alessio89g.mysearchwidget

import android.content.Intent
import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo as Node
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.config.*
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.i18n.*
import org.junit.Assert.*
import org.junit.Test

class CardNavigationUiTest {
 private val ins=InstrumentationRegistry.getInstrumentation()
 private fun find(n:Node?,predicate:(Node)->Boolean):Node? {
  if(n==null)return null
  if(predicate(n))return n
  for(i in 0 until n.childCount)find(n.getChild(i),predicate)?.let{return it}
  return null
 }
 private fun root():Node? {ins.uiAutomation.clearCache();return ins.uiAutomation.rootInActiveWindow}
 private fun text(label:String)=find(root()){it.text?.toString()==label || it.contentDescription?.toString()==label}
 private fun wait(label:String):Node {
  repeat(80){text(label)?.let{return it};SystemClock.sleep(100)}
  error("Missing $label")
 }
 private fun click(label:String) {
  var n=wait(label)
  while(!n.isClickable)n=n.parent ?: error("Not clickable $label")
  assertTrue(n.performAction(Node.ACTION_CLICK));ins.uiAutomation.waitForIdle(200,5000)
 }
 private fun reveal(label:String) {
  repeat(20) {
   val n=text(label);val scroll=find(root()){it.isScrollable}
   val bounds=Rect();scroll?.getBoundsInScreen(bounds)
   val target=Rect();n?.getBoundsInScreen(target)
   if(n!=null && (scroll==null || (target.top>=bounds.top && target.bottom<=bounds.bottom)))return
   scroll?.performAction(Node.ACTION_SCROLL_FORWARD)
   ins.uiAutomation.waitForIdle(200,5000)
  }
  error("Cannot reveal $label")
 }
 private fun scenario(block:(ActivityScenario<ConfigActivity>,WidgetConfig)->Unit) {
  val language=AppLanguage.code;AppLanguage.select("en")
  try {
   ActivityScenario.launch<ConfigActivity>(Intent(ins.targetContext,ConfigActivity::class.java)).use {scenario->
    wait("Switch to Italian")
    val original=WidgetConfig(referenceWidthDp=374,dynamic=false)
    scenario.onActivity {
     val state=ViewModelProvider(it)[ConfigState::class.java]
     state.configs=mapOf(987663 to original);state.history=EditHistory(original);state.selected=987663;state.ready=true
    }
    click("Preview ▾ · Widget 987663")
    block(scenario,original)
   }
  }finally {AppLanguage.select(language)}
 }
 @Test fun actionsGroupHapticsByAreaWithoutSavingDraft()=scenario {scenario,original->
  click("Actions");click("Search field")
  reveal("Haptic feedback");click("Haptic feedback")
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertFalse(state.config.hapticEnabled("field"));assertFalse(state.config.hapticEnabled("text"))
   assertTrue(state.config.hapticEnabled("logo"));assertTrue(state.config.hapticEnabled("button0"))
   assertEquals(original,state.configs[987663])
  }
  click("‹ All settings")
  click("Button 1");reveal("Haptic feedback");click("Haptic feedback")
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertFalse(state.config.hapticEnabled("button0"));assertFalse(state.config.hapticEnabled("icon0"))
   assertTrue(state.config.hapticEnabled("button1"));assertEquals(original,state.configs[987663])
  }
  click("↶ Undo")
  scenario.onActivity {assertTrue(ViewModelProvider(it)[ConfigState::class.java].config.hapticEnabled("button0"))}
 }
 @Test fun dimensionsAndPositionRemainAccessibleAndIndependent()=scenario {scenario,original->
  reveal("Outer border");click("Outer border");click("Position")
  reveal("Move right");click("Move right")
  scenario.onActivity {
   val current=ViewModelProvider(it)[ConfigState::class.java].config
   assertEquals(1f,current.placement.outer.x,0f);assertEquals(original.sizing,current.sizing)
  }
  reveal("Reset position (0, 0)");click("Reset position (0, 0)")
  scenario.onActivity {assertEquals(original.placement,ViewModelProvider(it)[ConfigState::class.java].config.placement)}
  repeat(8){find(root()){it.isScrollable}?.performAction(Node.ACTION_SCROLL_BACKWARD)}
  ins.uiAutomation.waitForIdle(200,5000)
  reveal("Dimensions");click("Dimensions")
  reveal("Reset default dimensions");click("Reset default dimensions")
  scenario.onActivity {assertEquals(original,ViewModelProvider(it)[ConfigState::class.java].configs[987663])}
 }
}
