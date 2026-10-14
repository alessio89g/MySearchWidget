package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.encodeToString

class ThemeModesTest {
 @Test fun defaultSharesEverythingExceptColors() {
  val c=WidgetConfig()
  assertEquals(COLOR_GROUPS.toSet(),c.modes.separate)
  val edited=ModeSettings.edit(c,c.copy(heightDp=80f,count=3,engineId="bing",placeholder="Hello"),true)
  assertEquals(80f,ModeSettings.resolve(edited,false).heightDp)
  assertEquals(3,ModeSettings.resolve(edited,false).count)
  assertEquals("bing",edited.engineId)
  assertEquals("Hello",edited.placeholder)
 }
 @Test fun enablingFreezesOtherModeAndDisablingCopiesEditedMode() {
  var c=ModeSettings.toggle(WidgetConfig(),"size.outer",true,false)
  c=ModeSettings.edit(c,ModeSettings.resolve(c,false).copy(heightDp=80f),false)
  assertEquals(WidgetDimensions.DEFAULT_HEIGHT,ModeSettings.resolve(c,true).heightDp) // legacy serialized default
  c=ModeSettings.edit(c,ModeSettings.resolve(c,true).copy(heightDp=90f),true)
  assertEquals(80f,c.heightDp)
  assertEquals(90f,ModeSettings.resolve(c,true).heightDp)
  c=ModeSettings.toggle(c,"size.outer",false,true)
  assertEquals(90f,c.heightDp);assertTrue(c.modes.dark.isEmpty())
 }
 @Test fun independentButtonDimensionsPositionsAndActions() {
  var c=WidgetConfig()
  for(key in listOf("size.button1","position.icon1","action.button1"))c=ModeSettings.toggle(c,key,true,false)
  val b=c.buttons[1].copy(widthDp=70f,iconPosition=OffsetDp(10f,5f),tap=Shortcut("builtin","lens"))
  c=ModeSettings.edit(c,ModeSettings.resolve(c,true).copy(buttons=c.buttons.mapIndexed {i,s->if(i==1)b else s}),true)
  assertNull(c.buttons[1].widthDp)
  val dark=ModeSettings.resolve(c,true)
  assertEquals(70f,dark.buttons[1].widthDp);assertEquals(OffsetDp(10f,5f),dark.buttons[1].iconPosition)
  assertEquals("lens",dark.buttons[1].tap.value);assertEquals(c.buttons[0],dark.buttons[0])
  Validation.config(c)
 }
 @Test fun separateTextAndColorsRoundTrip() {
  var c=ModeSettings.toggle(ModeSettings.toggle(WidgetConfig(),"placeholder",true,true),"text.hint",true,true)
  c=ModeSettings.edit(c,ModeSettings.resolve(c,true).copy(placeholder="Dark",hintRuns=listOf(TextRun(0,4,TextStyle(weight=700)))),true)
  val decoded=Catalog.json.decodeFromString<WidgetConfig>(Catalog.json.encodeToString(c))
  assertEquals("",decoded.placeholder);assertEquals("Dark",ModeSettings.resolve(decoded,true).placeholder)
  assertEquals(700,ModeSettings.resolve(decoded,true).hintRuns.single().style.weight)
  Validation.config(decoded)
 }
 @Test fun colorLinkSharesSolidAndGradientWithoutAffectingOthers() {
  var c=ModeSettings.toggle(WidgetConfig(),"color.field",false,true)
  val g=Gradient(true,"#112233","#445566",45f)
  c=ModeSettings.edit(c,c.copy(field=c.field.copy(light=Tone("#ABCDEF",gradient=g))),false)
  assertEquals(c.field.light,c.field.dark)
  assertTrue("color.outer" in c.modes.separate)
 }
 @Test fun perElementLayerChangesAreActuallyApplied() {
  var c=ModeSettings.toggle(WidgetConfig(),"layer.outer",true,false)
  c=ModeSettings.edit(c,ModeSettings.resolve(c,true).moveLayer("outer",true),true)
  assertTrue(ModeSettings.resolve(c,true).placement.layers.indexOf("outer")>ModeSettings.resolve(c,true).placement.layers.indexOf("field"))
  Validation.config(c)
 }
 @Test fun rejectsUnexpectedOverrides() {
  val c=WidgetConfig(modes=ThemeModes(setOf("size.outer"),mapOf("dynamic" to JsonPrimitive(false))))
  assertThrows(IllegalArgumentException::class.java){Validation.config(c)}
 }

