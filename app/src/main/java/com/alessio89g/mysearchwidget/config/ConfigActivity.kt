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
import androidx.compose.material.icons.filled.Delete
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
 var librarySelection by mutableStateOf<Set<String>>(emptySet())
 var confirmLibraryDelete by mutableStateOf(false)
 var selected by mutableStateOf<Int?>(null)
 var history by mutableStateOf(EditHistory(WidgetConfig()))
 var gestureBase:EditHistory<WidgetConfig>?=null
 var config:WidgetConfig
  get()=history.present
  set(value){history=(gestureBase ?: history).record(value)}
 fun gesture(active:Boolean){gestureBase=if(active)history else null}
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
 private var viewportRevision by mutableIntStateOf(0)
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
    // A new configuration or a request from an existing widget is explicit;
    // allow it even if no saved library entry exists (or it was hidden).
    if(requested>0 && requested in SearchWidget.ids(this@ConfigActivity)) {
     configs=configs+(requested to repo.config(requested))
    }
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
    Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.surfaceContainerLow) {
     Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
      Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
       if(selected==null && editorState.librarySelection.isNotEmpty())Spacer(Modifier.height(64.dp))
       if(selected==null && editorState.librarySelection.isEmpty())Row(Modifier.fillMaxWidth().padding(top=12.dp,bottom=8.dp),verticalAlignment=Alignment.CenterVertically) {
        Text("MySearchWidget",Modifier.weight(1f),style=MaterialTheme.typography.headlineSmall)
        LanguageButton()
       }
       if(!ready || busy)LinearProgressIndicator(Modifier.fillMaxWidth())
       if(ready)if(selected==null)InstanceScreen() else Editor()
      }
      LibrarySelectionActions(Modifier.align(Alignment.TopEnd).padding(12.dp))
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
  viewportRevision++
  launcherAccess=PinnedShortcuts.hasAccess(this)
  val wanted=getSharedPreferences("interface",0).getBoolean("wallpaper_preview",false)
  val pending=wallpaperPending;wallpaperPending=false
  if(pending && android.os.Build.VERSION.SDK_INT>=33 && android.os.Environment.isExternalStorageManager())requestWallpaperImages()
  else if(wanted && WallpaperPreview.allowed(this))setWallpaper(true,false)
  else if(wanted)setWallpaper(false)
  lifecycleScope.launch { SearchWidget.ids(this@ConfigActivity).forEach { SearchWidget.update(this@ConfigActivity,it) } }
  if(ready && selected==null)task { reload() }
 }
 private suspend fun reload(){
  configs=repo.library(SearchWidget.ids(this).toSet());engines=repo.engines();templates=repo.templates()
  val keys=configs.keys.map {"w:$it"}.toSet()+templates.map {"t:${it.id}"}
  editorState.librarySelection=editorState.librarySelection.intersect(keys)
 }
 private fun select(id:Int){
  selected=id
  val current=configs[id] ?: WidgetConfig()
  val options=AppWidgetManager.getInstance(this).getAppWidgetOptions(id)
  val width=WidgetViewport.from(this,options).width
  val anchored=if(current.referenceWidthDp==null && WidgetViewport.hasAllocation(options))current.copy(referenceWidthDp=width) else current
  if(id in configs)configs=configs+(id to anchored)
  editorState.gesture(false)
  editorState.history=EditHistory(anchored)
 }
 private fun setSlot(index:Int,slot:Slot){config=if(index==-1)config.copy(logo=slot) else config.copy(buttons=config.buttons.mapIndexed {i,s->if(i==index)slot else s})}
 private fun task(block:suspend ()->Unit){lifecycleScope.launch {busy=true;try {block()}catch(e:Exception){message=tr(R.string.operation_failed,errorText(e))}finally{busy=false}}}
 private fun save(){task {
  val id=selected ?: return@task
  require(id>0){tr(R.string.add_widget_first)}
  val options=AppWidgetManager.getInstance(this@ConfigActivity).getAppWidgetOptions(id)
  val reference=config.referenceWidthDp ?: if(WidgetViewport.hasAllocation(options))WidgetViewport.from(this@ConfigActivity,options).width else null
  val submitted=Assets.prune(config.copy(referenceWidthDp=reference))
  repo.save(id,submitted);SearchWidget.update(this@ConfigActivity,id,submitted)
  configs=configs+(id to submitted)
  if(configuring){setResult(RESULT_OK,Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id));finish()}
  else message=tr(R.string.widget_updated,id)
 }}
 @Composable private fun ColumnScope.InstanceScreen(){
  Text(tr(R.string.your_widgets),style=MaterialTheme.typography.titleLarge)
  WallpaperControl()
  if(configs.isEmpty())Text(tr(R.string.add_widget_help),modifier=Modifier.padding(vertical=8.dp))
  FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {Button(shapes=ButtonDefaults.shapes(),onClick={
   val manager=AppWidgetManager.getInstance(this@ConfigActivity)
   if(manager.isRequestPinAppWidgetSupported)manager.requestPinAppWidget(ComponentName(this@ConfigActivity,SearchWidget::class.java),null,
    android.app.PendingIntent.getBroadcast(this@ConfigActivity,0,Intent(this@ConfigActivity,SearchWidget::class.java).setAction(SearchWidget.PIN_CONFIRMED),android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE))
   else message=tr(R.string.launcher_help)
  }){Text(tr(R.string.add_widget))};TextButton(onClick={task {reload()}}){Text(tr(R.string.refresh_list))}}
  OutlinedButton(onClick={importDocument.launch(arrayOf("application/json","text/*","application/octet-stream"))}){Text(tr(R.string.import_backup))}
  LazyColumn(Modifier.weight(1f)) {
   items(configs.entries.toList(),key={it.key}){entry->LibraryCard("w:${entry.key}","Widget ${entry.key}",{select(entry.key)}) {
    if(editorState.librarySelection.isEmpty())Text("Widget ${entry.key}")
    Preview(entry.value,wallpaperImage,viewport=launcherViewport(entry.key),engine=engines.firstOrNull {it.id==entry.value.engineId})
   }}
   if(configs.isEmpty())item {Text(tr(R.string.default_preview));Preview(WidgetConfig(),wallpaperImage)}
   item {TemplateLibrary()}
  }
  TextButton(onClick={
   try {startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://github.com/alessio89g/MySearchWidget")))}
   catch(_:android.content.ActivityNotFoundException){message=tr(R.string.link_unavailable)}
  },modifier=Modifier.fillMaxWidth()) {
   Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_github),contentDescription=null,modifier=Modifier.size(24.dp))
   Spacer(Modifier.width(8.dp))
   Text(tr(R.string.github_repo))
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
 private fun resetOuterHeight() {
  config=config.copy(heightDp=64f,sizing=if(config.sizing.outerRatio!=null)config.sizing.copy(outerRatio=null,outerWidthDp=null,layoutHeightDp=null) else config.sizing.copy(layoutHeightDp=null))
 }
 private fun updateOuterHeight(height:Float,previewWidth:Float) {
  val ratio=if(config.sizing.outerLocked)config.sizing.outerRatio ?: ((config.sizing.outerWidthDp ?: previewWidth)/config.heightDp) else null
  if(ratio==null)config=config.copy(heightDp=height,sizing=config.sizing.copy(layoutHeightDp=null))
  else {
   val (w,h)=AspectRatio.resize(height,false,ratio,16f,1000f,16f,256f)
   config=config.copy(heightDp=h,sizing=config.sizing.copy(outerWidthDp=w,outerRatio=ratio,layoutHeightDp=null))
  }
 }
 @Composable private fun launcherViewport(id:Int):WidgetViewport? {
  val orientation=LocalConfiguration.current.orientation
  return remember(id,viewportRevision,orientation) {
   val options=AppWidgetManager.getInstance(this).getAppWidgetOptions(id)
   if(WidgetViewport.hasAllocation(options))WidgetViewport.from(this,options) else null
  }
 }
 @Composable private fun ColumnScope.Editor(){
  CompositionLocalProvider(LocalManualColorsEnabled provides !config.dynamic){EditorContent()}
 }
 @Composable private fun ColumnScope.EditorContent(){
  val id=selected ?: return
  var page by rememberSaveable(id){mutableStateOf("Geometria")}
  var element by rememberSaveable(id){mutableStateOf("")}
  var queryPreview by rememberSaveable(id){mutableStateOf(false)}
  var actionTarget by rememberSaveable(id){mutableStateOf("")}
  var searchTarget by rememberSaveable(id){mutableStateOf("")}
  var previewTheme by rememberSaveable(id){mutableStateOf(if(Renderer.dark(this@ConfigActivity,config))"dark" else "light")}
  val viewport=launcherViewport(id)
  var previewWidth by remember(id,viewport){mutableFloatStateOf(config.referenceWidthDp?.toFloat() ?: viewport?.width?.toFloat() ?: 356f)}
  val sizeUnit=ElementLayout(previewWidth,config).unit
  LaunchedEffect(page,element){if(page!="Aspetto" || element!="Testo")queryPreview=false}
  val compactHeight=LocalConfiguration.current.screenHeightDp<500
  var previewExpanded by rememberSaveable(id,compactHeight){mutableStateOf(!compactHeight)}
  LaunchedEffect(compactHeight){if(compactHeight)previewExpanded=false}
  val pageStates=rememberSaveableStateHolder()
  var confirmDiscard by remember {mutableStateOf(false)}
  val dirty=config!=configs[id]
  fun leaveEditor(){if(dirty)confirmDiscard=true else if(configuring)finish() else {selected=null}}
  BackHandler(enabled=!busy && editorState.librarySelection.isEmpty()){leaveEditor()}
  if(editorState.librarySelection.isNotEmpty())Spacer(Modifier.height(64.dp))
  else Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
   TextButton(onClick={leaveEditor()},enabled=!busy){Text(tr(R.string.cancel))}
   Text("Widget $id",Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
   Button(onClick={save()},shapes=ButtonDefaults.shapes(),enabled=!busy,modifier=Modifier.heightIn(min=48.dp)){Text(if(configuring)tr(R.string.add) else tr(R.string.save))}
   Spacer(Modifier.width(4.dp))
   LanguageButton()
  }
  Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)) {
   Column(Modifier.padding(horizontal=12.dp,vertical=8.dp)) {
    Row(Modifier.fillMaxWidth().heightIn(min=32.dp).clickable(onClickLabel=tr(R.string.toggle_preview)){previewExpanded=!previewExpanded},horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
     Text(tr(R.string.preview_heading,if(previewExpanded)"▾" else "▸",id),style=MaterialTheme.typography.labelMedium)
     Text(if(dirty)tr(R.string.unsaved) else tr(R.string.saved),style=MaterialTheme.typography.labelMedium)
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
     TextButton(enabled=editorState.history.past.isNotEmpty(),onClick={editorState.gesture(false);editorState.history=editorState.history.undo()}){Text("↶ "+tr(R.string.undo_edit))}
     TextButton(enabled=editorState.history.future.isNotEmpty(),onClick={editorState.gesture(false);editorState.history=editorState.history.redo()}){Text("↷ "+tr(R.string.redo_edit))}
     FilledTonalButton(shapes=ButtonDefaults.shapes(),onClick={previewTheme=if(previewTheme=="light")"dark" else "light"}){Text(tr(if(previewTheme=="light")R.string.preview_light else R.string.preview_dark))}
    }
    if(previewExpanded) {
     Preview(config.copy(theme=previewTheme),wallpaperImage,queryPreview,viewport,engines.firstOrNull {it.id==config.engineId}){previewWidth=it.toFloat()}
     Section(tr(R.string.preview_options)){WallpaperControl()}
    }
   }
  }
  val pages=listOf("Geometria" to "Settings","Aspetto" to "AutoAwesome","Azioni" to "Star","Ricerca" to "Search","Backup" to "Cloud")
  CompositionLocalProvider(LocalPreviewTheme provides {theme:String->previewTheme=theme},LocalEditGesture provides editorState::gesture) {
  pageStates.SaveableStateProvider(page) {
   val scroll=rememberScrollState()
   val scope=rememberCoroutineScope()
   fun top(){scope.launch {scroll.scrollTo(0)}}
   Column(Modifier.weight(1f).verticalScroll(scroll).imePadding().padding(top=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
     Text(navigationTitle(page),style=MaterialTheme.typography.headlineSmall)
     Text(tr(when(page){"Geometria"->R.string.layout_overview;"Aspetto"->R.string.appearance_overview;"Azioni"->R.string.actions_overview;"Ricerca"->R.string.search_overview;else->R.string.backup_overview}),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
    when(page) {
     "Aspetto" -> {
      val elements=listOf("Generale","Barra","Testo","Logo")+(1..config.count).map {"Pulsante $it"}
      if(element.isEmpty()) {
       SettingsGroup(listOf(
        SettingsEntry(navigationTitle("Generale"),tr(R.string.general_style_summary),"AutoAwesome"){element="Generale";top()},
        SettingsEntry(navigationTitle("Barra"),tr(R.string.bar_style_summary),"Settings"){element="Barra";top()}
       ))
       SettingsGroup(listOf(
        SettingsEntry(navigationTitle("Testo"),tr(R.string.text_style_summary),"Description"){element="Testo";top()},
        SettingsEntry("Logo",tr(R.string.icon_style_summary),"Photo"){element="Logo";top()}
       ),1)
       if(config.count>0)SettingsGroup((1..config.count).map {i->SettingsEntry(tr(R.string.button_number,i),tr(R.string.button_style_summary),"Star"){element="Pulsante $i";top()}},2)
      } else {
      if(element !in elements)LaunchedEffect(element){element=""}
      DetailBack {element="";top()}
      if(element in elements)key(element) {when(element) {
       "Generale" -> {
        SettingCard(tr(R.string.widget_style),tr(R.string.style_help)) {
         Choice(tr(R.string.theme),config.theme,listOf("system" to tr(R.string.system),"light" to tr(R.string.light),"dark" to tr(R.string.dark))){config=config.copy(theme=it);previewTheme=if(Renderer.dark(this@ConfigActivity,config))"dark" else "light"}
         Toggle(tr(R.string.material_you),config.dynamic){config=config.copy(dynamic=it)}
         Text(tr(R.string.material_notice),style=MaterialTheme.typography.bodySmall)
        }
       }
       "Barra" -> {
        Section(tr(R.string.outer_border),true){
         SurfaceEditor(config.outer){config=config.copy(outer=it)}
        }
        Section(tr(R.string.search_field)){
         Text(tr(R.string.dimensions_help),style=MaterialTheme.typography.bodySmall)
         SurfaceEditor(config.field){config=config.copy(field=it)}
        }
       }
       "Testo" -> {
        Choice(tr(R.string.text_target),if(queryPreview)"query" else "hint",listOf("hint" to tr(R.string.placeholder_label),"query" to tr(R.string.typed_text))){queryPreview=it=="query"}
        if(config.dynamic)Text(tr(R.string.material_notice),style=MaterialTheme.typography.bodySmall)
        SettingCard(tr(if(queryPreview)R.string.typed_text else R.string.placeholder_label)) {
        key(queryPreview){RichTextEditor(config,queryPreview,{config=it},engines.firstOrNull {it.id==config.engineId}){start,end->
         fontTarget=if(queryPreview)"query" else "hint"
         if(!queryPreview && end>start && config.placeholder.isEmpty())config=config.copy(placeholder=placeholderText(config,engines.firstOrNull {it.id==config.engineId}))
         editorState.fontStart=start;editorState.fontEnd=end;fontDocument.launch(arrayOf("*/*"))
        }}
        }
       }
       else -> {
        val slotIndex=if(element=="Logo")-1 else element.substringAfter(" ").toIntOrNull()?.minus(1) ?: -1
        val slot=if(slotIndex==-1)config.logo else config.buttons[slotIndex]
        if(config.dynamic)Text(tr(R.string.disable_dynamic_help),style=MaterialTheme.typography.bodySmall)
        SlotEditor(slot,slotIndex>=0,{setSlot(slotIndex,it)},showAction=false,unit=sizeUnit,firstButton=slotIndex==0){iconTarget=slotIndex;iconDocument.launch(arrayOf("image/*"))}
       }
      }}
      }
     }
     "Geometria" -> GeometryEditor(config,previewWidth,{config=it},{updateOuterHeight(it,previewWidth)},{resetOuterHeight()},::top)
     "Azioni" -> {
      val areas=listOf("field" to tr(R.string.search_field),"logo" to "Logo")+(0 until config.count).map {"button$it" to tr(R.string.button_number,it+1)}
      if(actionTarget.isEmpty()) {
       SettingsGroup(areas.take(2).map {(key,title)->SettingsEntry(title,tr(R.string.area_action_summary),if(key=="field")"Search" else "Photo"){actionTarget=key;top()}})
       if(config.count>0)SettingsGroup(areas.drop(2).map {(key,title)->SettingsEntry(title,tr(R.string.area_action_summary),"Star"){actionTarget=key;top()}},1)
       SettingsGroup(listOf(SettingsEntry(tr(R.string.launcher_access),tr(R.string.launcher_shortcuts_summary),"Home"){actionTarget="launcher";top()}),2)
      } else {
       DetailBack {actionTarget="";top()}
       if(actionTarget=="launcher") {
        SettingCard(tr(R.string.launcher_access),tr(if(launcherAccess)R.string.launcher_active else R.string.launcher_explain)) {
         if(!launcherAccess)Button(onClick={HomeAccess.request(this@ConfigActivity)},shapes=ButtonDefaults.shapes()){Text(tr(R.string.launcher_select))}
         OutlinedButton(onClick={HomeAccess.settings(this@ConfigActivity)}){Text(tr(R.string.launcher_restore))}
        }
       } else {
        val area=actionTarget.takeIf {key->areas.any {it.first==key}} ?: "field"
        key(area) {
         SettingCard(areas.first {it.first==area}.second) {
          if(area=="field") {
           Text(tr(R.string.field_action_help),style=MaterialTheme.typography.bodyMedium)
           FilledTonalButton(onClick={page="Ricerca"},shapes=ButtonDefaults.shapes()){Text(tr(R.string.open_search_settings))}
          } else {
           val index=if(area=="logo")-1 else area.last().digitToInt()
           val slot=if(index==-1)config.logo else config.buttons[index]
           ActionPicker(tr(R.string.tap_action),slot.tap){setSlot(index,slot.copy(tap=it))}
          }
         }
         SettingCard(tr(R.string.touch_feedback),if(area.startsWith("button"))tr(R.string.haptic_area_help) else "") {
          Toggle(tr(R.string.haptic_feedback),config.hapticEnabled(area)){config=config.copy(haptics=config.haptics+(area to it))}
         }
        }
       }
      }
     }
     "Ricerca" -> {
      if(searchTarget.isEmpty()) {
       SettingsGroup(listOf(
        SettingsEntry(tr(R.string.search_field),tr(R.string.placeholder_help),"Description"){searchTarget="text";top()}
       ),1)
       SettingsGroup(listOf(
        SettingsEntry(tr(R.string.google_input_title),tr(if(config.googleInput)R.string.setting_on else R.string.setting_off),"Search"){searchTarget="google";top()},
        SettingsEntry(tr(R.string.search_engine),tr(R.string.engine_help),"Public"){searchTarget="engine";top()}
       ))
      } else {
      DetailBack {searchTarget="";top()}
      if(searchTarget=="text")SettingCard(tr(R.string.search_field),tr(R.string.placeholder_help)) {
       OutlinedTextField(config.placeholder,{config=config.copy(placeholder=it.take(500),hintRuns=RichText.edit(config.placeholder,it.take(500),config.hintRuns,config.hint))},label={Text(tr(R.string.placeholder_label))},placeholder={Text(placeholderText(config,engines.firstOrNull {it.id==config.engineId}))},supportingText={Text(tr(R.string.placeholder_empty))},modifier=Modifier.fillMaxWidth(),singleLine=true)
       TextButton(onClick={page="Aspetto";element="Testo";queryPreview=false}){Text(tr(R.string.format_search_text))}
      }
      if(searchTarget=="google")SettingCard(tr(R.string.google_input_title),tr(R.string.google_input_help)){
       Toggle(tr(R.string.google_input_toggle),config.googleInput){config=config.copy(googleInput=it)}
      }
      if(searchTarget=="engine")SettingCard(tr(R.string.search_engine),tr(R.string.engine_help)){
       if(config.googleInput)Text(tr(R.string.google_engine_disabled),style=MaterialTheme.typography.bodySmall)
       CompositionLocalProvider(LocalControlsEnabled provides !config.googleInput){
        Column(Modifier.graphicsLayer {alpha=if(config.googleInput).45f else 1f}){EngineLibrary()}
       }
      }
      }
     }
     "Backup" -> {
      SettingsGroup(listOf(
       SettingsEntry(tr(R.string.export_backup),tr(R.string.export_summary),"Cloud"){exportBackup(id)},
       SettingsEntry(tr(R.string.import_backup),tr(R.string.import_summary),"Folder"){importDocument.launch(arrayOf("application/json","text/*","application/octet-stream"))}
      ))
      TemplateLibrary()
     }
    }
    Spacer(Modifier.height(12.dp))
   }
  }
  }
  NavigationBar(containerColor=MaterialTheme.colorScheme.surfaceContainer,windowInsets=WindowInsets(0,0,0,0)) {
   pages.forEach {(name,icon)->NavigationBarItem(selected=page==name,onClick={page=name;editorState.librarySelection=emptySet();editorState.confirmLibraryDelete=false},icon={Icon(IconCatalog.vector(icon,false),null)},label={Text(navigationTitle(name),maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)})}
  }
  if(confirmDiscard)AlertDialog(onDismissRequest={confirmDiscard=false},title={Text(tr(R.string.discard_title))},text={Text(tr(R.string.discard_help))},confirmButton={TextButton(onClick={confirmDiscard=false;if(configuring)finish() else {selected=null}}){Text(tr(R.string.discard))}},dismissButton={TextButton(onClick={confirmDiscard=false}){Text(tr(R.string.keep_editing))}})
  applyModel?.let {t->AlertDialog(onDismissRequest={applyModel=null},title={Text(tr(R.string.apply_widget,id))},text={Text(tr(R.string.replace_template,t.name))},confirmButton={TextButton(onClick={applyModel=null;task {
   config=repo.prepareTemplate(t).let {it.copy(referenceWidthDp=it.referenceWidthDp ?: config.referenceWidthDp)};engines=repo.engines()
  }}){Text(tr(R.string.apply))}},dismissButton={TextButton(onClick={applyModel=null}){Text(tr(R.string.cancel))}})}
 }
 @Composable private fun TemplateLibrary(){
  Text(tr(R.string.saved_templates,templates.size),style=MaterialTheme.typography.titleMedium)
  if(templates.isEmpty())Text(tr(R.string.empty_templates))
  templates.forEach { t->LibraryCard("t:${t.id}",t.name,{if(selected!=null)applyModel=t else message=tr(R.string.open_target_first)}) {
   if(editorState.librarySelection.isEmpty())Text(t.name)
   Preview(t.backup.config,wallpaperImage,engine=t.backup.engine)
   if(editorState.librarySelection.isEmpty())TextButton(onClick={if(selected!=null)applyModel=t else message=tr(R.string.open_target_first)}){Text(tr(R.string.apply_template))}
  }}
 }
 private fun toggleLibrary(key:String) {
  editorState.librarySelection=editorState.librarySelection.let {if(key in it)it-key else it+key}
 }
 @Composable private fun LibraryCard(key:String,label:String,onOpen:()->Unit,content:@Composable ColumnScope.()->Unit) {
  val selection=editorState.librarySelection
  Card(Modifier.fillMaxWidth().padding(vertical=8.dp).combinedClickable(
   onClick={if(selection.isNotEmpty())toggleLibrary(key) else onOpen()},
   onLongClick={toggleLibrary(key)},onLongClickLabel=tr(R.string.select_item)),
   colors=CardDefaults.cardColors(containerColor=if(key in selection)MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)) {
   Column(Modifier.padding(12.dp)) {
    if(selection.isNotEmpty())Row(verticalAlignment=Alignment.CenterVertically) {
     Checkbox(key in selection,{toggleLibrary(key)},Modifier.semantics {contentDescription=tr(R.string.select_named,label)})
     Text(label,style=MaterialTheme.typography.labelMedium)
    }
    content()
   }
  }
 }
 @Composable private fun LibrarySelectionActions(modifier:Modifier) {
  val selection=editorState.librarySelection
  if(selection.isEmpty())return
  BackHandler {editorState.librarySelection=emptySet();editorState.confirmLibraryDelete=false}
  Surface(modifier,shape=RoundedCornerShape(24.dp),tonalElevation=6.dp,shadowElevation=4.dp) {
   Row(verticalAlignment=Alignment.CenterVertically) {
    TextButton(onClick={editorState.librarySelection=emptySet()}){Text(tr(R.string.cancel))}
    Text(AppLanguage.context(this@ConfigActivity).resources.getQuantityString(R.plurals.selected_items,selection.size,selection.size))
    IconButton(onClick={editorState.confirmLibraryDelete=true},enabled=!busy) {
     Icon(androidx.compose.material.icons.Icons.Default.Delete,tr(R.string.delete_selected))
    }
   }
  }
  if(editorState.confirmLibraryDelete)AlertDialog(onDismissRequest={editorState.confirmLibraryDelete=false},
   title={Text(AppLanguage.context(this@ConfigActivity).resources.getQuantityString(R.plurals.delete_selection_title,selection.size,selection.size))},
   text={Text(tr(if(selection.any {it.startsWith("w:")})R.string.delete_widgets_help else R.string.delete_templates_help))},
   confirmButton={TextButton(onClick={
    editorState.confirmLibraryDelete=false
    val widgets=selection.filter {it.startsWith("w:")}.map {it.substring(2).toInt()}.toSet()
    val models=selection.filter {it.startsWith("t:")}.map {it.substring(2)}.toSet()
    task {repo.removeLibraryItems(widgets,models);reload();editorState.librarySelection=emptySet()}
   }){Text(tr(R.string.delete))}},
   dismissButton={TextButton(onClick={editorState.confirmLibraryDelete=false}){Text(tr(R.string.cancel))}})
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
   }},confirmButton={TextButton(onClick={val updated=e.copy(name=name,template=url);runCatching {Validation.engine(updated)}.onSuccess {task {repo.saveEngine(updated);engines=repo.engines();editing=null;SearchWidget.ids(this@ConfigActivity).forEach {widgetId->if(repo.config(widgetId).engineId==updated.id)SearchWidget.update(this@ConfigActivity,widgetId)}}}.onFailure {error=errorText(it)}}){Text(tr(R.string.save))}},dismissButton={TextButton(onClick={editing=null}){Text(tr(R.string.cancel))}})
  }
 }
}
@Composable fun Preview(config:WidgetConfig,wallpaper:android.graphics.Bitmap?=null,queryPreview:Boolean=false,viewport:WidgetViewport?=null,engine:Engine?=null,onWidth:(Int)->Unit={}) {
 val context=LocalContext.current
 BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical=8.dp)) {
  val presentation=WidgetPresentation(config,viewport?.width ?: maxWidth.value.toInt().coerceIn(180,1000),viewport?.availableHeight)
  val width=presentation.designWidth
  val shown=presentation.config
  // Scale the whole composition to the card; never reflow its elements to the card width.
  val displayScale=minOf(1f,maxWidth.value/presentation.width)
  val displayWidth=presentation.width*displayScale
  val displayHeight=presentation.height*displayScale
  LaunchedEffect(width){onWidth(width)}
  val uiMode=LocalConfiguration.current
  val bitmap=remember(shown,width,uiMode.uiMode,uiMode.fontScale,AppLanguage.code,queryPreview,engine){Renderer.render(context,shown,width,if(queryPreview)com.alessio89g.mysearchwidget.widget.SessionText(tr(R.string.query_preview),0,false) else null,engine)}
  Box(Modifier.fillMaxWidth().height((displayHeight+if(wallpaper!=null)40f else 0f).dp),contentAlignment=Alignment.Center) {
   if(wallpaper!=null)Image(wallpaper.asImageBitmap(),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop,alignment=Alignment.Center)
   Image(bitmap.asImageBitmap(),tr(R.string.widget_preview),Modifier.size(displayWidth.dp,displayHeight.dp),contentScale=ContentScale.FillBounds)
  }
 }
}
