package com.alessio89g.mysearchwidget.config

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.i18n.*

data class ResetOption(val label:String,val paths:List<String>,val enabled:Boolean=true)
fun resetOption(label:Int,vararg paths:String)=ResetOption(tr(label),paths.toList())

@Composable fun ResetOptions(config:WidgetConfig,options:List<ResetOption>,onChange:(WidgetConfig)->Unit) {
 val defaults=DefaultWidget.create(LocalContext.current).initialWidth(config.referenceWidthDp ?: 374)
 if(options.isNotEmpty())Section(tr(R.string.reset_options)) {
  Text(tr(R.string.reset_options_help),style=MaterialTheme.typography.bodySmall)
  options.forEach {option->
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
    Text(option.label,Modifier.weight(1f))
    TextButton(enabled=option.enabled && LocalControlsEnabled.current,onClick={onChange(OptionReset.apply(config,defaults,option.paths))},modifier=Modifier.semantics {contentDescription=tr(R.string.reset_named,option.label)}){Text(tr(R.string.reset_option))}
   }
  }
 }
}
fun paintResets(color:String,gradient:String,enabled:Boolean)=listOf(
 resetOption(R.string.reset_color,color),resetOption(R.string.gradient,"$gradient.enabled"),
 resetOption(R.string.gradient_start,"$gradient.start"),resetOption(R.string.gradient_end,"$gradient.end"),resetOption(R.string.gradient_angle,"$gradient.angle")
).map {it.copy(enabled=enabled)}
fun surfaceResets(prefix:String,dark:Boolean,config:WidgetConfig,surface:Surface)=
 paintResets("$prefix.${if(dark)"dark" else "light"}.color","$prefix.${if(dark)"dark" else "light"}.gradient",!config.dynamic)+
 listOf(resetOption(R.string.opacity,"$prefix.${if(dark)"dark" else "light"}.opacity"),resetOption(R.string.shape,"$prefix.shape"))+
 if(surface.shape=="circle")listOf(resetOption(R.string.rounding,"$prefix.rounding")) else emptyList()
fun iconResets(prefix:String,dark:Boolean,config:WidgetConfig,icon:IconSpec)=listOf(
 resetOption(R.string.icon,"$prefix.name","$prefix.asset"),resetOption(R.string.outline,"$prefix.outline"),resetOption(R.string.keep_outer_outline,"$prefix.keepOuterOutline"),resetOption(R.string.monochrome_icon,"$prefix.monochrome")
)+paintResets("$prefix.${if(dark)"dark" else "light"}","$prefix.${if(dark)"dark" else "light"}Gradient",!config.dynamic && icon.monochrome)
fun textResets(prefix:String,dark:Boolean,config:WidgetConfig)=listOf(
 resetOption(R.string.bundled_font,"$prefix.font"),resetOption(R.string.font_size,"$prefix.size"),resetOption(R.string.bold,"$prefix.weight"),
 resetOption(R.string.italic,"$prefix.italic"),resetOption(R.string.underline,"$prefix.underline"),resetOption(R.string.strike,"$prefix.strike")
)+paintResets("$prefix.${if(dark)"dark" else "light"}","$prefix.${if(dark)"dark" else "light"}Gradient",!config.dynamic)

fun geometryResets(element:String,position:Boolean):List<ResetOption> {
 val index=element.last().digitToIntOrNull()
 if(position) {
  val prefix=when {element.startsWith("button")->"buttons.$index.position";element.startsWith("icon")->"buttons.$index.iconPosition";else->"placement.$element"}
  return listOf(resetOption(R.string.position_x,"$prefix.x"),resetOption(R.string.position_y,"$prefix.y"))+
   when {element=="field"->listOf(resetOption(R.string.link_field_contents,"placement.fieldLinked"));element.startsWith("button")->listOf(resetOption(R.string.link_button_icon,"buttons.$index.iconLinked"));else->emptyList()}
 }
 return when(element) {
  "outer"->listOf(resetOption(R.string.width_dp,"sizing.outerWidthDp"),resetOption(R.string.height_dp,"heightDp"),resetOption(R.string.side_padding_dp,"sizing.sidePaddingDp"),resetOption(R.string.lock_ratio,"sizing.outerLocked"))
  "field"->listOf(resetOption(R.string.width_dp,"sizing.fieldWidthDp"),resetOption(R.string.height_dp,"sizing.fieldHeightDp"),resetOption(R.string.lock_ratio,"sizing.fieldLocked"))
  "logo"->listOf(resetOption(R.string.icon_size_dp,"logo.icon.sizeDp"),resetOption(R.string.logo_inset_dp,"sizing.logoInsetDp"))
  "text"->listOf(resetOption(R.string.text_gap_dp,"sizing.textGapDp"))
  else->if(element.startsWith("button"))listOf(resetOption(R.string.width_dp,"buttons.$index.widthDp"),resetOption(R.string.height_dp,"buttons.$index.heightDp"),resetOption(R.string.gap_before_dp,"buttons.$index.gapDp"),resetOption(R.string.lock_ratio,"buttons.$index.proportionsLocked")) else listOf(resetOption(R.string.icon_size_dp,"buttons.$index.icon.sizeDp"))
 }
}
