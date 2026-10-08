package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.encodeToString

class EditingUpgradeTest {
 @Test fun launcherPaddingScalesAllBoundsUniformly() {
  val c=WidgetConfig(referenceWidthDp=374,heightDp=69f,count=3,sizing=ElementSizing(fieldWidthDp=193f,fieldHeightDp=47f),placement=Placement(field=OffsetDp(5f,0f),text=OffsetDp(-5f,0f)))
  val design=ElementLayout(374f,c)
  for(width in listOf(220,374,391,600)) {
   val p=WidgetPresentation(c,width,300f)
   assertEquals(374,p.designWidth)
   assertEquals(width/374f,p.scale,.0001f)
   val bounds=listOf(design.outer,design.field,design.logo)+design.buttonBounds+design.iconBounds+design.buttonHits
   for(b in bounds) {
    val output=p.transform(b)
    assertEquals(b.left/374f,output.left/p.width,.0001f)
    assertEquals(b.width/374f,output.width/p.width,.0001f)
    assertEquals(b.top/69f,output.top/p.height,.0001f)
   }
   assertEquals(69f,c.heightDp,0f)
  }
 }
 @Test fun shortAllocationFitsWithoutDistortingOrChangingSavedSettings() {
  val c=WidgetConfig(referenceWidthDp=374,heightDp=69f)
  val p=WidgetPresentation(c,391,40f)
  assertEquals(40f,p.height,.001f)
  assertEquals(374f/69f,p.width/p.height,.001f)
  assertEquals(c,p.config)
 }
 @Test fun undoRedoRestoresAssetsAndBranchesWithoutTouchingSavedConfig() {
  val saved=WidgetConfig()
  val edited=saved.copy(placeholder="Example",assets=mapOf("asset" to Asset("image","abc")),hintRuns=listOf(TextRun(0,2,TextStyle(weight=700))))
  var h=EditHistory(saved).record(edited).record(edited.copy(heightDp=90f))
  assertEquals(edited,h.undo().present)
  assertEquals(saved,h.undo().undo().present)
  assertEquals(h.present,h.undo().redo().present)
  h=h.undo().record(edited.copy(count=3))
  assertTrue(h.future.isEmpty())
  assertEquals(edited,h.undo().present)
  assertEquals("",saved.placeholder)
  assertEquals(h,h.record(h.present))
 }
 @Test fun historyIsBoundedAndNewSessionStartsEmpty() {
  var h=EditHistory(0)
  repeat(250){h=h.record(it+1)}
  assertEquals(100,h.past.size)
  repeat(100){h=h.undo()}
  assertEquals(150,h.present)
  assertEquals(h,h.undo())
  assertTrue(EditHistory(h.present).past.isEmpty())
 }
 @Test fun backupsKeepReferenceWidthAndSharedHapticsWithEnabledDefaults() {
  val c=WidgetConfig(referenceWidthDp=374,haptics=mapOf("button0" to false,"field" to false))
  Validation.config(c)
  val restored=Catalog.json.decodeFromString<WidgetConfig>(Catalog.json.encodeToString(c))
  assertEquals(c,restored)
  assertFalse(restored.hapticEnabled("icon0"))
  assertFalse(restored.hapticEnabled("button0"))
  assertFalse(restored.hapticEnabled("text"))
  assertTrue(restored.hapticEnabled("logo"))
  assertTrue(restored.hapticEnabled("icon1"))
  val legacy=Catalog.json.decodeFromString<WidgetConfig>("{}")
  assertNull(legacy.referenceWidthDp)
  assertTrue(legacy.hapticEnabled("button0"))
  assertTrue(runCatching {Validation.config(c.copy(haptics=mapOf("icon0" to false)))}.isFailure)
 }
}