 @Test fun spanFontsDimensionsAndColorsStayIndependentFromFormatting() {
  var c=WidgetConfig(placeholder="Hello",hintRuns=listOf(TextRun(0,5,TextStyle(size=18f,weight=400,light="#112233",dark="#445566"))))
  for(key in listOf("text.hint","size.text","font.hint"))c=ModeSettings.toggle(c,key,true,false)
  val dark=ModeSettings.resolve(c,true)
  c=ModeSettings.edit(c,dark.copy(hintRuns=dark.hintRuns.map {it.copy(style=it.style.copy(weight=700,size=25f,dark="#AABBCC"))}),true)
  assertEquals(400,ModeSettings.resolve(c,false).hintRuns.single().style.weight)
  assertEquals(18f,ModeSettings.resolve(c,false).hintRuns.single().style.size)
  assertEquals("#112233",ModeSettings.resolve(c,false).hintRuns.single().style.light)
  val edited=ModeSettings.resolve(c,true).hintRuns.single().style
  assertEquals(700,edited.weight);assertEquals(25f,edited.size);assertEquals("#AABBCC",edited.dark)
  c=ModeSettings.toggle(c,"color.hint",false,true)
  assertEquals("#AABBCC",ModeSettings.resolve(c,false).hintRuns.single().style.light)
  assertEquals(18f,ModeSettings.resolve(c,false).hintRuns.single().style.size)
  Validation.config(c)
 }
 @Test fun hapticsCanBeDifferentAndSharedAgain() {
  var c=ModeSettings.toggle(WidgetConfig(),"action.field",true,false)
  c=ModeSettings.edit(c,ModeSettings.resolve(c,true).copy(haptics=mapOf("field" to false)),true)
  assertTrue(c.hapticEnabled("field"));assertFalse(ModeSettings.resolve(c,true).hapticEnabled("field"))
  c=ModeSettings.toggle(c,"action.field",false,true)
  assertFalse(c.hapticEnabled("field"));Validation.config(c)
 }

 @Test fun linkingAutomaticColorsUsesTheChosenModesActualFallback() {
  val light=ModeSettings.toggle(WidgetConfig(dynamic=false),"color.field",false,false)
  assertEquals("#FFFFFF",light.field.light.color);assertEquals(light.field.light,light.field.dark)
  val dark=ModeSettings.toggle(WidgetConfig(dynamic=false),"color.outer",false,true)
  assertEquals("#474645",dark.outer.light.color);assertEquals(dark.outer.light,dark.outer.dark)
 }

 @Test fun longerDarkPlaceholderKeepsSharedFormattingAndIndependentColors() {
  var c=ModeSettings.toggle(WidgetConfig(placeholder="a"),"placeholder",true,false)
  c=ModeSettings.edit(c,ModeSettings.resolve(c,true).copy(placeholder="abcdefghij"),true)
  val dark=ModeSettings.resolve(c,true)
  c=ModeSettings.edit(c,dark.copy(hintRuns=listOf(TextRun(5,10,TextStyle(size=24f,weight=700,dark="#AA3300")))),true)
  val shown=ModeSettings.resolve(c,true)
  assertEquals("abcdefghij",shown.placeholder)
  assertEquals(700,RichText.styleAt(shown.hintRuns,7,shown.hint).weight)
  assertEquals(24f,RichText.styleAt(shown.hintRuns,7,shown.hint).size)
  assertEquals("#AA3300",RichText.styleAt(shown.hintRuns,7,shown.hint).dark)
  assertEquals("a",ModeSettings.resolve(c,false).placeholder)
  Validation.config(c)
  c=ModeSettings.toggle(c,"color.hint",false,true)
  val shared=ModeSettings.resolve(c,true)
  assertEquals("#AA3300",RichText.styleAt(shared.hintRuns,7,shared.hint).light)
  Validation.config(c)
  assertEquals(c,Catalog.json.decodeFromString<WidgetConfig>(Catalog.json.encodeToString(c)))
 }
 @Test fun relinkingOnlyNeedsChoiceForDifferentValues() {
  var c=ModeSettings.toggle(WidgetConfig(),"size.outer",true,false)
  assertFalse(ModeSettings.hasDifferentValues(c,"size.outer"))
  c=ModeSettings.edit(c,ModeSettings.resolve(c,true).copy(heightDp=99f),true)
  assertTrue(ModeSettings.hasDifferentValues(c,"size.outer"))
  assertEquals(99f,ModeSettings.resolve(ModeSettings.toggle(c,"size.outer",false,true),false).heightDp)
  assertEquals(c.heightDp,ModeSettings.resolve(ModeSettings.toggle(c,"size.outer",false,false),true).heightDp)
  assertTrue(ModeSettings.hasDifferentValues(WidgetConfig(),"color.field"))
 }
}
