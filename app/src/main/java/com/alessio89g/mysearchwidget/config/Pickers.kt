package com.alessio89g.mysearchwidget.config

import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.*

import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.icons.IconCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable fun ActionPicker(label:String,value:Shortcut,onChange:(Shortcut)->Unit) {
 var shortcuts by remember {mutableStateOf(false)}
 if(shortcuts)ShortcutDialog({shortcuts=false}){onChange(it);shortcuts=false}
 var apps by remember {mutableStateOf(false)}
 val options=listOf("none" to tr(R.string.no_action),"app" to tr(R.string.choose_app),"static" to tr(R.string.shortcuts))+Catalog.functions.map {it.key to builtinTitle(it.key)}
 Choice(label,if(value.kind=="builtin")value.value else if(value.kind=="pinned")"static" else value.kind,options){key->
  if(key=="static")shortcuts=true else if(key=="app")apps=true else onChange(if(key=="none")Shortcut() else Shortcut("builtin",key,Catalog.functions.getValue(key)))
 }
 if(value.kind in listOf("static","pinned"))Text(tr(R.string.selected_shortcut,shortcutTitle(value)))
 if(value.kind=="builtin" && value.value in Catalog.retired)Text(tr(R.string.retired_action))
 if(value.kind=="app")Text("App: ${value.label.ifEmpty {value.value}}")
 if(apps) {
  val context=LocalContext.current
  var installed by remember {mutableStateOf<List<Pair<String,String>>?>(null)}
  var filter by remember {mutableStateOf("")}
  LaunchedEffect(Unit){installed=withContext(Dispatchers.IO){context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0).map {it.activityInfo.packageName to it.loadLabel(context.packageManager).toString()}.distinctBy {it.first}.sortedBy {it.second.lowercase()}}}
  AlertDialog(onDismissRequest={apps=false},title={Text(tr(R.string.installed_apps))},text={Column {
   OutlinedTextField(filter,{filter=it},label={Text(tr(R.string.find_app))})
   if(installed==null)CircularProgressIndicator()
   LazyColumn(Modifier.heightIn(max=360.dp)){items(installed.orEmpty().filter {it.second.contains(filter,true) || it.first.contains(filter,true)}) { (pkg,name)->TextButton(onClick={onChange(Shortcut("app",pkg,name));apps=false}){Text(name)} }}
  }},confirmButton={TextButton(onClick={apps=false}){Text(tr(R.string.close))}})
 }
}
@Composable fun SlotEditor(value:Slot,background:Boolean,onChange:(Slot)->Unit,showAction:Boolean=true,pickImage:()->Unit) {
 var catalog by remember {mutableStateOf(false)}
 var part by rememberSaveable {mutableStateOf("Icona")}
 if(background)Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
  listOf("Icona","Cerchio").forEach {name->FilterChip(selected=part==name,onClick={part=name},label={Text(navigationTitle(name))})}
 }
 if(background && part=="Cerchio")SettingCard(tr(R.string.button_background),tr(R.string.circle_help)) {
  SurfaceEditor(value.surface,true){onChange(value.copy(surface=it))}
 } else SettingCard(tr(R.string.icon),if(value.icon.asset.isNotEmpty())tr(R.string.imported_image) else value.icon.name) {
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   FilledTonalButton(onClick={catalog=true}){Text(tr(R.string.choose_icon))}
   OutlinedButton(onClick=pickImage){Text(tr(R.string.from_file))}
  }
  Toggle(tr(R.string.outline),value.icon.outline){onChange(value.copy(icon=value.icon.copy(outline=it)))}
  PaintControl(tr(R.string.icon_dark),value.icon.dark,value.icon.darkGradient,{onChange(value.copy(icon=value.icon.copy(dark=it)))},{onChange(value.copy(icon=value.icon.copy(darkGradient=it)))})
  PaintControl(tr(R.string.icon_light),value.icon.light,value.icon.lightGradient,{onChange(value.copy(icon=value.icon.copy(light=it)))},{onChange(value.copy(icon=value.icon.copy(lightGradient=it)))})
 }
 if(catalog) {
  var filter by remember {mutableStateOf("")}
  AlertDialog(onDismissRequest={catalog=false},title={Text(tr(R.string.offline_icons))},text={Column {
   OutlinedTextField(filter,{filter=it},label={Text(tr(R.string.icon_search))})
   LazyColumn(Modifier.heightIn(max=380.dp)){
    items(listOf("Google","Chrome").filter {it.contains(filter,true)}){name->TextButton(onClick={onChange(value.copy(icon=value.icon.copy(name=name,asset="")));catalog=false}){Text(name)}}
    items(IconCatalog.entries.filter {it.name.contains(filter,true) || it.words.contains(filter,true)}) { entry ->
     TextButton(onClick={onChange(value.copy(icon=value.icon.copy(name=entry.name,asset="")));catalog=false}) {Icon(if(value.icon.outline)entry.outlined else entry.filled,null);Spacer(Modifier.width(12.dp));Text(entry.name)}
    }
   }
  }},confirmButton={TextButton(onClick={catalog=false}){Text(tr(R.string.close))}})
 }
 if(showAction)ActionPicker(tr(R.string.primary_tap),value.tap){onChange(value.copy(tap=it))}
}
