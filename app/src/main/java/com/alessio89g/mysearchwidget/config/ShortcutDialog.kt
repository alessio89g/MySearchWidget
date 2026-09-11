package com.alessio89g.mysearchwidget.config

import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.*

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alessio89g.mysearchwidget.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable fun ShortcutDialog(onDismiss:()->Unit,onSelect:(Shortcut)->Unit) {
 val context=LocalContext.current
 var groups by remember {mutableStateOf<List<AppShortcuts.Group>?>(null)}
 var error by remember {mutableStateOf(false)}
 var filter by remember {mutableStateOf("")}
 var expanded by remember {mutableStateOf<String?>(null)}
 LaunchedEffect(AppLanguage.code) {runCatching {withContext(Dispatchers.IO){AppShortcuts.groups(context)}}.onSuccess {groups=it}.onFailure {error=true}}
 AlertDialog(onDismissRequest=onDismiss,title={Text(tr(R.string.shortcuts))},text={Column {
  Text(tr(R.string.shortcut_help),style=MaterialTheme.typography.bodySmall)
  OutlinedTextField(filter,{filter=it},label={Text(tr(R.string.find_shortcut))})
  if(error)Text(tr(R.string.shortcut_read_error)) else if(groups==null)CircularProgressIndicator()
  if(groups?.isEmpty()==true)Text(tr(R.string.no_shortcut_apps))
  LazyColumn(Modifier.heightIn(max=360.dp)) {
   items(groups.orEmpty().filter {it.label.contains(filter,true) || it.pkg.contains(filter,true) || it.entries.any {e->e.label.contains(filter,true)}},key={it.pkg}) {group->
    TextButton(onClick={expanded=if(expanded==group.pkg)null else group.pkg},modifier=Modifier.fillMaxWidth()) {Text((if(expanded==group.pkg)"− " else "+ ")+group.label)}
    if(expanded==group.pkg && group.entries.isEmpty())Text(tr(R.string.no_shortcuts),modifier=Modifier.padding(start=16.dp),style=MaterialTheme.typography.bodySmall)
    if(expanded==group.pkg)group.entries.forEach {entry->
     TextButton(enabled=entry.unavailable==null && (entry.kind=="pinned" || entry.intents.isNotEmpty()),onClick={val shortcut=Shortcut(entry.kind,"${group.pkg}/${entry.id}","${group.label} · ${entry.label}".take(200))
      runCatching {if(entry.kind=="pinned") {
       PinnedShortcuts.pin(context,shortcut)
       if(PinnedShortcuts.hasAccess(context))android.widget.Toast.makeText(context,tr(R.string.pinned_saved),android.widget.Toast.LENGTH_LONG).show()
      };onSelect(shortcut)}.onFailure {android.widget.Toast.makeText(context,tr(R.string.pin_failed),android.widget.Toast.LENGTH_LONG).show()}},modifier=Modifier.padding(start=16.dp)) {Text(entry.label)}
     entry.unavailable?.let {Text(it,modifier=Modifier.padding(start=16.dp,bottom=8.dp),style=MaterialTheme.typography.bodySmall)}
    }
   }
  }
 }},confirmButton={TextButton(onClick=onDismiss){Text(tr(R.string.close))}})
}
