package com.alessio89g.mysearchwidget.icons

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Xml
import com.alessio89g.mysearchwidget.data.Asset
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.UUID

/** Copies selected resources into the configuration: backups do not depend on installed packs. */
object ExternalIcons {
 const val PICK_ACTION="org.adw.launcher.icons.ACTION_PICK_ICON"
 fun picker(c:Context,pkg:String):Intent? = listOf(Intent(PICK_ACTION),Intent("com.novalauncher.THEME").addCategory("com.novalauncher.category.CUSTOM_ICON_PICKER"))
  .map {it.setPackage(pkg)}.firstOrNull {i->c.packageManager.resolveActivity(i,android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.exported==true}
 @Suppress("DEPRECATION")
 fun result(c:Context,data:Intent):Pair<String,Asset> {
  val resource=data.getParcelableExtra<Intent.ShortcutIconResource>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
  if(resource!=null) {
   val r=c.packageManager.getResourcesForApplication(resource.packageName)
   return asset(r.getDrawable(r.getIdentifier(resource.resourceName,null,null),null))
  }
  val bitmap=data.getParcelableExtra<Bitmap>(Intent.EXTRA_SHORTCUT_ICON)
  if(bitmap!=null) {
   require(bitmap.width.toLong()*bitmap.height<=8_000_000)
   return asset(android.graphics.drawable.BitmapDrawable(c.resources,bitmap))
  }
  val uri=data.data ?: data.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM) ?: data.clipData?.takeIf {it.itemCount>0}?.getItemAt(0)?.uri
  require(uri!=null && uri.scheme in listOf("content","android.resource"))
  return com.alessio89g.mysearchwidget.data.Assets.read(c,uri,"image")
 }

 val actions=listOf(PICK_ACTION,"org.adw.launcher.THEMES","org.adw.ActivityStarter.THEMES","com.gau.go.launcherex.theme","com.novalauncher.THEME","app.lawnchair.icons.THEMED_ICON")
 data class Pack(val pkg:String,val label:String)
 data class Entry(val name:String,val component:String="")
 fun packs(c:Context):List<Pack> = actions.flatMap {a->c.packageManager.queryIntentActivities(Intent(a),0)}
  .distinctBy {it.activityInfo.packageName}.map {Pack(it.activityInfo.packageName,it.loadLabel(c.packageManager).toString())}.sortedBy {it.label.lowercase()}
 fun entries(c:Context,pkg:String):List<Entry> {
  val r=c.packageManager.getResourcesForApplication(pkg)
  val result=linkedMapOf<String,Entry>()
  for(file in listOf("drawable","appfilter")) {
   fun read(p:XmlPullParser) {
    var count=0
    while(p.eventType!=XmlPullParser.END_DOCUMENT && count++<100000) {
     if(p.eventType==XmlPullParser.START_TAG && p.name=="item") {
      val name=p.getAttributeValue(null,"drawable").orEmpty()
      if(name.matches(Regex("[a-zA-Z0-9_]+")) && r.getIdentifier(name,"drawable",pkg)!=0)result.putIfAbsent(name,Entry(name,p.getAttributeValue(null,"component").orEmpty()))
     };p.next()
    }
   }
   runCatching {val id=r.getIdentifier(file,"xml",pkg);if(id!=0)r.getXml(id).use {read(it)} else r.assets.open("$file.xml").use {stream->read(Xml.newPullParser().apply {setInput(stream,null)})}}
  }
  return result.values.sortedBy {it.name}
 }
 fun drawable(c:Context,pkg:String,name:String):Drawable {
  val r=c.packageManager.getResourcesForApplication(pkg)
  return r.getDrawable(r.getIdentifier(name,"drawable",pkg),null)
 }
 fun bitmap(d:Drawable):Bitmap=Bitmap.createBitmap(192,192,Bitmap.Config.ARGB_8888).also {b->
  val copy=d.constantState?.newDrawable()?.mutate() ?: d.mutate()
  val w=copy.intrinsicWidth.coerceAtLeast(1);val h=copy.intrinsicHeight.coerceAtLeast(1)
  val scale=192f/maxOf(w,h);val bw=(w*scale).toInt();val bh=(h*scale).toInt()
  copy.setBounds((192-bw)/2,(192-bh)/2,(192+bw)/2,(192+bh)/2);copy.draw(Canvas(b))
 }
 fun asset(d:Drawable):Pair<String,Asset> {
  val b=bitmap(d);val bytes=ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,bytes);b.recycle()
  return UUID.randomUUID().toString() to Asset("image",Base64.getEncoder().encodeToString(bytes.toByteArray()))
 }
 fun app(c:Context,pkg:String)=asset(c.packageManager.getApplicationIcon(pkg))
}
