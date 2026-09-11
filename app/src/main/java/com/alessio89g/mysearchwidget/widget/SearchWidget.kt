package com.alessio89g.mysearchwidget.widget

import com.alessio89g.mysearchwidget.i18n.*
import android.app.PendingIntent
import android.appwidget.*
import android.content.*
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.overlay.*
import kotlinx.coroutines.*

class SearchWidget:AppWidgetProvider() {
 override fun onReceive(context:Context,intent:Intent) {
  super.onReceive(context,intent)
  if(intent.action in listOf(Intent.ACTION_WALLPAPER_CHANGED,Intent.ACTION_CONFIGURATION_CHANGED,Intent.ACTION_MY_PACKAGE_REPLACED))work { ids(context).forEach { update(context,it) } }
 }
 override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray) { work { ids.forEach { update(context,it) } } }
 override fun onAppWidgetOptionsChanged(context:Context,manager:AppWidgetManager,id:Int,options:Bundle) { work { update(context,id) } }
 override fun onDeleted(context:Context,ids:IntArray) { work { ids.forEach { Repository(context).remove(it) } } }
 private fun work(block:suspend ()->Unit) {
  val pending=goAsync()
  CoroutineScope(Dispatchers.IO).launch { try { block() } finally { pending.finish() } }
 }
 companion object {
  fun ids(context:Context)=AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context,SearchWidget::class.java))
  suspend fun update(context:Context,id:Int,c:WidgetConfig?=null,text:SessionText?=null) {
   val manager=AppWidgetManager.getInstance(context)
   val options=manager.getAppWidgetOptions(id)
   val width=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,356).coerceAtLeast(180)
   val landscape=context.resources.configuration.orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE
   val actual=if(landscape)options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,width).coerceAtLeast(width) else width
   manager.updateAppWidget(id,views(context,id,c ?: Repository(context).config(id),actual,text))
  }
  fun views(context:Context,id:Int,c:WidgetConfig,width:Int,text:SessionText?=null):RemoteViews {
   val rv=RemoteViews(context.packageName,R.layout.widget)
   rv.setImageViewBitmap(R.id.art,Renderer.render(context,c,width,text))
   fun pending(slot:Int,input:Boolean=false):PendingIntent {
    val intent=Intent(context,if(input && !c.googleInput)InputActivity::class.java else ActionActivity::class.java)
     .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
     .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("slot",slot)
     .setData(android.net.Uri.parse("mysearchwidget://widget/$id/${if(input)"input" else slot.toString()}"))
    return PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
   }
   rv.setOnClickPendingIntent(R.id.logo,pending(-1))
   rv.setOnClickPendingIntent(R.id.input,pending(-2,true))
   rv.setContentDescription(R.id.input, text?.text?.let { tr(R.string.search_query,it) } ?: c.placeholder.ifEmpty { tr(R.string.placeholder) })
   rv.setContentDescription(R.id.logo,label(c.logo))
   listOf(R.id.button0,R.id.button1,R.id.button2).forEachIndexed { i,view ->
    rv.setViewVisibility(view,if(i<c.count)View.VISIBLE else View.GONE)
    rv.setOnClickPendingIntent(view,pending(i));rv.setContentDescription(view,label(c.buttons[i]))
   };return rv
  }
  private fun label(s:Slot)=shortcutTitle(s.tap).ifEmpty {tr(R.string.shortcut_label)}
 }
}
