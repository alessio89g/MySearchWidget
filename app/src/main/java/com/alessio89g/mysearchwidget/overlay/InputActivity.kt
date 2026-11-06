package com.alessio89g.mysearchwidget.overlay

import com.alessio89g.mysearchwidget.i18n.*
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import kotlin.math.roundToInt
import android.text.*
import android.view.*
import android.view.inputmethod.*
import android.widget.EditText
import android.widget.FrameLayout
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
 private var ready=false
 private var active=false
 private var pixelsPerDesignUnit:Float?=null
 private lateinit var editor:EditText
 private lateinit var handle:CursorHandle
 private lateinit var touchRoot:FrameLayout
 override fun onCreate(state:Bundle?) {
  super.onCreate(state);id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,0)
  if(id !in SearchWidget.ids(this)) { finish();return }
  window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
  window.setLayout(1,1)
  window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
  editor=object:EditText(this) {
   override fun onSelectionChanged(start:Int,end:Int) { super.onSelectionChanged(start,end);if(::editor.isInitialized) publish() }
   override fun onScrollChanged(l:Int,t:Int,oldl:Int,oldt:Int) {
    super.onScrollChanged(l,t,oldl,oldt)
    if(::editor.isInitialized && l!=oldl)publish()
   }
   override fun onKeyPreIme(keyCode:Int,event:KeyEvent):Boolean {
    if(keyCode==KeyEvent.KEYCODE_BACK && event.action==KeyEvent.ACTION_UP) { finish();return true };return super.onKeyPreIme(keyCode,event)
   }
  }.apply {
   setSingleLine(true);setTextColor(Color.TRANSPARENT);setBackgroundColor(Color.TRANSPARENT)
   // The widget draws the caret; our handle is independent of platform popup heuristics.
   isCursorVisible=false
   textCursorDrawable=ColorDrawable(Color.TRANSPARENT)
   setPadding(0,0,0,0);includeFontPadding=false;gravity=Gravity.CENTER_VERTICAL or Gravity.START
   minimumWidth=0;minimumHeight=0;isHorizontalScrollBarEnabled=false
   inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
   imeOptions=EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_EXTRACT_UI
   setImeActionLabel(tr(R.string.search),EditorInfo.IME_ACTION_SEARCH)
   filters=arrayOf(InputFilter.LengthFilter(8192))
  }
  touchRoot=FrameLayout(this)
  handle=CursorHandle(this,editor)
  touchRoot.addView(editor,FrameLayout.LayoutParams(-1,-1))
  touchRoot.addView(handle,FrameLayout.LayoutParams(-1,-1))
  setContentView(touchRoot);window.setLayout(1,1)
  window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
  scope.launch {
   config=withContext(Dispatchers.IO) { Repository(this@InputActivity).config(id) }
   val mode=intent.getStringExtra("renderedTheme")
   config=ModeSettings.resolve(config,if(mode in listOf("light","dark"))mode=="dark" else Renderer.dark(this@InputActivity,config)).copy(theme=mode?.takeIf {it in listOf("light","dark")} ?: config.theme)
   if(config.googleInput){Actions.openGoogleInput(this@InputActivity);finish();return@launch}
   if(state==null)WidgetHaptics.tap(this@InputActivity,config,intent.getStringExtra("element") ?: "field")
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
   handle.setColor(Renderer.cursorColor(this@InputActivity,config,false))
   placeTouchEditor()
   ready=true
   editor.requestFocus();editor.post { showKeyboard() };publish()
  }
 }
 override fun onResume(){super.onResume();active=true;if(ready)publish()}
 override fun onWindowFocusChanged(hasFocus:Boolean) {
  super.onWindowFocusChanged(hasFocus)
  if(hasFocus && ::editor.isInitialized){showKeyboard();publish()} else redraw?.cancel()
 }
 /** The launcher supplies the clicked view's bounds through the explicit input PendingIntent. */
 private fun placeTouchEditor() {
  val source=intent.sourceBounds ?: return
  if(source.isEmpty)return
  val size=WidgetViewport.from(this,AppWidgetManager.getInstance(this).getAppWidgetOptions(id))
  val presentation=WidgetPresentation(config,size.width,size.availableHeight)
  val c=presentation.config
  val d=ElementLayout(presentation.designWidth.toFloat(),c)
  val textBounds=Bounds(d.textLeft,d.textTop,d.textRight,d.textBottom)
  val clicked=(if(intent.getStringExtra("element")=="text")textBounds else d.inputHit)
   .visibleWithin(d.width,d.height) ?: return
  val sx=source.width()/clicked.width
  val sy=source.height()/clicked.height
  val textArea=textBounds.visibleWithin(d.width,d.height) ?: return
  val left=(source.left+(textArea.left-clicked.left)*sx).roundToInt()
  val top=(source.top+(textArea.top-clicked.top)*sy).roundToInt()
  val style=c.query
  val face=c.assets[style.font]?.let {runCatching {Assets.font(this,it)}.getOrNull()} ?: resources.getFont(R.font.google_sans)
  editor.typeface=Typeface.create(face,style.weight,style.italic)
  editor.setTextSize(TypedValue.COMPLEX_UNIT_PX,style.size*resources.configuration.fontScale*d.textScale*sy)
  editor.textScaleX=sx/sy
  editor.letterSpacing=0f
  pixelsPerDesignUnit=sx
  editor.layoutParams=FrameLayout.LayoutParams((textArea.width*sx).roundToInt().coerceAtLeast(1),(textArea.height*sy).roundToInt().coerceAtLeast(1)).apply {leftMargin=handle.sideInset}
  window.setDecorFitsSystemWindows(false)
  window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
  window.attributes=window.attributes.apply {
   // The launcher reports absolute screen coordinates, including the cutout area.
   layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
   fitInsetsTypes=0
   gravity=Gravity.TOP or Gravity.LEFT;x=left-handle.sideInset;y=top
   width=(textArea.width*sx).roundToInt().coerceAtLeast(1)+2*handle.sideInset
   height=(textArea.height*sy).roundToInt().coerceAtLeast(1)+handle.extraHeight
  }
 }
 private fun showKeyboard() { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editor,InputMethodManager.SHOW_IMPLICIT) }
 override fun dispatchTouchEvent(event:MotionEvent):Boolean {
  if(event.actionMasked==MotionEvent.ACTION_OUTSIDE) {finish();return true}
  return super.dispatchTouchEvent(event)
 }
 private fun publish() {
  if(::handle.isInitialized)handle.invalidate()
  redraw?.cancel()
  if(!ready || !active || !hasWindowFocus())return
  val session=SessionText(editor.text.toString(),editor.selectionStart.coerceAtLeast(0),scrollOffset=pixelsPerDesignUnit?.let {editor.scrollX/it},editorPaint=pixelsPerDesignUnit?.let {android.text.TextPaint(editor.paint)},editorScale=pixelsPerDesignUnit ?: 1f)
  redraw=scope.launch {
   delay(16)
   val manager=AppWidgetManager.getInstance(this@InputActivity)
   val size=WidgetViewport.from(this@InputActivity,manager.getAppWidgetOptions(id))
   val repo=Repository(this@InputActivity)
   val engine=repo.engine(ModeSettings.resolve(config,config.theme=="dark").engineId)
   val darkEngine=repo.engine(ModeSettings.resolve(config,true).engineId)
   ensureActive()
   // Reuse the two frames while idle: no rendering or repository reads on each blink.
   val visible=SearchWidget.views(this@InputActivity,id,config,size.width,session,size.availableHeight,engine,darkEngine)
   val hidden=SearchWidget.views(this@InputActivity,id,config,size.width,session.copy(cursorVisible=false),size.availableHeight,engine,darkEngine)
   var show=true
   while(isActive && active && hasWindowFocus()) {
    manager.updateAppWidget(id,if(show)visible else hidden)
    delay(500)
    show=!show
   }
  }
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
 override fun onPause(){active=false;redraw?.cancel();super.onPause();finish()}
 override fun onDestroy() {
  scope.cancel()
  // One final, bounded update; no worker/service survives the input session.
  if(id>0) CoroutineScope(Dispatchers.Main).launch { runCatching { SearchWidget.update(applicationContext,id) } }
  super.onDestroy()
 }
}
