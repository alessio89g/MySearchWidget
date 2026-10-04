package com.alessio89g.mysearchwidget.data

import org.junit.Test
import org.junit.Assert.*
import kotlinx.serialization.encodeToString

class AspectRatioTest {
 @Test fun defaultsAreLockedAndExplicitUnlockSurvivesBackup() {
  val c=WidgetConfig();assertTrue(c.sizing.outerLocked);assertTrue(c.sizing.fieldLocked)
  assertTrue(c.buttons.all {it.proportionsLocked})
  val unlocked=c.copy(sizing=c.sizing.copy(outerLocked=false),buttons=c.buttons.mapIndexed {i,s->if(i==0)s.copy(proportionsLocked=false) else s})
  val restored=Validation.parse(Catalog.json.encodeToString(Backup(1,unlocked,Catalog.engines.first()))).config
  assertFalse(restored.sizing.outerLocked);assertFalse(restored.buttons[0].proportionsLocked);assertTrue(restored.buttons[1].proportionsLocked)
 }
 @Test fun bothDirectionsMaintainCurrentRatio() {
  assertEquals(90f to 45f,AspectRatio.resize(90f,true,2f,1f,256f,1f,256f))
  assertEquals(120f to 60f,AspectRatio.resize(60f,false,2f,1f,256f,1f,256f))
 }
 @Test fun limitsClampBothDimensionsTogether() {
  val (w,h)=AspectRatio.resize(200f,false,4f,1f,256f,1f,256f)
  assertEquals(256f,w,0f);assertEquals(64f,h,0f)
  val pair=AspectRatio.resize(1f,true,.25f,16f,1000f,1f,256f)
  assertEquals(16f to 64f,pair)
 }
 @Test fun locksRoundTripAndRemainIndependent() {
  val base=WidgetConfig()
  val c=base.copy(sizing=ElementSizing(outerWidthDp=320f,outerRatio=5f,fieldWidthDp=150f,fieldHeightDp=50f,fieldRatio=3f),buttons=base.buttons.mapIndexed {i,s->if(i==0)s.copy(widthDp=80f,heightDp=40f,aspectRatio=2f) else s})
  val restored=Validation.parse(Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first()))).config
  assertEquals(c,restored);assertNull(restored.buttons[1].aspectRatio)
 }
 @Test fun renderingFitsLockedShapesWithoutDistortion() {
  val base=WidgetConfig()
  val c=base.copy(buttons=base.buttons.mapIndexed {i,s->if(i==0)s.copy(widthDp=120f,heightDp=120f,aspectRatio=1f) else s},sizing=ElementSizing(fieldWidthDp=100f,fieldHeightDp=200f,fieldRatio=.5f))
  val d=ElementLayout(500f,c)
  assertEquals(1f,d.buttonBounds[0].width/d.buttonBounds[0].height,.0001f)
  assertEquals(.5f,d.field.width/d.field.height,.0001f)
  val outer=base.copy(heightDp=100f,sizing=ElementSizing(outerWidthDp=500f,outerRatio=5f))
  val fitted=outer.fitOuter(250f,80f)
  assertEquals(50f,fitted.heightDp,0f);assertEquals(250f,fitted.sizing.outerWidthDp!!,0f)
  assertEquals(100f,outer.heightDp,0f)
 }
 @Test fun impossibleImportedRatiosAreRejected() {
  assertTrue(runCatching {Validation.config(WidgetConfig(sizing=ElementSizing(outerRatio=1000f,outerWidthDp=320f)))}.isFailure)
  assertTrue(runCatching {Validation.config(WidgetConfig(sizing=ElementSizing(fieldRatio=2f)))}.isFailure)
 }
}
