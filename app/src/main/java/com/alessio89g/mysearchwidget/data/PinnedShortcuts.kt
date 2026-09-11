package com.alessio89g.mysearchwidget.data

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import kotlinx.serialization.encodeToString

/** Pins belong to this app, not to the user's previous launcher. Never replace its pins. */
object PinnedShortcuts {
 fun hasAccess(context:Context)=runCatching {context.getSystemService(LauncherApps::class.java).hasShortcutHostPermission()}.getOrDefault(false)
 private fun store(context:Context)=context.getSharedPreferences("pinned_shortcuts",Context.MODE_PRIVATE)
 fun saved(context:Context):List<Shortcut> = runCatching {
  Catalog.json.decodeFromString<List<Shortcut>>(store(context).getString("items","[]")!!)
 }.getOrDefault(emptyList())
 fun published(context:Context,pkg:String):List<AppShortcuts.Entry> {
  if(!hasAccess(context))return emptyList()
  val query=LauncherApps.ShortcutQuery().setPackage(pkg).setQueryFlags(
   LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
  return context.getSystemService(LauncherApps::class.java).getShortcuts(query,Process.myUserHandle()).orEmpty()
   .filter {it.isEnabled}.map {AppShortcuts.Entry(it.id,(it.longLabel ?: it.shortLabel ?: it.id).toString(),emptyList(),kind="pinned")}
 }
 fun pin(context:Context,shortcut:Shortcut) {
  require(shortcut.kind=="pinned")
  val pkg=shortcut.value.substringBefore('/');val id=shortcut.value.substringAfter('/')
  if(!hasAccess(context)) {
   require(saved(context).any {it.value==shortcut.value})
   return
  }
  val launcher=context.getSystemService(LauncherApps::class.java)
  val existing=launcher.getShortcuts(LauncherApps.ShortcutQuery().setPackage(pkg).setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED),Process.myUserHandle()).orEmpty().map {it.id}
  launcher.pinShortcuts(pkg,(existing+id).distinct(),Process.myUserHandle())
  check(launcher.getShortcuts(LauncherApps.ShortcutQuery().setPackage(pkg).setShortcutIds(listOf(id)).setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED),Process.myUserHandle()).orEmpty().any {it.id==id && it.isPinned})
  store(context).edit().putString("items",Catalog.json.encodeToString(saved(context).filterNot {it.value==shortcut.value}+shortcut)).apply()
 }
 fun launch(context:Context,value:String):Boolean = runCatching {
  // No hasShortcutHostPermission gate: Android permits pins owned by a former launcher.
  context.getSystemService(LauncherApps::class.java).startShortcut(value.substringBefore('/'),value.substringAfter('/'),null,null,Process.myUserHandle())
  true
 }.getOrDefault(false)
}
