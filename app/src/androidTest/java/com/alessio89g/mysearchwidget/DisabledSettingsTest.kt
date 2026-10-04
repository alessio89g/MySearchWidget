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
    click("Appearance")
    block(scenario)
   }
  }finally {AppLanguage.select(language)}
 }
 @Test fun backDiscardsDraftAndOnlySaveCommits()=scenario {scenario->
  val repo=Repository(ins.targetContext);val id=987657
  val original=WidgetConfig(placeholder="Original")
  kotlinx.coroutines.runBlocking {repo.save(id,original)}
  try {
   scenario.onActivity {val state=ViewModelProvider(it)[ConfigState::class.java];state.configs=mapOf(id to original);state.config=original.copy(placeholder="Draft")}
   assertEquals(original,kotlinx.coroutines.runBlocking {repo.config(id)})
   ins.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
   wait("Discard changes?")
   assertEquals(original,kotlinx.coroutines.runBlocking {repo.config(id)})
   click("Discard")
   assertEquals(original,kotlinx.coroutines.runBlocking {repo.config(id)})
   scenario.onActivity {val state=ViewModelProvider(it)[ConfigState::class.java];state.selected=id;state.config=original.copy(placeholder="Confirmed")}
   click("Save")
   val deadline=SystemClock.uptimeMillis()+5000
   while(kotlinx.coroutines.runBlocking {repo.config(id).placeholder}!="Confirmed" && SystemClock.uptimeMillis()<deadline)SystemClock.sleep(100)
   assertEquals("Confirmed",kotlinx.coroutines.runBlocking {repo.config(id).placeholder})
  } finally {kotlinx.coroutines.runBlocking {repo.remove(id)}}
 }
 @Test fun geometryArrowsOnlyChangeTheDraft()=scenario {scenario->
  click("Layout");scrollTo("Outer border");click("Outer border");click("Search field")
  scrollTo("Move right");click("Move right")
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertEquals(1f,state.config.placement.field.x,0f)
   assertEquals(0f,state.configs.getValue(987657).placement.field.x,0f)
   assertTrue(state.config.placement.fieldLinked)
  }
 }
 @Test fun proportionLockIsIndependentAndCanBeUnlocked()=scenario {scenario->
  click("Layout");scrollTo("Outer border");click("Outer border");click("Button 1")
  scrollTo("Unlock proportions");click("Unlock proportions")
  scrollTo("Lock proportions");click("Lock proportions")
  scenario.onActivity {
   val c=ViewModelProvider(it)[ConfigState::class.java].config
   assertEquals(1f,c.buttons[0].aspectRatio!!,0f)
   assertEquals(46f,c.buttons[0].widthDp!!,0f);assertEquals(46f,c.buttons[0].heightDp!!,0f)
   assertNull(c.buttons[1].aspectRatio)
  }
  scrollTo("Unlock proportions");click("Unlock proportions")
  scenario.onActivity {
   val c=ViewModelProvider(it)[ConfigState::class.java].config
   assertNull(c.buttons[0].aspectRatio);assertEquals(46f,c.buttons[0].widthDp!!,0f)
  }
 }
 @Test fun independentButtonSizeControlsWorkWithMaterialYou()=scenario {scenario->
  click("Layout");scrollTo("Outer border");click("Outer border");click("Button 1")
  scrollTo("Custom: Width (dp)");click("Custom: Width (dp)")
  scenario.onActivity {
   val c=ViewModelProvider(it)[ConfigState::class.java].config
   assertTrue(c.dynamic);assertEquals(46f,c.buttons[0].widthDp!!,0f)
   assertNull(c.buttons[1].widthDp);assertEquals(46f,c.buttons[0].heightDp!!,0f)
  }
  scrollTo("Restore automatic size");click("Restore automatic size")
  scenario.onActivity {assertNull(ViewModelProvider(it)[ConfigState::class.java].config.buttons[0].widthDp)}
 }
 @Test fun heightResetUses64WithMaterialYouEnabled()=scenario {scenario->
  click("Layout")
  scenario.onActivity {ViewModelProvider(it)[ConfigState::class.java].let {s->s.config=s.config.copy(heightDp=96.5f)}}
  scrollTo("Reset to 64 dp");click("Reset to 64 dp")
  scenario.onActivity {
   val c=ViewModelProvider(it)[ConfigState::class.java].config
   assertEquals(64f,c.heightDp,0f);assertTrue(c.dynamic)
  }
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
  click("Button")
  scrollTo("Circle / Square");click("Circle / Square")
  click("Clover")
  scenario.onActivity {
   val state=ViewModelProvider(it)[ConfigState::class.java]
   assertEquals("clover",state.config.buttons[0].surface.shape)
   assertEquals("circle",state.config.buttons[1].surface.shape)
  }
  click("Switch to Italian")
  repeat(12) {
   if(text("Pulsante")==null) {
    find(root()){it.className?.toString()=="android.widget.ScrollView"}?.performAction(Node.ACTION_SCROLL_BACKWARD)
    SystemClock.sleep(150)
   }
  }
  wait("Pulsante")
  scrollTo("Clover")
  assertNotNull(wait("Clover"))
 }
 @Test fun cornerRoundingIsAvailableOnlyForCircleSquare()=scenario {scenario->
  click("Button 1");click("Button")
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
 @Test fun longPressEnablesSingleTrashAndBulkTemplateSelection() {
  val repo=Repository(ins.targetContext)
  val models=kotlinx.coroutines.runBlocking {(1..3).map {repo.importBackup(Catalog.json.encodeToString(Backup.serializer(),Backup(1,WidgetConfig(),Catalog.engines.first())))}}
  try {scenario {scenario->
   scenario.onActivity {ViewModelProvider(it)[ConfigState::class.java].templates=models.mapIndexed {i,t->t.copy(name="Selection ${i+1}")}}
   click("Backup")
   val first=actionable(scrollTo("Selection 1"))
   assertTrue(first.performAction(Node.ACTION_LONG_CLICK))
   wait("1 selected");wait("Delete selected items")
   scrollTo("Select Selection 2");click("Select Selection 2")
   wait("2 selected")
   ins.uiAutomation.takeScreenshot()?.let {bitmap->
    java.io.File(ins.targetContext.filesDir,"library-selection.png").outputStream().use {bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
    bitmap.recycle()
   }
   click("Delete selected items");wait("Delete 2 selected items?")
   click("Cancel")
   assertEquals(3,kotlinx.coroutines.runBlocking {repo.templates().count {it.id in models.map {m->m.id}}})
   click("Delete selected items");click("Delete")
   repeat(70) {if(text("2 selected")!=null)SystemClock.sleep(100)}
   assertNull(text("Delete selected items"))
   val remaining=kotlinx.coroutines.runBlocking {repo.templates().map {it.id}}
   assertFalse(models[0].id in remaining);assertFalse(models[1].id in remaining);assertTrue(models[2].id in remaining)
  }} finally {kotlinx.coroutines.runBlocking {models.forEach {repo.deleteTemplate(it.id)}}}
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
