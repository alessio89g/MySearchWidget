package com.alessio89g.mysearchwidget.config

import androidx.compose.runtime.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.alessio89g.mysearchwidget.data.Gradient
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.tr
import kotlin.math.*

@Composable fun PaintControl(label:String,color:String,gradient:Gradient,onColor:(String)->Unit,onGradient:(Gradient)->Unit) {
 val enabled=LocalControlsEnabled.current && LocalManualColorsEnabled.current
 if(!enabled)Text(tr(R.string.material_notice),style=MaterialTheme.typography.bodySmall)
 CompositionLocalProvider(LocalControlsEnabled provides enabled){
  Column(Modifier.graphicsLayer {alpha=if(enabled)1f else .45f}){
   PaintControlContent(label,color,gradient,onColor,onGradient)
  }
 }
}
@Composable private fun PaintControlContent(label:String,color:String,gradient:Gradient,onColor:(String)->Unit,onGradient:(Gradient)->Unit) {
 val enabled=LocalControlsEnabled.current
 ColorControl(label,color,onColor)
 Toggle(tr(R.string.gradient),gradient.enabled){onGradient(gradient.copy(enabled=it))}
 if(gradient.enabled) {
  ColorControl(tr(R.string.gradient_start),gradient.start){onGradient(gradient.copy(start=it.ifEmpty {"#00C8FF"}))}
  ColorControl(tr(R.string.gradient_end),gradient.end){onGradient(gradient.copy(end=it.ifEmpty {"#00D99B"}))}
  val directionLabel=tr(R.string.gradient_direction)
  val current by rememberUpdatedState(gradient)
  val update by rememberUpdatedState(onGradient)
  Canvas(Modifier.fillMaxWidth().height(100.dp).semantics {contentDescription=directionLabel;if(!enabled)disabled()}
   .pointerInput(enabled){if(enabled)detectTapGestures {p->update(current.copy(angle=((Math.toDegrees(atan2((p.y-size.height/2).toDouble(),(p.x-size.width/2).toDouble()))+360)%360).toFloat()))}}
   .pointerInput(enabled){if(enabled)detectDragGestures {change,_->change.consume();val p=change.position;update(current.copy(angle=((Math.toDegrees(atan2((p.y-size.height/2).toDouble(),(p.x-size.width/2).toDouble()))+360)%360).toFloat()))}}) {
   val rad=Math.toRadians(gradient.angle.toDouble());val d=Offset(cos(rad).toFloat(),sin(rad).toFloat())
   val center=Offset(size.width/2,size.height/2);val length=(abs(size.width*d.x)+abs(size.height*d.y))/2
   drawRect(Brush.linearGradient(listOf(Color(android.graphics.Color.parseColor(gradient.start)),Color(android.graphics.Color.parseColor(gradient.end))),center-d*length,center+d*length))
   val tip=center+d*38f
   drawLine(Color.Black,center,tip,7f);drawLine(Color.White,center,tip,3f)
   drawCircle(Color.Black,7f,tip);drawCircle(Color.White,4f,tip)
  }
  NumberControl(tr(R.string.gradient_angle),gradient.angle,0f,360f){onGradient(gradient.copy(angle=it))}
 }
}
