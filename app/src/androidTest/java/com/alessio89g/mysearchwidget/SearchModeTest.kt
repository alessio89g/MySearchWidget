package com.alessio89g.mysearchwidget

import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.overlay.Actions
import org.junit.Assert.*
import org.junit.Test

class SearchModeTest {
 @Test fun defaultsAndBackupPreserveCustomSettingsAcrossModeChanges() {
  assertTrue(WidgetConfig().dynamic)
  assertFalse(WidgetConfig().googleInput)
  val original=WidgetConfig(engineId="custom",hint=TextStyle(dark="#123456",darkGradient=Gradient(true)))
  val engine=Engine("custom","Custom","https://example.com/?q=%s")
  val stored=Backup(1,original.copy(googleInput=true,dynamic=true),engine)
  val restored=Validation.parse(Catalog.json.encodeToString(Backup.serializer(),stored))
  assertTrue(restored.config.googleInput)
  assertEquals(original,restored.config.copy(googleInput=false))
  assertEquals(engine,restored.engine)
  val legacy=Catalog.json.encodeToString(Backup.serializer(),Backup(1,WidgetConfig(),Catalog.engines.first())).replace("\"googleInput\":false,","")
  assertFalse(Validation.parse(legacy).config.googleInput)
 }
 @Test fun googleSearchRequestsEmptySearchUiOnlyInGooglePackage() {
  val intents=Actions.googleInputIntents()
  assertEquals("com.google.android.gms.actions.SEARCH_ACTION",intents.first().action)
  assertTrue(intents.all {it.getPackage()==Actions.GOOGLE && it.extras==null && it.data==null})
  assertTrue(intents.none {it.action==android.content.Intent.ACTION_MAIN || it.action==android.content.Intent.ACTION_WEB_SEARCH})
 }
}
