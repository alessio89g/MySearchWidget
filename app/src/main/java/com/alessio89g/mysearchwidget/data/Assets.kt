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
 private val vectors=LruCache<String,Boolean>(24)
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
   if(SvgIcons.recognizes(data)){SvgIcons.decode(data).recycle();return@forEach}
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
 fun isVector(asset:Asset):Boolean { val k=key(asset);return vectors[k] ?: SvgIcons.recognizes(Base64.getDecoder().decode(asset.base64)).also {vectors.put(k,it)} }
 fun bitmap(asset:Asset):Bitmap? { val k=key(asset); return bitmaps[k] ?: Base64.getDecoder().decode(asset.base64).let { b -> (if(isVector(asset))SvgIcons.decode(b) else BitmapFactory.decodeByteArray(b,0,b.size))?.also { bitmaps.put(k,it) } } }
 /** Preserve internal contrast in flattened icons instead of tinting their entire opaque disc. */
 fun monochromeBitmap(asset:Asset):Bitmap? {
  if(isVector(asset))return bitmap(asset)
  val k=key(asset)+":monochrome";bitmaps[k]?.let {return it}
  val source=bitmap(asset) ?: return null
  val pixels=IntArray(source.width*source.height)
  source.getPixels(pixels,0,source.width,0,0,source.width,source.height)
  val output=IconToneMask.convert(pixels)
  return Bitmap.createBitmap(output,source.width,source.height,Bitmap.Config.ARGB_8888).also {bitmaps.put(k,it)}
 }
 /** Trace silhouette and internal color boundaries, preserving source RGB. */
 fun outlinedBitmap(asset:Asset,keepOuterOutline:Boolean=false):Bitmap? {
  val k=key(asset)+":outline:$keepOuterOutline";bitmaps[k]?.let {return it}
  val source=bitmap(asset) ?: return null
  // Normalize outline resolution so stroke weight does not depend on the imported file size.
  val scale=256f/maxOf(source.width,source.height)
  val b=Bitmap.createScaledBitmap(source,(source.width*scale).toInt().coerceAtLeast(1),(source.height*scale).toInt().coerceAtLeast(1),true)
  val w=b.width;val h=b.height;val pixels=IntArray(w*h);b.getPixels(pixels,0,w,0,0,w,h)
  val output=IconOutline.convert(pixels,w,h,(minOf(w,h)/48).coerceAtLeast(1),keepOuterOutline)
  if(b!==source)b.recycle()
  return Bitmap.createBitmap(output,w,h,Bitmap.Config.ARGB_8888).also {bitmaps.put(k,it)}
 }
 fun font(context:Context,asset:Asset):Typeface {
  val k=key(asset);fonts[k]?.let { return it }
  val f=File.createTempFile("renderfont", ".font",context.cacheDir)
  return try { f.writeBytes(Base64.getDecoder().decode(asset.base64));Typeface.createFromFile(f).also { fonts.put(k,it) } } finally { f.delete() }
 }
 fun prune(c:WidgetConfig):WidgetConfig {
  val used=listOf(c,ModeSettings.resolve(c,false),ModeSettings.resolve(c,true)).flatMap {v->listOf(v.hint.font,v.query.font)+v.hintRuns.map {it.style.font}+(listOf(v.logo)+v.buttons).map {it.icon.asset}}.toSet()
  val referenced=mutableSetOf<String>()
  fun collect(value:kotlinx.serialization.json.JsonElement) {when(value){
   is kotlinx.serialization.json.JsonPrimitive->if(value.isString && value.content in c.assets)referenced+=value.content
   is kotlinx.serialization.json.JsonArray->value.forEach(::collect)
   is kotlinx.serialization.json.JsonObject->value.values.forEach(::collect)
  }}
  (c.modes.dark.values+c.modes.sharedRuns.values).forEach(::collect)
  return c.copy(assets=c.assets.filterKeys { it in used || it in referenced })
 }
}
