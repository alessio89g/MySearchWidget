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
  wait(label)
  repeat(30) {
   var n=text(label)
   while(n!=null && !n.isClickable)n=n.parent
   if(n?.isEnabled==true && n.performAction(Node.ACTION_CLICK)) {
    ins.uiAutomation.waitForIdle(200,5000);return
   }
   SystemClock.sleep(100)
  }
  error("Not clickable or not enabled: $label")
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
  reveal("Reset default position");click("Reset default position")
  scenario.onActivity {assertEquals(original.placement,ViewModelProvider(it)[ConfigState::class.java].config.placement)}
  repeat(8){find(root()){it.isScrollable}?.performAction(Node.ACTION_SCROLL_BACKWARD)}
  ins.uiAutomation.waitForIdle(200,5000)
  reveal("Dimensions");click("Dimensions")
  reveal("Reset default dimensions");click("Reset default dimensions")
  scenario.onActivity {assertEquals(original,ViewModelProvider(it)[ConfigState::class.java].configs[987663])}
 }
 @Test fun heightResetUsesNewPresetAndRemainsUndoableUntilSave()=scenario {scenario,original->
  scenario.onActivity {ViewModelProvider(it)[ConfigState::class.java].config=original.copy(heightDp=110f)}
  click("Widget height");reveal("Reset to 68 dp");click("Reset to 68 dp")
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertEquals(68f,state.config.heightDp,0f)
   assertEquals(original,state.configs[987663])
  }
  click("Preview ▸ · Widget 987663")
  click("↶ Undo")
  scenario.onActivity {assertEquals(110f,ViewModelProvider(it)[ConfigState::class.java].config.heightDp,0f)}
 }


 @Test fun linkingTabKeepsChangesAsUndoableDrafts()=scenario {scenario,original->
  click("Light/Dark linking")
  reveal("Element dimensions");click("Element dimensions")
  reveal("Outer border");click("Outer border")
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertTrue("size.outer" in state.config.modes.separate)
   assertEquals(original,state.configs[987663])
  }
  click("↶ Undo")
  scenario.onActivity {assertFalse("size.outer" in ViewModelProvider(it)[ConfigState::class.java].config.modes.separate)}
 }
 @Test fun relinkingAsksWhichValuesToKeepAndCanBeCancelled()=scenario {scenario,original->
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   val split=ModeSettings.toggle(original,"size.outer",true,false)
   state.config=ModeSettings.edit(split,ModeSettings.resolve(split,true).copy(heightDp=99f),true)
  }
  wait("Editing mode")
  click("Light/Dark linking")
  assertNull(text("Editing mode"))
  scenario.onActivity {AppLanguage.select("it")}
  wait("Legame Chiaro/Scuro")
  SystemClock.sleep(500)
  ins.uiAutomation.takeScreenshot()?.let {bitmap->java.io.File(ins.targetContext.cacheDir,"linking-it.png").outputStream().use {bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}}
  scenario.onActivity {AppLanguage.select("en")}
  wait("Light/Dark linking")
  click("Element dimensions");reveal("Outer border");click("Outer border")
  wait("Which values should be kept?")
  scenario.onActivity {assertTrue("size.outer" in ViewModelProvider(it)[ConfigState::class.java].config.modes.separate)}
  click("Cancel")
  click("Outer border");click("Keep dark values")
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertFalse("size.outer" in state.config.modes.separate)
   assertEquals(99f,ModeSettings.resolve(state.config,false).heightDp)
   assertEquals(original,state.configs[987663])
  }
  click("↶ Undo");click("Outer border");click("Keep light values")
  scenario.onActivity {assertEquals(original.heightDp,ModeSettings.resolve(ViewModelProvider(it)[ConfigState::class.java].config,true).heightDp)}
 }
 @Test fun buttonCountReflowsPreviewAndKeepsChangesInDraft()=scenario {scenario,_->
  val preset=DefaultWidget.create(ins.targetContext)
  scenario.onActivity {val state=ViewModelProvider(it)[ConfigState::class.java];state.config=preset}
  click("Number of buttons")
  for(n in listOf(2,1,0,3)) {
   click(n.toString())
   scenario.onActivity {
    val c=ViewModelProvider(it)[ConfigState::class.java].config
    assertEquals(n,c.count)
    val g=ElementLayout(374f,c)
    assertEquals(g.field.left-g.outer.left,g.outer.right-(g.buttonBounds.lastOrNull()?.right ?: g.field.right),.001f)
    val bitmap=com.alessio89g.mysearchwidget.widget.Renderer.render(it,c.copy(theme="dark"),374)
    java.io.File(it.cacheDir,"count-$n.png").outputStream().use {out->bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out)}
   }
  }
  ins.uiAutomation.takeScreenshot()?.let {bitmap->java.io.File(ins.targetContext.cacheDir,"count-en.png").outputStream().use {bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}}
 }
}
