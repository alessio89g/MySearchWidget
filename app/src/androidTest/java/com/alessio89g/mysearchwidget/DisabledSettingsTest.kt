package com.alessio89g.mysearchwidget

import android.content.Intent
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

class DisabledSettingsTest {
 private val ins=InstrumentationRegistry.getInstrumentation()
 private fun root():Node? {ins.uiAutomation.clearCache();return ins.uiAutomation.rootInActiveWindow}
 private fun find(n:Node?,predicate:(Node)->Boolean):Node? {
  if(n==null)return null
  if(predicate(n))return n
  for(i in 0 until n.childCount)find(n.getChild(i),predicate)?.let{return it}
  return null
 }
 private fun text(value:String)=find(root()){it.text?.toString()==value || it.contentDescription?.toString()==value}
 private fun wait(value:String):Node {
  repeat(70){text(value)?.let{return it};SystemClock.sleep(100)}
  error("Missing text: $value")
 }
 private fun actionable(n:Node):Node {
  var node=n
  while(!node.isClickable && node.className?.toString() !in listOf("android.widget.Button","android.widget.Switch"))node=node.parent ?: error("No control")
  return node
 }
 private fun click(value:String){assertTrue(actionable(wait(value)).performAction(Node.ACTION_CLICK));ins.uiAutomation.waitForIdle(200,5000)}
 private fun scrollTo(value:String):Node {
  repeat(24) {
   text(value)?.let{return it}
   val bounds=android.graphics.Rect()
   find(root()){it.className?.toString()=="android.widget.ScrollView"}?.getBoundsInScreen(bounds)
   val x=bounds.right-12f;val y=bounds.centerY().toFloat()
   val down=SystemClock.uptimeMillis()
   fun touch(action:Int,yy:Float,time:Long) {android.view.MotionEvent.obtain(down,time,action,x,yy,0).also {it.source=android.view.InputDevice.SOURCE_TOUCHSCREEN;ins.uiAutomation.injectInputEvent(it,true);it.recycle()}}
   touch(0,y,down);for(step in 1..10)touch(2,y-step*12f,down+step*20);touch(1,y-120f,down+220)
   SystemClock.sleep(150)
  }
  error("Could not scroll to $value")
 }
 private fun assertEnabled(value:String) {
  val deadline=SystemClock.uptimeMillis()+5000
  while(!actionable(scrollTo(value)).isEnabled && SystemClock.uptimeMillis()<deadline)SystemClock.sleep(100)
  assertTrue("Expected enabled control: $value",actionable(scrollTo(value)).isEnabled)
 }
 private fun scenario(block:(ActivityScenario<ConfigActivity>)->Unit) {
  val language=AppLanguage.code;AppLanguage.select("en")
  try {
   ActivityScenario.launch<ConfigActivity>(Intent(ins.targetContext,ConfigActivity::class.java)).use {scenario->
    wait("Switch to Italian")
    scenario.onActivity {
     val state=ViewModelProvider(it)[ConfigState::class.java]
     val c=WidgetConfig(engineId="kept",outer=Surface(dark=Tone("#123456",gradient=Gradient(true))))
     state.configs=mapOf(987657 to c);state.config=c;state.selected=987657
     state.engines=Catalog.engines+Engine("kept","Personal engine","https://example.com/?q=%s");state.ready=true
    }
    wait("Widget 987657")
    text("Preview ▾ · Widget 987657")?.let {actionable(it).performAction(Node.ACTION_CLICK)}
    block(scenario)
   }
  }finally {AppLanguage.select(language)}
 }
 @Test fun directGoogleSearchDisablesEngineControlsWithoutDeletingThem()=scenario {scenario->
  click("Search")
  scrollTo("Open Google when tapping the search field")
  click("Open Google when tapping the search field")
  assertFalse(actionable(scrollTo("Personal engine")).isEnabled)
  assertFalse(actionable(scrollTo("Add custom engine")).isEnabled)
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertTrue(state.config.googleInput);assertEquals("kept",state.config.engineId)
   assertTrue(state.engines.any {e->e.id=="kept"})
   state.config=state.config.copy(googleInput=false)
  }
  ins.uiAutomation.waitForIdle(200,5000)
  assertEnabled("Personal engine")
  assertEnabled("Add custom engine")
 }
 @Test fun buttonShapeTabAndNewChoiceAreLocalizedAndIndependent()=scenario {scenario->
  click("Button 1")
  click("Button shape")
  scrollTo("Circle / Square");click("Circle / Square")
  click("Clover")
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertEquals("clover",state.config.buttons[0].surface.shape)
   assertEquals("circle",state.config.buttons[1].surface.shape)
  }
  click("Switch to Italian")
  repeat(12) {
   if(text("Forma pulsante")==null) {
    find(root()){it.className?.toString()=="android.widget.ScrollView"}?.performAction(Node.ACTION_SCROLL_BACKWARD)
    SystemClock.sleep(150)
   }
  }
  wait("Forma pulsante")
  scrollTo("Clover")
  assertNotNull(wait("Clover"))
 }
 @Test fun cornerRoundingIsAvailableOnlyForCircleSquare()=scenario {scenario->
  click("Button 1");click("Button shape")
  scrollTo("Corner rounding %")
  for(shape in Catalog.shapes.filter {it!="circle"}) {
   scenario.onActivity {
    val state=ViewModelProvider(it)[ConfigState::class.java]
    state.config=state.config.copy(buttons=state.config.buttons.mapIndexed {i,b->if(i==0)b.copy(surface=b.surface.copy(shape=shape,rounding=37f)) else b})
   }
   val label=shape.replaceFirstChar {it.uppercase()}
   wait(label)
   ins.uiAutomation.waitForIdle(200,5000)
   assertNull("Rounding must be hidden for $shape",text("Corner rounding %"))
  }
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   state.config=state.config.copy(buttons=state.config.buttons.mapIndexed {i,b->if(i==0)b.copy(surface=b.surface.copy(shape="circle")) else b})
   assertEquals(37f,state.config.buttons[0].surface.rounding)
  }
  scrollTo("Corner rounding %")
  assertNotNull(wait("Corner rounding %"))
 }
 @Test fun originalIconSwitchOverridesMaterialYouAndKeepsStoredTint()=scenario {scenario->
  click("Logo")
  val toggle=actionable(scrollTo("Monochrome icon"))
  assertTrue(toggle.isEnabled)
  click("Monochrome icon")
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertFalse(state.config.logo.icon.monochrome)
   assertTrue(state.config.dynamic)
   assertTrue(state.config.buttons.all {s->s.icon.monochrome})
   state.config=state.config.copy(dynamic=false,logo=state.config.logo.copy(icon=state.config.logo.icon.copy(dark="#123456")))
  }
  ins.uiAutomation.waitForIdle(200,5000)
  assertFalse(actionable(scrollTo("Icon · dark theme")).isEnabled)
  scrollTo("Monochrome icon");click("Monochrome icon")
  assertEnabled("Icon · dark theme")
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertTrue(state.config.logo.icon.monochrome)
   assertEquals("#123456",state.config.logo.icon.dark)
  }
 }
 @Test fun materialYouDisablesColourAndGradientControlsButKeepsTheirValues()=scenario {scenario->
  click("Bar")
  assertFalse(actionable(scrollTo("Background")).isEnabled)
  assertFalse(actionable(scrollTo("Gradient")).isEnabled)
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertEquals("#123456",state.config.outer.dark.color)
   assertTrue(state.config.outer.dark.gradient.enabled)
   state.config=state.config.copy(dynamic=false)
  }
  ins.uiAutomation.waitForIdle(200,5000)
  assertEnabled("Background")
  assertEnabled("Gradient")
 }
}
