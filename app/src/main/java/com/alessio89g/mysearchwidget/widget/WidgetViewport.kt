package com.alessio89g.mysearchwidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import com.alessio89g.mysearchwidget.data.*

/** The launcher allocation, shared by its RemoteViews and the configurator preview. */
data class WidgetViewport(val width:Int,val availableHeight:Float?) {
 fun fit(config:WidgetConfig)=config.fitOuter(width.toFloat(),availableHeight ?: config.heightDp)
 companion object {
  fun hasAllocation(options:Bundle)=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,0)>0
  fun from(context:Context,options:Bundle):WidgetViewport {
   val landscape=context.resources.configuration.orientation==Configuration.ORIENTATION_LANDSCAPE
   val minimum=(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,0).takeIf {it>0} ?: 356).coerceAtLeast(180)
   val width=if(landscape)options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,minimum).coerceAtLeast(minimum) else minimum
   val height=options.getInt(if(landscape)AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,0)
   return WidgetViewport(width.coerceIn(180,1000),height.takeIf {it>=WidgetDimensions.MIN_HEIGHT}?.toFloat())
  }
 }
}
