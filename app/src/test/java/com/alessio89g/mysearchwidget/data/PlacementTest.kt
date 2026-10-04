package com.alessio89g.mysearchwidget.data

import org.junit.Test
import org.junit.Assert.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class PlacementTest {
 private val base=WidgetConfig()
 private fun layout(c:WidgetConfig)=ElementLayout(500f,c)
 @Test fun defaultOffsetsAndLinksPreserveOldBackups() {
  val root=Catalog.json.parseToJsonElement(Catalog.json.encodeToString(Backup(1,base,Catalog.engines.first()))).jsonObject.toMutableMap()
  val config=root.getValue("config").jsonObject.toMutableMap();config.remove("placement")
  config["buttons"]=JsonArray(config.getValue("buttons").jsonArray.map {slot->JsonObject(slot.jsonObject.filterKeys {it !in setOf("position","iconPosition","iconLinked")})})
  root["config"]=JsonObject(config)
  val c=Validation.parse(JsonObject(root).toString()).config
  assertEquals(base,c);assertTrue(c.placement.fieldLinked);assertTrue(c.buttons.all {it.iconLinked})
 }
 @Test fun parentMovementOnlyMovesLinkedChildren() {
  val before=layout(base);val shift=OffsetDp(35f,-22f)
  val changed=layout(base.copy(placement=Placement(field=shift)))
  assertEquals(before.field.shift(shift),changed.field)
  assertEquals(before.logo.shift(shift),changed.logo)
  assertEquals(before.textLeft+35,changed.textLeft,0f)
  assertEquals(before.textCenterY-22,changed.textCenterY,0f)
  assertEquals(before.buttonBounds,changed.buttonBounds)
  val unlinked=layout(base.copy(placement=Placement(field=shift,fieldLinked=false)))
  assertEquals(before.logo,unlinked.logo);assertEquals(before.textLeft,unlinked.textLeft,0f)
 }
 @Test fun childrenMoveIndependently() {
  val before=layout(base)
  val c=base.copy(placement=Placement(logo=OffsetDp(-25f,8f),text=OffsetDp(50f,-12f)))
  val after=layout(c)
  assertEquals(before.field,after.field)
  assertEquals(before.logo.shift(OffsetDp(-25f,8f)),after.logo)
  assertEquals(before.textLeft+50,after.textLeft,0f)
  assertEquals(before.buttonBounds,after.buttonBounds)
 }
 @Test fun linkChangesDoNotJumpAndUnlinkedChildrenIgnoreParentResize() {
  val c=base.copy(placement=Placement(field=OffsetDp(20f,10f)),sizing=ElementSizing(fieldWidthDp=240f))
  val before=layout(c);val detached=c.linkField(false,500f);val after=layout(detached)
  assertEquals(before.logo,after.logo)
  assertEquals(before.textLeft,after.textLeft,.001f)
  assertEquals(before.textCenterY,after.textCenterY,0f)
  val resized=layout(detached.copy(sizing=detached.sizing.copy(fieldWidthDp=140f)))
  assertEquals(after.logo,resized.logo);assertEquals(after.textLeft,resized.textLeft,0f)
  val attached=layout(detached.linkField(true,500f))
  assertEquals(before.logo,attached.logo);assertEquals(before.textLeft,attached.textLeft,.001f)
 }
 @Test fun unlinkingTinyFieldsPreservesChildGeometry() {
  val c=base.copy(sizing=ElementSizing(fieldWidthDp=30f,fieldHeightDp=12f),placement=Placement(field=OffsetDp(17f,-9f)))
  val before=layout(c);val after=layout(c.linkField(false,500f))
  assertEquals(before.logo,after.logo)
  assertEquals(before.textLeft,after.textLeft,.001f)
  assertEquals(before.textRight,after.textRight,.001f)
  assertEquals(before.textScale,after.textScale,.001f)
 }
 @Test fun buttonLinksAreIndependentAndDoNotJump() {
  val slot=base.buttons[0].copy(position=OffsetDp(80f,9f),iconPosition=OffsetDp(-4f,2f))
  fun withSlot(s:Slot)=base.copy(buttons=listOf(s)+base.buttons.drop(1))
  val before=layout(withSlot(slot));val detached=slot.linkIcon(false)
  assertEquals(before.iconBounds,layout(withSlot(detached)).iconBounds)
  val moved=layout(withSlot(detached.copy(position=OffsetDp(-30f,-9f))))
  assertEquals(before.iconBounds,moved.iconBounds)
  assertEquals(before.buttonBounds[1],moved.buttonBounds[1])
  assertEquals(slot,detached.linkIcon(true));assertEquals(slot,slot.linkIcon(true))
 }
 @Test fun offscreenAndOverlappingElementsRemainStoredAndHaveClippedHits() {
  val c=base.copy(placement=Placement(outer=OffsetDp(1200f,-800f),field=OffsetDp(-1000f,1000f)),buttons=base.buttons.map {it.copy(position=OffsetDp(-500f,-500f))})
  val d=layout(c)
  assertTrue(d.outer.left>500f);assertTrue(d.field.bottom>64f)
  assertNull(d.inputHit.visibleWithin(500f,64f))
  assertTrue(d.buttonHits.all {it.visibleWithin(500f,64f)==null})
  assertEquals(Bounds(0f,0f,20f,30f),Bounds(-10f,-5f,20f,30f).visibleWithin(500f,64f))
  assertEquals(c,Validation.parse(Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first()))).config)
 }
 @Test fun layerOrderIsIndependentPersistentAndSkipsHiddenButtons() {
  val c=base.moveLayer("field",true)
  assertTrue(c.visibleLayers().indexOf("field")>c.visibleLayers().indexOf("logo"))
  assertFalse(c.visibleLayers().contains("button2"))
  assertEquals(base.placement.field,c.placement.field)
  assertEquals(c,Validation.parse(Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first()))).config)
  val invalid=base.copy(placement=base.placement.copy(layers=List(10){"field"}))
  assertTrue(runCatching {Validation.config(invalid)}.isFailure)
 }
 @Test fun invalidCoordinatesAreRejected() {
  listOf(Float.NaN,Float.POSITIVE_INFINITY,10001f).forEach {n->
   assertTrue(runCatching {Validation.config(base.copy(placement=Placement(text=OffsetDp(n,0f))))}.isFailure)
  }
 }
}
