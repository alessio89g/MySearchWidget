package com.alessio89g.mysearchwidget.overlay

import com.alessio89g.mysearchwidget.i18n.*
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.graphics.Color
import android.text.*
import android.view.*
import android.view.inputmethod.*
import android.widget.EditText
import android.widget.Toast
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.*
import kotlinx.coroutines.*

class InputActivity:Activity() {
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
 private var id=0
 private var config=WidgetConfig()
 private var redraw:Job?=null
 private lateinit var editor:EditText
 override fun onCreate(state:Bundle?) {
  super.onCreate(state);id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,0)
  if(id !in SearchWidget.ids(this)) { finish();return }
  window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
  window.setLayout(1,1)
  window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
  editor=object:EditText(this) {
   override fun onSelectionChanged(start:Int,end:Int) { super.onSelectionChanged(start,end);if(::editor.isInitialized) publish() }
   override fun onKeyPreIme(keyCode:Int,event:KeyEvent):Boolean {
    if(keyCode==KeyEvent.KEYCODE_BACK && event.action==KeyEvent.ACTION_UP) { finish();return true };return super.onKeyPreIme(keyCode,event)
   }
  }.apply {
   setSingleLine(true);setTextColor(Color.TRANSPARENT);setBackgroundColor(Color.TRANSPARENT)
   inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
   imeOptions=EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI
   setImeActionLabel(tr(R.string.search),EditorInfo.IME_ACTION_SEARCH)
   filters=arrayOf(InputFilter.LengthFilter(8192))
  }
  setContentView(editor);window.setLayout(1,1)
  scope.launch {
   config=withContext(Dispatchers.IO) { Repository(this@InputActivity).config(id) }
   editor.addTextChangedListener(object:TextWatcher {
    override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}
    override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){}
    override fun afterTextChanged(s:Editable?){ publish() }
   })
   editor.setOnEditorActionListener { _,action,event ->
    val enter=event?.keyCode==KeyEvent.KEYCODE_ENTER && event.action==KeyEvent.ACTION_UP
    if(enter || (event==null && action in listOf(EditorInfo.IME_ACTION_SEARCH,EditorInfo.IME_ACTION_GO,EditorInfo.IME_ACTION_DONE,EditorInfo.IME_ACTION_SEND,EditorInfo.IME_ACTION_NEXT,EditorInfo.IME_NULL))) {
     submit();true
    } else event?.keyCode==KeyEvent.KEYCODE_ENTER
   }
   editor.requestFocus();editor.post { showKeyboard() };publish()
  }
 }
 override fun onWindowFocusChanged(hasFocus:Boolean) { super.onWindowFocusChanged(hasFocus);if(hasFocus && ::editor.isInitialized)showKeyboard() }
 private fun showKeyboard() { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editor,InputMethodManager.SHOW_IMPLICIT) }
 private fun publish() {
  redraw?.cancel()
  val session=SessionText(editor.text.toString(),editor.selectionStart.coerceAtLeast(0))
  redraw=scope.launch { delay(16);SearchWidget.update(this@InputActivity,id,config,session) }
 }
 private fun submit() {
  val query=editor.text.toString().trim();if(query.isEmpty())return
  scope.launch {
   val engine=Repository(this@InputActivity).engine(config.engineId)
   runCatching { Validation.engine(engine);Actions.openSearch(this@InputActivity,engine.template.replace("%s",android.net.Uri.encode(query))) }
    .onFailure { Toast.makeText(this@InputActivity,tr(R.string.operation_failed,errorText(it)),Toast.LENGTH_LONG).show() }
   finish()
  }
 }
 override fun onPause(){super.onPause();finish()}
 override fun onDestroy() {
  scope.cancel()
  // One final, bounded update; no worker/service survives the input session.
  if(id>0) CoroutineScope(Dispatchers.Main).launch { runCatching { SearchWidget.update(applicationContext,id) } }
  super.onDestroy()
 }
}
