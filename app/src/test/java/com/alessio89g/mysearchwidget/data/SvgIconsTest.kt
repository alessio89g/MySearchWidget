package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test

class SvgIconsTest {
 @Test fun acceptsStandaloneStrokesAndViewBox() {
  val svg="""<?xml version="1.0"?><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="#000" stroke-linecap="round" d="M2 2 L22 22"/></svg>"""
  assertEquals(svg,SvgIcons.checkedText(svg.toByteArray()))
  assertFalse(SvgIcons.recognizes(byteArrayOf(0x50,0x4e,0x47)))
 }
 @Test fun rejectsEntitiesEmbeddedImagesAndUnboundedDocuments() {
  val cases=listOf("<!DOCTYPE svg><svg/>","<svg><image href='data:image/png;base64,AAAA'/></svg>","<svg><script/></svg>","<svg>"+"<path/>".repeat(4097)+"</svg>","<svg>"+" ".repeat(512000)+"</svg>")
  for(svg in cases)assertThrows(IllegalArgumentException::class.java){SvgIcons.checkedText(svg.toByteArray())}
 }
}
