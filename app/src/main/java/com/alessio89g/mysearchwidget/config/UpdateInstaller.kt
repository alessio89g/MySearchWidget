package com.alessio89g.mysearchwidget.config

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/** A completed system download is inspected only while the configurator is open. */
object UpdateInstaller {
 private fun prefs(c:Context)=c.getSharedPreferences("release_updates",Context.MODE_PRIVATE)
 fun remember(c:Context,id:Long){prefs(c).edit().putLong("download",id).apply()}
 fun pending(c:Context)=prefs(c).getLong("download",-1)
 fun forget(c:Context){prefs(c).edit().remove("download").apply()}
 suspend fun completed(c:Context):File?=withContext(Dispatchers.IO) {
  val id=pending(c);if(id<0)return@withContext null
  val manager=c.getSystemService(DownloadManager::class.java)
  val status=manager.query(DownloadManager.Query().setFilterById(id))?.use {cursor->
   if(!cursor.moveToFirst())return@withContext null
   cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
  }
  if(status==DownloadManager.STATUS_FAILED){forget(c);error("Download failed")}
  if(status!=DownloadManager.STATUS_SUCCESSFUL)return@withContext null
  val folder=File(c.cacheDir,"updates").apply {mkdirs()}
  val file=File(folder,"release-$id.apk")
  val uri=manager.getUriForDownloadedFile(id) ?: return@withContext null
  c.contentResolver.openInputStream(uri)?.use {input->file.outputStream().use {out->
   var total=0L;val buffer=ByteArray(8192)
   while(true){val count=input.read(buffer);if(count<0)break;total+=count;require(total<=64L*1024*1024);out.write(buffer,0,count)}
  }} ?: error("Download unavailable")
  val archive=c.packageManager.getPackageArchiveInfo(file.path,0)
  if(archive?.packageName==c.packageName && archive.longVersionCode<=c.packageManager.getPackageInfo(c.packageName,0).longVersionCode){forget(c);return@withContext null}
  require(valid(c,file)){"Invalid update package"}
  file
 }
 fun valid(c:Context,file:File):Boolean {
  val pm=c.packageManager
  val update=pm.getPackageArchiveInfo(file.path,PackageManager.GET_SIGNING_CERTIFICATES) ?: return false
  val installed=pm.getPackageInfo(c.packageName,PackageManager.GET_SIGNING_CERTIFICATES)
  fun hashes(info:android.content.pm.PackageInfo)=info.signingInfo?.apkContentsSigners?.map {MessageDigest.getInstance("SHA-256").digest(it.toByteArray()).toList()}?.toSet()
  return update.packageName==c.packageName && update.longVersionCode>installed.longVersionCode && hashes(update)!=null && hashes(update)==hashes(installed)
 }
 fun install(c:Context,file:File):Boolean {
  require(valid(c,file))
  if(!c.packageManager.canRequestPackageInstalls()) {
   c.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:${c.packageName}")))
   return false
  }
  val uri=FileProvider.getUriForFile(c,c.packageName+".updates",file)
  c.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
  return true
 }
}
