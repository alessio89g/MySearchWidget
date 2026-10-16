package com.alessio89g.mysearchwidget.config

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.tr
import com.alessio89g.mysearchwidget.icons.ExternalIcons
import com.alessio89g.mysearchwidget.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable fun IconPackDialog(onDismiss:()->Unit,onSelect:(Pair<String,Asset>)->Unit) {
 val c=LocalContext.current
 var packs by remember {mutableStateOf<List<ExternalIcons.Pack>?>(null)}
 var pack by remember {mutableStateOf<ExternalIcons.Pack?>(null)}
 var entries by remember {mutableStateOf<List<ExternalIcons.Entry>?>(null)}
 var filter by remember {mutableStateOf("")}
 var error by remember {mutableStateOf(false)}
 var nativePkg by rememberSaveable {mutableStateOf("")}
 var fallback by remember {mutableStateOf<ExternalIcons.Pack?>(null)}
 val nativePicker=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
  if(result.resultCode==Activity.RESULT_OK) {
   runCatching {ExternalIcons.result(c,requireNotNull(result.data))}.onSuccess(onSelect).onFailure {
    error=true;fallback=packs?.firstOrNull {it.pkg==nativePkg}
   }
  }
  nativePkg=""
 }
 fun choose(p:ExternalIcons.Pack) {
  error=false;fallback=null
  val intent=ExternalIcons.picker(c,p.pkg)
  if(intent==null)fallback=p else {
   nativePkg=p.pkg
   runCatching {nativePicker.launch(intent)}.onFailure {nativePkg="";error=true;fallback=p}
  }
 }
 LaunchedEffect(Unit){packs=withContext(Dispatchers.IO){runCatching {ExternalIcons.packs(c)}.getOrDefault(emptyList())}}
 LaunchedEffect(pack){entries=null;filter="";error=false;pack?.let {p->entries=withContext(Dispatchers.IO){runCatching {ExternalIcons.entries(c,p.pkg)}.getOrDefault(emptyList())}}}
 AlertDialog(onDismissRequest=onDismiss,title={Text(pack?.label ?: tr(R.string.icon_packs))},text={Column {
  if(error)Text(tr(R.string.icon_load_failed))
  if(pack==null) {
   Text(tr(R.string.native_pack_help),style=MaterialTheme.typography.bodySmall)
   fallback?.let {p->
    Text(tr(R.string.native_pack_unavailable),style=MaterialTheme.typography.bodySmall)
    TextButton(onClick={pack=p;fallback=null;error=false}){Text(tr(R.string.browse_pack_here))}
   }
   if(packs==null)CircularProgressIndicator()
   if(packs?.isEmpty()==true)Text(tr(R.string.no_icon_packs))
   LazyColumn(Modifier.heightIn(max=360.dp)){items(packs.orEmpty(),key={it.pkg}) {p->TextButton(onClick={choose(p)}){Text(p.label)}}}
  } else {
   TextButton(onClick={pack=null}){Text(tr(R.string.icon_packs))}
   OutlinedTextField(filter,{filter=it},label={Text(tr(R.string.icon_search))})
   if(entries==null)CircularProgressIndicator()
   if(entries?.isEmpty()==true)Text(tr(R.string.pack_no_icons))
   LazyColumn(Modifier.heightIn(max=360.dp)){items(entries.orEmpty().filter {it.name.contains(filter,true)||it.component.contains(filter,true)},key={it.name}) {e->
    val pkg=pack!!.pkg
    val bitmap by produceState<android.graphics.Bitmap?>(null,pkg,e.name){value=withContext(Dispatchers.IO){runCatching {ExternalIcons.bitmap(ExternalIcons.drawable(c,pkg,e.name))}.getOrNull()}}
    TextButton(onClick={runCatching {ExternalIcons.asset(ExternalIcons.drawable(c,pkg,e.name))}.onSuccess(onSelect).onFailure {error=true}},modifier=Modifier.fillMaxWidth()) {
     bitmap?.let {Image(it.asImageBitmap(),null,Modifier.size(40.dp));Spacer(Modifier.width(8.dp))};Text(e.name,Modifier.weight(1f))
    }
   }}
  }
 }},confirmButton={TextButton(onClick=onDismiss){Text(tr(R.string.close))}})
}

/** Keep action and image selection in the same draft update, including independent theme links. */
@Composable fun ActionIconDialog(action:Shortcut,onDismiss:()->Unit,onSelect:(Shortcut,Pair<String,Asset>?)->Unit) {
 val c=LocalContext.current
 var expanded by rememberSaveable {mutableStateOf(false)}
 var mode by rememberSaveable {mutableStateOf("keep")}
 var asset by remember {mutableStateOf<Pair<String,Asset>?>(null)}
 var packs by rememberSaveable {mutableStateOf(false)}
 var error by remember {mutableStateOf(false)}
 if(packs)IconPackDialog({packs=false}){asset=it;mode="pack";packs=false;error=false}
 AlertDialog(onDismissRequest=onDismiss,title={Text(action.label)},text={Column(Modifier.verticalScroll(rememberScrollState())) {
  Card { Column(Modifier.padding(8.dp)) {
  TextButton(onClick={expanded=!expanded}){Text((if(expanded)"▾ " else "▸ ")+tr(R.string.action_icon))}
  if(expanded) {
   TextButton(onClick={mode="keep";asset=null;error=false}){Text(tr(R.string.keep_icon))}
   TextButton(onClick={runCatching {ExternalIcons.app(c,if(action.kind=="app")action.value else action.value.substringBefore('/'))}.onSuccess {asset=it;mode="app";error=false}.onFailure {error=true}}){Text(tr(R.string.original_app_icon))}
   TextButton(onClick={packs=true}){Text(tr(R.string.from_icon_pack))}
   Text(tr(R.string.launcher_icon_help),style=MaterialTheme.typography.bodySmall)
  }
  Text(tr(R.string.action_icon_links),style=MaterialTheme.typography.bodySmall)
  }}
  Text(tr(when(mode){"app"->R.string.original_app_icon;"pack"->R.string.from_icon_pack;else->R.string.keep_icon}))
  asset?.let {Assets.bitmap(it.second)?.let {b->Image(b.asImageBitmap(),null,Modifier.size(64.dp))}}
  if(error)Text(tr(R.string.icon_load_failed))
 }},confirmButton={TextButton(onClick={onSelect(action,asset)}){Text(tr(R.string.use_action_icon))}},dismissButton={TextButton(onClick=onDismiss){Text(tr(R.string.cancel))}})
}
