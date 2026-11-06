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
   val size=WidgetViewport.from(context,options)
   val actual=size.width
   val config=c ?: Repository(context).let {if(WidgetViewport.hasAllocation(options))it.anchorWidth(id,actual) else it.config(id)}
   manager.updateAppWidget(id,views(context,id,config,actual,text,size.availableHeight,Repository(context).engine(ModeSettings.resolve(config,config.theme=="dark").engineId),Repository(context).engine(ModeSettings.resolve(config,true).engineId)))
  }
  fun views(context:Context,id:Int,config:WidgetConfig,width:Int,text:SessionText?=null,availableHeight:Float?=null,engine:Engine?=null,darkEngine:Engine?=engine):RemoteViews {
   if(config.theme=="system" && config.modes.dark.isNotEmpty()) {
    val rv=RemoteViews(context.packageName,R.layout.widget_modes)
    rv.addView(R.id.light_variant,views(context,id,ModeSettings.resolve(config,false).copy(theme="light"),width,text,availableHeight,engine))
    rv.addView(R.id.dark_variant,views(context,id,ModeSettings.resolve(config,true).copy(theme="dark"),width,text,availableHeight,darkEngine))
    rv.setColorInt(R.id.light_variant,"setVisibility",View.VISIBLE,View.GONE)
    rv.setColorInt(R.id.dark_variant,"setVisibility",View.GONE,View.VISIBLE)
    return rv
   }
   val resolved=ModeSettings.resolve(config,Renderer.dark(context,config))
   val presentation=WidgetPresentation(resolved,width.coerceIn(180,1000),availableHeight)
   val c=presentation.config
   val designWidth=presentation.designWidth
   val idle=text==null && c.idleCursor && !c.googleInput
   val rv=RemoteViews(context.packageName,if(idle)R.layout.widget_blink else R.layout.widget)
   val dimensions=ElementLayout(designWidth.toFloat(),c)
   val dp=android.util.TypedValue.COMPLEX_UNIT_DIP
   // Keep bitmap and hit targets in the same dp coordinate space, even if a
   // launcher changes its padding without updating the reported dimensions.
   rv.setViewLayoutWidth(R.id.widget_frame,presentation.width,dp)
   rv.setViewLayoutHeight(R.id.widget_frame,presentation.height,dp)
   val light=if(c.theme=="system")c.copy(theme="light") else c
   val base=Renderer.render(context,light,designWidth,text,engine,idleCaret=false)
   val darkBase=if(c.theme=="system")Renderer.render(context,c.copy(theme="dark"),designWidth,text,engine,idleCaret=false) else base
   if(c.theme=="system")rv.setIcon(R.id.art,"setImageIcon",Icon.createWithBitmap(base),Icon.createWithBitmap(darkBase))
   else rv.setImageViewBitmap(R.id.art,base)
   if(idle) {
    // Like Android's clock widgets, animation belongs to a native host view.
    // Only a narrow strip changes; no app timer, wake lock or per-tick bitmap allocation.
    val density=base.width/designWidth.toFloat()
    val left=kotlin.math.floor(dimensions.textLeft*density).toInt().coerceIn(0,base.width-1)
    val top=kotlin.math.floor(dimensions.textTop*density).toInt().coerceIn(0,base.height-1)
    val right=kotlin.math.ceil((dimensions.textLeft+dimensions.textScale)*density).toInt().coerceIn(left+1,base.width)
    val bottom=kotlin.math.ceil(dimensions.textBottom*density).toInt().coerceIn(top+1,base.height)
    fun patch(bitmap:android.graphics.Bitmap)=android.graphics.Bitmap.createBitmap(bitmap,left,top,right-left,bottom-top)
    val on=patch(Renderer.render(context,light,designWidth,engine=engine,idleCaret=true))
    val off=patch(base)
    if(c.theme=="system") {
     val darkOn=patch(Renderer.render(context,c.copy(theme="dark"),designWidth,engine=engine,idleCaret=true))
     rv.setIcon(R.id.caret_on,"setImageIcon",Icon.createWithBitmap(on),Icon.createWithBitmap(darkOn))
     rv.setIcon(R.id.caret_off,"setImageIcon",Icon.createWithBitmap(off),Icon.createWithBitmap(patch(darkBase)))
    } else {rv.setImageViewBitmap(R.id.caret_on,on);rv.setImageViewBitmap(R.id.caret_off,off)}
    val scale=presentation.scale/density
    rv.setViewLayoutWidth(R.id.idle_caret,(right-left)*scale,dp)
    rv.setViewLayoutHeight(R.id.idle_caret,(bottom-top)*scale,dp)
    rv.setViewLayoutMargin(R.id.idle_caret,RemoteViews.MARGIN_LEFT,left*scale,dp)
    rv.setViewLayoutMargin(R.id.idle_caret,RemoteViews.MARGIN_TOP,top*scale,dp)
   }
   fun pending(slot:Int,input:Boolean=false,element:String):PendingIntent {
    val intent=Intent(context,if(input && !c.googleInput)InputActivity::class.java else ActionActivity::class.java)
     .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
     .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("slot",slot).putExtra("element",element).putExtra("renderedTheme",c.theme)
     .setData(android.net.Uri.parse("mysearchwidget://widget/$id/${element}/${c.theme}"))
    // Only input needs the host-provided source bounds to align its touch editor.
    // The target component, widget ID and route are explicit and cannot be replaced by fill-in.
    val mutability=if(input && !c.googleInput)PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
    return PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or mutability)
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
    val designBounds=when(layer) {
     "field"->dimensions.inputHit
     "logo"->dimensions.logoHit
     "text"->Bounds(dimensions.textLeft,dimensions.textTop,dimensions.textRight,dimensions.textBottom)
     else->if(layer.startsWith("button"))dimensions.buttonHits[index!!] else dimensions.iconHits[index!!]
    }.visibleWithin(dimensions.width,dimensions.height) ?: continue
    val bounds=presentation.transform(designBounds)
    val child=RemoteViews(context.packageName,layout)
    child.setViewLayoutWidth(view,bounds.width,dp);child.setViewLayoutHeight(view,bounds.height,dp)
    child.setViewLayoutMargin(view,RemoteViews.MARGIN_LEFT,bounds.left,dp)
    child.setViewLayoutMargin(view,RemoteViews.MARGIN_TOP,bounds.top,dp)
    val input=layer=="field" || layer=="text"
    child.setOnClickPendingIntent(view,pending(if(input)-2 else index ?: -1,input,layer))
    child.setContentDescription(view,if(input)text?.text ?: placeholderText(c,engine) else label(if(index==null)c.logo else c.buttons[index]))
    rv.addView(R.id.hit_row,child)
   }
   return rv
  }
  private fun label(s:Slot)=shortcutTitle(s.tap).ifEmpty {tr(R.string.shortcut_label)}
 }
}
