package com.alessio89g.mysearchwidget

import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.*
import org.junit.Assert.*
import org.junit.Test

class FormattingTest {
 private val context=InstrumentationRegistry.getInstrumentation().targetContext
 @Test fun styledPlaceholderRangesWithGradientsRenderWithoutRecursiveMeasurement() {
  val g=Gradient(true,"#FF0000","#0000FF",137.5f)
  val style=TextStyle(size=24f,weight=700,italic=true,underline=true,strike=true,darkGradient=g,lightGradient=g)
  for(theme in listOf("dark","light"))for(width in listOf(180,356,600))for(text in listOf("Alpha Beta","A long formatted placeholder that must be ellipsized","العربية مرحبا 👋")) {
   val c=WidgetConfig(dynamic=false,theme=theme,placeholder=text,hintRuns=listOf(TextRun(0,minOf(8,text.length),style)))
   val image=Renderer.render(context,c,width)
   assertTrue(image.width>0);image.recycle()
  }
 }
 @Test fun partialFormattingSurvivesTextInsertionAndDeletion() {
  val style=TextStyle(weight=700,italic=true)
  val runs=RichText.apply(emptyList(),2,5,style)
  val inserted=RichText.edit("abcdef","abXYcdef",runs,TextStyle())
  assertEquals(style,RichText.styleAt(inserted,4,TextStyle()))
  assertEquals(TextStyle(),RichText.styleAt(inserted,0,TextStyle()))
  val deleted=RichText.edit("abcdef","abf",runs,TextStyle())
  assertTrue(deleted.isEmpty())
 }
 @Test fun bbcodeRoundTripRetainsGradientsAndBothThemeStyles() {
  val style=TextStyle(size=23f,weight=700,italic=true,underline=true,strike=true,dark="#FF0000",light="#0000FF",darkGradient=Gradient(true,angle=137.5f))
  val text="a & b < c [x] \\ y"
  val runs=listOf(TextRun(2,5,style))
  val (parsed,restored)=RichText.parse(RichText.bbcode(text,runs,TextStyle()),TextStyle())
  assertEquals(text,parsed)
  for(i in text.indices)assertEquals(RichText.styleAt(runs,i,TextStyle()),RichText.styleAt(restored,i,TextStyle()))
  val simple=RichText.parse("[b]Bold[/b] [i][u]test[/u][/i]",TextStyle())
  assertEquals("Bold test",simple.first)
  assertEquals(700,RichText.styleAt(simple.second,0,TextStyle()).weight)
  assertTrue(RichText.styleAt(simple.second,6,TextStyle()).underline)
 }
 @Test fun gradientsAffectSurfacesIconsAndTextButRespectMaterialYou() {
  val c=WidgetConfig(dynamic=false,theme="dark",placeholder="Gradient")
  val g=Gradient(true,"#FF0000","#0000FF",0f)
  val changes=listOf(c.copy(outer=c.outer.copy(dark=c.outer.dark.copy(gradient=g))),
   c.copy(logo=c.logo.copy(icon=c.logo.icon.copy(darkGradient=g))),
   c.copy(hint=c.hint.copy(darkGradient=g)),
   c.copy(hintRuns=listOf(TextRun(0,4,c.hint.copy(weight=700,italic=true,underline=true,strike=true,size=24f)))))
  val original=Renderer.render(context,c,356)
  for(changed in changes) {
   val actual=Renderer.render(context,changed,356)
   assertFalse(actual.sameAs(original));actual.recycle()
  }
  val dynamic=Renderer.render(context,c.copy(dynamic=true),356)
  val ignored=Renderer.render(context,changes[0].copy(dynamic=true),356)
  assertTrue(dynamic.sameAs(ignored))
  listOf(original,dynamic,ignored).forEach {it.recycle()}
 }
 @Test fun bbcodeAcceptsReadableGradientsAndRejectsInvalidTagsWithoutLosingLiteralText() {
  val (text,runs)=RichText.parse("[b][gradient=#FF0000,#0000FF,45]Hello[/gradient][/b] <b>literal</b>",TextStyle())
  assertEquals("Hello <b>literal</b>",text)
  val style=RichText.styleAt(runs,0,TextStyle())
  assertEquals(700,style.weight)
  assertEquals(Gradient(true,"#FF0000","#0000FF",45f),style.darkGradient)
  assertEquals(style.darkGradient,style.lightGradient)
  for(source in listOf("[b]unclosed","[b]x[/i]","[size=NaN]x[/size]","[gradient=#FF0000,#0000FF,361]x[/gradient]","[unknown]x[/unknown]")){
   assertTrue(source,runCatching {RichText.parse(source,TextStyle())}.isFailure)
  }
  val saved=TextStyle(lightGradient=Gradient(false,"#112233","#445566",55f),darkGradient=Gradient(true,angle=230f),font="my-font",weight=500)
  val encoded=RichText.bbcode("Saved",emptyList(),saved)
  assertEquals(saved,RichText.parse(encoded,TextStyle(weight=900)).second.single().style)
 }
 @Test fun formattingBackupValidatesAndRoundTrips() {
  val c=WidgetConfig(placeholder="Saved",hintRuns=listOf(TextRun(1,4,TextStyle(strike=true,darkGradient=Gradient(true,angle=270f)))))
  val backup=Backup(1,c,Catalog.engines.first())
  val json=Catalog.json.encodeToString(Backup.serializer(),backup)
  assertEquals(c,Validation.parse(json).config)
  assertTrue(runCatching {Validation.config(c.copy(hintRuns=listOf(TextRun(0,100,TextStyle()))))}.isFailure)
  assertTrue(runCatching {Validation.config(c.copy(query=TextStyle(darkGradient=Gradient(angle=Float.NaN))))}.isFailure)
 }
 @Test fun queryStyleIsAppliedToChangingTextWithoutParsingUserInputAsHtml() {
  val c=WidgetConfig(dynamic=false,query=TextStyle(italic=true,underline=true,strike=true,darkGradient=Gradient(true)))
  for(text in listOf("Short","A much longer query with <b>literal tags</b>","Emoji 👋 and العربية")){
   val image=Renderer.render(context,c,356,SessionText(text))
   assertTrue(image.width>0);image.recycle()
  }
 }
}
