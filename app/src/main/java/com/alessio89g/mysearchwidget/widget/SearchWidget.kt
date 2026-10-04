package com.alessio89g.mysearchwidget.widget

import com.alessio89g.mysearchwidget.i18n.*
import android.app.PendingIntent
import android.appwidget.*
import android.content.*
import android.os.Bundle
import android.graphics.drawable.Icon
import android.view.View
import android.widget.RemoteViews
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.overlay.*
import kotlinx.coroutines.*

class SearchWidget:AppWidgetProvider() {
 override fun onReceive(context:Context,intent:Intent) {
  super.onReceive(context,intent)
  if(intent.action==PIN_CONFIRMED) {
   val id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID)
   if(id in ids(context))work {
    val repo=Repository(context)
    repo.confirmPinned(id)
    update(context,id)
   }
  }
  if(intent.action in listOf(Intent.ACTION_WALLPAPER_CHANGED,Intent.ACTION_MY_PACKAGE_REPLACED))work { ids(context).forEach { update(context,it) } }
 }
 override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray) { work { ids.forEach { update(context,it) } } }
 override fun onAppWidgetOptionsChanged(context:Context,manager:AppWidgetManager,id:Int,options:Bundle) { work { update(context,id) } }
 override fun onDeleted(context:Context,ids:IntArray) { work { ids.forEach { Repository(context).remove(it) } } }
 private fun work(block:suspend ()->Unit) {
  val pending=goAsync()
  CoroutineScope(Dispatchers.IO).launch { try { block() } finally { pending.finish() } }
 }
 companion object {
  const val PIN_CONFIRMED="com.alessio89g.mysearchwidget.PIN_CONFIRMED"
  fun ids(context:Context)=AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context,SearchWidget::class.java))
  suspend fun update(context:Context,id:Int,c:WidgetConfig?=null,text:SessionText?=null) {
   val manager=AppWidgetManager.getInstance(context)
   val options=manager.getAppWidgetOptions(id)
   val width=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,356).coerceAtLeast(180)
   val landscape=context.resources.configuration.orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE
   val actual=if(landscape)options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,width).coerceAtLeast(width) else width
   val config=c ?: Repository(context).config(id)
   val availableHeight=options.getInt(if(landscape)AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,0)
   // Preserve the saved preference; fit only this rendering into the launcher allocation.
   val shown=config.fitOuter(actual.coerceIn(180,1000).toFloat(),if(availableHeight>=WidgetDimensions.MIN_HEIGHT)availableHeight.toFloat() else config.heightDp)
   manager.updateAppWidget(id,views(context,id,shown,actual,text))
  }
  fun views(context:Context,id:Int,config:WidgetConfig,width:Int,text:SessionText?=null):RemoteViews {
   val c=config.fitOuter(width.coerceIn(180,1000).toFloat())
   val rv=RemoteViews(context.packageName,R.layout.widget)
   val dimensions=ElementLayout(width.coerceIn(180,1000).toFloat(),c)
   val dp=android.util.TypedValue.COMPLEX_UNIT_DIP
   rv.setViewLayoutHeight(R.id.widget_frame,c.heightDp,dp)
   if(c.theme=="system") {
    // Android resolves these in the host configuration, even when our process is
    // not running. Do not depend on CONFIGURATION_CHANGED delivery to a receiver.
    rv.setIcon(R.id.art,"setImageIcon",
     Icon.createWithBitmap(Renderer.render(context,c.copy(theme="light"),width,text)),
     Icon.createWithBitmap(Renderer.render(context,c.copy(theme="dark"),width,text)))
   }else rv.setImageViewBitmap(R.id.art,Renderer.render(context,c,width,text))
   fun pending(slot:Int,input:Boolean=false):PendingIntent {
    val intent=Intent(context,if(input && !c.googleInput)InputActivity::class.java else ActionActivity::class.java)
     .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
     .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("slot",slot)
     .setData(android.net.Uri.parse("mysearchwidget://widget/$id/${if(input)"input" else slot.toString()}"))
    return PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
   }
   rv.removeAllViews(R.id.hit_row)
   for(layer in c.visibleLayers()) {
    if(layer=="outer")continue
    val index=layer.last().digitToIntOrNull()
    val (layout,view)=when(layer) {
     "field"->R.layout.hit_input to R.id.input
     "logo"->R.layout.hit_logo to R.id.logo
     "text"->R.layout.hit_text to R.id.text_target
     "button0"->R.layout.hit_button0 to R.id.button0
     "button1"->R.layout.hit_button1 to R.id.button1
     "button2"->R.layout.hit_button2 to R.id.button2
     "icon0"->R.layout.hit_icon0 to R.id.icon0
     "icon1"->R.layout.hit_icon1 to R.id.icon1
     else->R.layout.hit_icon2 to R.id.icon2
    }
    val bounds=when(layer) {
     "field"->dimensions.inputHit
     "logo"->dimensions.logoHit
     "text"->Bounds(dimensions.textLeft,dimensions.textTop,dimensions.textRight,dimensions.textBottom)
     else->if(layer.startsWith("button"))dimensions.buttonHits[index!!] else dimensions.iconHits[index!!]
    }.visibleWithin(dimensions.width,dimensions.height) ?: continue
    val child=RemoteViews(context.packageName,layout)
    child.setViewLayoutWidth(view,bounds.width,dp);child.setViewLayoutHeight(view,bounds.height,dp)
    child.setViewLayoutMargin(view,RemoteViews.MARGIN_LEFT,bounds.left,dp)
    child.setViewLayoutMargin(view,RemoteViews.MARGIN_TOP,bounds.top,dp)
    val input=layer=="field" || layer=="text"
    child.setOnClickPendingIntent(view,pending(if(input)-2 else index ?: -1,input))
    child.setContentDescription(view,if(input)text?.text ?: c.placeholder.ifEmpty {tr(R.string.placeholder)} else label(if(index==null)c.logo else c.buttons[index]))
    rv.addView(R.id.hit_row,child)
   }
   return rv
  }
  private fun label(s:Slot)=shortcutTitle(s.tap).ifEmpty {tr(R.string.shortcut_label)}
 }
}
