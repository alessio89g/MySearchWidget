package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test

class IconOutlineTest {
 @Test fun opaqueIconHasInternalOutlineAndHollowAreas(){
  val p=IntArray(21*21){0xff222222.toInt()}
  for(y in 6..14)for(x in 6..14)p[y*21+x]=0xffffffff.toInt()
  val out=IconOutline.convert(p,21,21,1)
  assertTrue(out[6*21+10] ushr 24>0)
  assertEquals(0,out[10*21+10] ushr 24)
  assertEquals(0,out[3*21+3] ushr 24)
  assertEquals(0,out[10] ushr 24)
  assertEquals(255,IconOutline.convert(p,21,21,1,true)[10] ushr 24)
 }
 @Test fun transparencyAndOriginalEdgeColorArePreserved(){
  val p=IntArray(49){0x00ffffff};for(y in 2..4)for(x in 2..4)p[y*7+x]=0xff22aacc.toInt()
  val out=IconOutline.convert(p,7,7,1)
  assertEquals(0,out[0] ushr 24)
  assertEquals(0xff22aacc.toInt(),out[2*7+3])
  assertEquals(0,out[3*7+3] ushr 24)
 }
 @Test fun detectsColorBoundariesWithoutLuminanceDependence(){
  val p=IntArray(81){if(it%9<4)0xffff0000.toInt() else 0xff009400.toInt()}
  assertTrue(IconOutline.convert(p,9,9,1)[4*9+4] ushr 24>0)
 }
 @Test fun tinyColorNoiseDoesNotFillFlatAreas(){
  val p=IntArray(81){if(it%2==0)0xff777777.toInt() else 0xff7a7a7a.toInt()}
  assertEquals(0,IconOutline.convert(p,9,9,1)[40] ushr 24)
 }
 @Test fun circularBackgroundIsRemovedButSymbolRemains(){
  val p=IntArray(31*31)
  for(y in 0..30)for(x in 0..30)if((x-15)*(x-15)+(y-15)*(y-15)<=196)p[y*31+x]=0xffff00ff.toInt()
  for(y in 9..21)for(x in 12..18)p[y*31+x]=0xffffffff.toInt()
  val out=IconOutline.convert(p,31,31,1)
  assertEquals(0,out[15*31+1] ushr 24)
  assertTrue(out[9*31+15] ushr 24>0)
  assertEquals(0,out[15*31+15] ushr 24)
 }
 @Test fun multicolorTransparentSymbolKeepsItsSilhouette(){
  val p=IntArray(81)
  for(y in 2..6)for(x in 2..6)p[y*9+x]=if(x<4)0xffff0000.toInt() else 0xff00ff00.toInt()
  val out=IconOutline.convert(p,9,9,1)
  assertTrue(out[2*9+3] ushr 24>0)
  assertTrue(out[2*9+5] ushr 24>0)
 }

}
