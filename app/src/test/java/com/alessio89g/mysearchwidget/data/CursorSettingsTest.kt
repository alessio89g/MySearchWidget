package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.json.*

class CursorSettingsTest {
 @Test fun oldConfigurationsHaveNoIdleAnimation() {
  val old=JsonObject(Catalog.json.encodeToJsonElement(WidgetConfig()).jsonObject.filterKeys {
   it !in setOf("idleCursor","idleCursorColors","queryCursorColors")
  })
  val restored=Catalog.json.decodeFromJsonElement<WidgetConfig>(old)
  assertFalse(restored.idleCursor)
  assertEquals(CursorColors(),restored.queryCursorColors)
 }
 @Test fun backupRetainsBothColorsAndIdleChoice() {
  val c=WidgetConfig(idleCursor=true,idleCursorColors=CursorColors("#112233","#445566"),queryCursorColors=CursorColors("#778899","#AABBCC"))
  val backup=Backup(1,c,Catalog.engines.first())
  assertEquals(c,Validation.parse(Catalog.json.encodeToString(backup)).config)
 }
 @Test fun cursorColorLinksAreIndependentAndResetDoesNotChangeText() {
  var c=WidgetConfig(placeholder="Keep me",idleCursor=true,idleCursorColors=CursorColors("#112233","#445566"),queryCursorColors=CursorColors("#778899","#AABBCC"))
  c=ModeSettings.toggle(c,"color.idleCursor",false,false)
  assertEquals(c.idleCursorColors.light,c.idleCursorColors.dark)
  assertEquals("#AABBCC",c.queryCursorColors.dark)
  val reset=OptionReset.apply(c,WidgetConfig(),listOf("idleCursor","queryCursorColors.light"))
  assertFalse(reset.idleCursor)
  assertEquals("",reset.queryCursorColors.light)
  assertEquals("#AABBCC",reset.queryCursorColors.dark)
  assertEquals("Keep me",reset.placeholder)
 }
}
