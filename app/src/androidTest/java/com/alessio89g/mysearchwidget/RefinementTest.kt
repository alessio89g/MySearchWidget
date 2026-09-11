package com.alessio89g.mysearchwidget
import android.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.*
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.roundToInt

class RefinementTest {
 private val context=InstrumentationRegistry.getInstrumentation().targetContext
 @Test fun shortcutsReadExtrasAndExcludeDisabledAndPrivateActivities() {
  val group=AppShortcuts.groups(context,"com.alessio89g.mysearchwidget.test").single()
  assertEquals(listOf("precise"),group.entries.filter{it.unavailable==null}.map{it.id})
  assertNotNull(group.entries.first {it.id=="private"}.unavailable)
  val entry=group.entries.first {it.id=="precise"}
  assertEquals("Azione precisa",entry.label)
  assertEquals("com.alessio89g.TEST_PRECISE",entry.intents.single().action)
  assertEquals("precise-mode",entry.intents.single().getStringExtra("mode"))
 }
 @Test fun chromeUsesPublicIncognitoEntryForStableAndBeta() {
  for(pkg in listOf("com.android.chrome","com.chrome.beta")) {
   val intent=AppShortcuts.chromeEntries(pkg).single().intents.single()
   assertEquals("org.chromium.chrome.browser.incognito.OPEN_PRIVATE_TAB",intent.action)
   assertEquals("org.chromium.chrome.browser.incognito.IncognitoTabLauncher",intent.component!!.className)
   assertEquals(pkg,intent.component!!.packageName)
  }
 }
 @Test fun launcherAppsWithoutShortcutsAreExcluded() {
  assertTrue(AppShortcuts.groups(context,context.packageName).isEmpty())
 }
 @Test fun runtimeEnabledComponentIsSelectableDespiteDisabledManifestDefault() {
  val own=InstrumentationRegistry.getInstrumentation().context
  val component=android.content.ComponentName(own.packageName,"com.alessio89g.mysearchwidget.RuntimeShortcutFixtureActivity")
  val pm=own.packageManager
  val automation=InstrumentationRegistry.getInstrumentation().uiAutomation
  automation.adoptShellPermissionIdentity("android.permission.CHANGE_COMPONENT_ENABLED_STATE")
  try {
   pm.setComponentEnabledSetting(component,android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,android.content.pm.PackageManager.DONT_KILL_APP)
   val entry=AppShortcuts.groups(context,own.packageName).single().entries.first {it.id=="runtime"}
   assertNull(entry.unavailable)
  } finally {
   try {pm.setComponentEnabledSetting(component,android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,android.content.pm.PackageManager.DONT_KILL_APP)}
   finally {automation.dropShellPermissionIdentity()}
  }
 }
 @Test fun materialYouOverridesManualColorsWithoutErasingThem() {
  val c=WidgetConfig(dynamic=true,theme="dark",outer=Surface(dark=Tone("#FF0000")))
  val automatic=Renderer.render(context,c,356)
  val manual=Renderer.render(context,c.copy(dynamic=false),356)
  val density=context.resources.displayMetrics.density.coerceAtMost(3f)
  val x=(178*density).roundToInt();val y=(3*density).roundToInt()
  assertEquals(Color.RED,manual.getPixel(x,y))
  assertNotEquals(Color.RED,automatic.getPixel(x,y))
  assertEquals("#FF0000",c.outer.dark.color)
  assertEquals((64*density).roundToInt(),automatic.height)
 }
 @Test fun circleColorDoesNotChangeIconOrNeighbourAndSurvivesBackup() {
  val original=WidgetConfig(dynamic=false,theme="dark")
  val first=original.buttons[0].copy(surface=Surface(dark=Tone("#FF0000")),icon=original.buttons[0].icon.copy(dark="#00FF00"))
  val c=original.copy(buttons=listOf(first)+original.buttons.drop(1))
  val bitmap=Renderer.render(context,c,356)
  val density=context.resources.displayMetrics.density.coerceAtMost(3f)
  val g=Geometry(356f,2)
  assertEquals(Color.RED,bitmap.getPixel((g.center(0)*density).roundToInt(),(12*density).roundToInt()))
  assertEquals(Color.GREEN,bitmap.getPixel((g.center(0)*density).roundToInt(),(32*density).roundToInt()))
  assertEquals(Color.BLACK,bitmap.getPixel((g.center(1)*density).roundToInt(),(12*density).roundToInt()))
  assertEquals(c,Validation.parse(Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first()))).config)
 }
 @Test fun legacyBackupStillLoadsWithoutOfferingRetiredFunctions() {
  val c=WidgetConfig(outer=Surface(dark=Tone(blur=40f)),logo=Slot(tap=Shortcut("builtin","gemini"),up=Shortcut("builtin","ai")))
  val parsed=Validation.parse(Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first())))
  assertEquals(c,parsed.config)
  assertFalse(Catalog.functions.containsKey("gemini"))
  assertFalse(Catalog.functions.containsKey("ai"))
 }
 @Test fun staticShortcutSurvivesBackupRoundTrip() {
  val c=WidgetConfig(logo=Slot(tap=Shortcut("static","com.example.app/action","Azione")))
  assertEquals(c,Validation.parse(Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first()))).config)
 }
}
