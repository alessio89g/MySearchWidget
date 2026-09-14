package com.alessio89g.mysearchwidget.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.net.URI
import java.util.Base64

@Serializable data class Gradient(val enabled:Boolean=false,val start:String="#00C8FF",val end:String="#00D99B",val angle:Float=0f)
@Serializable data class TextRun(val start:Int,val end:Int,val style:TextStyle)
@Serializable data class Tone(val color: String = "", val opacity: Float = 1f, val blur: Float = 0f, val gradient:Gradient=Gradient())
@Serializable data class Surface(val light: Tone = Tone(), val dark: Tone = Tone(), val rounding: Float = 100f, val shape: String = "circle")
@Serializable data class TextStyle(val font: String = "", val size: Float = 15f, val weight: Int = 400, val light: String = "", val dark: String = "", val italic:Boolean=false,val underline:Boolean=false,val strike:Boolean=false,val lightGradient:Gradient=Gradient(),val darkGradient:Gradient=Gradient())
@Serializable data class Shortcut(val kind: String = "none", val value: String = "", val label: String = "")
@Serializable data class IconSpec(val name: String = "Search", val monochrome:Boolean = true, val outline: Boolean = false, val asset: String = "", val light: String = "", val dark: String = "",val lightGradient:Gradient=Gradient(),val darkGradient:Gradient=Gradient())
@Serializable data class Slot(val icon: IconSpec = IconSpec(), val surface: Surface = Surface(), val tap: Shortcut = Shortcut(), val up: Shortcut = Shortcut(), val down: Shortcut = Shortcut())
@Serializable data class Asset(val kind: String, val base64: String)
@Serializable data class WidgetConfig(
 val theme: String = "system", val dynamic: Boolean = true, val placeholder: String = "",
 val outer: Surface = Surface(Tone(opacity=.9f, blur=0f), Tone(opacity=.9f, blur=0f)),
 val field: Surface = Surface(), val hint: TextStyle = TextStyle(), val query: TextStyle = TextStyle(),
 val googleInput:Boolean = false, val engineId: String = "google", val count: Int = 2,
 val logo: Slot = Slot(icon=IconSpec("Google"), tap=Shortcut("app", "com.google.android.googlequicksearchbox", "Google")),
 val buttons: List<Slot> = listOf(Slot(icon=IconSpec("Chrome"), tap=Shortcut("app","com.android.chrome","Chrome")), Slot(icon=IconSpec("Mic"), tap=Shortcut("builtin","voice","Ricerca vocale")), Slot()),
 val hintRuns:List<TextRun> = emptyList(), val assets: Map<String, Asset> = emptyMap()
)
@Serializable data class Engine(val id: String, val name: String, val template: String)
@Serializable data class Backup(val schemaVersion: Int, val config: WidgetConfig, val engine: Engine, val warnings: List<String> = emptyList())
@Serializable data class Template(val id: String, val name: String, val backup: Backup)

object Catalog {
 val shapes=listOf("circle","squircle","flower","clover","leaf","pebble","scallop","teardrop")
 // Legacy IDs and blur/up/down fields are accepted only to preserve schema-1 backups.
 val retired = setOf("ai","gemini","translate","weather","camera")
 val functions = linkedMapOf("music" to "Cerca brano", "voice" to "Ricerca vocale", "lens" to "Google Lens")
 val engines = listOf(
  Engine("google","Google","https://www.google.com/search?q=%s"),
  Engine("bing","Bing","https://www.bing.com/search?q=%s"),
  Engine("ddg","DuckDuckGo","https://duckduckgo.com/?q=%s"),
  Engine("yahoo","Yahoo","https://search.yahoo.com/search?p=%s"),
  Engine("ecosia","Ecosia","https://www.ecosia.org/search?q=%s"),
  Engine("startpage","Startpage","https://www.startpage.com/sp/search?query=%s")
 )
 val json = Json { encodeDefaults = true; ignoreUnknownKeys = false }
}

