package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.encodeToString

class EditingUpgradeTest {
 @Test fun horizontalResizeKeepsHeightAndMargins() {
  val original=Catalog.json.decodeFromString<WidgetConfig>(java.io.File("src/main/res/raw/default_widget.json").readText())
  for(count in 0..3) {
   val c=original.withButtonCount(count,original)
   val baseline=ElementLayout(374f,c)
   for(width in listOf(220,280,320,374,391,600)) {
    val p=WidgetPresentation(c,width,300f)
    val layout=ElementLayout(p.designWidth.toFloat(),p.config)
    assertEquals(width,p.designWidth)
    assertEquals(1f,p.scale,0f)
    assertEquals(68f,p.height,.001f)
    assertEquals(baseline.field.height,layout.field.height,.001f)
    assertEquals(baseline.field.left-baseline.outer.left,layout.field.left-layout.outer.left,.001f)
    assertEquals(baseline.field.top-baseline.outer.top,layout.field.top-layout.outer.top,.001f)
    assertEquals(baseline.field.width+width-374,layout.field.width,.001f)
    for(i in 0 until count) {
     assertEquals(baseline.buttonBounds[i].width,layout.buttonBounds[i].width,.001f)
     assertEquals(baseline.outer.right-baseline.buttonBounds[i].right,layout.outer.right-layout.buttonBounds[i].right,.001f)
     assertEquals(baseline.buttonHits[i].left+width-374,layout.buttonHits[i].left,.001f)
    }
   }
   assertEquals(374,c.referenceWidthDp)
  }
 }
 @Test fun shortAllocationFitsWithoutDistortingOrChangingSavedSettings() {
  val c=WidgetConfig(referenceWidthDp=374,heightDp=69f)
  val p=WidgetPresentation(c,391,40f)
  assertEquals(40f,p.height,.001f)
  assertEquals(391f/69f,p.width/p.height,.001f)
  assertEquals(374,c.referenceWidthDp)
  assertEquals(69f,c.heightDp,0f)
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
