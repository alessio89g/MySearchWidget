package com.alessio89g.mysearchwidget.data

import org.junit.Test
import org.junit.Assert.*
import kotlinx.serialization.encodeToString

class GeometryResetTest {
 private val config=WidgetConfig(
  heightDp=80f,sizing=ElementSizing(outerWidthDp=400f,fieldWidthDp=180f,fieldHeightDp=60f,outerRatio=5f,fieldRatio=3f,outerLocked=false),
  placement=Placement(outer=OffsetDp(4f,9f),field=OffsetDp(-8f,20f),logo=OffsetDp(2f,3f),text=OffsetDp(10f,11f),fieldLinked=false),
  placeholder="Example",hint=TextStyle(size=22f,weight=700),query=TextStyle(size=20f,italic=true),hintRuns=listOf(TextRun(0,3,TextStyle(size=30f,underline=true))),
  logo=Slot(icon=IconSpec(sizeDp=50f)),buttons=List(3){Slot(widthDp=70f,heightDp=35f,aspectRatio=2f,position=OffsetDp(20f,30f),iconPosition=OffsetDp(-10f,-4f),iconLinked=false,icon=IconSpec(sizeDp=40f))}
 )
 @Test fun positionResetNeverChangesDimensionsOrOtherElements() {
  DEFAULT_LAYERS.forEach {id->
   val result=config.resetElementPosition(id)
   assertEquals(config.heightDp,result.heightDp,0f);assertEquals(config.sizing,result.sizing)
   assertEquals(config.hint,result.hint);assertEquals(config.query,result.query)
   assertEquals(config.placement.fieldLinked,result.placement.fieldLinked)
   when(id) {
    "outer"->assertEquals(config.copy(placement=config.placement.copy(outer=OffsetDp())),result)
    "field"->assertEquals(config.copy(placement=config.placement.copy(field=OffsetDp())),result)
    "logo"->assertEquals(config.copy(placement=config.placement.copy(logo=OffsetDp())),result)
    "text"->assertEquals(config.copy(placement=config.placement.copy(text=OffsetDp())),result)
    else->{val i=id.last().digitToInt();val old=config.buttons[i]
     val reset=if(id.startsWith("button"))old.copy(position=OffsetDp()) else old.copy(iconPosition=OffsetDp())
     assertEquals(config.buttons.mapIndexed {j,s->if(j==i)reset else s},result.buttons)
     assertEquals(config.placement,result.placement)
    }
   }
  }
 }
 @Test fun dimensionResetPreservesOffsetsAndSeparatesButtonFromIcon() {
  DEFAULT_LAYERS.forEach {id->
   val result=config.resetElementDimensions(id)
   assertEquals(config.placement,result.placement)
   assertEquals(config.buttons.map {it.position to it.iconPosition},result.buttons.map {it.position to it.iconPosition})
   assertEquals(config.buttons.map {it.iconLinked},result.buttons.map {it.iconLinked})
   Validation.config(result)
   assertEquals(result,Validation.parse(Catalog.json.encodeToString(Backup(1,result,Catalog.engines.first()))).config)
  }
  val button=config.resetElementDimensions("button1")
  assertNull(button.buttons[1].widthDp);assertNull(button.buttons[1].heightDp);assertNull(button.buttons[1].aspectRatio)
  assertEquals(config.buttons[1].icon,button.buttons[1].icon)
  assertEquals(config.buttons[0],button.buttons[0]);assertEquals(config.buttons[2],button.buttons[2])
  val icon=config.resetElementDimensions("icon1")
  assertEquals(config.buttons[1].copy(icon=config.buttons[1].icon.copy(sizeDp=null)),icon.buttons[1])
 }
 @Test fun outerFieldAndTextRestoreTheirDefaults() {
  val outer=config.resetElementDimensions("outer")
  assertEquals(64f,outer.heightDp,0f);assertNull(outer.sizing.outerWidthDp);assertNull(outer.sizing.outerRatio)
  assertFalse(outer.sizing.outerLocked);assertEquals(80f,outer.sizing.layoutHeightDp!!,0f)
  val field=config.resetElementDimensions("field")
  assertNull(field.sizing.fieldWidthDp);assertNull(field.sizing.fieldHeightDp);assertNull(field.sizing.fieldRatio)
  assertEquals(config.heightDp,field.heightDp,0f)
  val text=config.resetElementDimensions("text")
  assertEquals(config.hint.copy(size=15f),text.hint);assertEquals(config.query.copy(size=15f),text.query)
  assertEquals(config.hintRuns.map {it.copy(style=it.style.copy(size=15f))},text.hintRuns)
  assertEquals(config.placeholder,text.placeholder)
 }
}
