package com.alessio89g.mysearchwidget.data

import kotlin.math.*

/** Final dp bounds shared by preview, RemoteViews artwork and click targets. */
data class Bounds(val left:Float,val top:Float,val right:Float,val bottom:Float) {
 val width get()=right-left
 val height get()=bottom-top
 val centerX get()=(left+right)/2f
}
class ElementLayout(val width:Float,requested:WidgetConfig) {
 val config=requested.fitOuter(width)
 val height=config.heightDp
 val outerWidth=min(width,config.sizing.outerWidthDp ?: width)
 val outer=Bounds((width-outerWidth)/2f,0f,(width+outerWidth)/2f,height).shift(config.placement.outer)
 val unit=WidgetDimensions(width,config.sizing.layoutHeightDp ?: height,config.count).scale
 private val slots=config.buttons.take(config.count)
 private val pad=config.sizing.sidePaddingDp ?: 9f*unit
 private val rightPad=pad+if(config.count>0 && config.sizing.sidePaddingDp==null)3f*unit else 0f
 private val inset=config.sizing.logoInsetDp ?: 8f*unit
 private val textGap=config.sizing.textGapDp ?: 11f*unit
 private val gaps=slots.mapIndexed {i,s->s.gapDp ?: (if(i==0)4f else 6f)*unit}
 // Position allocation uses the automatic sizes, never the user-sized artwork.
 // Changing a size therefore cannot push a neighbour or recenter the group.
 private val minimumField=inset+32f*unit+textGap+34f*unit
 val fit=min(1f,width/(pad+rightPad+minimumField+slots.size*46f*unit+gaps.sum()))
 private val cell=46f*unit*fit
 private val fieldLeft=pad*fit
 private val fieldRight=width-(rightPad+slots.size*46f*unit+gaps.sum())*fit
 private val fieldCenter=(fieldLeft+fieldRight)/2f
 private fun centered(cx:Float,w:Float,h:Float)=Bounds(cx-w/2f,(height-h)/2f,cx+w/2f,(height+h)/2f)
 private fun artwork(cx:Float,w:Float,h:Float,locked:Boolean):Bounds {
  val limit=(2f*min(cx,width-cx)).coerceAtLeast(.001f)
  return if(locked) {
   val scale=min(1f,min(limit/w,height/h));centered(cx,w*scale,h*scale)
  }else centered(cx,min(w,limit),min(h,height))
 }
 private val baseField=artwork(fieldCenter,config.sizing.fieldWidthDp ?: (fieldRight-fieldLeft),config.sizing.fieldHeightDp ?: 46f*unit,config.sizing.fieldLocked)
 val field=baseField.shift(config.placement.field)
 private val contentField=if(config.placement.fieldLinked)baseField else centered(fieldCenter,config.placement.detachedFieldWidth ?: (fieldRight-fieldLeft),config.placement.detachedFieldHeight ?: (46f*unit))
 private val parentOffset=if(config.placement.fieldLinked)config.placement.field else OffsetDp()
 private val contentScale=fit*min(1f,contentField.width/(minimumField*fit))
 private val logoCenter=contentField.left+(inset+16f*unit)*contentScale
 private val baseLogo=artwork(logoCenter,config.logo.icon.sizeDp ?: 32f*unit*contentScale,config.logo.icon.sizeDp ?: 32f*unit*contentScale,true)
 val logo=baseLogo.shift(parentOffset+config.placement.logo)
 val textLeft=contentField.left+(inset+32f*unit+textGap)*contentScale+parentOffset.x+config.placement.text.x
 val textRight=contentField.right-10f*unit*contentScale+parentOffset.x+config.placement.text.x
 val textCenterY=height/2f+parentOffset.y+config.placement.text.y
 val textTop=textCenterY-contentField.height/2f
 val textBottom=textCenterY+contentField.height/2f
 val textScale=unit*contentScale
 val buttonBounds:List<Bounds>
 val iconBounds:List<Bounds>
 val buttonHits:List<Bounds>
 val iconHits:List<Bounds>
 val logoHit:Bounds
 val inputHit:Bounds
 init {
  var x=fieldRight
  val centers=slots.indices.map {i->x+=gaps[i]*fit;val center=x+cell/2f;x+=cell;center}
  buttonBounds=slots.mapIndexed {i,s->artwork(centers[i],s.widthDp ?: 46f*unit*fit,s.heightDp ?: 46f*unit*fit,s.proportionsLocked).shift(s.position)}
  iconBounds=slots.mapIndexed {i,s->val size=s.icon.sizeDp ?: 25f*unit*fit;artwork(centers[i],size,size,true).shift(s.iconPosition+if(s.iconLinked)s.position else OffsetDp())}
  // Stable action partitions prevent enlarged artwork from stealing neighbouring taps.
  val hitLeft=baseField.left
  val hitRight=baseField.right
  val split=(baseField.left+(inset+36f*unit)*contentScale).coerceIn(hitLeft+.001f,hitRight-.001f)
  val touchHeight=min(height,52f*unit*fit)
  logoHit=centered((hitLeft+split)/2f,split-hitLeft,touchHeight).shift(parentOffset+config.placement.logo+OffsetDp(contentField.left-baseField.left,0f))
  inputHit=field
  buttonHits=centers.mapIndexed {i,c->
   val left=if(i==0)(fieldRight+c-cell/2f)/2f else (centers[i-1]+c)/2f
   val right=if(i==centers.lastIndex)min(width,c+cell/2f+3f*unit*fit) else (c+centers[i+1])/2f
   centered((left+right)/2f,right-left,touchHeight).shift(slots[i].position)
  }
  iconHits=iconBounds
 }
}
