package com.alessio89g.mysearchwidget.data
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class InitialGeometryTest {
 private fun preset()=Catalog.json.decodeFromString<WidgetConfig>(File("src/main/res/raw/default_widget.json").readText())
 @Test fun initialHeightIs68AtDifferentLauncherWidths() {
  val original=preset()
  for(w in listOf(280,320,356,374,400,500)) {
   val c=original.initialWidth(w);val p=WidgetPresentation(c,w,100f)
   assertEquals(68f,p.height,.001f);assertEquals(w,c.referenceWidthDp)
   assertEquals(47f,ElementLayout(w.toFloat(),c).field.height,.001f)
   assertNull(c.withButtonCount(2,original).sizing.fieldWidthDp)
   Validation.config(c)
  }
  assertEquals(original,original.initialWidth(374))
 }
 @Test fun increasingHeightDoesNotGrowWidthAndGetScaledBackDown() {
  val initial=preset().initialWidth(356)
  for(h in listOf(48f,68f,80f,100f,184.49f,200f,256f)) {
   val c=initial.withWidgetHeight(h,356f)
   assertEquals(h,WidgetPresentation(c,356).height,.001f)
   for(w in listOf(220,280,356)) {
    val p=WidgetPresentation(c,w)
    val layout=ElementLayout(w.toFloat(),p.config)
    assertTrue(layout.field.width>0f)
    assertTrue(layout.logoHit.width>0f)
    assertEquals(h,p.height,.001f)
   }
   assertEquals(initial.sizing.outerWidthDp,c.sizing.outerWidthDp)
   Validation.config(c)
  }
  assertEquals(90f,WidgetPresentation(initial.withWidgetHeight(120f,356f),356,90f).height,.001f)
 }
}
