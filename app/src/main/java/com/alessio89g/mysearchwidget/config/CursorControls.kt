package com.alessio89g.mysearchwidget.config

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.i18n.tr

@Composable fun CursorControls(config:WidgetConfig,dark:Boolean,onChange:(WidgetConfig)->Unit) {
 Text(tr(if(config.googleInput)R.string.idle_cursor_google_disabled else R.string.idle_cursor_help),style=MaterialTheme.typography.bodySmall)
 CompositionLocalProvider(LocalControlsEnabled provides !config.googleInput) {
  Column(Modifier.graphicsLayer {alpha=if(config.googleInput).45f else 1f}) {
   Toggle(tr(R.string.idle_cursor),config.idleCursor){onChange(config.copy(idleCursor=it))}
   ResetOptions(config,listOf(resetOption(R.string.idle_cursor,"idleCursor")),onChange)
  }
 }
 if(config.dynamic)Text(tr(R.string.material_notice),style=MaterialTheme.typography.bodySmall)
 for(idle in listOf(true,false)) {
  val label=if(idle)R.string.idle_cursor_color else R.string.query_cursor_color
  val prefix=if(idle)"idleCursorColors" else "queryCursorColors"
  val colors=if(idle)config.idleCursorColors else config.queryCursorColors
  val enabled=!config.dynamic && !config.googleInput && (!idle || config.idleCursor)
  ModeNotice(listOf(if(idle)"color.idleCursor" else "color.queryCursor"))
  CompositionLocalProvider(LocalControlsEnabled provides enabled) {
   Column(Modifier.graphicsLayer {alpha=if(enabled)1f else .45f}) {
    ColorControl(tr(label),if(dark)colors.dark else colors.light){value->
     val changed=if(dark)colors.copy(dark=value) else colors.copy(light=value)
     onChange(if(idle)config.copy(idleCursorColors=changed) else config.copy(queryCursorColors=changed))
    }
    ResetOptions(config,listOf(resetOption(label,prefix+if(dark)".dark" else ".light")),onChange)
   }
  }
 }
}
