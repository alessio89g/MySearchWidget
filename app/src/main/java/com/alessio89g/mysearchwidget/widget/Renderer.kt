package com.alessio89g.mysearchwidget.widget

import com.alessio89g.mysearchwidget.i18n.AppLanguage
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.text.*
import android.text.style.MetricAffectingSpan
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.vector.*
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.icons.IconCatalog
import kotlin.math.*

data class SessionText(val text:String,val cursor:Int=text.length,val editing:Boolean=true)
data class Geometry(val width:Float,val count:Int) {
 // Revised from home.png: 64dp outer, 46dp circles, 52dp hit cells.
 val fieldRight=width-9-count*52- if(count>0) 1 else 0
 fun center(index:Int)=width-9-count*52+index*52+26
}
object Renderer {
 fun dark(context:Context,c:WidgetConfig)=c.theme=="dark" || (c.theme=="system" && context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK==Configuration.UI_MODE_NIGHT_YES)
 fun render(baseContext:Context,c:WidgetConfig,widthDp:Int,session:SessionText?=null):Bitmap {
  val context=AppLanguage.context(baseContext)
  val density=context.resources.displayMetrics.density.coerceAtMost(3f)
  val width=widthDp.coerceIn(180,1000)
  val bitmap=Bitmap.createBitmap((width*density).roundToInt(),(64*density).roundToInt(),Bitmap.Config.ARGB_8888)
  val canvas=Canvas(bitmap);canvas.scale(density,density)
  val g=Geometry(width.toFloat(),c.count);val dark=dark(context,c)
  val scheme=if(dark)dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
  val on=if(c.dynamic)scheme.primary.toArgb() else Color.parseColor(if(dark)"#B8B3A1" else "#48463B")
  val outer=if(c.dynamic)scheme.secondaryContainer.toArgb() else Color.parseColor(if(dark)"#474645" else "#E5E2DA")
  val field=if(dark)Color.BLACK else if(c.dynamic)scheme.surfaceContainerLowest.toArgb() else Color.WHITE
  fun surface(s:Surface,rect:RectF,fallback:Int) {
   val tone=if(dark)s.dark else s.light
   val color=if(c.dynamic || tone.color.isEmpty())fallback else Color.parseColor(tone.color)
   val paint=Paint(3).apply { this.color=color;alpha=(Color.alpha(color)*tone.opacity).roundToInt() }
   if(!c.dynamic && tone.gradient.enabled){paint.shader=gradient(tone.gradient,rect);paint.alpha=(255*tone.opacity).roundToInt()}
   // RemoteViews cannot request a backdrop blur or sample wallpaper. Applying RenderEffect
   // to our bitmap would blur the content, not the background. Always use the truthful host fallback.
   canvas.drawPath(shape(rect,s),paint)
  }
  surface(c.outer,RectF(0f,0f,width.toFloat(),64f),outer)
  surface(c.field,RectF(9f,9f,g.fieldRight,55f),field)
  fun ink(light:String,darkColor:String)= (if(dark)darkColor else light).let { if(c.dynamic || it.isEmpty())on else Color.parseColor(it) }
  drawIcon(canvas,c,c.logo.icon,RectF(17f,16f,49f,48f),ink(c.logo.icon.light,c.logo.icon.dark),dark)
  for(i in 0 until c.count) {
   val slot=c.buttons[i];val x=g.center(i)
   surface(slot.surface,RectF(x-23,9f,x+23,55f),field)
   drawIcon(canvas,c,slot.icon,RectF(x-12.5f,19.5f,x+12.5f,44.5f),ink(slot.icon.light,slot.icon.dark),dark)
  }
  val textStyle=if(session!=null)c.query else c.hint
  val text=session?.text ?: c.placeholder.ifEmpty { context.getString(R.string.placeholder) }
  val start=60f;val end=(g.fieldRight-10).coerceAtLeast(start);val available=end-start
  fun styledPaint(p:TextPaint,style:com.alessio89g.mysearchwidget.data.TextStyle) {
   val face=c.assets[style.font]?.let {runCatching {Assets.font(context,it)}.getOrNull()} ?: context.resources.getFont(R.font.google_sans)
   p.color=ink(style.light,style.dark);p.typeface=Typeface.create(face,style.weight,style.italic)
   p.textSize=style.size*context.resources.configuration.fontScale;p.isUnderlineText=style.underline;p.isStrikeThruText=style.strike
   val gr=if(dark)style.darkGradient else style.lightGradient
   p.shader=if(!c.dynamic && gr.enabled)gradient(gr,RectF(0f,0f,available.coerceAtLeast(1f),46f)) else null
  }
  val paint=TextPaint(3).apply {styledPaint(this,textStyle)}
  // TextLine also calls updateDrawState while measuring. Never query the layout
  // from that callback: it would recursively measure the same span.
  val rangeShaders=mutableMapOf<TextRun,Shader>()
  val rich=SpannableString(text.replace('\n',' '))
  if(session==null)c.hintRuns.forEach {run->
   if(run.start<rich.length && run.end>run.start)rich.setSpan(object:MetricAffectingSpan(){
    override fun updateMeasureState(p:TextPaint){styledPaint(p,run.style)}
    override fun updateDrawState(p:TextPaint){
     styledPaint(p,run.style)
     rangeShaders[run]?.let {p.shader=it}
    }
   },run.start,run.end.coerceAtMost(rich.length),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
  val display=if(session==null || !session.editing)TextUtils.ellipsize(rich,paint,available,TextUtils.TruncateAt.END) else rich
  val layout=StaticLayout.Builder.obtain(display,0,display.length,paint,ceil(Layout.getDesiredWidth(display,paint).coerceAtLeast(available)).toInt().coerceIn(1,100000))
   .setIncludePad(false).setMaxLines(1).build()
  // Resolve gradient geometry once, outside span callbacks. Measurement can
  // safely revisit spans because their callbacks only read cached shaders.
  if(session==null && !c.dynamic)c.hintRuns.forEach {run->
   val gr=if(dark)run.style.darkGradient else run.style.lightGradient
   if(gr.enabled && run.start<layout.text.length) {
    val a=layout.getPrimaryHorizontal(run.start)
    val b=layout.getPrimaryHorizontal(run.end.coerceAtMost(layout.text.length))
    rangeShaders[run]=gradient(gr,RectF(min(a,b),0f,max(a,b).coerceAtLeast(min(a,b)+1f),layout.height.toFloat()))
   }
  }
  val textGradient=if(dark)textStyle.darkGradient else textStyle.lightGradient
  if(!c.dynamic && textGradient.enabled)paint.shader=gradient(textGradient,RectF(0f,0f,layout.getLineWidth(0).coerceAtLeast(1f),layout.height.toFloat()))
  val cursor=session?.cursor?.coerceIn(0,text.length) ?: 0
  val advance=if(session?.editing==true)layout.getPrimaryHorizontal(cursor) else 0f
  val shift=if(session?.editing==true)max(0f,advance-available+2) else 0f
  canvas.save();canvas.clipRect(start,9f,end,55f)
  canvas.translate(start-shift,32f-layout.height/2f)
  layout.draw(canvas)
  if(session?.editing==true)canvas.drawRect(advance,0f,advance+1,layout.height.toFloat(),Paint(3).apply {color=on})
  canvas.restore();return bitmap
 }
 fun shape(r:RectF,s:Surface):Path {
  val path=Path()
  when(s.shape) {
   "squircle","flower" -> {
    // Analytic superellipse n=4 and an 8-petal radial curve; shared by preview and RemoteViews.
    for(i in 0..240) {
     val t=i*2*PI/240;val co=cos(t);val si=sin(t)
     val radial=if(s.shape=="flower") .87+.13*cos(8*t) else 1.0
     val x=r.centerX()+r.width()/2*(if(s.shape=="squircle")sign(co)*sqrt(abs(co)) else co*radial)
     val y=r.centerY()+r.height()/2*(if(s.shape=="squircle")sign(si)*sqrt(abs(si)) else si*radial)
     if(i==0)path.moveTo(x.toFloat(),y.toFloat()) else path.lineTo(x.toFloat(),y.toFloat())
    };path.close()
   }
   else -> path.addRoundRect(r,r.height()/2*s.rounding/100,r.height()/2*s.rounding/100,Path.Direction.CW)
  };return path
 }
 fun gradient(g:Gradient,r:RectF):Shader {
  val rad=Math.toRadians(g.angle.toDouble());val dx=cos(rad).toFloat();val dy=sin(rad).toFloat()
  val extent=(abs(r.width()*dx)+abs(r.height()*dy))/2
  return LinearGradient(r.centerX()-dx*extent,r.centerY()-dy*extent,r.centerX()+dx*extent,r.centerY()+dy*extent,Color.parseColor(g.start),Color.parseColor(g.end),Shader.TileMode.CLAMP)
 }
 private fun drawIcon(canvas:Canvas,c:WidgetConfig,s:IconSpec,r:RectF,color:Int,darkIcon:Boolean) {
  val gr=if(darkIcon) s.darkGradient else s.lightGradient
  if(c.dynamic || !gr.enabled){drawIconSolid(canvas,c,s,r,color);return}
  val layer=canvas.saveLayer(r,null)
  drawIconSolid(canvas,c,s,r,Color.WHITE)
  canvas.drawRect(r,Paint(3).apply {shader=gradient(gr,r);xfermode=PorterDuffXfermode(PorterDuff.Mode.SRC_IN)})
  canvas.restoreToCount(layer)
 }
 private fun drawIconSolid(canvas:Canvas,c:WidgetConfig,s:IconSpec,r:RectF,color:Int) {
  val p=Paint(3).apply { this.color=color }
  val asset=c.assets[s.asset]
  if(asset!=null) { Assets.bitmap(asset)?.let { b ->
   p.colorFilter=PorterDuffColorFilter(color,PorterDuff.Mode.SRC_IN)
   val scale=min(r.width()/b.width,r.height()/b.height);val w=b.width*scale;val h=b.height*scale
   canvas.drawBitmap(b,null,RectF(r.centerX()-w/2,r.centerY()-h/2,r.centerX()+w/2,r.centerY()+h/2),p)
  };return }
  canvas.save();canvas.translate(r.left,r.top);canvas.scale(r.width()/24,r.height()/24)
  when(s.name) {
   "Google" -> {
    val path=PathParser().parsePathString("M12,2 C6.48,2 2,6.48 2,12 C2,17.52 6.48,22 12,22 C17.77,22 21.6,17.95 21.6,12.23 C21.6,11.52 21.54,10.84 21.42,10.18 L12,10.18 L12,14.06 L17.38,14.06 C16.67,16.67 14.8,18 12,18 C8.69,18 6,15.31 6,12 C6,8.69 8.69,6 12,6 C13.47,6 14.79,6.5 15.83,7.48 L18.7,4.61 C16.95,2.98 14.68,2 12,2 Z").toPath().asAndroidPath()
    canvas.drawPath(path,p)
   }
   "Chrome" -> {
    val layer=canvas.saveLayer(0f,0f,24f,24f,null)
    canvas.drawCircle(12f,12f,11f,p)
    p.style=Paint.Style.STROKE;p.strokeWidth=1.1f;p.xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    canvas.drawCircle(12f,12f,4.5f,p)
    for(i in 0..2) { canvas.save();canvas.rotate(i*120f,12f,12f);canvas.drawLine(12f,7.5f,23f,7.5f,p);canvas.restore() }
    p.xfermode=null;canvas.restoreToCount(layer)
   }
   else -> {
    val vector=IconCatalog.vector(s.name,s.outline)
    fun group(g:VectorGroup) {
     canvas.save();canvas.translate(g.translationX+g.pivotX,g.translationY+g.pivotY);canvas.rotate(g.rotation);canvas.scale(g.scaleX,g.scaleY);canvas.translate(-g.pivotX,-g.pivotY)
     for(node in g) when(node) {
      is VectorGroup -> group(node)
      is VectorPath -> {
       val path=PathParser().addPathNodes(node.pathData).toPath().asAndroidPath()
       p.style=Paint.Style.FILL;p.alpha=(255*node.fillAlpha).roundToInt();canvas.drawPath(path,p)
      }
     };canvas.restore()
    };canvas.scale(24/vector.viewportWidth,24/vector.viewportHeight);group(vector.root)
   }
  };canvas.restore()
 }
}
