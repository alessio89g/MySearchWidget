package com.alessio89g.mysearchwidget.i18n

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.data.Shortcut
import java.util.Locale

class WidgetApplication : Application() {
 override fun onCreate() { super.onCreate(); AppLanguage.initialize(this) }
}

/** An app-owned preference: changing it never recreates an Activity or edits a widget backup. */
object AppLanguage {
 private lateinit var app: Application
 var code by mutableStateOf("en")
  private set
 fun initialize(context: Context) {
  app=context.applicationContext as Application
  code=read(context)
 }
 fun read(context:Context):String = context.getSharedPreferences("interface",Context.MODE_PRIVATE)
  .getString("language","en").let {if(it=="it")"it" else "en"}
 fun select(value:String) {
  require(value in setOf("en","it"))
  app.getSharedPreferences("interface",Context.MODE_PRIVATE).edit().putString("language",value).apply()
  code=value
 }
 fun context(context:Context):Context {
  val config=Configuration(context.resources.configuration)
  config.setLocale(Locale.forLanguageTag(code))
  return context.createConfigurationContext(config)
 }
 fun text(@StringRes id:Int,vararg args:Any):String {
  val resources=context(app).resources
  return if(args.isEmpty())resources.getString(id) else resources.getString(id,*args)
 }
}

// Reading the snapshot-backed language inside text() also observes it from Compose call sites.
fun tr(@StringRes id:Int,vararg args:Any)=AppLanguage.text(id,*args)

fun navigationTitle(key:String):String = when(key) {
 "Aspetto"->tr(R.string.appearance)
 "Azioni"->tr(R.string.actions)
 "Ricerca"->tr(R.string.search_tab)
 "Generale"->tr(R.string.general)
 "Barra"->tr(R.string.bar)
 "Testo"->tr(R.string.text)
 "Icona"->tr(R.string.icon)
 "Cerchio"->tr(R.string.circle)
 else->if(key.startsWith("Pulsante "))tr(R.string.button_number,key.substringAfter(" ")) else key
}
fun builtinTitle(key:String):String=when(key) {
 "voice"->tr(R.string.voice)
 "music"->tr(R.string.music)
 "lens"->"Google Lens"
 else->tr(R.string.shortcut_label)
}
fun shortcutTitle(shortcut:Shortcut):String=when {
 shortcut.kind=="builtin"->builtinTitle(shortcut.value)
 shortcut.kind=="static" && shortcut.value.endsWith("/mysearchwidget.chrome.incognito")->tr(R.string.incognito)
 else->shortcut.label
}
private val validationMessages=mapOf(
  "Nome o ID motore non valido" to R.string.validation_0,
  "Il template deve contenere %s" to R.string.validation_1,
  "Usa un URL http/https valido con %s" to R.string.validation_2,
  "Colore non valido: usa #RRGGBB o #AARRGGBB" to R.string.validation_3,
  "Tema o numero di pulsanti non valido" to R.string.validation_4,
  "Testo o motore non valido" to R.string.validation_5,
  "Valore numerico fuori intervallo" to R.string.validation_6,
  "Forma sconosciuta" to R.string.validation_7,
  "Font mancante" to R.string.validation_8,
  "Icona mancante" to R.string.validation_9,
  "Tipo scorciatoia sconosciuto" to R.string.validation_10,
  "Funzione sconosciuta" to R.string.validation_11,
  "Scorciatoia non valida" to R.string.validation_12,
  "Pacchetto app non valido" to R.string.validation_13,
  "Troppe risorse incorporate" to R.string.validation_14,
  "Risorsa non valida" to R.string.validation_15,
  "Risorsa troppo grande" to R.string.validation_16,
  "Risorsa vuota o troppo grande" to R.string.validation_17,
  "Risorse complessive oltre 8 MB" to R.string.validation_18,
  "Versione backup non supportata" to R.string.validation_19,
  "Backup incompleto: config o engine assente" to R.string.validation_20,
  "Configurazione incompleta" to R.string.validation_21,
  "Riferimento al motore incoerente" to R.string.validation_22,
  "Backup oltre 12 MB" to R.string.backup_too_large,
)
fun errorText(error:Throwable):String = validationMessages[error.message]?.let {tr(it)}
 ?: error.message ?: tr(R.string.invalid_file)
