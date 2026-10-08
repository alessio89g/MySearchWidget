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
import kotlinx.coroutines.runBlocking

class EditingUpgradeUiTest {
 private val ins=InstrumentationRegistry.getInstrumentation()
 private fun find(n:Node?,label:String):Node? {
  if(n==null)return null
  if(n.text?.toString()==label || n.contentDescription?.toString()==label)return n
  for(i in 0 until n.childCount)find(n.getChild(i),label)?.let{return it}
  return null
 }
 private fun wait(label:String):Node {
  repeat(100){ins.uiAutomation.clearCache();find(ins.uiAutomation.rootInActiveWindow,label)?.let{return it};SystemClock.sleep(100)}
  error("Missing $label")
 }
 private fun click(label:String) {
  repeat(70) {
   var n=wait(label)
   while(!n.isClickable)n=n.parent ?: error("Not clickable: $label")
   if(n.isEnabled && n.performAction(Node.ACTION_CLICK)){ins.uiAutomation.waitForIdle(200,5000);return}
   SystemClock.sleep(100)
  }
  error("Control did not become enabled: $label")
 }
 @Test fun undoRedoAndPreviewOverrideDoNotCommitDraft() {
  val language=AppLanguage.code;AppLanguage.select("en")
  try {
   ActivityScenario.launch<ConfigActivity>(Intent(ins.targetContext,ConfigActivity::class.java)).use {scenario->
    wait("Switch to Italian")
    val original=WidgetConfig(referenceWidthDp=374,theme="light",dynamic=false)
    scenario.onActivity {
     val state=ViewModelProvider(it)[ConfigState::class.java]
     state.configs=mapOf(987661 to original);state.history=EditHistory(original);state.selected=987661;state.ready=true
    }
    wait("Light")
    scenario.onActivity {ViewModelProvider(it)[ConfigState::class.java].config=original.copy(placeholder="Changed")}
    click("↶ Undo")
    scenario.onActivity {assertEquals(original,ViewModelProvider(it)[ConfigState::class.java].config)}
    click("↷ Redo")
    scenario.onActivity {assertEquals("Changed",ViewModelProvider(it)[ConfigState::class.java].config.placeholder)}
    click("Light");wait("Dark")
    scenario.onActivity {
     val state=ViewModelProvider(it)[ConfigState::class.java]
     assertEquals("light",state.config.theme)
     assertEquals(original,state.configs[987661])
     state.gesture(true);state.config=state.config.copy(heightDp=80f);state.config=state.config.copy(heightDp=90f);state.gesture(false)
    }
    click("↶ Undo")
    scenario.onActivity {assertEquals(64f,ViewModelProvider(it)[ConfigState::class.java].config.heightDp,0f)}
    click("Appearance");click("Bar")
    wait("Dark theme")
    click("Light theme");wait("Light")
    click("Light");wait("Dark")
    scenario.onActivity {assertEquals("light",ViewModelProvider(it)[ConfigState::class.java].config.theme)}
   }
  }finally {AppLanguage.select(language)}
 }
 @Test fun legacyReferenceIsCapturedOnceWithoutCreatingUnconfiguredEntries()=runBlocking {
  val repo=Repository(ins.targetContext);val id=987662
  val original=WidgetConfig(placeholder="Stable",heightDp=69f)
  try {
   repo.remove(id)
   repo.anchorWidth(id,374)
   assertFalse(repo.hasConfig(id))
   repo.save(id,original)
   assertEquals(original.copy(referenceWidthDp=374),repo.anchorWidth(id,374))
   assertEquals(374,repo.anchorWidth(id,391).referenceWidthDp)
   assertEquals("Stable",repo.config(id).placeholder)
  } finally {repo.remove(id)}
 }
}