object Validation {
 const val MAX_BACKUP = 12 * 1024 * 1024
 const val MAX_ASSET = 2 * 1024 * 1024
 fun engine(e: Engine) {
  require(e.id.isNotBlank() && e.id.length <= 100 && e.name.isNotBlank() && e.name.length <= 100) { "Nome o ID motore non valido" }
  require(e.template.length <= 4096 && e.template.contains("%s")) { "Il template deve contenere %s" }
  val uri = runCatching { URI(e.template.replace("%s", "test")) }.getOrNull()
  require(uri != null && uri.scheme in listOf("https", "http") && !uri.host.isNullOrBlank() && uri.userInfo == null) { "Usa un URL http/https valido con %s" }
 }
 fun color(s: String) { require(s.isEmpty() || Regex("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?").matches(s)) { "Colore non valido: usa #RRGGBB o #AARRGGBB" } }
 fun config(c: WidgetConfig) {
  require(c.theme in listOf("system","light","dark") && c.count in 0..3 && c.buttons.size == 3) { "Tema o numero di pulsanti non valido" }
  require(c.placeholder.length <= 500 && c.engineId.length in 1..100) { "Testo o motore non valido" }
  fun range(n: Float, a: Float, b: Float) { require(n.isFinite() && n in a..b) { "Valore numerico fuori intervallo" } }
  fun gradient(g:Gradient) { color(g.start);color(g.end);require(g.start.isNotEmpty() && g.end.isNotEmpty());range(g.angle,0f,360f) }
  fun surface(s: Surface) {
   range(s.rounding,0f,100f); require(s.shape in Catalog.shapes) { "Forma sconosciuta" }
   listOf(s.light,s.dark).forEach { color(it.color);gradient(it.gradient);range(it.opacity,0f,1f);range(it.blur,0f,100f) }
  }
  surface(c.outer);surface(c.field)
  (listOf(c.hint,c.query)+c.hintRuns.map {it.style}).forEach { range(it.size,8f,32f);require(it.weight in 100..900);color(it.light);color(it.dark);gradient(it.lightGradient);gradient(it.darkGradient);require(it.font.isEmpty() || c.assets[it.font]?.kind=="font") { "Font mancante" } }
  require(c.hintRuns.size<=500 && c.hintRuns.all {it.start>=0 && it.end>it.start && it.end<=c.placeholder.length})
  (listOf(c.logo)+c.buttons).forEach { s ->
   surface(s.surface);color(s.icon.light);color(s.icon.dark);gradient(s.icon.lightGradient);gradient(s.icon.darkGradient)
   require(s.icon.name.length in 1..100 && (s.icon.asset.isEmpty() || c.assets[s.icon.asset]?.kind=="image")) { "Icona mancante" }
   listOf(s.tap,s.up,s.down).forEach { a ->
    require(a.label.length <= 200 && a.value.length <= 250)
    require(a.kind in listOf("none","app","builtin","static","pinned")) { "Tipo scorciatoia sconosciuto" }
    if(a.kind=="builtin") require(a.value in Catalog.functions || a.value in Catalog.retired) { "Funzione sconosciuta" }
    if(a.kind in listOf("static","pinned")) require(a.value.contains("/") && a.value.substringBefore("/").contains(".") && a.value.substringAfter("/").isNotBlank()) { "Scorciatoia non valida" }
    if(a.kind=="app") require(Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+").matches(a.value)) { "Pacchetto app non valido" }
   }
  }
  require(c.assets.size <= 6) { "Troppe risorse incorporate" }
  var bytes = 0
  c.assets.forEach { (id,a) ->
   require(Regex("[a-zA-Z0-9_-]{1,100}").matches(id) && a.kind in listOf("font","image")) { "Risorsa non valida" }
   require(a.base64.length <= MAX_ASSET*4/3+8) { "Risorsa troppo grande" }
   val decoded = Base64.getDecoder().decode(a.base64)
   require(decoded.isNotEmpty() && decoded.size <= MAX_ASSET) { "Risorsa vuota o troppo grande" }; bytes += decoded.size
  }
  require(bytes <= 8*1024*1024) { "Risorse complessive oltre 8 MB" }
 }
 fun parse(text: String): Backup {
  require(text.toByteArray().size <= MAX_BACKUP) { "Backup oltre 12 MB" }
  val root = Catalog.json.parseToJsonElement(text).jsonObject
  require(root["schemaVersion"]?.jsonPrimitive?.intOrNull == 1) { "Versione backup non supportata" }
  require(root.keys.containsAll(listOf("config","engine"))) { "Backup incompleto: config o engine assente" }
  val c = root.getValue("config").jsonObject
  require(c.keys.containsAll(listOf("theme","dynamic","placeholder","outer","field","hint","query","engineId","count","logo","buttons","assets"))) { "Configurazione incompleta" }
  // Optional for older backups, but strictly boolean when present.
  (listOf(c.getValue("logo"))+c.getValue("buttons").jsonArray).forEach {slot->
   slot.jsonObject["icon"]?.jsonObject?.get("monochrome")?.let {value->
    require(value is JsonPrimitive && !value.isString && value.booleanOrNull!=null) { "monochrome must be a boolean" }
   }
  }
  val b = Catalog.json.decodeFromString<Backup>(text)
  config(b.config);engine(b.engine)
  require(b.config.engineId == b.engine.id) { "Riferimento al motore incoerente" }
  require(b.warnings.size <= 20 && b.warnings.all { it.length <= 500 })
  return b
 }
}
