package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.random.Random

class ElementLayoutTest {
 private fun backup(c:WidgetConfig)=Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first()))
 @Test fun automaticGeometryMatchesPreviousProportions() {
  for(w in listOf(180f,356f,1000f))for(h in listOf(16f,48.5f,64f,96f,256f))for(n in 0..3) {
   val c=WidgetConfig(heightDp=h,count=n);val d=ElementLayout(w,c)
   val u=WidgetDimensions(w,h,n).scale
   assertEquals(9*u,d.field.left,.001f)
   assertEquals(w-9*u-n*52*u-if(n>0)u else 0f,d.field.right,.001f)
   assertEquals(46*u,d.field.height,.001f)
   assertEquals(32*u,d.logo.width,.001f)
   assertEquals(60*u,d.textLeft,.001f)
   for(i in 0 until n) {
    val center=w-9*u-n*52*u+i*52*u+26*u
    assertEquals(center-23*u,d.buttonBounds[i].left,.001f)
    assertEquals(25*u,d.iconBounds[i].width,.001f)
   }
  }
 }
 @Test fun manualSizesAreIndependentAndPersistInBackups() {
  val initial=WidgetConfig()
  val c=initial.copy(sizing=ElementSizing(fieldHeightDp=54f,textGapDp=7.5f),buttons=initial.buttons.mapIndexed {i,s->if(i==0)s.copy(widthDp=65f,heightDp=38f,proportionsLocked=false,gapDp=12f,icon=s.icon.copy(sizeDp=18f)) else s})
  val d=ElementLayout(500f,c)
  assertEquals(c,Validation.parse(backup(c)).config)
  assertEquals(54f,d.field.height,.001f)
  assertEquals(65f,d.buttonBounds[0].width,.001f)
  assertEquals(38f,d.buttonBounds[0].height,.001f)
  assertEquals(18f,d.iconBounds[0].width,.001f)
  assertEquals(46f,d.buttonBounds[1].width,.001f)
  assertEquals(25f,d.iconBounds[1].width,.001f)
  assertEquals(12f+23f,d.buttonBounds[0].centerX-d.field.right,.001f)
  val taller=ElementLayout(700f,c.copy(heightDp=96f))
  assertEquals(18f,taller.iconBounds[0].width,.001f)
  assertEquals(37.5f,taller.iconBounds[1].width,.001f)
 }
 @Test fun manualMarginsAndOuterWidthAreExactWhenSpaceAllows() {
  val d=ElementLayout(500f,WidgetConfig(sizing=ElementSizing(outerWidthDp=400f,sidePaddingDp=20f)))
  assertEquals(400f,d.outer.width,.001f)
  assertEquals(20f,d.field.left,.001f)
  assertEquals(20f,500f-d.buttonBounds.last().right,.001f)
 }
 @Test fun oldBackupsRestoreAutomaticSizes() {
  val root=Catalog.json.parseToJsonElement(backup(WidgetConfig())).jsonObject.toMutableMap()
  val config=root.getValue("config").jsonObject.toMutableMap();config.remove("sizing")
  fun strip(slot:JsonElement):JsonObject {
   val s=slot.jsonObject.toMutableMap();listOf("widthDp","heightDp","gapDp","aspectRatio").forEach {s.remove(it)}
   s["icon"]=JsonObject(s.getValue("icon").jsonObject.filterKeys {it!="sizeDp"});return JsonObject(s)
  }
  config["logo"]=strip(config.getValue("logo"));config["buttons"]=JsonArray(config.getValue("buttons").jsonArray.map(::strip));root["config"]=JsonObject(config)
  assertEquals(WidgetConfig(),Validation.parse(JsonObject(root).toString()).config)
 }
 @Test fun resizingDoesNotMoveOtherElementsOrTouchPartitions() {
  val base=WidgetConfig();val original=ElementLayout(500f,base)
  val changes=listOf(
   base.copy(buttons=base.buttons.mapIndexed {i,s->if(i==0)s.copy(widthDp=110f,heightDp=40f) else s}),
   base.copy(buttons=base.buttons.mapIndexed {i,s->if(i==0)s.copy(icon=s.icon.copy(sizeDp=80f)) else s}),
   base.copy(logo=base.logo.copy(icon=base.logo.icon.copy(sizeDp=90f))),
   base.copy(sizing=base.sizing.copy(outerWidthDp=400f)),
   base.copy(heightDp=80f,sizing=base.sizing.copy(outerWidthDp=400f,outerRatio=5f,layoutHeightDp=64f))
  )
  changes.forEach {c->val d=ElementLayout(500f,c)
   assertEquals(original.field.centerX,d.field.centerX,0f)
   assertEquals(original.logo.centerX,d.logo.centerX,0f)
   assertEquals(original.textLeft,d.textLeft,0f)
   assertEquals(original.buttonBounds.map {it.centerX},d.buttonBounds.map {it.centerX})
   assertEquals(original.iconBounds.map {it.centerX},d.iconBounds.map {it.centerX})
   assertEquals(original.buttonHits.map {it.left to it.right},d.buttonHits.map {it.left to it.right})
  }
 }
 @Test fun logoAndTextStayAttachedToSearchField() {
  val base=ElementLayout(500f,WidgetConfig())
  val changed=ElementLayout(500f,WidgetConfig(sizing=ElementSizing(fieldWidthDp=250f)))
  assertEquals(base.logo.left-base.field.left,changed.logo.left-changed.field.left,.001f)
  assertEquals(base.textLeft-base.field.left,changed.textLeft-changed.field.left,.001f)
  assertNotEquals(base.logo.centerX,changed.logo.centerX)
  assertEquals(base.buttonBounds,changed.buttonBounds)
  assertEquals(base.buttonHits,changed.buttonHits)
  assertEquals(changed.field.left,changed.logoHit.left,.001f)
 }
 @Test fun invalidSizesAreRejected() {
  for(v in listOf(-1f,0f,257f,Float.NaN,Float.POSITIVE_INFINITY)) {
   assertTrue(runCatching {Validation.config(WidgetConfig(logo=Slot(icon=IconSpec(sizeDp=v))))}.isFailure)
  }
  assertTrue(runCatching {Validation.config(WidgetConfig(sizing=ElementSizing(fieldWidthDp=1001f)))}.isFailure)
  assertTrue(runCatching {Validation.config(WidgetConfig(sizing=ElementSizing(textGapDp=-1f)))}.isFailure)
 }
 @Test fun extremeCombinationsKeepContentAndHitTargetsWithinBounds() {
  val random=Random(61)
  fun value(max:Float,min:Float=1f):Float?=if(random.nextBoolean())null else min+random.nextFloat()*(max-min)
  repeat(2000) {
   val c=WidgetConfig(heightDp=16+random.nextFloat()*240,count=random.nextInt(4),sizing=ElementSizing(value(1000f,16f),value(1000f,16f),value(256f),value(128f,0f),value(128f,0f),value(128f,0f)),logo=Slot(icon=IconSpec(sizeDp=value(256f))),buttons=List(3){Slot(widthDp=value(256f),heightDp=value(256f),gapDp=value(128f,0f),icon=IconSpec(sizeDp=value(256f)))})
   Validation.config(c)
   val w=180+random.nextFloat()*820;val d=ElementLayout(w,c)
   for(b in listOf(d.field,d.logo,d.logoHit,d.inputHit)+d.buttonBounds+d.iconBounds+d.buttonHits) {
    assertTrue("$b at $w x ${c.heightDp}",b.left>=-.002f && b.top>=-.002f && b.right<=w+.002f && b.bottom<=c.heightDp+.002f && b.width>0 && b.height>0)
   }
   assertTrue("text ${d.textLeft}..${d.textRight}, field ${d.field}, config $c",d.textLeft<d.textRight && d.textRight<=w+.002f)
  }
 }
}
