@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.alessio89g.mysearchwidget.config

import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.*

import com.alessio89g.mysearchwidget.util.readLimited
import android.appwidget.AppWidgetManager
import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.activity.compose.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import com.alessio89g.mysearchwidget.icons.IconCatalog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import java.util.UUID

class ConfigState:ViewModel() {
 var selected by mutableStateOf<Int?>(null)
 var config by mutableStateOf(WidgetConfig())
 var configs by mutableStateOf<Map<Int,WidgetConfig>>(emptyMap())
 var engines by mutableStateOf<List<Engine>>(Catalog.engines)
 var templates by mutableStateOf<List<Template>>(emptyList())
 var ready by mutableStateOf(false)
 var busy by mutableStateOf(false)
 var message by mutableStateOf("")
 var fontStart=0
 var fontEnd=0
 var fontTarget="hint"
 var iconTarget=-1
 var applyModel by mutableStateOf<Template?>(null)
 var exportedText=""
}

class ConfigActivity:ComponentActivity() {
 private val repo by lazy {Repository(this)}
 private val editorState by viewModels<ConfigState>()
 private var wallpaperEnabled by mutableStateOf(false)
 private var wallpaperImage by mutableStateOf<android.graphics.Bitmap?>(null)
 private var wallpaperPermission by mutableStateOf(false)
 private var wallpaperBroadPermission by mutableStateOf(false)
 private var wallpaperPending=false
 private var wallpaperGeneration=0
 private val wallpaperPermissionResult=registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->if(granted)setWallpaper(true) else setWallpaper(false)}
 private var configuring=false
 private var launcherAccess by mutableStateOf(false)
 private var selected:Int?
  get()=editorState.selected
  set(value){editorState.selected=value}
 private var config:WidgetConfig
  get()=editorState.config
  set(value){editorState.config=value}
 private var configs:Map<Int,WidgetConfig>
  get()=editorState.configs
  set(value){editorState.configs=value}
 private var engines:List<Engine>
  get()=editorState.engines
  set(value){editorState.engines=value}
 private var templates:List<Template>
  get()=editorState.templates
  set(value){editorState.templates=value}
 private var ready:Boolean
  get()=editorState.ready
  set(value){editorState.ready=value}
 private var busy:Boolean
  get()=editorState.busy
  set(value){editorState.busy=value}
 private var message:String
  get()=editorState.message
  set(value){editorState.message=value}
 private var fontTarget:String
  get()=editorState.fontTarget
  set(value){editorState.fontTarget=value}
 private var iconTarget:Int
  get()=editorState.iconTarget
  set(value){editorState.iconTarget=value}
 private var applyModel:Template?
  get()=editorState.applyModel
  set(value){editorState.applyModel=value}
 private var exportedText:String
  get()=editorState.exportedText
  set(value){editorState.exportedText=value}
 private val importDocument=registerForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)task {
  val raw=withContext(Dispatchers.IO){contentResolver.openInputStream(uri)?.use {it.readLimited(Validation.MAX_BACKUP+1)} ?: error(tr(R.string.file_unreadable))}
  require(raw.size<=Validation.MAX_BACKUP){tr(R.string.backup_too_large)}
  val t=withContext(Dispatchers.IO){repo.importBackup(raw.toString(Charsets.UTF_8))}
  templates=repo.templates();message=tr(R.string.imported_template,t.name)+t.backup.warnings.joinToString(prefix=if(t.backup.warnings.isEmpty())"" else "\n")
 }}
 private val exportDocument=registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->if(uri!=null)task {
  withContext(Dispatchers.IO){contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {it.write(exportedText)} ?: error(tr(R.string.file_unwritable))}
  message=tr(R.string.backup_exported)
 }}
 private val fontDocument=registerForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)task {
  val (id,asset)=withContext(Dispatchers.IO){Assets.read(this@ConfigActivity,uri,"font")}
  val start=editorState.fontStart;val end=editorState.fontEnd
  config=if(fontTarget=="hint" && end>start && end<=config.placeholder.length) {
   var runs=config.hintRuns
   val points=(listOf(start,end)+runs.flatMap {listOf(it.start,it.end)}.filter {it in start..end}).distinct().sorted()
   for((a,b) in points.zipWithNext())runs=RichText.apply(runs,a,b,RichText.styleAt(runs,a,config.hint).copy(font=id))
   config.copy(hintRuns=runs,assets=config.assets+(id to asset))
  }else if(fontTarget=="hint")config.copy(hint=config.hint.copy(font=id),hintRuns=config.hintRuns.map {it.copy(style=it.style.copy(font=id))},assets=config.assets+(id to asset))
  else config.copy(query=config.query.copy(font=id),assets=config.assets+(id to asset))
  config=Assets.prune(config)
 }}
 private val iconDocument=registerForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)task {
  val (id,asset)=withContext(Dispatchers.IO){Assets.read(this@ConfigActivity,uri,"image")}
  val slot=if(iconTarget==-1)config.logo else config.buttons[iconTarget]
  config=config.copy(assets=config.assets+(id to asset));setSlot(iconTarget,slot.copy(icon=slot.icon.copy(asset=id)));config=Assets.prune(config)
 }}
 override fun onCreate(state:Bundle?) {
  super.onCreate(state);setResult(RESULT_CANCELED)
  configuring=intent.action==AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
  val requested=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,0)
  lifecycleScope.launch {
   try {
    reload()
    if(!ready) {
    if(configuring && requested !in configs) { message=tr(R.string.invalid_widget) }
    else if(requested>0 && requested in configs)select(requested)
    else if(configs.size==1)select(configs.keys.first())
    }
   } catch(e:Exception){message=tr(R.string.load_failed,errorText(e))} finally {ready=true}
  }
  setContent {
   val deviceConfiguration=LocalConfiguration.current
   val languageContext=remember(AppLanguage.code,deviceConfiguration){AppLanguage.context(this)}
   CompositionLocalProvider(LocalContext provides languageContext,LocalConfiguration provides languageContext.resources.configuration) {
   val dark=isSystemInDarkTheme()
   val notices=remember {SnackbarHostState()}
   LaunchedEffect(message,AppLanguage.code) {
    if(message.isNotEmpty()) {
     val current=message
     notices.showSnackbar(current,actionLabel=tr(R.string.close),duration=SnackbarDuration.Long)
     if(message==current)message=""
    }
   }
   MaterialExpressiveTheme(colorScheme=if(dark)dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)) {
    Surface(Modifier.fillMaxSize()) {
     Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
      Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
       if(selected==null)Row(Modifier.fillMaxWidth().padding(top=12.dp,bottom=8.dp),verticalAlignment=Alignment.CenterVertically) {
        Text("MySearchWidget",Modifier.weight(1f),style=MaterialTheme.typography.headlineSmall)
        LanguageButton()
       }
       if(!ready || busy)LinearProgressIndicator(Modifier.fillMaxWidth())
       if(ready)if(selected==null)InstanceScreen() else Editor()
      }
      SnackbarHost(notices,modifier=Modifier.align(Alignment.BottomCenter).padding(horizontal=16.dp).padding(bottom=if(selected!=null)84.dp else 12.dp))
     }
    }
   }
   }
  }
 }

 private fun setWallpaper(enabled:Boolean,requestPermission:Boolean=true) {
  val generation=++wallpaperGeneration
  if(!enabled) {
   wallpaperEnabled=false;wallpaperImage=null
   getSharedPreferences("interface",0).edit().putBoolean("wallpaper_preview",false).apply()
   return
  }
  if(!WallpaperPreview.allowed(this)){if(requestPermission){wallpaperBroadPermission=false;wallpaperPermission=true}else setWallpaper(false);return}
  lifecycleScope.launch {
   runCatching {withContext(Dispatchers.IO){WallpaperPreview.load(this@ConfigActivity)}}
    .onSuccess {if(generation!=wallpaperGeneration)return@onSuccess;wallpaperImage=it;wallpaperEnabled=true;getSharedPreferences("interface",0).edit().putBoolean("wallpaper_preview",true).apply()}
     .onFailure {
     if(generation!=wallpaperGeneration)return@onFailure
     setWallpaper(false)
     if(requestPermission && it is SecurityException && android.os.Build.VERSION.SDK_INT>=33 && !android.os.Environment.isExternalStorageManager()) {
      wallpaperBroadPermission=true;wallpaperPermission=true
     } else message=tr(R.string.wallpaper_unavailable)
    }
  }
 }
 private fun requestWallpaperImages() {
  val permission=if(android.os.Build.VERSION.SDK_INT>=33)android.Manifest.permission.READ_MEDIA_IMAGES else android.Manifest.permission.READ_EXTERNAL_STORAGE
  if(checkSelfPermission(permission)==android.content.pm.PackageManager.PERMISSION_GRANTED)setWallpaper(true)
  else wallpaperPermissionResult.launch(permission)
 }
 @Composable private fun WallpaperControl() {
  Toggle(tr(R.string.wallpaper_preview),wallpaperEnabled){setWallpaper(it)}
  if(wallpaperPermission)AlertDialog(onDismissRequest={wallpaperPermission=false},title={Text(tr(R.string.wallpaper_permission_title))},text={Text(tr(if(wallpaperBroadPermission)R.string.wallpaper_permission_help else R.string.wallpaper_images_help))},confirmButton={TextButton(onClick={
   wallpaperPermission=false
   if(wallpaperBroadPermission && android.os.Build.VERSION.SDK_INT>=33 && !android.os.Environment.isExternalStorageManager()) {
    wallpaperPending=true
    runCatching {startActivity(Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,android.net.Uri.parse("package:$packageName")))}.onFailure {wallpaperPending=false;message=tr(R.string.wallpaper_unavailable)}
   } else requestWallpaperImages()
  }){Text(tr(R.string.wallpaper_grant))}},dismissButton={TextButton(onClick={wallpaperPermission=false}){Text(tr(R.string.cancel))}})
 }

 @Composable private fun LanguageButton() {
  val description=tr(R.string.switch_language)
  OutlinedIconButton(onClick={
   AppLanguage.select(if(AppLanguage.code=="en")"it" else "en")
   lifecycleScope.launch {SearchWidget.ids(this@ConfigActivity).forEach {SearchWidget.update(this@ConfigActivity,it)}}
  },modifier=Modifier.size(48.dp).semantics {contentDescription=description}) {
   Text(AppLanguage.code.uppercase(java.util.Locale.ROOT),style=MaterialTheme.typography.labelLarge)
  }
 }

 override fun onResume() {
  super.onResume()
  launcherAccess=PinnedShortcuts.hasAccess(this)
  val wanted=getSharedPreferences("interface",0).getBoolean("wallpaper_preview",false)
  val pending=wallpaperPending;wallpaperPending=false
  if(pending && android.os.Build.VERSION.SDK_INT>=33 && android.os.Environment.isExternalStorageManager())requestWallpaperImages()
  else if(wanted && WallpaperPreview.allowed(this))setWallpaper(true,false)
  else if(wanted)setWallpaper(false)
  lifecycleScope.launch { SearchWidget.ids(this@ConfigActivity).forEach { SearchWidget.update(this@ConfigActivity,it) } }
  if(ready && selected==null)task { reload() }
 }
 private suspend fun reload(){configs=SearchWidget.ids(this).associateWith {repo.config(it)};engines=repo.engines();templates=repo.templates()}
 private fun select(id:Int){selected=id;config=configs[id] ?: WidgetConfig()}
 private fun setSlot(index:Int,slot:Slot){config=if(index==-1)config.copy(logo=slot) else config.copy(buttons=config.buttons.mapIndexed {i,s->if(i==index)slot else s})}
 private fun task(block:suspend ()->Unit){lifecycleScope.launch {busy=true;try {block()}catch(e:Exception){message=tr(R.string.operation_failed,errorText(e))}finally{busy=false}}}
 private fun save(exit:Boolean){task {
  val id=selected ?: return@task
  require(id>0){tr(R.string.add_widget_first)}
  config=Assets.prune(config);repo.save(id,config);SearchWidget.update(this@ConfigActivity,id,config)
  configs=configs+(id to config)
  if(configuring){setResult(RESULT_OK,Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id));finish()}
  else if(exit){selected=null}else message=tr(R.string.widget_updated,id)
 }}
 @Composable private fun ColumnScope.InstanceScreen(){
  Text(tr(R.string.your_widgets),style=MaterialTheme.typography.titleLarge)
  WallpaperControl()
  if(configs.isEmpty())Text(tr(R.string.add_widget_help),modifier=Modifier.padding(vertical=8.dp))
  Row {TextButton(onClick={
   val manager=AppWidgetManager.getInstance(this@ConfigActivity)
   if(manager.isRequestPinAppWidgetSupported)manager.requestPinAppWidget(ComponentName(this@ConfigActivity,SearchWidget::class.java),null,null)
   else message=tr(R.string.launcher_help)
  }){Text(tr(R.string.add_widget))};TextButton(onClick={task {reload()}}){Text(tr(R.string.refresh_list))}}
  OutlinedButton(onClick={importDocument.launch(arrayOf("application/json","text/*","application/octet-stream"))}){Text(tr(R.string.import_backup))}
  LazyColumn(Modifier.weight(1f)) {
   items(configs.entries.toList(),key={it.key}){entry->Card(Modifier.fillMaxWidth().padding(vertical=8.dp).clickable {select(entry.key)}){Column(Modifier.padding(12.dp)){Text("Widget ${entry.key}");Preview(entry.value,wallpaperImage)}}}
   if(configs.isEmpty())item {Text(tr(R.string.default_preview));Preview(WidgetConfig(),wallpaperImage)}
   item {TemplateLibrary()}
  }
 }
 private fun exportBackup(id:Int) { task {
     val clean=Assets.prune(config)
     val validAssets=mutableMapOf<String,Asset>();val warnings=mutableListOf<String>()
     withContext(Dispatchers.IO){clean.assets.forEach { (id,a)->runCatching {Assets.validate(this@ConfigActivity,mapOf(id to a));validAssets[id]=a}.onFailure {warnings+=tr(R.string.asset_omitted,id)} }}
     // Preserve a usable configuration even if a previously imported asset becomes unreadable.
     fun fixedSlot(s:Slot)=s.copy(icon=s.icon.copy(asset=s.icon.asset.takeIf {it in validAssets} ?: ""))
     val safe=clean.copy(assets=validAssets,hint=clean.hint.copy(font=clean.hint.font.takeIf {it in validAssets} ?: ""),query=clean.query.copy(font=clean.query.font.takeIf {it in validAssets} ?: ""),logo=fixedSlot(clean.logo),buttons=clean.buttons.map(::fixedSlot))
     exportedText=Catalog.json.encodeToString(Backup(1,safe,repo.engine(safe.engineId),warnings))
     if(warnings.isNotEmpty())message=warnings.joinToString("\n")
     exportDocument.launch("MySearchWidget-$id.json")

 } }
 @Composable private fun ColumnScope.Editor(){
  CompositionLocalProvider(LocalManualColorsEnabled provides !config.dynamic){EditorContent()}
 }
 @Composable private fun ColumnScope.EditorContent(){
  val id=selected ?: return
  var page by rememberSaveable(id){mutableStateOf("Aspetto")}
  var element by rememberSaveable(id){mutableStateOf("Generale")}
  var queryPreview by rememberSaveable(id){mutableStateOf(false)}
  LaunchedEffect(page,element){if(page!="Aspetto" || element!="Testo")queryPreview=false}
  val compactHeight=LocalConfiguration.current.screenHeightDp<500
  var previewExpanded by rememberSaveable(id,compactHeight){mutableStateOf(!compactHeight)}
  LaunchedEffect(compactHeight){if(compactHeight)previewExpanded=false}
  val pageStates=rememberSaveableStateHolder()
  var confirmDiscard by remember {mutableStateOf(false)}
  val dirty=config!=configs[id]
  BackHandler(enabled=!busy){save(true)}
  Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
   TextButton(onClick={if(dirty)confirmDiscard=true else if(configuring)finish() else {selected=null}},enabled=!busy){Text(tr(R.string.cancel))}
   Text("Widget $id",Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
   Button(onClick={save(false)},shapes=ButtonDefaults.shapes(),enabled=!busy,modifier=Modifier.heightIn(min=48.dp)){Text(if(configuring)tr(R.string.add) else tr(R.string.save))}
   Spacer(Modifier.width(4.dp))
   LanguageButton()
  }
  Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)) {
   Column(Modifier.padding(horizontal=12.dp,vertical=8.dp)) {
    Row(Modifier.fillMaxWidth().heightIn(min=32.dp).clickable(onClickLabel=tr(R.string.toggle_preview)){previewExpanded=!previewExpanded},horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
     Text(tr(R.string.preview_heading,if(previewExpanded)"▾" else "▸",id),style=MaterialTheme.typography.labelMedium)
     Text(if(dirty)tr(R.string.unsaved) else tr(R.string.saved),style=MaterialTheme.typography.labelMedium)
    }
    WallpaperControl()
    if(previewExpanded)Preview(config,wallpaperImage,queryPreview)
   }
  }
  val pages=listOf("Aspetto" to "AutoAwesome","Azioni" to "Star","Ricerca" to "Search","Backup" to "Cloud")
  pageStates.SaveableStateProvider(page) {
   Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(top=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
    Text(navigationTitle(page),style=MaterialTheme.typography.headlineSmall)
    when(page) {
     "Aspetto" -> {
      val elements=listOf("Generale","Barra","Testo","Logo")+(1..config.count).map {"Pulsante $it"}
      if(element !in elements)LaunchedEffect(element){element="Generale"}
      Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
       elements.forEach {name->FilterChip(selected=element==name,onClick={element=name},label={Text(navigationTitle(name))})}
      }
      key(element) {when(element) {
       "Generale" -> {
        SettingCard(tr(R.string.widget_style),tr(R.string.style_help)) {
         Choice(tr(R.string.theme),config.theme,listOf("system" to tr(R.string.system),"light" to tr(R.string.light),"dark" to tr(R.string.dark))){config=config.copy(theme=it)}
         Toggle(tr(R.string.material_you),config.dynamic){config=config.copy(dynamic=it)}
         Text(tr(R.string.material_notice),style=MaterialTheme.typography.bodySmall)
        }
        SettingCard(tr(R.string.buttons),tr(R.string.buttons_help)) {
         Choice(tr(R.string.button_count),config.count.toString(),(0..3).map {it.toString() to it.toString()}){config=config.copy(count=it.toInt())}
        }
       }
       "Barra" -> {
        Section(tr(R.string.outer_border),true){SurfaceEditor(config.outer){config=config.copy(outer=it)}}
        Section(tr(R.string.search_field)){SurfaceEditor(config.field){config=config.copy(field=it)}}
       }
       "Testo" -> {
        Choice(tr(R.string.text_target),if(queryPreview)"query" else "hint",listOf("hint" to tr(R.string.placeholder_label),"query" to tr(R.string.typed_text))){queryPreview=it=="query"}
        if(config.dynamic)Text(tr(R.string.material_notice),style=MaterialTheme.typography.bodySmall)
        key(queryPreview){RichTextEditor(config,queryPreview,{config=it}){start,end->
         fontTarget=if(queryPreview)"query" else "hint"
         if(!queryPreview && end>start && config.placeholder.isEmpty())config=config.copy(placeholder=tr(R.string.placeholder))
         editorState.fontStart=start;editorState.fontEnd=end;fontDocument.launch(arrayOf("*/*"))
        }}
       }
       else -> {
        val slotIndex=if(element=="Logo")-1 else element.substringAfter(" ").toIntOrNull()?.minus(1) ?: -1
        val slot=if(slotIndex==-1)config.logo else config.buttons[slotIndex]
        if(config.dynamic)Text(tr(R.string.disable_dynamic_help),style=MaterialTheme.typography.bodySmall)
        SlotEditor(slot,slotIndex>=0,{setSlot(slotIndex,it)},showAction=false){iconTarget=slotIndex;iconDocument.launch(arrayOf("image/*"))}
       }
      }}
     }
     "Azioni" -> {
      Text(tr(R.string.actions_help),style=MaterialTheme.typography.bodyMedium)
      (listOf(-1)+(0 until config.count)).forEach {index->key(index) {
       val slot=if(index==-1)config.logo else config.buttons[index]
       SettingCard(if(index==-1)"Logo" else tr(R.string.button_number,index+1)) {
        ActionPicker(tr(R.string.tap_action),slot.tap){setSlot(index,slot.copy(tap=it))}
       }
      }}
      SettingCard(tr(R.string.launcher_access),tr(if(launcherAccess)R.string.launcher_active else R.string.launcher_explain)) {
       if(!launcherAccess)Button(onClick={HomeAccess.request(this@ConfigActivity)}){Text(tr(R.string.launcher_select))}
       OutlinedButton(onClick={HomeAccess.settings(this@ConfigActivity)}){Text(tr(R.string.launcher_restore))}
      }
     }
     "Ricerca" -> {
      SettingCard(tr(R.string.search_field),tr(R.string.placeholder_help)) {
       OutlinedTextField(config.placeholder,{config=config.copy(placeholder=it.take(500),hintRuns=RichText.edit(config.placeholder,it.take(500),config.hintRuns,config.hint))},label={Text(tr(R.string.placeholder_label))},placeholder={Text(tr(R.string.placeholder))},supportingText={Text(tr(R.string.placeholder_empty))},modifier=Modifier.fillMaxWidth(),singleLine=true)
      }
      SettingCard(tr(R.string.google_input_title),tr(R.string.google_input_help)){
       Toggle(tr(R.string.google_input_toggle),config.googleInput){config=config.copy(googleInput=it)}
      }
      SettingCard(tr(R.string.search_engine),tr(R.string.engine_help)){
       if(config.googleInput)Text(tr(R.string.google_engine_disabled),style=MaterialTheme.typography.bodySmall)
       CompositionLocalProvider(LocalControlsEnabled provides !config.googleInput){
        Column(Modifier.graphicsLayer {alpha=if(config.googleInput).45f else 1f}){EngineLibrary()}
       }
      }
     }
     "Backup" -> {
      SettingCard(tr(R.string.backup_title),tr(R.string.backup_help)) {
       Button(onClick={exportBackup(id)},shapes=ButtonDefaults.shapes(),modifier=Modifier.fillMaxWidth()){Text(tr(R.string.export_backup))}
       OutlinedButton(onClick={importDocument.launch(arrayOf("application/json","text/*","application/octet-stream"))},modifier=Modifier.fillMaxWidth()){Text(tr(R.string.import_backup))}
      }
      TemplateLibrary()
     }
    }
    Spacer(Modifier.height(12.dp))
   }
  }
  NavigationBar(containerColor=MaterialTheme.colorScheme.surfaceContainer,windowInsets=WindowInsets(0,0,0,0)) {
   pages.forEach {(name,icon)->NavigationBarItem(selected=page==name,onClick={page=name},icon={Icon(IconCatalog.vector(icon,false),null)},label={Text(navigationTitle(name))})}
  }
  if(confirmDiscard)AlertDialog(onDismissRequest={confirmDiscard=false},title={Text(tr(R.string.discard_title))},text={Text(tr(R.string.discard_help))},confirmButton={TextButton(onClick={confirmDiscard=false;if(configuring)finish() else {selected=null}}){Text(tr(R.string.discard))}},dismissButton={TextButton(onClick={confirmDiscard=false}){Text(tr(R.string.keep_editing))}})
  applyModel?.let {t->AlertDialog(onDismissRequest={applyModel=null},title={Text(tr(R.string.apply_widget,id))},text={Text(tr(R.string.replace_template,t.name))},confirmButton={TextButton(onClick={applyModel=null;task {
   val c=repo.prepareTemplate(t);repo.save(id,c);SearchWidget.update(this@ConfigActivity,id,c);config=c;engines=repo.engines();configs=configs+(id to c);message=tr(R.string.template_applied,id)
  }}){Text(tr(R.string.apply))}},dismissButton={TextButton(onClick={applyModel=null}){Text(tr(R.string.cancel))}})}
 }
 @Composable private fun TemplateLibrary(){
  Text(tr(R.string.saved_templates,templates.size),style=MaterialTheme.typography.titleMedium)
  if(templates.isEmpty())Text(tr(R.string.empty_templates))
  templates.forEach { t->Column(Modifier.padding(vertical=8.dp)){
   Text(t.name);Preview(t.backup.config,wallpaperImage)
   Row {TextButton(onClick={if(selected!=null)applyModel=t else message=tr(R.string.open_target_first)}){Text(tr(R.string.apply_template))};TextButton(onClick={task {repo.deleteTemplate(t.id);templates=repo.templates()}}){Text(tr(R.string.remove_template))}}
  }}
 }
 @Composable private fun EngineLibrary(){
  val enabled=LocalControlsEnabled.current
  Choice(tr(R.string.selected_engine),config.engineId,engines.map {it.id to it.name}){config=config.copy(engineId=it)}
  var editing by remember {mutableStateOf<Engine?>(null)}
  TextButton(enabled=enabled,onClick={editing=Engine(UUID.randomUUID().toString(),"","https://example.com/search?q=%s")}){Text(tr(R.string.add_engine))}
  engines.filter {e->Catalog.engines.none {it.id==e.id}}.forEach { e->Row {
   TextButton(enabled=enabled,onClick={editing=e}){Text(tr(R.string.edit_engine,e.name))}
   TextButton(enabled=enabled,onClick={task {require(config.engineId!=e.id){tr(R.string.select_other_engine)};repo.deleteEngine(e.id);engines=repo.engines()}}){Text(tr(R.string.delete))}
  }}
  editing?.takeIf {enabled}?.let { e->
   var name by remember(e.id){mutableStateOf(e.name)};var url by remember(e.id){mutableStateOf(e.template)};var error by remember {mutableStateOf("")}
   AlertDialog(onDismissRequest={editing=null},title={Text(tr(R.string.custom_engine))},text={Column {
    OutlinedTextField(name,{name=it},label={Text(tr(R.string.name))});OutlinedTextField(url,{url=it},label={Text(tr(R.string.url_template))});if(error.isNotEmpty())Text(error,color=MaterialTheme.colorScheme.error)
   }},confirmButton={TextButton(onClick={val updated=e.copy(name=name,template=url);runCatching {Validation.engine(updated)}.onSuccess {task {repo.saveEngine(updated);engines=repo.engines();editing=null}}.onFailure {error=errorText(it)}}){Text(tr(R.string.save))}},dismissButton={TextButton(onClick={editing=null}){Text(tr(R.string.cancel))}})
  }
 }
}
@Composable fun Preview(config:WidgetConfig,wallpaper:android.graphics.Bitmap?=null,queryPreview:Boolean=false) {
 val context=LocalContext.current
 BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical=8.dp)) {
  val width=maxWidth.value.toInt().coerceAtLeast(180)
  val uiMode=LocalConfiguration.current
  val bitmap=remember(config,width,uiMode.uiMode,uiMode.fontScale,AppLanguage.code,queryPreview){Renderer.render(context,config,width,if(queryPreview)com.alessio89g.mysearchwidget.widget.SessionText(tr(R.string.query_preview),0,false) else null)}
  Box(Modifier.fillMaxWidth().height(if(wallpaper!=null)104.dp else 64.dp),contentAlignment=Alignment.Center) {
   if(wallpaper!=null)Image(wallpaper.asImageBitmap(),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop,alignment=Alignment.Center)
   Image(bitmap.asImageBitmap(),tr(R.string.widget_preview),Modifier.fillMaxWidth().height(64.dp))
  }
 }
}
