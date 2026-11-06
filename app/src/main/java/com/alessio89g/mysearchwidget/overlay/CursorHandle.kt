package com.alessio89g.mysearchwidget.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import kotlin.math.abs
import kotlin.math.roundToInt

/** Insertion handle for the focused session. Android owns range-selection handles. */
internal class CursorHandle(context:Context,private val editor:EditText):View(context) {
 private val unit=resources.displayMetrics.density
 val extraHeight=(32*unit).roundToInt()
 val sideInset=(24*unit).roundToInt()
 private val radius=9*unit
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 fun setColor(color:Int){
  paint.color=color
  editor.highlightColor=(color and 0x00ffffff) or 0x55000000
  editor.textSelectHandleLeft?.mutate()?.apply {setTint(color)}?.let {editor.setTextSelectHandleLeft(it)}
  editor.textSelectHandleRight?.mutate()?.apply {setTint(color)}?.let {editor.setTextSelectHandleRight(it)}
  invalidate()
 }
 private var dragging=false
 private var pointerX=0f
 private var grabOffset=0f
 private fun x(offset:Int):Float {
  val layout=editor.layout ?: return editor.left.toFloat()
  return (editor.left+editor.compoundPaddingLeft+layout.getPrimaryHorizontal(offset.coerceIn(0,editor.length()))-editor.scrollX)
   .coerceIn(editor.left.toFloat(),editor.right.toFloat())
 }
 private fun y()=editor.bottom.toFloat()+radius
 init { importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO }
 override fun onDraw(canvas:Canvas) {
  if(editor.text.isEmpty() || editor.layout==null || editor.selectionStart!=editor.selectionEnd)return
  val positions=listOf(editor.selectionStart)
  for(offset in positions) {
   val cx=x(offset);val cy=y()
   canvas.drawPath(Path().apply {
    moveTo(cx,editor.bottom.toFloat()-2*unit)
    lineTo(cx-radius*.8f,cy-radius*.3f);lineTo(cx+radius*.8f,cy-radius*.3f);close()
   },paint)
   canvas.drawCircle(cx,cy,radius,paint)
  }
 }
 override fun onTouchEvent(event:MotionEvent):Boolean {
  when(event.actionMasked) {
   MotionEvent.ACTION_DOWN->{
    if(editor.text.isEmpty() || editor.selectionStart!=editor.selectionEnd || abs(event.y-y())>24*unit)return false
    val cx=x(editor.selectionStart)
    if(abs(event.x-cx)>24*unit)return false
    dragging=true;grabOffset=event.x-cx;pointerX=event.x-grabOffset
    parent.requestDisallowInterceptTouchEvent(true);postOnAnimation(scrollWhileDragging)
    return true
   }
   MotionEvent.ACTION_MOVE->{if(!dragging)return false;pointerX=event.x-grabOffset;moveToPointer();return true}
   MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{
    if(!dragging)return false
    dragging=false;removeCallbacks(scrollWhileDragging);parent.requestDisallowInterceptTouchEvent(false)
    if(event.actionMasked==MotionEvent.ACTION_UP)performClick()
    return true
   }
  }
  return dragging
 }
 private fun select(offset:Int) {
  val next=offset.coerceIn(0,editor.length())
  editor.setSelection(next)
  editor.bringPointIntoView(next)
  invalidate()
 }
 private fun moveToPointer()=select(editor.getOffsetForPosition(pointerX-editor.left,editor.height/2f))
 private val scrollWhileDragging=object:Runnable {
  override fun run() {
   if(!dragging)return
   if(pointerX<editor.left || pointerX>editor.right) {
    val text=editor.text.toString();val old=editor.selectionStart.coerceAtLeast(0)
    val next=when {
     pointerX<editor.left && old>0->Character.offsetByCodePoints(text,old,-1)
     pointerX>editor.right && old<text.length->Character.offsetByCodePoints(text,old,1)
     else->old
    }
    select(next)
   }
   postDelayed(this,60)
  }
 }
 override fun performClick():Boolean {super.performClick();return true}
 override fun onDetachedFromWindow(){dragging=false;removeCallbacks(scrollWhileDragging);super.onDetachedFromWindow()}
}
