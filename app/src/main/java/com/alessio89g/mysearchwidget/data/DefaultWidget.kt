package com.alessio89g.mysearchwidget.data

import android.content.Context
import com.alessio89g.mysearchwidget.R

/** Creation and explicit-reset preset. Keep serialization defaults stable for existing backups. */
object DefaultWidget {
 @Volatile private var cached:WidgetConfig?=null
 fun create(context:Context):WidgetConfig = cached ?: synchronized(this) {
  cached ?: context.resources.openRawResource(R.raw.default_widget).bufferedReader().use {
   Catalog.json.decodeFromString<WidgetConfig>(it.readText())
  }.also { Validation.config(it);cached=it }
 }
}
