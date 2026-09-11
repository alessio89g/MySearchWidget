package com.alessio89g.mysearchwidget.data

import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.*

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.util.Xml
import org.xmlpull.v1.XmlPullParser

/** Resolve again from the installed app on every launch; never trust an imported Intent URI. */
object AppShortcuts {
 private const val ANDROID="http://schemas.android.com/apk/res/android"
 data class Entry(val id:String,val label:String,val intents:List<Intent>,val unavailable:String?=null,val kind:String="static")
 data class Group(val pkg:String,val label:String,val entries:List<Entry>)
 fun groups(context:Context,onlyPackage:String?=null):List<Group> {
  val pm=context.packageManager
  val query=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).apply {if(onlyPackage!=null)setPackage(onlyPackage)}
  return pm.queryIntentActivities(query,PackageManager.GET_META_DATA).groupBy {it.activityInfo.packageName}.mapNotNull { (pkg,activities)->
   val parsed=activities.flatMap {info->runCatching {
    val resources=pm.getResourcesForApplication(pkg)
    val infoWithMetadata=info.activityInfo
    val owners=listOfNotNull(infoWithMetadata,infoWithMetadata.targetActivity?.let {target->
     pm.getActivityInfo(android.content.ComponentName(pkg,target),PackageManager.GET_META_DATA)
    }).distinctBy {it.name}
    owners.flatMap {it.loadXmlMetaData(pm,"android.app.shortcuts")?.use {xml->parse(resources,xml)}.orEmpty()}
   }.getOrDefault(emptyList())}
   // LauncherShortcutActivity is private in Chrome. Use its public IncognitoTabLauncher.
   val known=chromeEntries(pkg)
   val published=runCatching {PinnedShortcuts.published(context,pkg)}.getOrDefault(emptyList())
   val cached=PinnedShortcuts.saved(context).filter {it.value.substringBefore('/')==pkg}.map {Entry(it.value.substringAfter('/'),it.label.substringAfter(" · ",it.label),emptyList(),kind="pinned")}
   val entries=(published+cached+parsed+known).distinctBy {it.id}.map {entry->entry.copy(unavailable=entry.intents.firstNotNullOfOrNull {unavailable(context,it)})}
   Group(pkg,pm.getApplicationLabel(activities.first().activityInfo.applicationInfo).toString(),entries)

  }.filter {it.entries.isNotEmpty()}.sortedBy {it.label.lowercase()}
 }
 fun parse(resources:Resources,xml:XmlResourceParser):List<Entry> {
  val entries=mutableListOf<Entry>();var id="";var label="";var enabled=false;var intents=mutableListOf<Intent>()
  while(xml.next()!=XmlPullParser.END_DOCUMENT) {
   if(xml.eventType==XmlPullParser.START_TAG && xml.name=="shortcut") {
    id=xml.getAttributeValue(ANDROID,"shortcutId").orEmpty()
    val labelId=xml.getAttributeResourceValue(ANDROID,"shortcutLongLabel",0).takeIf {it!=0} ?: xml.getAttributeResourceValue(ANDROID,"shortcutShortLabel",0)
    label=if(labelId!=0)resources.getString(labelId) else id
    enabled=xml.getAttributeBooleanValue(ANDROID,"enabled",true);intents=mutableListOf()
   } else if(xml.eventType==XmlPullParser.START_TAG && xml.name=="intent") {
    intents.add(Intent.parseIntent(resources,xml,Xml.asAttributeSet(xml)))
   } else if(xml.eventType==XmlPullParser.END_TAG && xml.name=="shortcut" && enabled && id.isNotEmpty())entries.add(Entry(id,label,intents.toList()))
  }
  return entries
 }
 fun chromeEntries(pkg:String):List<Entry> = if(pkg in setOf("com.android.chrome","com.chrome.beta","com.chrome.dev","com.chrome.canary","org.chromium.chrome"))
  listOf(Entry("mysearchwidget.chrome.incognito",tr(R.string.incognito),listOf(
   Intent("org.chromium.chrome.browser.incognito.OPEN_PRIVATE_TAB").setClassName(pkg,"org.chromium.chrome.browser.incognito.IncognitoTabLauncher")
  ))) else emptyList()
 private fun unavailable(context:Context,intent:Intent):String? {
  val pm=context.packageManager
  val activity=pm.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo
   ?: return tr(R.string.shortcut_unavailable)
  // Resolution without MATCH_DISABLED_COMPONENTS already checks runtime enabled overrides.
  // ActivityInfo.enabled alone can describe the manifest default (Chrome defaults to false).
  if(!activity.exported)return tr(R.string.shortcut_private)
  if(activity.permission!=null && context.checkSelfPermission(activity.permission)!=PackageManager.PERMISSION_GRANTED)return tr(R.string.shortcut_permission)
  return null
 }
 fun launch(activity:Activity,value:String):Boolean=runCatching {
  val pkg=value.substringBefore('/');val id=value.substringAfter('/')
  val entry=groups(activity,pkg).flatMap {it.entries}.firstOrNull {it.id==id && it.unavailable==null && it.intents.isNotEmpty()} ?: return false
  val intents=entry.intents.map {Intent(it)}.toTypedArray()
  intents[0].addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
  activity.startActivities(intents);true
 }.getOrDefault(false)
}
