package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class OptionResetTest {
 private val defaults=Catalog.json.decodeFromString<WidgetConfig>(File("src/main/res/raw/default_widget.json").readText())
 @Test fun colorsResetIndependentlyWithoutChangingGradientOrOtherElements() {
  val c=defaults.copy(field=defaults.field.copy(dark=defaults.field.dark.copy(color="#123456",opacity=.3f,gradient=Gradient(enabled=true,angle=75f))))
  val r=OptionReset.apply(c,defaults,listOf("field.dark.color"))
  assertEquals(defaults.field.dark.color,r.field.dark.color)
  assertEquals(c.field.dark.gradient,r.field.dark.gradient)
  assertEquals(.3f,r.field.dark.opacity,0f)
  assertEquals(c.outer,r.outer);assertEquals(c.buttons,r.buttons)
  Validation.config(r)
 }
 @Test fun gradientAngleDoesNotResetColorsOrEnabledFlag() {
  val c=defaults.copy(hint=defaults.hint.copy(darkGradient=Gradient(true,"#123456","#654321",90f)))
  val r=OptionReset.apply(c,defaults,listOf("hint.darkGradient.angle"))
  assertEquals(c.hint.darkGradient.copy(angle=defaults.hint.darkGradient.angle),r.hint.darkGradient)
 }
 @Test fun formattingResetsOnlyChosenAttributeInEveryRange() {
  val c=defaults.copy(placeholder="hello",hint=defaults.hint.copy(weight=700,italic=true),hintRuns=listOf(TextRun(0,3,defaults.hint.copy(weight=900,italic=true,dark="#123456"))))
  val r=OptionReset.apply(c,defaults,listOf("hint.weight"))
  assertEquals(defaults.hint.weight,r.hint.weight)
  assertEquals(defaults.hint.weight,r.hintRuns[0].style.weight)
  assertTrue(r.hintRuns[0].style.italic);assertEquals("#123456",r.hintRuns[0].style.dark)
  assertEquals(c.placeholder,r.placeholder);Validation.config(r)
 }
 @Test fun iconRestoresEmbeddedAssetWithoutResettingItsSizeOrStyle() {
  val c=defaults.copy(assets=emptyMap(),buttons=defaults.buttons.map {it.copy(icon=it.icon.copy(asset="",name="Search",sizeDp=40f,outline=true))})
  val r=OptionReset.apply(c,defaults,listOf("buttons.2.icon.name","buttons.2.icon.asset"))
  assertEquals(defaults.buttons[2].icon.asset,r.buttons[2].icon.asset)
  assertEquals(defaults.assets[r.buttons[2].icon.asset],r.assets[r.buttons[2].icon.asset])
  assertEquals(40f,r.buttons[2].icon.sizeDp!!,0f);assertTrue(r.buttons[2].icon.outline)
 }
 @Test fun sharedAndSeparateModesUseNormalEditorSemantics() {
  var c=defaults.copy(query=defaults.query.copy(italic=true))
  c=ModeSettings.toggle(c,"text.query",true,false)
  val draft=ModeSettings.resolve(c,true)
  val edited=ModeSettings.edit(c,OptionReset.apply(draft,defaults,listOf("query.italic")),true)
  assertTrue(ModeSettings.resolve(edited,false).query.italic)
  assertFalse(ModeSettings.resolve(edited,true).query.italic)
  val shared=defaults.copy(query=defaults.query.copy(italic=true))
  val reset=ModeSettings.edit(shared,OptionReset.apply(ModeSettings.resolve(shared,true),defaults,listOf("query.italic")),true)
  assertFalse(ModeSettings.resolve(reset,false).query.italic)
  Validation.config(edited);Validation.config(reset)
 }
 @Test fun hapticAndActionResetAreIndependent() {
  val c=defaults.copy(haptics=mapOf("button0" to false,"button1" to false),buttons=defaults.buttons.map {it.copy(tap=Shortcut("none"))})
  val h=OptionReset.apply(c,defaults,listOf("haptics.button0"))
  assertTrue(h.hapticEnabled("button0"));assertFalse(h.hapticEnabled("button1"));assertEquals(c.buttons,h.buttons)
  val a=OptionReset.apply(c,defaults,listOf("buttons.0.tap"))
  assertEquals(defaults.buttons[0].tap,a.buttons[0].tap);assertEquals(c.haptics,a.haptics)
 }
 @Test fun singleAxisAndLayerResetsPreserveOtherProperties() {
  val c=defaults.copy(placement=defaults.placement.copy(logo=OffsetDp(20f,30f),layers=defaults.placement.layers.reversed()))
  val r=OptionReset.apply(c,defaults,listOf("placement.logo.x","layer:logo"))
  assertEquals(30f,r.placement.logo.y,0f);assertEquals(defaults.placement.logo.x,r.placement.logo.x,0f)
  assertEquals(defaults.placement.layers.indexOf("logo"),r.placement.layers.indexOf("logo"))
  assertEquals(c.placement.layers.filter {it!="logo"},r.placement.layers.filter {it!="logo"})
 }
 @Test fun adaptiveFieldResetKeepsAutomaticWidth() {
  val c=defaults.withButtonCount(1,defaults).copy(sizing=defaults.sizing.copy(fieldWidthDp=150f))
  val r=OptionReset.apply(c,defaults,listOf("sizing.fieldWidthDp"))
  assertNull(r.sizing.fieldWidthDp);assertEquals(1,r.count)
 }
 @Test fun resetIsUndoableAndDoesNotMutateOriginal() {
  val c=defaults.copy(theme="dark")
  val h=EditHistory(c).record(OptionReset.apply(c,defaults,listOf("theme")))
  assertEquals("dark",c.theme);assertEquals(c,h.undo().present);assertEquals(defaults.theme,h.present.theme)
 }
 @Test fun placeholderResetUsesCurrentCreationPresetAndPreservesStyle() {
  assertEquals("",defaults.placeholder)
  val saved=defaults.copy(placeholder="Personal text",engineId="bing",hint=defaults.hint.copy(weight=700,italic=true))
  val reset=OptionReset.apply(saved,defaults,listOf("placeholder"))
  assertEquals("",reset.placeholder)
  assertEquals("bing",reset.engineId)
  assertEquals(saved.hint,reset.hint)
  assertEquals("Personal text",saved.placeholder)
  val futurePreset=defaults.copy(placeholder="Future default")
  assertEquals(futurePreset.placeholder,OptionReset.apply(saved,futurePreset,listOf("placeholder")).placeholder)
 }

}
