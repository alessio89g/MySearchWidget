package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test

class IconToneMaskTest {
 @Test fun opaqueBackgroundDoesNotEraseGlyph() {
  val out=IconToneMask.convert(intArrayOf(0xff202020.toInt(),0xffffffff.toInt(),0))
  assertTrue((out[0] ushr 24)<(out[1] ushr 24))
  assertEquals(255,out[1] ushr 24)
  assertEquals(0,out[2] ushr 24)
 }
 @Test fun flatSilhouettePreservesAntialiasing() {
  val out=IconToneMask.convert(intArrayOf(0xff2299cc.toInt(),0x802299cc.toInt(),0x002299cc))
  assertArrayEquals(intArrayOf(255,128,0),out.map {it ushr 24}.toIntArray())
 }
 @Test fun coloredDetailsHaveDistinctTones() {
  val out=IconToneMask.convert(intArrayOf(0xffff0000.toInt(),0xff00ff00.toInt(),0xff0000ff.toInt()))
  assertEquals(3,out.toSet().size)
 }
}
