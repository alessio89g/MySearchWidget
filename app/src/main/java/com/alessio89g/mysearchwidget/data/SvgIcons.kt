package com.alessio89g.mysearchwidget.data

import android.graphics.Bitmap
import android.graphics.Canvas
import com.caverock.androidsvg.SVG

/** Static vector artwork. Keep its strokes instead of tracing a second outline. */
object SvgIcons {
 fun recognizes(bytes:ByteArray)=Regex("<svg(?:\\s|>)",RegexOption.IGNORE_CASE).containsMatchIn(bytes.toString(Charsets.UTF_8))
 internal fun checkedText(bytes:ByteArray):String {
  val text=bytes.toString(Charsets.UTF_8)
  require(bytes.size<=512_000 && recognizes(bytes)) { "Invalid SVG icon" }
  // Icons are self-contained: no entity expansion, embedded rasters or external resources.
  require(!Regex("<!DOCTYPE|<!ENTITY|<\\s*(?:image|script|foreignObject)\\b|@import",RegexOption.IGNORE_CASE).containsMatchIn(text)) { "Unsupported SVG content" }
  require(Regex("<").findAll(text).take(4097).count()<=4096) { "SVG icon is too complex" }
  return text
 }
 fun decode(bytes:ByteArray):Bitmap {
  val text=checkedText(bytes)
  SVG.setInternalEntitiesEnabled(false)
  val svg=SVG.getFromString(text)
  val box=svg.documentViewBox
  val width=box?.width() ?: svg.documentWidth
  val height=box?.height() ?: svg.documentHeight
  require(width.isFinite() && height.isFinite() && width>0 && height>0) { "SVG needs dimensions or a viewBox" }
  if(box==null)svg.setDocumentViewBox(0f,0f,width,height)
  val scale=512f/maxOf(width,height)
  val w=(width*scale).toInt().coerceIn(1,512)
  val h=(height*scale).toInt().coerceIn(1,512)
  svg.setDocumentWidth(w.toFloat());svg.setDocumentHeight(h.toFloat())
  val bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
  try { svg.renderToCanvas(Canvas(bitmap));return bitmap } catch(e:Exception){bitmap.recycle();throw e}
 }
}
