package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ButtonCountLayoutTest {
 private fun preset()=Catalog.json.decodeFromString<WidgetConfig>(File("src/main/res/raw/default_widget.json").readText())
 @Test fun fewerButtonsFillAvailableWidthWithUniformInsets() {
  val original=preset()
  var c=original
  var previous=0f
  for(n in listOf(2,1,0)) {
   c=c.withButtonCount(n,original)
   val g=ElementLayout(374f,c)
   val inset=g.field.left-g.outer.left
   assertEquals(10.5f,inset,.001f)
   assertEquals(inset,g.field.top-g.outer.top,.001f)
   assertEquals(inset,g.outer.bottom-g.field.bottom,.001f)
   assertEquals(47f,g.field.height,.001f)
   assertTrue(g.field.width>previous);previous=g.field.width
   assertEquals(inset,g.outer.right-(g.buttonBounds.lastOrNull()?.right ?: g.field.right),.001f)
   g.buttonBounds.forEach {assertEquals(inset,it.top,.001f);assertEquals(47f,it.height,.001f)}
   assertEquals(g.buttonBounds.map {it.centerX},g.iconBounds.map {it.centerX})
  }
  val roundTrip=c.withButtonCount(3,original).withButtonCount(0,original)
  assertEquals(ElementLayout(374f,c).field,ElementLayout(374f,roundTrip).field)
  assertEquals(original.buttons.map {it.tap},roundTrip.buttons.map {it.tap})
 }
 @Test fun manualDimensionsAndOffsetsSurviveCountChanges() {
  val defaults=preset()
  val manual=defaults.copy(sizing=defaults.sizing.copy(fieldWidthDp=150f,fieldHeightDp=40f),placement=defaults.placement.copy(field=OffsetDp(16f,4f)))
  val c=manual.withButtonCount(1,defaults)
  assertEquals(150f,c.sizing.fieldWidthDp);assertEquals(40f,c.sizing.fieldHeightDp)
  assertEquals(OffsetDp(10f,4f),c.placement.field)
  assertEquals(c.sizing,c.withButtonCount(0,defaults).sizing)
  val reset=c.resetElementDimensions("field",defaults).resetElementPosition("field",defaults)
  assertNull(reset.sizing.fieldWidthDp);assertEquals(OffsetDp(),reset.placement.field)
  Validation.config(c)
  val locked=defaults.copy(sizing=defaults.sizing.copy(fieldLocked=true,fieldRatio=193f/47f)).withButtonCount(1,defaults)
  Validation.config(locked)
  assertEquals(47f,ElementLayout(374f,locked).field.height,.001f)
 }
 @Test fun loadingAndSameCountDoNotChangeExistingWidgets() {
  val c=preset();assertEquals(c,c.withButtonCount(3,c));assertFalse(c.adaptiveButtons)
 }
}
