package com.alessio89g.mysearchwidget.data

/** Alpha tones work with both solid tint and the renderer's gradient mask. */
object IconToneMask {
 private fun luminance(c:Int)=0.2126f*((c ushr 16) and 255)+0.7152f*((c ushr 8) and 255)+0.0722f*(c and 255)
 fun convert(pixels:IntArray):IntArray {
  var low=255f;var high=0f
  for(c in pixels)if(c ushr 24>=128){val l=luminance(c);low=minOf(low,l);high=maxOf(high,l)}
  val range=high-low
  return IntArray(pixels.size){i->
   val c=pixels[i];val alpha=c ushr 24
   // Flat silhouettes keep their original antialiasing and opacity.
   val tone=if(range<8f)1f else 0.15f+0.85f*((luminance(c)-low)/range).coerceIn(0f,1f)
   ((alpha*tone).toInt() shl 24) or 0x00ffffff
  }
 }
}
