package com.alessio89g.mysearchwidget.overlay

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.*
import android.widget.*
import android.graphics.Color
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.SearchWidget
import kotlinx.coroutines.*

class ActionActivity:Activity() {
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main)
 override fun onCreate(state:Bundle?) {
  super.onCreate(state)
  val id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,0)
  if(id !in SearchWidget.ids(this)) { finish();return }
  scope.launch {
   val c=Repository(this@ActionActivity).config(id)
   val index=intent.getIntExtra("slot",-1)
   if(index==-2){if(c.googleInput)Actions.openGoogleInput(this@ActionActivity);finish();return@launch}
   val s=if(index==-1)c.logo else c.buttons.getOrNull(index) ?: run { finish();return@launch }
   run(s.tap)
  }
 }
 private fun run(action:Shortcut) { Actions.execute(this,action);finish() }
 override fun onPause(){super.onPause();finish()}
 override fun onDestroy(){scope.cancel();super.onDestroy()}
}
