package com.alessio89g.mysearchwidget

import android.app.DownloadManager
import android.content.Intent
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.config.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class UpdateInstallerTest {
 private val ins=InstrumentationRegistry.getInstrumentation()
 private val context=ins.targetContext
 @Test fun refusesInstalledVersionAsAnUpdate() {
  assertFalse(UpdateInstaller.valid(context,File(context.applicationInfo.sourceDir)))
 }
 /** Opt-in integration test: downloads the real public APK, never installs it. */
 @Test fun directDownloadCompletesAndOldReleaseIsNotOffered()=runBlocking {
  assumeTrue(InstrumentationRegistry.getArguments().getString("networkUpdates")=="true")
  val release=ReleaseUpdate("v1.13.4","https://github.com/alessio89g/MySearchWidget/releases/download/v1.13.4/MySearchWidget-1.13.4.apk","MySearchWidget-1.13.4.apk")
  val id=ReleaseUpdates.download(context,release);UpdateInstaller.remember(context,id)
  val manager=context.getSystemService(DownloadManager::class.java)
  val deadline=SystemClock.elapsedRealtime()+60000
  var status=0
  while(SystemClock.elapsedRealtime()<deadline) {
   manager.query(DownloadManager.Query().setFilterById(id)).use {c->if(c.moveToFirst())status=c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))}
   if(status in listOf(DownloadManager.STATUS_SUCCESSFUL,DownloadManager.STATUS_FAILED))break
   SystemClock.sleep(300)
  }
  assertEquals(DownloadManager.STATUS_SUCCESSFUL,status)
  val digest=MessageDigest.getInstance("SHA-256")
  context.contentResolver.openInputStream(manager.getUriForDownloadedFile(id))!!.use {input->val b=ByteArray(8192);while(true){val n=input.read(b);if(n<0)break;digest.update(b,0,n)}}
  assertEquals("df408b201390be1b8bbd43a723483b46862664be7db4b31e6bddaf4c74e84e30",digest.digest().joinToString(""){"%02x".format(it)})
  assertNull(UpdateInstaller.completed(context));assertEquals(-1L,UpdateInstaller.pending(context))
 }
 /** Optional signed manifest-only fixture, supplied externally; no installation is confirmed. */
 @Test fun verifiedNewVersionOpensNativeInstaller() {
  val fixture=File(context.filesDir,"test-update.apk")
  assumeTrue(fixture.exists())
  val file=File(context.cacheDir,"updates/test-update.apk").apply {parentFile!!.mkdirs();writeBytes(fixture.readBytes())}
  assertTrue(UpdateInstaller.valid(context,file))
  ActivityScenario.launch<ConfigActivity>(Intent(context,ConfigActivity::class.java)).use {scenario->
   scenario.onActivity {assertTrue(UpdateInstaller.install(it,file))}
   SystemClock.sleep(1500)
   ins.uiAutomation.clearCache()
   val packageName=ins.uiAutomation.rootInActiveWindow?.packageName?.toString().orEmpty()
   assertTrue(packageName.contains("packageinstaller") || packageName.contains("permissioncontroller"))
   ins.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
  }
 }
}
