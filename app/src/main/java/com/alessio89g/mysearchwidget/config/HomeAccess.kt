package com.alessio89g.mysearchwidget.config

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.ComponentName
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.*

object HomeAccess {
 fun rememberPrevious(context:Context) {
  val home=context.packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo ?: return
  if(home.packageName!=context.packageName && home.packageName!="android")context.getSharedPreferences("interface",0).edit().putString("previous_home",ComponentName(home.packageName,home.name).flattenToString()).apply()
 }
 fun request(context:android.app.Activity) {
  rememberPrevious(context)
  runCatching {context.startActivityForResult(context.getSystemService(RoleManager::class.java).createRequestRoleIntent(RoleManager.ROLE_HOME),901)}.onFailure {settings(context)}
 }
 fun settings(context:Context) {
  if(runCatching {context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK));true}.getOrDefault(false))return
  if(runCatching {context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));true}.getOrDefault(false))return
  runCatching {context.startActivity(Intent(Settings.ACTION_SETTINGS))}.onFailure {Toast.makeText(context,tr(R.string.launcher_error),Toast.LENGTH_LONG).show()}
 }
 fun previous(context:Context) {
  val component=context.getSharedPreferences("interface",0).getString("previous_home",null)?.let {ComponentName.unflattenFromString(it)}
  if(component==null || component.packageName==context.packageName){settings(context);return}
  runCatching {context.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setComponent(component))}.onFailure {settings(context)}
 }
}

/** A recovery screen, never a blank Home. HOME opens the exact default-home settings. */
class HomeRecoveryActivity:ComponentActivity() {
 override fun onCreate(state:Bundle?) {
  super.onCreate(state)
  setContent {MaterialTheme {
   Surface(Modifier.fillMaxSize()) {Column(Modifier.safeDrawingPadding().padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
    Text("MySearchWidget",style=MaterialTheme.typography.headlineMedium)
    Text(tr(R.string.launcher_safety))
    Button(onClick={HomeAccess.settings(this@HomeRecoveryActivity)}){Text(tr(R.string.launcher_settings))}
    OutlinedButton(onClick={startActivity(Intent(this@HomeRecoveryActivity,ConfigActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}){Text(tr(R.string.open_config))}
    OutlinedButton(onClick={HomeAccess.previous(this@HomeRecoveryActivity)}){Text(tr(R.string.previous_launcher))}
   }}
  }}
  if(state==null)HomeAccess.settings(this)
 }
 // Returning from Settings must reveal recovery controls, not reopen Settings in a loop.
 override fun onNewIntent(intent:Intent) {super.onNewIntent(intent)}
}
