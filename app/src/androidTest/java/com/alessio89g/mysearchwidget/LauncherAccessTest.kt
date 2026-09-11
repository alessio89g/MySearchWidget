package com.alessio89g.mysearchwidget

import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.data.*
import org.junit.Assert.*
import org.junit.Test

class LauncherAccessTest {
 private val ins=InstrumentationRegistry.getInstrumentation()
 private val context=ins.targetContext
 private fun shell(command:String)=ins.uiAutomation.executeShellCommand(command).use {android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().readText().trim()}
 private fun containsText(node:android.view.accessibility.AccessibilityNodeInfo?, text:String):Boolean {
  if(node==null)return false
  if(node.text?.toString()==text || node.contentDescription?.toString()==text)return true
  return (0 until node.childCount).any {containsText(node.getChild(it),text)}
 }
 private fun waitForUi(matches:(android.view.accessibility.AccessibilityNodeInfo)->Boolean) {
  val until=android.os.SystemClock.uptimeMillis()+10000
  while(android.os.SystemClock.uptimeMillis()<until) {
   val root=ins.uiAutomation.rootInActiveWindow
   if(root!=null && matches(root))return
   android.os.SystemClock.sleep(100)
  }
  fail("Expected Home recovery / Settings UI did not appear")
 }
 private fun waitForAccess(expected:Boolean) {
  val until=android.os.SystemClock.uptimeMillis()+10000
  while(android.os.SystemClock.uptimeMillis()<until) {
   if(PinnedShortcuts.hasAccess(context)==expected)return
   android.os.SystemClock.sleep(100)
  }
  assertEquals("Launcher role permission did not settle",expected,PinnedShortcuts.hasAccess(context))
 }
 @Test fun pinnedDynamicShortcutStartsAfterRestoringPreviousLauncher() {
  val previous=shell("cmd role get-role-holders --user 0 android.app.role.HOME").lineSequence().first {it.contains('.')}
  require(Regex("[a-zA-Z0-9_.]+").matches(previous))
  val pkg=context.packageName
  val fixture=ins.context.packageName
  val saved=context.getSharedPreferences("pinned_shortcuts",0).getString("items",null)
  var oldPins=listOf<String>()
  val launcher=context.getSystemService(android.content.pm.LauncherApps::class.java)
  try {
   shell("am start -W -n $fixture/com.alessio89g.mysearchwidget.ShortcutFixtureActivity --ez publish true")
   shell("cmd role add-role-holder --user 0 android.app.role.HOME $pkg")
   waitForAccess(true)
   context.startActivity(android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
   waitForUi {it.packageName?.toString() in setOf("com.android.settings","com.android.permissioncontroller")}
   ins.uiAutomation.waitForIdle(500,5000)
   assertTrue(ins.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK))
   waitForUi {containsText(it,com.alessio89g.mysearchwidget.i18n.tr(R.string.launcher_settings))}
   oldPins=launcher.getShortcuts(android.content.pm.LauncherApps.ShortcutQuery().setPackage(fixture).setQueryFlags(android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED),android.os.Process.myUserHandle()).orEmpty().map {it.id}
   val entries=AppShortcuts.groups(context,fixture).single().entries
   assertTrue(entries.any {it.id=="dynamic_precise" && it.kind=="pinned"})
   for(id in listOf("dynamic_second","dynamic_precise"))PinnedShortcuts.pin(context,Shortcut("pinned","$fixture/$id",id))
   assertTrue(PinnedShortcuts.saved(context).map {it.value}.containsAll(listOf("$fixture/dynamic_second","$fixture/dynamic_precise")))
   shell("cmd role add-role-holder --user 0 android.app.role.HOME $previous")
   waitForAccess(false)
   assertTrue(PinnedShortcuts.launch(context,"$fixture/dynamic_precise"))
   val until=android.os.SystemClock.uptimeMillis()+5000
   while(android.os.SystemClock.uptimeMillis()<until) {
    if(ins.uiAutomation.rootInActiveWindow?.findAccessibilityNodeInfosByText("Pinned dynamic shortcut launched")?.isNotEmpty()==true)return
    android.os.SystemClock.sleep(100)
   }
   fail("The pinned dynamic destination did not appear after restoring Home")
  } finally {
   shell("cmd role add-role-holder --user 0 android.app.role.HOME $pkg")
   waitForAccess(true)
   launcher.pinShortcuts(fixture,oldPins,android.os.Process.myUserHandle())
   context.getSharedPreferences("pinned_shortcuts",0).edit().putString("items",saved).commit()
   shell("cmd role add-role-holder --user 0 android.app.role.HOME $previous")
  }
 }
}
