package com.alessio89g.mysearchwidget.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class WidgetHeightTest {
 @Test fun oldBackupsUse64AndNewBackupsPreserveFractionalHeights() {
  fun backup(c:WidgetConfig)=Catalog.json.encodeToString(Backup(1,c,Catalog.engines.first()))
  val root=Catalog.json.parseToJsonElement(backup(WidgetConfig())).jsonObject.toMutableMap()
  root["config"]=JsonObject(root.getValue("config").jsonObject.filterKeys {it!="heightDp"})
  assertEquals(64f,Validation.parse(JsonObject(root).toString()).config.heightDp,0f)
  for(height in listOf(16f,48.5f,64f,92.75f,256f)) {
   val c=WidgetConfig(heightDp=height)
   assertEquals(c,Validation.parse(backup(c)).config)
  }
 }
 @Test fun invalidHeightIsRejected() {
  for(height in listOf(-1f,0f,15.9f,256.1f,Float.NaN,Float.POSITIVE_INFINITY)) {
   assertTrue(runCatching {Validation.config(WidgetConfig(heightDp=height))}.isFailure)
  }
 }
 @Test fun proportionsAndTouchCellsFitAtEverySupportedHeight() {
  for(width in listOf(180f,220f,356f,600f,1000f))for(count in 0..3)for(height in listOf(16f,48.5f,64f,96f,256f)) {
   val d=WidgetDimensions(width,height,count)
   assertTrue(d.scale>0)
   assertTrue(64*d.scale<=height+.001f)
   assertTrue((104+count*52)*d.scale<=width+.001f)
   assertEquals(height,(64+2*d.verticalInset)*d.scale,.001f)
  }
  assertEquals(1f,WidgetDimensions(356f,64f,3).scale,0f)
  assertEquals(.75f,WidgetDimensions(356f,48f,2).scale,0f)
  assertEquals(1.5f,WidgetDimensions(356f,96f,2).scale,0f)
 }
}
