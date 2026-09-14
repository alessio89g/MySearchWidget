@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.alessio89g.mysearchwidget.config

import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.*

import androidx.compose.runtime.*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.alessio89g.mysearchwidget.data.*

val LocalControlsEnabled=staticCompositionLocalOf {true}
val LocalManualColorsEnabled=staticCompositionLocalOf {true}

@Composable fun SettingCard(title:String,description:String="",content:@Composable ColumnScope.()->Unit) {
 Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceContainerLow)) {
  Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Text(title,style=MaterialTheme.typography.titleMedium)
   if(description.isNotEmpty())Text(description,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
   content()
  }
 }
}
@Composable fun Section(title:String,initial:Boolean=false,content:@Composable ()->Unit) {
 var open by remember {mutableStateOf(initial)}
 Card(Modifier.fillMaxWidth().animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec()),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceContainerLow)) {
  Row(Modifier.fillMaxWidth().clickable {open=!open}.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
   Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
   Text(if(open)"−" else "+",style=MaterialTheme.typography.titleLarge)
  }
  if(open)Column(Modifier.padding(start=16.dp,end=16.dp,bottom=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){content()}
 }
}
@Composable fun Toggle(label:String,value:Boolean,onChange:(Boolean)->Unit) {
 val enabled=LocalControlsEnabled.current
 Row(Modifier.fillMaxWidth().toggleable(value=value,enabled=enabled,role=Role.Switch,onValueChange=onChange).padding(vertical=4.dp),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f));Switch(value,null,enabled=enabled)}
}
@Composable fun Choice(label:String,value:String,options:List<Pair<String,String>>,onChange:(String)->Unit) {
 val enabled=LocalControlsEnabled.current
 var open by remember { mutableStateOf(false) }
 Column {
  Text(label,style=MaterialTheme.typography.labelLarge)
  Box {
   OutlinedButton(enabled=enabled,onClick={open=true},modifier=Modifier.fillMaxWidth()){Text(options.firstOrNull {it.first==value}?.second ?: value)}
   DropdownMenu(open && enabled,{open=false}){options.forEach { (key,name)->DropdownMenuItem(text={Text(name)},onClick={onChange(key);open=false}) }}
  }
 }
}
@Composable fun NumberControl(label:String,value:Float,min:Float=0f,max:Float=100f,onChange:(Float)->Unit) {
 val enabled=LocalControlsEnabled.current
 var text by remember(value) { mutableStateOf(if(value==value.toInt().toFloat())value.toInt().toString() else "%.2f".format(java.util.Locale.ROOT,value).trimEnd('0').trimEnd('.')) }
 Row(verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f));OutlinedTextField(text,{text=it;it.replace(',','.').toFloatOrNull()?.takeIf { n->n.isFinite() && n in min..max }?.let(onChange)},singleLine=true,enabled=enabled,modifier=Modifier.width(88.dp),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))}
 Slider(value.coerceIn(min,max),onChange,enabled=enabled,valueRange=min..max)
}
@Composable fun ColorControl(label:String,value:String,onChange:(String)->Unit) {
 val enabled=LocalControlsEnabled.current
 var open by remember {mutableStateOf(false)}
 Card(enabled=enabled,onClick={open=true},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceContainerHigh)) {
  Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   val color=runCatching {Color(android.graphics.Color.parseColor(value))}.getOrDefault(MaterialTheme.colorScheme.primaryContainer)
   Box(Modifier.size(36.dp).background(color,androidx.compose.foundation.shape.CircleShape).border(1.dp,MaterialTheme.colorScheme.outlineVariant,androidx.compose.foundation.shape.CircleShape))
   Column(Modifier.weight(1f)) {
    Text(label,style=MaterialTheme.typography.bodyLarge)
    Text(value.ifEmpty {tr(R.string.automatic)},style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
   }
   Text("›",style=MaterialTheme.typography.titleLarge)
  }
 }
 if(open && enabled)ColorEditDialog(label,value,{open=false}){onChange(it);open=false}
}
@Composable private fun ColorEditDialog(label:String,value:String,onDismiss:()->Unit,onApply:(String)->Unit) {
 var draft by remember {mutableStateOf(value)}
 var error by remember {mutableStateOf(false)}
 var graphic by remember {mutableStateOf(false)}
 if(graphic)HsvColorDialog(draft,{graphic=false},title=label){onApply(it)}
 else AlertDialog(onDismissRequest=onDismiss,title={Text(label)},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
  Button(onClick={graphic=true},shapes=ButtonDefaults.shapes(),modifier=Modifier.fillMaxWidth()){Text(tr(R.string.visual_picker))}
  Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   listOf("#000000","#FFFFFF","#474645","#B8B3A1","#D0BCFF","#A8DAB5","#FFB4AB","#A8C7FA").forEach {hex->
    Box(Modifier.size(48.dp).semantics {contentDescription=hex}.background(Color(android.graphics.Color.parseColor(hex)),androidx.compose.foundation.shape.CircleShape).border(1.dp,MaterialTheme.colorScheme.outline,androidx.compose.foundation.shape.CircleShape).clickable {draft=hex;error=false})
   }
  }
  OutlinedTextField(draft,{new->
   draft=new
   error=runCatching {Validation.color(normalizedColor(new))}.isFailure
  },label={Text(tr(R.string.hex_rgb))},supportingText={if(error)Text(tr(R.string.color_help))},singleLine=true,isError=error,modifier=Modifier.fillMaxWidth())
  TextButton(onClick={draft="";error=false}){Text(tr(R.string.auto_color))}
 }},confirmButton={TextButton(onClick={onApply(normalizedColor(draft))},enabled=!error){Text(tr(R.string.apply))}},dismissButton={TextButton(onClick=onDismiss){Text(tr(R.string.cancel))}})
}
private fun normalizedColor(value:String):String = if(value.contains(','))value.split(',').map {it.trim().toIntOrNull()}.let {parts->if(parts.size==3 && parts.all {it!=null && it in 0..255})"#%02X%02X%02X".format(parts[0],parts[1],parts[2]) else value} else value
@Composable fun SurfaceEditor(value:Surface,shapes:Boolean=false,showColors:Boolean=true,onChange:(Surface)->Unit) {
 var mode by remember {mutableStateOf("dark")}
 Choice(tr(R.string.edit_colors),mode,listOf("dark" to tr(R.string.dark_theme),"light" to tr(R.string.light_theme))){mode=it}
 val tone=if(mode=="dark")value.dark else value.light
 fun change(t:Tone){onChange(if(mode=="dark")value.copy(dark=t) else value.copy(light=t))}
 if(showColors)PaintControl(tr(R.string.background),tone.color,tone.gradient,{change(tone.copy(color=it))},{change(tone.copy(gradient=it))})
 NumberControl(tr(R.string.opacity),tone.opacity*100){change(tone.copy(opacity=it/100))}
 if(shapes)Choice(tr(R.string.shape),value.shape,Catalog.shapes.map {it to when(it){"circle"->tr(R.string.circle_square);"flower"->tr(R.string.flower);else->it.replaceFirstChar {c->c.uppercase()}}}){onChange(value.copy(shape=it))}
 if(value.shape=="circle")NumberControl(tr(R.string.rounding),value.rounding){onChange(value.copy(rounding=it))}
}
@Composable fun TextEditor(value:TextStyle,onChange:(TextStyle)->Unit,pickFont:()->Unit) {
 Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
  FilterChip(value.weight>=700,{onChange(value.copy(weight=if(value.weight>=700)400 else 700))},label={Text(tr(R.string.bold))})
  FilterChip(value.italic,{onChange(value.copy(italic=!value.italic))},label={Text(tr(R.string.italic))})
  FilterChip(value.underline,{onChange(value.copy(underline=!value.underline))},label={Text(tr(R.string.underline))})
  FilterChip(value.strike,{onChange(value.copy(strike=!value.strike))},label={Text(tr(R.string.strike))})
 }
 NumberControl(tr(R.string.font_size),value.size,8f,32f){onChange(value.copy(size=it))}
 Text(if(value.font.isEmpty())tr(R.string.bundled_font) else tr(R.string.embedded_font))
 Row {TextButton(onClick=pickFont){Text(tr(R.string.import_font))};TextButton(onClick={onChange(value.copy(font=""))}){Text("Google Sans")}}
 PaintControl(tr(R.string.text_dark),value.dark,value.darkGradient,{onChange(value.copy(dark=it))},{onChange(value.copy(darkGradient=it))})
 PaintControl(tr(R.string.text_light),value.light,value.lightGradient,{onChange(value.copy(light=it))},{onChange(value.copy(lightGradient=it))})
}
