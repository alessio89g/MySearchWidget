package com.alessio89g.mysearchwidget

import android.content.Intent
import android.os.Bundle
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

class FormattingUiTest {
 private val ins=InstrumentationRegistry.getInstrumentation()
 private fun root():Node? {ins.uiAutomation.clearCache();return ins.uiAutomation.rootInActiveWindow}
 private fun find(n:Node?,match:(Node)->Boolean):Node? {
  if(n==null)return null
  if(match(n))return n
  for(i in 0 until n.childCount)find(n.getChild(i),match)?.let {return it}
  return null
 }
 private fun waitText(text:String):Node {
  repeat(60){
   find(root()){n->(if(text=="Alpha Beta")n.isEditable && n.text?.toString()?.contains(text)==true else n.text?.toString()==text) || n.contentDescription?.toString()==text}?.let {return it}
   SystemClock.sleep(100)
  }
  val file=java.io.File(ins.targetContext.getExternalFilesDir(null),"formatting-failure.png")
  ins.uiAutomation.takeScreenshot()?.let {bitmap->file.outputStream().use {bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}}
  fun dump(n:Node?):String {if(n==null)return "null";return "\n${n.className}: ${n.text} / ${n.contentDescription} editable=${n.isEditable}"+(0 until n.childCount).joinToString(""){dump(n.getChild(it))}}
  error("Missing UI text: $text "+dump(root()))
 }
 private fun click(text:String){
  var n=waitText(text)
  while(!n.isClickable)n=n.parent ?: error("Not clickable: $text")
  assertTrue(n.performAction(Node.ACTION_CLICK))
  ins.uiAutomation.waitForIdle(200,5000)
 }
 @Test fun selectionFormattingLeavesOtherWordsUnchanged() {
  val previous=AppLanguage.code;AppLanguage.select("en")
  try {
   ActivityScenario.launch<ConfigActivity>(Intent(ins.targetContext,ConfigActivity::class.java)).use {scenario->
    waitText("Switch to Italian")
    scenario.onActivity {activity->
     val state=ViewModelProvider(activity)[ConfigState::class.java]
     state.configs=mapOf(987656 to WidgetConfig(placeholder="Alpha Beta",dynamic=false))
     state.selected=987656;state.config=state.configs.getValue(987656);state.ready=true
    }
    click("Text")
    // Collapse the large wallpaper preview to leave room for the formatting controls.
    val preview=find(root()){it.text?.toString()?.startsWith("Preview ▾")==true}
    preview?.let {var n=it;while(!n.isClickable)n=n.parent; n.performAction(Node.ACTION_CLICK)}
    val field=waitText("Alpha Beta")
    assertTrue(field.performAction(Node.ACTION_SET_SELECTION,Bundle().apply{
     putInt(Node.ACTION_ARGUMENT_SELECTION_START_INT,0);putInt(Node.ACTION_ARGUMENT_SELECTION_END_INT,5)
    }))
    ins.uiAutomation.waitForIdle(200,5000)
    scenario.onActivity {it.window.insetsController?.hide(android.view.WindowInsets.Type.ime())}
    ins.uiAutomation.waitForIdle(500,5000)
    repeat(10){
     if(find(root()){it.text?.toString()=="Bold"}==null){
      val display=ins.targetContext.resources.displayMetrics
      val bounds=android.graphics.Rect()
      find(root()){it.className?.toString()=="android.widget.ScrollView"}?.getBoundsInScreen(bounds)
      val x=display.widthPixels*.8f;val y=if(bounds.height()>0)bounds.centerY().toFloat() else display.heightPixels*.5f
      val down=SystemClock.uptimeMillis()
      fun touch(action:Int,yy:Float,time:Long){android.view.MotionEvent.obtain(down,time,action,x,yy,0).also {it.source=android.view.InputDevice.SOURCE_TOUCHSCREEN;ins.uiAutomation.injectInputEvent(it,true);it.recycle()}}
      touch(android.view.MotionEvent.ACTION_DOWN,y,down)
      for(step in 1..10)touch(android.view.MotionEvent.ACTION_MOVE,y-step*12f,down+step*20)
      touch(android.view.MotionEvent.ACTION_UP,y-120f,down+220)
      SystemClock.sleep(200)
     }
    }
    click("Bold")
    scenario.onActivity {
     val c=ViewModelProvider(it)[ConfigState::class.java].config
     assertEquals(700,RichText.styleAt(c.hintRuns,1,c.hint).weight)
     assertEquals(400,RichText.styleAt(c.hintRuns,7,c.hint).weight)
     val gradient=Gradient(true,angle=137.5f)
     ViewModelProvider(it)[ConfigState::class.java].config=c.copy(hintRuns=c.hintRuns.map {r->r.copy(style=r.style.copy(italic=true,underline=true,strike=true,darkGradient=gradient,lightGradient=gradient))})
    }
    ins.uiAutomation.waitForIdle(500,5000)
    waitText("Bold")
    scenario.onActivity {
     val c=ViewModelProvider(it)[ConfigState::class.java].config
     assertTrue(c.hintRuns.first().style.darkGradient.enabled)
    }
   }
  } finally {AppLanguage.select(previous)}
 }
}
