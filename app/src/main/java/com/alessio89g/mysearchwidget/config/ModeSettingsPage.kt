package com.alessio89g.mysearchwidget.config

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.i18n.*

@Composable fun ModeSettingsPage(config:WidgetConfig,onNavigate:()->Unit={},onChange:(WidgetConfig)->Unit) {
 var pendingLink by rememberSaveable {mutableStateOf<String?>(null)}
 var selected by rememberSaveable {mutableStateOf(0)}
 val sections=listOf(
  R.string.mode_sizes to (ModeSettings.elements.map {"size.$it"}+"size.query"),
  R.string.mode_positions to ModeSettings.elements.map {"position.$it"},
  R.string.mode_layers to ModeSettings.elements.map {"layer.$it"},
  R.string.mode_colors to COLOR_GROUPS,
  R.string.mode_content to listOf("search.engine","search.google","logo","count","shape.outer","shape.field")+(0..2).flatMap {listOf("shape.button$it","icon.$it")},
  R.string.mode_text to listOf("placeholder","font.hint","text.hint","font.query","text.query"),
  R.string.actions to listOf("action.field","action.logo")+(0..2).map {"action.button$it"}
 )
 if(selected==0) {
  SettingsGroup(sections.map {(title,_)->SettingsEntry(tr(title),"", "Lock"){selected=title;onNavigate()}})
 } else {
  DetailBack {selected=0;onNavigate()}
  val (title,keys)=sections.first {it.first==selected}
  SettingCard(tr(title),tr(R.string.mode_switch_detail)) {
   keys.forEach {key->
    fun change(enabled:Boolean) {
     if(!enabled && ModeSettings.hasDifferentValues(config,key))pendingLink=key
     else onChange(ModeSettings.toggle(config,key,enabled,false))
    }
    Toggle(modeLabel(key),key in config.modes.separate){change(it)}
    TextButton(onClick={change(key in COLOR_GROUPS)}){Text(tr(R.string.reset_named,modeLabel(key)))}
   }
  }
 }
 pendingLink?.let {key->
  AlertDialog(onDismissRequest={pendingLink=null},title={Text(tr(R.string.mode_relink_title))},
   text={Text(tr(R.string.mode_relink_help,modeLabel(key)))},
   confirmButton={Column {
    TextButton(onClick={onChange(ModeSettings.toggle(config,key,false,false));pendingLink=null}){Text(tr(R.string.mode_keep_light))}
    TextButton(onClick={onChange(ModeSettings.toggle(config,key,false,true));pendingLink=null}){Text(tr(R.string.mode_keep_dark))}
   }},dismissButton={TextButton(onClick={pendingLink=null}){Text(tr(R.string.cancel))}})
 }
}
fun modeLabel(key:String):String {
 val part=key.substringAfter('.')
 return when {
  key=="search.engine"->tr(R.string.selected_engine)
  key=="search.google"->tr(R.string.google_input_title)
  key.startsWith("shape.")->tr(R.string.mode_shape,modeLabel("color.$part"))
  key=="placeholder"->tr(R.string.placeholder_label)
  key.startsWith("font.")->tr(R.string.mode_font,if(part=="hint")tr(R.string.placeholder_label) else tr(R.string.typed_text))
  key.startsWith("text.")->tr(R.string.mode_format,if(part=="hint")tr(R.string.placeholder_label) else tr(R.string.typed_text))
  key=="logo"->"Logo"
  key=="count"->tr(R.string.mode_count)
  key.startsWith("icon.")->tr(R.string.mode_button_icon,part.toInt()+1)
  part.startsWith("button")->tr(R.string.button_number,part.removePrefix("button").toInt()+1)
  part.startsWith("icon")->tr(R.string.mode_button_icon,part.removePrefix("icon").toInt()+1)
  part=="outer"->tr(R.string.outer_border)
  part=="field"->tr(R.string.search_field)
  part=="logo"->"Logo"
  part=="query"->tr(R.string.typed_text)
  else->tr(R.string.placeholder_label)
 }
}

val LocalModeSeparations=staticCompositionLocalOf {COLOR_GROUPS.toSet()}
@Composable fun ModeNotice(keys:List<String>) {
 val separate=LocalModeSeparations.current
 fun label(key:String):String {
  val prefix=when(key.substringBefore('.')){"color"->tr(R.string.mode_colors);"size"->tr(R.string.layout_dimensions);"position"->tr(R.string.layout_position);"layer"->tr(R.string.layer_order);else->""}
  return (if(prefix.isEmpty())"" else "$prefix · ")+modeLabel(key)
 }
 for(split in listOf(false,true)) {
  val matching=keys.distinct().filter {(it in separate)==split}
  if(matching.isNotEmpty())Text(tr(if(split)R.string.mode_separate_notice else R.string.mode_linked_notice,matching.joinToString {label(it)}),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
 }
}
