package com.alessio89g.mysearchwidget.data

import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.*
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import java.util.UUID

private val Context.store by preferencesDataStore("my_search_widget")
class Repository(private val context: Context) {
 private val enginesKey=stringPreferencesKey("engines")
 private val hiddenKey=stringSetPreferencesKey("hidden_widgets")
 private val modelsKey=stringPreferencesKey("templates")
 suspend fun config(id: Int): WidgetConfig {
  val raw=context.store.data.first()[stringPreferencesKey("widget_$id")] ?: return WidgetConfig()
  return Catalog.json.decodeFromString(raw)
 }
 // Only committed configurations belong in the library. A launcher may bind
 // preview or abandoned IDs; missing data must not manufacture default entries.
 suspend fun library(boundIds:Set<Int>):Map<Int,WidgetConfig> {
  val p=context.store.data.first();val hidden=p[hiddenKey].orEmpty()
  return boundIds.filter {it.toString() !in hidden}.mapNotNull {id->
   p[stringPreferencesKey("widget_$id")]?.let {id to Catalog.json.decodeFromString<WidgetConfig>(it)}
  }.toMap()
 }
 suspend fun confirmPinned(id:Int) {
  require(id>0)
  context.store.edit {p->
   val key=stringPreferencesKey("widget_$id")
   if(!p.contains(key))p[key]=Catalog.json.encodeToString(WidgetConfig())
  }
 }
 suspend fun hasConfig(id:Int)=context.store.data.first().contains(stringPreferencesKey("widget_$id"))
 suspend fun removeLibraryItems(widgetIds:Set<Int>,templateIds:Set<String>) {context.store.edit {p->
  // Providers cannot delete IDs owned by a different AppWidgetHost. Keep active
  // settings intact and hide the library entry until explicitly configured again.
  p[hiddenKey]=p[hiddenKey].orEmpty()+widgetIds.map {it.toString()}
  val old=p[modelsKey]?.let {Catalog.json.decodeFromString<List<Template>>(it)} ?: emptyList()
  p[modelsKey]=Catalog.json.encodeToString(old.filterNot {it.id in templateIds})
 }}
 suspend fun save(id: Int,c: WidgetConfig) {
  require(id>0);Validation.config(c)
  context.store.edit { it[stringPreferencesKey("widget_$id")]=Catalog.json.encodeToString(c);it[hiddenKey]=it[hiddenKey].orEmpty()-id.toString() }
 }
 suspend fun remove(id: Int) { context.store.edit { it.remove(stringPreferencesKey("widget_$id"));it[hiddenKey]=it[hiddenKey].orEmpty()-id.toString() } }
 suspend fun engines(): List<Engine> = Catalog.engines + customEngines()
 private suspend fun customEngines(): List<Engine> = context.store.data.first()[enginesKey]?.let { Catalog.json.decodeFromString(it) } ?: emptyList()
 suspend fun engine(id: String): Engine = engines().firstOrNull { it.id==id } ?: Catalog.engines.first()
 suspend fun saveEngine(e: Engine) {
  Validation.engine(e);require(Catalog.engines.none { it.id==e.id })
  context.store.edit { p ->
   val old=p[enginesKey]?.let { Catalog.json.decodeFromString<List<Engine>>(it) } ?: emptyList()
   p[enginesKey]=Catalog.json.encodeToString(old.filterNot { it.id==e.id }+e)
  }
 }
 suspend fun deleteEngine(id: String) {
  context.store.edit { p ->
   require(p.asMap().filterKeys { it.name.startsWith("widget_") }.values.none { Catalog.json.decodeFromString<WidgetConfig>(it as String).engineId==id }) { tr(R.string.engine_in_use) }
   val old=p[enginesKey]?.let { Catalog.json.decodeFromString<List<Engine>>(it) } ?: emptyList()
   p[enginesKey]=Catalog.json.encodeToString(old.filterNot { it.id==id })
  }
 }
 suspend fun templates(): List<Template> = context.store.data.first()[modelsKey]?.let { Catalog.json.decodeFromString(it) } ?: emptyList()
 // Import only appends to the model library. It never writes widget_* or engines.
 suspend fun importBackup(raw: String): Template {
  val backup=Validation.parse(raw)
  Assets.validate(context, backup.config.assets)
  val model=Template(UUID.randomUUID().toString(),tr(R.string.template_name,java.text.SimpleDateFormat("dd/MM HH:mm",java.util.Locale.forLanguageTag(AppLanguage.code)).format(java.util.Date())),backup)
  context.store.edit { p ->
   val old=p[modelsKey]?.let { Catalog.json.decodeFromString<List<Template>>(it) } ?: emptyList()
   p[modelsKey]=Catalog.json.encodeToString(old+model)
  }
  return model
 }
 suspend fun deleteTemplate(id: String) { context.store.edit { p ->
  val old=p[modelsKey]?.let { Catalog.json.decodeFromString<List<Template>>(it) } ?: emptyList()
  p[modelsKey]=Catalog.json.encodeToString(old.filterNot { it.id==id })
 } }
 // Called only after the separate Apply confirmation. Resolve ID conflicts without changing other widgets.
 suspend fun prepareTemplate(t: Template): WidgetConfig {
  val e=t.backup.engine
  val same=engines().firstOrNull { it.id==e.id }
  if(same==e) return t.backup.config
  val newEngine=e.copy(id=UUID.randomUUID().toString())
  saveEngine(newEngine)
  return t.backup.config.copy(engineId=newEngine.id)
 }
}
