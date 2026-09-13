package com.alessio89g.mysearchwidget.data
import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class ValidationTest {
 private fun backup(c:WidgetConfig=WidgetConfig())=Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first()))
 private fun rejected(block:()->Unit){assertTrue(runCatching(block).isFailure)}
 @Test fun roundTripPreservesIndependentSettings(){
  val config=WidgetConfig(count=3,theme="dark",query=TextStyle(size=21f,weight=700),buttons=WidgetConfig().buttons.mapIndexed {i,s->s.copy(surface=s.surface.copy(rounding=i*25f,shape=if(i==2)"flower" else "circle"))})
  assertEquals(config,Validation.parse(backup(config)).config)
 }
 @Test fun legacyBackupsKeepMonochromeDefaults() {
  val legacy=backup().replace("\"monochrome\":true,", "")
  val c=Validation.parse(legacy).config
  assertTrue(c.logo.icon.monochrome)
  assertTrue(c.buttons.all {it.icon.monochrome})
 }
 @Test fun originalColorsRoundTripIndependentlyWithoutLosingTint() {
  val base=WidgetConfig()
  val original=base.copy(logo=base.logo.copy(icon=base.logo.icon.copy(monochrome=false,dark="#123456",darkGradient=Gradient(true))),buttons=base.buttons.mapIndexed {i,slot->slot.copy(icon=slot.icon.copy(monochrome=i!=1))})
  val restored=Validation.parse(backup(original)).config
  assertEquals(original,restored)
  assertFalse(restored.logo.icon.monochrome)
  assertTrue(restored.buttons[0].icon.monochrome)
  assertFalse(restored.buttons[1].icon.monochrome)
  rejected {Validation.parse(backup().replace("\"monochrome\":true","\"monochrome\":\"false\""))}
 }
 @Test fun schemaAndRequiredFieldsAreChecked(){
  rejected {Validation.parse("not JSON")}
  rejected {Validation.parse(backup().replace("\"schemaVersion\":1","\"schemaVersion\":99"))}
  val root=Catalog.json.parseToJsonElement(backup()).jsonObject.toMutableMap()
  val config=root.getValue("config").jsonObject.toMutableMap();config.remove("buttons");root["config"]=JsonObject(config)
  rejected {Validation.parse(JsonObject(root).toString())}
 }
 @Test fun invalidSettingsAreRejected(){
  rejected {Validation.parse(backup(WidgetConfig(count=4)))}
  rejected {Validation.parse(backup(WidgetConfig(query=TextStyle(size=100f))))}
  rejected {Validation.parse(backup(WidgetConfig(field=Surface(dark=Tone(color="red")))))}
  rejected {Validation.parse(backup(WidgetConfig(logo=Slot(tap=Shortcut("builtin","arbitrary")))))}
 }
 @Test fun missingAndMalformedAssetsAreRejected(){
  rejected {Validation.parse(backup(WidgetConfig(hint=TextStyle(font="missing"))))}
  rejected {Validation.parse(backup(WidgetConfig(assets=mapOf("a" to Asset("font","%%%")))))}
  rejected {Validation.parse(backup(WidgetConfig(assets=mapOf("../bad" to Asset("image","YQ==")))))}
 }
 @Test fun engineTemplatesRejectUnsafeOrIncompleteUrls(){
  listOf("javascript:alert('%s')","file:///tmp/%s","https://example.com/noquery","https://user:pass@example.com/?q=%s","https:///?q=%s").forEach {template->rejected {Validation.engine(Engine("test","Test",template))}}
  Validation.engine(Engine("test","Test","https://example.com/?q=%s"))
 }
 @Test fun engineReferenceMustMatchEmbeddedEngine(){rejected {Validation.parse(backup(WidgetConfig(engineId="missing")))}}
 @Test fun opacityAndFiniteNumbersAreValidated(){
  rejected {Validation.config(WidgetConfig(outer=Surface(dark=Tone(opacity=2f))))}
  rejected {Validation.config(WidgetConfig(outer=Surface(rounding=Float.NaN)))}
 }
}
