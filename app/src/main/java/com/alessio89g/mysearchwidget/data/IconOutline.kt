package com.alessio89g.mysearchwidget.data

import kotlin.math.abs

/** Traces the symbol, removing a uniform enclosing background when it can be identified. */
object IconOutline {
 fun convert(pixels:IntArray,w:Int,h:Int,radius:Int,keepOuterOutline:Boolean=false):IntArray {
  require(w>0 && h>0 && pixels.size==w*h && radius>0)
  val background=if(keepOuterOutline)BooleanArray(pixels.size) else background(pixels,w,h)
  return IntArray(pixels.size){i->
   val c=pixels[i];val a=if(background[i])0 else c ushr 24;val x=i%w;val y=i/w
   var edge=0
   if(a>0)for(dy in -radius..radius)for(dx in -radius..radius)if(dx*dx+dy*dy<=radius*radius){
    val nx=x+dx;val ny=y+dy
    val other=if(nx in 0 until w && ny in 0 until h)pixels[ny*w+nx] else 0
    val oa=if(nx in 0 until w && ny in 0 until h && background[ny*w+nx])0 else other ushr 24
    edge=maxOf(edge,a-oa)
    // Ignore RGB of transparent pixels and suppress small compression/color noise.
    val difference=maxOf(abs(((c ushr 16) and 255)-((other ushr 16) and 255)),abs(((c ushr 8) and 255)-((other ushr 8) and 255)),abs((c and 255)-(other and 255)))
    val contrast=((difference-24).coerceAtLeast(0)/136f).coerceAtMost(1f)
    edge=maxOf(edge,(minOf(a,oa)*contrast).toInt())
   }
   (edge shl 24) or (c and 0x00ffffff)
  }
 }
 private fun distance(a:Int,b:Int)=maxOf(abs(((a ushr 16) and 255)-((b ushr 16) and 255)),abs(((a ushr 8) and 255)-((b ushr 8) and 255)),abs((a and 255)-(b and 255)))
 private fun background(p:IntArray,w:Int,h:Int):BooleanArray {
  val empty=BooleanArray(p.size)
  fun opaque(x:Int,y:Int)=x in 0 until w && y in 0 until h && p[y*w+x] ushr 24>=128
  val border=p.indices.filter {i->val x=i%w;val y=i/w;opaque(x,y)&&(!opaque(x-1,y)||!opaque(x+1,y)||!opaque(x,y-1)||!opaque(x,y+1))}
  if(border.isEmpty())return empty
  fun bucket(c:Int)=((c ushr 19) and 31)*1024+((c ushr 11) and 31)*32+((c ushr 3) and 31)
  val groups=border.groupBy {bucket(p[it])}
  val largest=groups.values.maxByOrNull {it.size} ?: return empty
  val color=p[largest.first()]
  if(border.count {distance(p[it],color)<=48}<border.size*0.75f)return empty
  val found=BooleanArray(p.size);val queue=IntArray(p.size);var head=0;var tail=0
  fun add(i:Int){if(!found[i] && p[i] ushr 24>0 && distance(p[i],color)<=48){found[i]=true;queue[tail++]=i}}
  border.forEach(::add)
  while(head<tail){val i=queue[head++];val x=i%w;val y=i/w
   if(x>0)add(i-1);if(x+1<w)add(i+1);if(y>0)add(i-w);if(y+1<h)add(i+w)
  }
  val opaqueCount=p.count {it ushr 24>=128}
  val foreground=p.indices.count {p[it] ushr 24>=128 && !found[it]}
  // A single-color transparent symbol has no separate background to remove.
  if(foreground<maxOf(4,opaqueCount/100) || tail<opaqueCount/4)return empty
  return found
 }

}
