package com.alessio89g.mysearchwidget.config

import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.*

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable fun HsvColorDialog(value:String,onDismiss:()->Unit,title:String=tr(R.string.color_picker),onApply:(String)->Unit) {
 val original=remember {runCatching {AndroidColor.parseColor(value)}.getOrDefault(AndroidColor.WHITE)}
 val initial=remember {FloatArray(3).also {AndroidColor.colorToHSV(original,it)}}
 var hue by remember {mutableFloatStateOf(initial[0])}
 var saturation by remember {mutableFloatStateOf(initial[1])}
 var brightness by remember {mutableFloatStateOf(initial[2])}
 val alpha=AndroidColor.alpha(original)
 val selected=AndroidColor.HSVToColor(alpha,floatArrayOf(hue,saturation,brightness))
 val hex=if(alpha==255)"#%06X".format(selected and 0xFFFFFF) else "#%08X".format(selected)
 // No writes until Apply: cancel leaves manual color and automatic mode untouched.
 AlertDialog(onDismissRequest=onDismiss,title={Text(title)},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  BoxWithConstraints { Row(Modifier.fillMaxWidth().height(maxWidth-44.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   Canvas(Modifier.weight(1f).fillMaxHeight().pointerInput(Unit) {
    detectTapGestures {p->saturation=(p.x/size.width).coerceIn(0f,1f);brightness=1-(p.y/size.height).coerceIn(0f,1f)}
   }.pointerInput(Unit) {
    detectDragGestures(onDragStart={p->saturation=(p.x/size.width).coerceIn(0f,1f);brightness=1-(p.y/size.height).coerceIn(0f,1f)}) {change,_->change.consume();saturation=(change.position.x/size.width).coerceIn(0f,1f);brightness=1-(change.position.y/size.height).coerceIn(0f,1f)}
   }) {
    drawRect(Brush.horizontalGradient(listOf(Color.White,Color(AndroidColor.HSVToColor(floatArrayOf(hue,1f,1f))))))
    drawRect(Brush.verticalGradient(listOf(Color.Transparent,Color.Black)))
    val center=Offset(saturation*size.width,(1-brightness)*size.height)
    drawCircle(Color.Black,7.dp.toPx(),center,style=Stroke(3.dp.toPx()))
    drawCircle(Color.White,7.dp.toPx(),center,style=Stroke(1.dp.toPx()))
   }
   Canvas(Modifier.width(32.dp).fillMaxHeight().pointerInput(Unit) {
    detectTapGestures {hue=(it.y/size.height*360).coerceIn(0f,359.99f)}
   }.pointerInput(Unit) {
    detectDragGestures(onDragStart={hue=(it.y/size.height*360).coerceIn(0f,359.99f)}) {change,_->change.consume();hue=(change.position.y/size.height*360).coerceIn(0f,359.99f)}
   }) {
    drawRect(Brush.verticalGradient((0..6).map {Color(AndroidColor.HSVToColor(floatArrayOf(it*60f,1f,1f)))}))
    val y=hue/360*size.height
    drawLine(Color.Black,Offset(0f,y),Offset(size.width,y),6.dp.toPx())
    drawLine(Color.White,Offset(0f,y),Offset(size.width,y),2.dp.toPx())
   }
  } }
  Text(hex,style=MaterialTheme.typography.titleMedium)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   Column(Modifier.weight(1f)){Text(tr(R.string.before));Box(Modifier.fillMaxWidth().height(36.dp).background(Color(original)).border(1.dp,MaterialTheme.colorScheme.outline))}
   Column(Modifier.weight(1f)){Text(tr(R.string.after));Box(Modifier.fillMaxWidth().height(36.dp).background(Color(selected)).border(1.dp,MaterialTheme.colorScheme.outline))}
  }
  // Sliders also expose accessible values and keyboard adjustment.
  Section(tr(R.string.slider_adjustment)) {
  Text(tr(R.string.hue));Slider(hue,{hue=it},valueRange=0f..359.99f)
  Text(tr(R.string.saturation));Slider(saturation,{saturation=it})
  Text(tr(R.string.brightness));Slider(brightness,{brightness=it})
  }
 }},confirmButton={TextButton(onClick={onApply(hex)}){Text(tr(R.string.apply))}},dismissButton={TextButton(onClick=onDismiss){Text(tr(R.string.cancel))}})
}
