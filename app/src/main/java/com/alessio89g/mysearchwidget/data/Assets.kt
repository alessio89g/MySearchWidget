package com.alessio89g.mysearchwidget.data

import com.alessio89g.mysearchwidget.util.readLimited
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.*
import android.content.Context
import android.graphics.*
import android.net.Uri
import java.io.File
import java.util.Base64
import java.util.UUID
import android.util.LruCache

object Assets {
 private val bitmaps=object:LruCache<String,Bitmap>(8*1024*1024) { override fun sizeOf(key:String,value:Bitmap)=value.byteCount }
 private val fonts=LruCache<String,Typeface>(12)
 fun read(context:Context, uri:Uri, kind:String): Pair<String,Asset> {
  val bytes=context.contentResolver.openInputStream(uri)?.use { it.readLimited(Validation.MAX_ASSET+1) } ?: error(tr(R.string.file_unreadable))
  require(bytes.size<=Validation.MAX_ASSET) { tr(R.string.small_file) }
  val asset=Asset(kind,Base64.getEncoder().encodeToString(bytes));validate(context,mapOf("new" to asset))
  return UUID.randomUUID().toString() to asset
 }
 fun validate(context:Context, assets:Map<String,Asset>) { assets.forEach { (_,a) ->
  val data=Base64.getDecoder().decode(a.base64)
  if(a.kind=="image") {
   val bounds=BitmapFactory.Options().apply { inJustDecodeBounds=true };BitmapFactory.decodeByteArray(data,0,data.size,bounds)
   require(bounds.outWidth in 1..4096 && bounds.outHeight in 1..4096 && bounds.outWidth.toLong()*bounds.outHeight<=8_000_000) { tr(R.string.invalid_image) }
   val bitmap=BitmapFactory.decodeByteArray(data,0,data.size) ?: error(tr(R.string.damaged_image));bitmap.recycle()
  } else {
   require(data.size >= 12) { tr(R.string.incomplete_font) }
   val signature=data.take(4).toByteArray()
   require(signature.contentEquals(byteArrayOf(0,1,0,0)) || String(signature, Charsets.US_ASCII) in listOf("OTTO","ttcf","true")) { tr(R.string.invalid_font) }
   val f=File.createTempFile("fontcheck", ".font",context.cacheDir)
   try { f.writeBytes(data);Typeface.createFromFile(f) } finally { f.delete() }
  }
 } }
 private fun key(asset:Asset)=java.security.MessageDigest.getInstance("SHA-256").digest(asset.base64.toByteArray()).joinToString("") { "%02x".format(it) }
 fun bitmap(asset:Asset):Bitmap? { val k=key(asset); return bitmaps[k] ?: Base64.getDecoder().decode(asset.base64).let { b -> BitmapFactory.decodeByteArray(b,0,b.size)?.also { bitmaps.put(k,it) } } }
 fun font(context:Context,asset:Asset):Typeface {
  val k=key(asset);fonts[k]?.let { return it }
  val f=File.createTempFile("renderfont", ".font",context.cacheDir)
  return try { f.writeBytes(Base64.getDecoder().decode(asset.base64));Typeface.createFromFile(f).also { fonts.put(k,it) } } finally { f.delete() }
 }
 fun prune(c:WidgetConfig):WidgetConfig {
  val used=(listOf(c.hint.font,c.query.font)+c.hintRuns.map {it.style.font}+(listOf(c.logo)+c.buttons).map { it.icon.asset }).toSet()
  return c.copy(assets=c.assets.filterKeys { it in used })
 }
}
