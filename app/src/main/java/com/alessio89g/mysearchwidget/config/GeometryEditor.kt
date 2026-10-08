package com.alessio89g.mysearchwidget.config

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.i18n.*

@Composable private fun PositionControl(value:OffsetDp,onChange:(OffsetDp)->Unit) {
 var step by rememberSaveable {mutableStateOf(1f)}
 fun move(x:Float,y:Float){onChange(OffsetDp((value.x+x).coerceIn(-10000f,10000f),(value.y+y).coerceIn(-10000f,10000f)))}
 NumberControl(tr(R.string.position_x),value.x,-10000f,10000f,showSlider=false){onChange(value.copy(x=it))}
 NumberControl(tr(R.string.position_y),value.y,-10000f,10000f,showSlider=false){onChange(value.copy(y=it))}
 NumberControl(tr(R.string.position_step),step,.1f,100f,showSlider=false){step=it}
 Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally) {
  OutlinedButton(onClick={move(0f,-step)},modifier=Modifier.semantics {contentDescription=tr(R.string.move_up)}){Text("↑")}
  Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   OutlinedButton(onClick={move(-step,0f)},modifier=Modifier.semantics {contentDescription=tr(R.string.move_left)}){Text("←")}
   OutlinedButton(onClick={move(step,0f)},modifier=Modifier.semantics {contentDescription=tr(R.string.move_right)}){Text("→")}
  }
  OutlinedButton(onClick={move(0f,step)},modifier=Modifier.semantics {contentDescription=tr(R.string.move_down)}){Text("↓")}
 }
}
@Composable private fun LinkControl(label:String,linked:Boolean,onChange:(Boolean)->Unit) {
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
  Text(label,Modifier.weight(1f))
  IconToggleButton(checked=linked,onCheckedChange=onChange,modifier=Modifier.semantics {contentDescription=tr(if(linked)R.string.unlink_elements else R.string.link_elements)}) {
   Padlock(linked,if(linked)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
  }
 }
}
@Composable fun GeometryEditor(config:WidgetConfig,width:Float,onChange:(WidgetConfig)->Unit,onHeight:(Float)->Unit,onResetHeight:()->Unit,onNavigate:()->Unit={}) {
 var target by rememberSaveable {mutableStateOf("")}
 var panel by rememberSaveable {mutableStateOf("size")}
 val options=listOf("outer" to tr(R.string.outer_border),"field" to tr(R.string.search_field),"logo" to "Logo","text" to tr(R.string.geometry_text))+
  (0 until config.count).flatMap {listOf("button$it" to tr(R.string.button_number,it+1),"icon$it" to tr(R.string.button_icon_number,it+1))}
 val selected=options.firstOrNull {it.first==target}?.first ?: "outer"
 fun open(value:String){target=value;onNavigate()}
 if(target.isEmpty()) {
  SettingsGroup(listOf(
   SettingsEntry(tr(R.string.widget_height),"${config.heightDp.toInt()} dp · "+tr(R.string.widget_size_summary),"Settings"){open("height")},
   SettingsEntry(tr(R.string.button_count),tr(R.string.visible_button_count,config.count),"Star"){open("buttons")},
   SettingsEntry(tr(R.string.layer_order),tr(R.string.layers_summary),"MoreHoriz"){open("layers")}
  ))
  Text(tr(R.string.geometry_element),style=MaterialTheme.typography.titleSmall)
  SettingsGroup(options.take(4).map {(key,title)->SettingsEntry(title,tr(R.string.element_layout_summary),when(key){"text"->"Description";"field"->"Search";"logo"->"Photo";else->"Settings"}){open(key)}},1)
  if(config.count>0)SettingsGroup(options.drop(4).map {(key,title)->SettingsEntry(title,tr(R.string.element_layout_summary),"Star"){open(key)}},2)
  return
 }
 DetailBack {open("")}
 val unit=ElementLayout(width,config).unit
 if(target=="height") {
 SettingCard(tr(R.string.widget_height),tr(R.string.widget_height_help)) {
  NumberControl(tr(R.string.height_dp),config.heightDp,16f,256f,onChange=onHeight)
  TextButton(onClick=onResetHeight){Text(tr(R.string.reset_height))}
 }
 return
 }
 if(target=="buttons") {
  SettingCard(tr(R.string.button_count),tr(R.string.buttons_help)) {
   OptionButtons(config.count.toString(),(0..3).map {it.toString() to it.toString()}){onChange(config.copy(count=it.toInt()))}
   Text(tr(R.string.hidden_buttons_preserved),style=MaterialTheme.typography.bodySmall)
  }
  return
 }
 if(target=="layers") {
 SettingCard(tr(R.string.layer_order)) {
  Text(tr(R.string.layer_order_help),style=MaterialTheme.typography.bodySmall)
  val visible=config.visibleLayers()
  visible.asReversed().forEach {id->
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
    Text(options.first {it.first==id}.second,Modifier.weight(1f))
    IconButton(onClick={onChange(config.moveLayer(id,true))},enabled=id!=visible.last(),modifier=Modifier.semantics {contentDescription=tr(R.string.layer_forward,options.first {it.first==id}.second)}){Text("↑")}
    IconButton(onClick={onChange(config.moveLayer(id,false))},enabled=id!=visible.first(),modifier=Modifier.semantics {contentDescription=tr(R.string.layer_backward,options.first {it.first==id}.second)}){Text("↓")}
   }
  }
  TextButton(onClick={onChange(config.copy(placement=config.placement.copy(layers=DEFAULT_LAYERS)))}){Text(tr(R.string.reset_layers))}
 }
 return
 }
 key(selected) {SettingCard(options.first {it.first==selected}.second) {
  OptionButtons(panel,listOf("size" to tr(R.string.layout_dimensions),"position" to tr(R.string.layout_position))){panel=it}
  if(panel=="position") {
   Text(tr(R.string.position_help),style=MaterialTheme.typography.bodySmall)
   val p=config.placement
   when(selected) {
    "outer"->PositionControl(p.outer){onChange(config.copy(placement=p.copy(outer=it)))}
    "field"->{
     LinkControl(tr(R.string.link_field_contents),p.fieldLinked){onChange(config.linkField(it,width))}
     PositionControl(p.field){onChange(config.copy(placement=p.copy(field=it)))}
    }
    "logo"->PositionControl(p.logo){onChange(config.copy(placement=p.copy(logo=it)))}
    "text"->PositionControl(p.text){onChange(config.copy(placement=p.copy(text=it)))}
    else->{
     val index=selected.last().digitToInt();val slot=config.buttons[index]
     fun change(s:Slot){onChange(config.copy(buttons=config.buttons.mapIndexed {i,v->if(i==index)s else v}))}
     if(selected.startsWith("button")) {
      LinkControl(tr(R.string.link_button_icon),slot.iconLinked){change(slot.linkIcon(it))}
      PositionControl(slot.position){change(slot.copy(position=it))}
     } else PositionControl(slot.iconPosition){change(slot.copy(iconPosition=it))}
    }
   }
   TextButton(onClick={onChange(config.resetElementPosition(selected))}){Text(tr(R.string.reset_position))}
  } else {
  Text(tr(if(selected=="text")R.string.reset_text_dimensions_help else R.string.reset_geometry_help),style=MaterialTheme.typography.bodySmall)
  when(selected) {
   "outer"->{
    DimensionPair(config.sizing.outerWidthDp,config.heightDp,width,64f,config.sizing.outerRatio,16f,1000f,16f,256f,requiredHeight=true,locked=config.sizing.outerLocked){w,h,r,l->onChange(config.copy(heightDp=h ?: 64f,sizing=config.sizing.copy(outerWidthDp=w,outerRatio=r,outerLocked=l,layoutHeightDp=config.sizing.layoutHeightDp ?: config.heightDp)))}
    DimensionControl(tr(R.string.side_padding_dp),config.sizing.sidePaddingDp,9f*unit,0f,128f){onChange(config.copy(sizing=config.sizing.copy(sidePaddingDp=it)))}
   }
   "field"->{
    DimensionPair(config.sizing.fieldWidthDp,config.sizing.fieldHeightDp,ElementLayout(width,config.copy(sizing=config.sizing.copy(fieldWidthDp=null))).field.width,46f*unit,config.sizing.fieldRatio,16f,1000f,locked=config.sizing.fieldLocked){w,h,r,l->onChange(config.copy(sizing=config.sizing.copy(fieldWidthDp=w,fieldHeightDp=h,fieldRatio=r,fieldLocked=l)))}
   }
   "logo"->{
    DimensionControl(tr(R.string.icon_size_dp),config.logo.icon.sizeDp,32f*unit){onChange(config.copy(logo=config.logo.copy(icon=config.logo.icon.copy(sizeDp=it))))}
    DimensionControl(tr(R.string.logo_inset_dp),config.sizing.logoInsetDp,8f*unit,0f,128f){onChange(config.copy(sizing=config.sizing.copy(logoInsetDp=it)))}
   }
   "text"->{
    DimensionControl(tr(R.string.text_gap_dp),config.sizing.textGapDp,11f*unit,0f,128f){onChange(config.copy(sizing=config.sizing.copy(textGapDp=it)))}
    Text(tr(R.string.text_format_location),style=MaterialTheme.typography.bodySmall)
   }
   else->{
    val index=selected.last().digitToInt();val slot=config.buttons[index]
    fun change(s:Slot){onChange(config.copy(buttons=config.buttons.mapIndexed {i,v->if(i==index)s else v}))}
    if(selected.startsWith("button")) {
     DimensionPair(slot.widthDp,slot.heightDp,46f*unit,46f*unit,slot.aspectRatio,locked=slot.proportionsLocked){w,h,r,l->change(slot.copy(widthDp=w,heightDp=h,aspectRatio=r,proportionsLocked=l))}
     DimensionControl(tr(R.string.gap_before_dp),slot.gapDp,(if(index==0)4f else 6f)*unit,0f,128f){change(slot.copy(gapDp=it))}
    }else {
     DimensionControl(tr(R.string.icon_size_dp),slot.icon.sizeDp,25f*unit){change(slot.copy(icon=slot.icon.copy(sizeDp=it)))}
    }
   }
  }
  TextButton(onClick={onChange(config.resetElementDimensions(selected))}){Text(tr(R.string.reset_element_dimensions))}
  }
 }}
}
