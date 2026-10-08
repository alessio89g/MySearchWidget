package com.alessio89g.mysearchwidget.i18n

import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.data.*

/** Display-only resolution: never replace the stored text or its formatting. */
fun placeholderText(config:WidgetConfig,engine:Engine?=null):String = config.placeholder.ifEmpty {
 val name=if(config.googleInput)"Google" else engine?.takeIf {it.id==config.engineId}?.name
  ?: Catalog.engines.firstOrNull {it.id==config.engineId}?.name ?: "Google"
 tr(R.string.placeholder_engine,name)
}
