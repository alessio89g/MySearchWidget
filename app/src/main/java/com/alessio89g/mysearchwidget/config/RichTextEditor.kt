@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.alessio89g.mysearchwidget.config

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.data.TextStyle as WidgetTextStyle
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.tr

@Composable fun RichTextEditor(config:WidgetConfig,query:Boolean,onChange:(WidgetConfig)->Unit,pickFont:(Int,Int)->Unit) {
 var bbcode by rememberSaveable {mutableStateOf(false)}
 var bbcodeDraft by rememberSaveable {mutableStateOf("")}
 var error by remember {mutableStateOf(false)}
 val context=androidx.compose.ui.platform.LocalContext.current
 val density=androidx.compose.ui.platform.LocalDensity.current
 val text=config.placeholder.ifEmpty {tr(R.string.placeholder)}
 var field by remember {mutableStateOf(TextFieldValue(text))}
 val start=field.selection.min.coerceIn(0,text.length);val end=field.selection.max.coerceIn(start,text.length)
 val selected=!query && end>start
 val style=if(query)config.query else RichText.styleAt(config.hintRuns,start,config.hint)
 fun applyStyle(next:WidgetTextStyle) {
  if(query)onChange(config.copy(query=next))
  else if(selected || config.hintRuns.isNotEmpty()) {
   val materialized=if(config.placeholder.isEmpty())config.copy(placeholder=text) else config
   val from=if(selected)start else 0;val to=if(selected)end else text.length
   val points=(listOf(from,to)+materialized.hintRuns.flatMap {listOf(it.start,it.end)}.filter {it in from..to}).distinct().sorted()
   var runs=materialized.hintRuns
   for((a,b) in points.zipWithNext()){
    val old=RichText.styleAt(runs,a,materialized.hint)
    val changed=old.copy(font=if(next.font!=style.font)next.font else old.font,size=if(next.size!=style.size)next.size else old.size,
     weight=if(next.weight!=style.weight)next.weight else old.weight,italic=if(next.italic!=style.italic)next.italic else old.italic,
     underline=if(next.underline!=style.underline)next.underline else old.underline,strike=if(next.strike!=style.strike)next.strike else old.strike,
     light=if(next.light!=style.light)next.light else old.light,dark=if(next.dark!=style.dark)next.dark else old.dark,
     lightGradient=if(next.lightGradient!=style.lightGradient)next.lightGradient else old.lightGradient,darkGradient=if(next.darkGradient!=style.darkGradient)next.darkGradient else old.darkGradient)
    runs=RichText.apply(runs,a,b,changed)
   }
   onChange(materialized.copy(hint=if(selected)config.hint else next,hintRuns=runs))
  } else onChange(config.copy(hint=next,hintRuns=emptyList()))
 }
 Toggle(tr(R.string.bbcode_mode),bbcode){
  if(it)bbcodeDraft=RichText.bbcode(if(query)"{query}" else text,if(query)emptyList() else config.hintRuns,if(query)config.query else config.hint)
  bbcode=it;error=false
 }
 if(bbcode) {
  Text(tr(R.string.bbcode_help),style=MaterialTheme.typography.bodySmall)
  if(query)Text(tr(R.string.query_bbcode_help),style=MaterialTheme.typography.bodySmall)
  OutlinedTextField(bbcodeDraft,{bbcodeDraft=it.take(1_000_000);error=false},label={Text("BBCode")},isError=error,modifier=Modifier.fillMaxWidth(),minLines=3,maxLines=7)
  if(error)Text(tr(R.string.bbcode_invalid),color=MaterialTheme.colorScheme.error)
  Button(onClick={
   runCatching {
    val (plain,runs)=RichText.parse(bbcodeDraft,if(query)config.query else config.hint)
    val updated=if(query){
     require(plain=="{query}" && runs.map {it.style}.distinct().size<=1)
     config.copy(query=runs.firstOrNull()?.style ?: config.query)
    }else config.copy(placeholder=plain,hintRuns=runs)
    Validation.config(updated);onChange(updated);error=false
   }.onFailure {error=true}
  }){Text(tr(R.string.bbcode_apply))}
 } else {
  if(!query){
   val annotated=buildAnnotatedString {
    append(text)
    val all=listOf(TextRun(0,text.length,config.hint))+config.hintRuns
    all.forEach {run->if(run.end<=text.length && run.start<run.end){
     val s=run.style
     val color=runCatching {Color(android.graphics.Color.parseColor(s.dark))}.getOrDefault(Color.Unspecified)
     val face=config.assets[s.font]?.let {runCatching {Assets.font(context,it)}.getOrNull()} ?: context.resources.getFont(R.font.google_sans)
     val ink=SpanStyle(fontFamily=FontFamily(face),color=color,fontSize=s.size.sp,fontWeight=FontWeight(s.weight),fontStyle=if(s.italic)FontStyle.Italic else FontStyle.Normal,
      textDecoration=TextDecoration.combine(listOfNotNull(if(s.underline)TextDecoration.Underline else null,if(s.strike)TextDecoration.LineThrough else null)))
     val metrics=android.graphics.Paint().apply {typeface=face;textSize=s.size*density.density*density.fontScale}
     val left=metrics.measureText(text,0,run.start);val width=metrics.measureText(text,run.start,run.end).coerceAtLeast(1f)
     val gradient=if(s.darkGradient.enabled)androidx.compose.ui.graphics.ShaderBrush(com.alessio89g.mysearchwidget.widget.Renderer.gradient(s.darkGradient,android.graphics.RectF(left,0f,left+width,metrics.textSize))) else null
     addStyle(if(gradient!=null)ink.copy(brush=gradient) else ink,run.start,run.end)
    }}
   }
   val visible=field.copy(annotatedString=annotated,selection=TextRange(start,end))
   Text(tr(R.string.selection_help),style=MaterialTheme.typography.bodySmall)
   OutlinedTextField(visible,{next->
    field=next
    if(next.text!=text && next.text.length<=500)onChange(config.copy(placeholder=next.text,hintRuns=RichText.edit(text,next.text,config.hintRuns,config.hint)))
   },label={Text(tr(R.string.placeholder_label))},modifier=Modifier.fillMaxWidth(),singleLine=true)
   TextButton(onClick={onChange(config.copy(placeholder="",hintRuns=emptyList()));field=TextFieldValue("")}){Text(tr(R.string.auto_placeholder))}
  }
  Text(tr(if(selected)R.string.format_selection else R.string.format_all),style=MaterialTheme.typography.labelLarge)
  Choice("Font",style.font,listOf("" to "Google Sans")+config.assets.filterValues {it.kind=="font"}.keys.mapIndexed {index,id->id to "Font ${index+1} · ${id.take(8)}"}){applyStyle(style.copy(font=it))}
  TextEditor(style,::applyStyle){pickFont(if(selected)start else 0,if(selected)end else 0)}
 }
}
