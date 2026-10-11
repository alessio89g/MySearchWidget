package com.alessio89g.mysearchwidget.config

import android.app.ActivityOptions
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.alessio89g.mysearchwidget.widget.SearchWidget
import java.util.UUID

object WidgetPinning {
 // The launcher fills in EXTRA_APPWIDGET_ID only after accepting the pin request.
 // Use a direct activity callback: no receiver trampoline or background service.
 fun confirmation(context:Context):PendingIntent {
  val intent=Intent(context,ConfigActivity::class.java)
   .setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
   .setData(Uri.parse("mysearchwidget://pin/${UUID.randomUUID()}"))
   .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  val options=if(Build.VERSION.SDK_INT>=35)ActivityOptions.makeBasic().apply {
   setPendingIntentCreatorBackgroundActivityStartMode(
    if(Build.VERSION.SDK_INT>=36)ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_ALWAYS
    else ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
  }.toBundle() else null
  return PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE,options)
 }
 fun request(context:Context):Boolean {
  val manager=AppWidgetManager.getInstance(context)
  if(!manager.isRequestPinAppWidgetSupported)return false
  val callback=confirmation(context)
  return try {
   manager.requestPinAppWidget(ComponentName(context,SearchWidget::class.java),null,callback).also {if(!it)callback.cancel()}
  } catch(e:RuntimeException) {callback.cancel();throw e}
 }
}
