package com.alessio89g.mysearchwidget.overlay

import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.*

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import com.alessio89g.mysearchwidget.data.*

object Actions {
 const val GOOGLE="com.google.android.googlequicksearchbox"
 private fun start(a:Activity,i:Intent):Boolean=runCatching { a.startActivity(i);true }.getOrDefault(false)
 fun openSearch(a:Activity,url:String) {
  // Selector limits resolution to browser activities while leaving package unset.
  // Plain ACTION_VIEW of google.com may otherwise be claimed by the Google app.
  val i=Intent(Intent.ACTION_VIEW,Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)
  i.selector=Intent(Intent.ACTION_VIEW,Uri.parse("https://")).addCategory(Intent.CATEGORY_BROWSABLE)
  if(!start(a,i)) Toast.makeText(a,tr(R.string.install_browser),Toast.LENGTH_LONG).show()
 }
 // Public Google SearchIntents contract: no query means activate the search UI.
 fun googleInputIntents()=listOf(
  Intent("com.google.android.gms.actions.SEARCH_ACTION").setPackage(GOOGLE),
  Intent(android.app.SearchManager.INTENT_ACTION_GLOBAL_SEARCH).setPackage(GOOGLE)
 ).map {it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)}
 fun openGoogleInput(a:Activity) {
  if(googleInputIntents().none {start(a,it)})Toast.makeText(a,tr(R.string.google_input_unavailable),Toast.LENGTH_LONG).show()
 }
 private fun app(a:Activity,pkg:String) {
  if(a.packageManager.getLaunchIntentForPackage(pkg)?.let { start(a,it) }==true)return
  if(pkg=="com.android.chrome") { openSearch(a,"https://www.google.com/");return }
  if(!start(a,Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=$pkg")))) Toast.makeText(a,tr(R.string.app_not_installed,pkg),Toast.LENGTH_LONG).show()
 }
 private fun unavailable(a:Activity) { Toast.makeText(a,tr(R.string.unavailable_action),Toast.LENGTH_LONG).show() }
 fun execute(a:Activity,s:Shortcut) {
  if(s.kind=="pinned") {if(!PinnedShortcuts.launch(a,s.value))unavailable(a);return}
  if(s.kind=="static") { if(!AppShortcuts.launch(a,s.value))unavailable(a);return }
  if(s.kind=="none")return
  if(s.kind=="app") { app(a,s.value);return }
  when(s.value) {
   // Pixel Search 2.4: WidgetUtils.getVoiceCommand + Constants.VOICE_ACTION.
   "voice" -> if(!start(a,Intent("android.intent.action.VOICE_ASSIST").setPackage(GOOGLE)) && !start(a,Intent(RecognizerIntent.ACTION_WEB_SEARCH))) unavailable(a)
   // Exact ComponentName and action extracted from Pixel Search 2.4 WidgetUtils.
   "lens" -> if(!start(a,Intent(Intent.ACTION_MAIN).setClassName(GOOGLE,"com.google.android.apps.lens.DirectLensYoutubeActivity"))) unavailable(a)
   "music" -> if(!start(a,Intent("$GOOGLE.MUSIC_SEARCH").setPackage(GOOGLE))) unavailable(a)
   in Catalog.retired -> unavailable(a)

  }
 }
}
