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
 private val modelsKey=stringPreferencesKey("templates")
 suspend fun config(id: Int): WidgetConfig {
  val raw=context.store.data.first()[stringPreferencesKey("widget_$id")] ?: return WidgetConfig()
  return Catalog.json.decodeFromString(raw)
 }
 suspend fun save(id: Int,c: WidgetConfig) {
  require(id>0);Validation.config(c)
  context.store.edit { it[stringPreferencesKey("widget_$id")]=Catalog.json.encodeToString(c) }
 }
 suspend fun remove(id: Int) { context.store.edit { it.remove(stringPreferencesKey("widget_$id")) } }
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
