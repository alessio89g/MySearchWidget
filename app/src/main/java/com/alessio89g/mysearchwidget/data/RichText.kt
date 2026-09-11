package com.alessio89g.mysearchwidget.data

import android.graphics.Color

/** Disjoint ranges: edits preserve the unaffected prefix/suffix, formatting replaces only its range. */
object RichText {
 fun styleAt(runs:List<TextRun>,index:Int,base:TextStyle)=runs.lastOrNull {index>=it.start && index<it.end}?.style ?: base
 fun apply(runs:List<TextRun>,start:Int,end:Int,style:TextStyle):List<TextRun> {
  if(end<=start)return runs
  val kept=runs.flatMap {r->if(r.end<=start || r.start>=end)listOf(r) else listOfNotNull(if(r.start<start)r.copy(end=start) else null,if(r.end>end)r.copy(start=end) else null)}
  return merge((kept+TextRun(start,end,style)).sortedBy {it.start})
 }
 fun merge(runs:List<TextRun>):List<TextRun> {
  val result=mutableListOf<TextRun>()
  for(r in runs) {val last=result.lastOrNull();if(last!=null && last.end==r.start && last.style==r.style)result[result.lastIndex]=last.copy(end=r.end) else result+=r}
  return result
 }
 fun edit(old:String,new:String,runs:List<TextRun>,base:TextStyle):List<TextRun> {
  var prefix=0
  while(prefix<minOf(old.length,new.length) && old[prefix]==new[prefix])prefix++
  var suffix=0
  while(suffix<minOf(old.length-prefix,new.length-prefix) && old[old.lastIndex-suffix]==new[new.lastIndex-suffix])suffix++
  val end=old.length-suffix;val delta=new.length-old.length
  val kept=runs.flatMap {r->listOfNotNull(
   if(r.start<prefix)r.copy(end=minOf(r.end,prefix)).takeIf {it.end>it.start} else null,
   if(r.end>end)r.copy(start=maxOf(r.start,end)+delta,end=r.end+delta).takeIf {it.end>it.start} else null)}
  return if(new.length-suffix>prefix)apply(kept,prefix,new.length-suffix,styleAt(runs,(prefix-1).coerceAtLeast(0),base)) else merge(kept.sortedBy {it.start})
 }
 private fun escape(text:String)=text.replace("\\","\\\\").replace("[","\\[").replace("]","\\]")
 /** Explicit reset scopes make exports independent of the current editor base style. */
 fun bbcode(text:String,runs:List<TextRun>,base:TextStyle):String {
  val boundaries=(listOf(0,text.length)+runs.flatMap {listOf(it.start,it.end)}).distinct().sorted()
  return boundaries.zipWithNext().joinToString("") {(a,b)->
   val s=styleAt(runs,a,base);val tags=mutableListOf<Pair<String,String>>()
   fun add(name:String,value:String=""){tags+=name to value}
   fun gradient(g:Gradient)=listOf(if(g.enabled)"on" else "off",g.start,g.end,g.angle.toString()).joinToString(",")
   add("reset")
   if(s.font.isNotEmpty())add("font",s.font)
   if(s.size!=15f)add("size",s.size.toString())
   if(s.weight==700)add("b") else if(s.weight!=400)add("weight",s.weight.toString())
   if(s.italic)add("i")
   if(s.underline)add("u")
   if(s.strike)add("s")
   if(s.light==s.dark){if(s.light.isNotEmpty())add("color",s.light)}
   else {if(s.light.isNotEmpty())add("color-light",s.light);if(s.dark.isNotEmpty())add("color-dark",s.dark)}
   if(s.lightGradient==s.darkGradient){if(s.lightGradient!=Gradient())add("gradient",gradient(s.lightGradient))}
   else {if(s.lightGradient!=Gradient())add("gradient-light",gradient(s.lightGradient));if(s.darkGradient!=Gradient())add("gradient-dark",gradient(s.darkGradient))}
   tags.joinToString(""){(name,value)->"["+name+(if(value.isEmpty())"" else "=$value")+"]"}+
    escape(text.substring(a,b))+tags.asReversed().joinToString(""){"[/${it.first}]"}
  }
 }
 fun parse(source:String,base:TextStyle):Pair<String,List<TextRun>> {
  require(source.length<=1_000_000)
  val text=StringBuilder();val runs=mutableListOf<TextRun>()
  val stack=mutableListOf(base);val tags=mutableListOf<String>()
  fun append(value:Char) {require(text.length<500);val start=text.length;text.append(value);runs+=TextRun(start,text.length,stack.last())}
  fun color(value:String):String {
   if(value=="auto")return ""
   if(value.startsWith("#")){Validation.color(value);return value}
   return "#%08X".format(java.util.Locale.ROOT,Color.parseColor(value))
  }
  fun number(value:String,min:Float,max:Float)=value.toFloat().also {require(it.isFinite() && it in min..max)}
  fun gradient(value:String):Gradient {
   val parts=value.split(',').map {it.trim()}
   val explicit=parts.firstOrNull() in listOf("on","off")
   require(parts.size==if(explicit)4 else 3)
   val offset=if(explicit)1 else 0
   val start=color(parts[offset]);val end=color(parts[offset+1]);require(start.isNotEmpty() && end.isNotEmpty())
   return Gradient(!explicit || parts[0]=="on",start,end,number(parts[offset+2],0f,360f))
  }
  var index=0
  while(index<source.length) {
   val ch=source[index]
   if(ch=='\\' && index+1<source.length && source[index+1] in "[]\\"){append(source[index+1]);index+=2;continue}
   if(ch!='['){append(ch);index++;continue}
   val end=source.indexOf(']',index+1);require(end>=0)
   val raw=source.substring(index+1,end);index=end+1
   if(raw.startsWith("/")) {
    require(tags.isNotEmpty() && tags.last()==raw.substring(1).lowercase())
    tags.removeAt(tags.lastIndex);stack.removeAt(stack.lastIndex);continue
   }
   val name=raw.substringBefore('=').lowercase();val value=raw.substringAfter('=',"").trim()
   require(tags.size<128)
   var s=stack.last()
   fun flag()=when(value.lowercase()) {"" ,"true","on"->true;"false","off"->false;else->error("Invalid flag")}
   s=when(name) {
    "reset"->{require(value.isEmpty());TextStyle()}
    "b"->s.copy(weight=if(flag())700 else 400)
    "i"->s.copy(italic=flag())
    "u"->s.copy(underline=flag())
    "s","strike"->s.copy(strike=flag())
    "size"->s.copy(size=number(value,8f,32f))
    "weight"->s.copy(weight=value.toInt().also {require(it in 100..900)})
    "font"->s.copy(font=if(value in listOf("default","Google Sans"))"" else value.also {require(Regex("[a-zA-Z0-9_-]{1,100}").matches(it))})
    "color"->color(value).let {s.copy(light=it,dark=it)}
    "color-light"->s.copy(light=color(value))
    "color-dark"->s.copy(dark=color(value))
    "gradient"->gradient(value).let {s.copy(lightGradient=it,darkGradient=it)}
    "gradient-light"->s.copy(lightGradient=gradient(value))
    "gradient-dark"->s.copy(darkGradient=gradient(value))
    else->error("Unsupported BBCode tag")
   }
   tags+=name;stack+=s
  }
  require(tags.isEmpty())
  return text.toString() to merge(runs)
 }
}
