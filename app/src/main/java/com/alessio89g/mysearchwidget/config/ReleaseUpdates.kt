package com.alessio89g.mysearchwidget.config

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import com.alessio89g.mysearchwidget.util.readLimited
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseUpdate(val version:String,val url:String,val name:String)
object ReleaseUpdates {
 fun newer(candidate:String,current:String):Boolean {
  fun parts(v:String):List<Long>?=Regex("^v?(\\d+)\\.(\\d+)\\.(\\d+)$").matchEntire(v)?.groupValues?.drop(1)?.map {it.toLongOrNull() ?: -1L}?.takeIf {v->v.all {it>=0}}
  val a=parts(candidate) ?: return false;val b=parts(current) ?: return false
  return a.zip(b).firstOrNull {it.first!=it.second}?.let {it.first>it.second} ?: false
 }
 fun parse(text:String,current:String):ReleaseUpdate? {
  val root=Json.parseToJsonElement(text).jsonObject
  if(root["draft"]?.jsonPrimitive?.booleanOrNull!=false || root["prerelease"]?.jsonPrimitive?.booleanOrNull!=false)return null
  val version=root["tag_name"]?.jsonPrimitive?.content ?: return null
  if(!newer(version,current))return null
  val name="MySearchWidget-${version.removePrefix("v")}.apk"
  val asset=root["assets"]?.jsonArray?.map {it.jsonObject}?.firstOrNull {it["name"]?.jsonPrimitive?.content==name} ?: return null
  val url=asset["browser_download_url"]?.jsonPrimitive?.content ?: return null
  if(url!="https://github.com/alessio89g/MySearchWidget/releases/download/$version/$name")return null
  return ReleaseUpdate(version,url,name)
 }
 suspend fun check(context:Context):ReleaseUpdate?=withContext(Dispatchers.IO) {
  val current=context.packageManager.getPackageInfo(context.packageName,0).versionName ?: return@withContext null
  val connection=URL("https://api.github.com/repos/alessio89g/MySearchWidget/releases/latest").openConnection() as HttpURLConnection
  try {
   connection.connectTimeout=8000;connection.readTimeout=8000
   connection.setRequestProperty("Accept","application/vnd.github+json")
   connection.setRequestProperty("User-Agent","MySearchWidget/$current")
   if(connection.responseCode!=200)return@withContext null
   val bytes=connection.inputStream.use {it.readLimited(512*1024+1)}
   if(bytes.size>512*1024)return@withContext null
   parse(bytes.toString(Charsets.UTF_8),current)
  } finally {connection.disconnect()}
 }
 fun download(context:Context,update:ReleaseUpdate):Long {
  val request=DownloadManager.Request(Uri.parse(update.url))
   .setTitle(update.name).setMimeType("application/vnd.android.package-archive")
   .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
   .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,update.name.removeSuffix(".apk")+"-${System.currentTimeMillis()}.apk")
  return context.getSystemService(DownloadManager::class.java).enqueue(request)
 }
}
