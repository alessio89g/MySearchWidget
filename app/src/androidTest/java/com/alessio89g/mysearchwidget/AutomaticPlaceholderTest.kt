package com.alessio89g.mysearchwidget

import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.i18n.*
import com.alessio89g.mysearchwidget.widget.Renderer
import org.junit.Assert.*
import org.junit.Test

class AutomaticPlaceholderTest {
 private val context=InstrumentationRegistry.getInstrumentation().targetContext
 @Test fun engineAndLanguageResolveWithoutChangingStoredFormatting() {
  val language=AppLanguage.code
  try {
   val style=TextStyle(size=18f,weight=700,italic=true,underline=true,darkGradient=Gradient(true))
   val config=WidgetConfig(engineId="bing",dynamic=false,hint=style,theme="dark")
   AppLanguage.select("it")
   assertEquals("Cerca con Bing",placeholderText(config))
   val custom=config.copy(engineId="personal")
   val engine=Engine("personal","Qwant","https://www.qwant.com/?q=%s")
   assertEquals("Cerca con Qwant",placeholderText(custom,engine))
   assertEquals("Cerca con Qwant New",placeholderText(custom,engine.copy(name="Qwant New")))
   val automatic=Renderer.render(context,custom,374,engine=engine)
   val explicit=Renderer.render(context,custom.copy(placeholder="Cerca con Qwant"),374,engine=engine)
   assertTrue("Automatic text must retain the same formatting",automatic.sameAs(explicit))
   AppLanguage.select("en")
   assertEquals("Search with Qwant",placeholderText(custom,engine))
   assertEquals("Search with Google",placeholderText(custom.copy(googleInput=true),engine))
   assertEquals("",config.placeholder);assertEquals(style,config.hint);assertEquals(emptyList<TextRun>(),config.hintRuns)
  }finally {AppLanguage.select(language)}
 }
 @Test fun customTextAndRangesOverrideEngineAndLanguage() {
  val language=AppLanguage.code
  try {
   val custom=WidgetConfig(placeholder="My search",hintRuns=listOf(TextRun(0,2,TextStyle(weight=700,dark="#FF0000"))))
   AppLanguage.select("en")
   val first=Renderer.render(context,custom,374)
   AppLanguage.select("it")
   val changed=custom.copy(engineId="bing",googleInput=true)
   assertEquals("My search",placeholderText(changed))
   assertTrue(first.sameAs(Renderer.render(context,changed,374)))
   assertEquals(custom.hintRuns,changed.hintRuns)
  }finally {AppLanguage.select(language)}
 }
}
