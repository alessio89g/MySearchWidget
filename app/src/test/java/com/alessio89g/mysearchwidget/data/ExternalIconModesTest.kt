package com.alessio89g.mysearchwidget.data

import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.encodeToString

class ExternalIconModesTest {
 @Test fun selectedAssetAndActionFollowIndependentLinksAndBackupRoundTrip() {
  var c=ModeSettings.toggle(WidgetConfig(),"icon.0",true,false)
  val current=ModeSettings.resolve(c,true)
  val action=Shortcut("app","example.app","Example")
  val slot=current.buttons[0].copy(icon=current.buttons[0].icon.copy(asset="pack-icon"),tap=action)
  val bytes=Asset("image","iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIHWP4z8DwHwAFgAI/ScLttAAAAABJRU5ErkJggg==")
  c=ModeSettings.edit(c,current.copy(assets=current.assets+("pack-icon" to bytes),buttons=current.buttons.mapIndexed {i,b->if(i==0)slot else b}),true)
  c=Catalog.json.decodeFromString(Catalog.json.encodeToString(c))
  assertEquals("",ModeSettings.resolve(c,false).buttons[0].icon.asset)
  assertEquals("pack-icon",ModeSettings.resolve(c,true).buttons[0].icon.asset)
  assertEquals(bytes,c.assets["pack-icon"])
  assertEquals(action,ModeSettings.resolve(c,false).buttons[0].tap)
  assertEquals(action,ModeSettings.resolve(c,true).buttons[0].tap)
 }
 @Test fun logoImageSharesByDefaultAndRetainsStyling() {
  val c=WidgetConfig();val icon=c.logo.icon.copy(asset="app-icon",outline=true,monochrome=true,lightGradient=Gradient(true,"#112233","#abcdef",30f))
  val edited=ModeSettings.edit(c,c.copy(logo=c.logo.copy(icon=icon)),false)
  assertEquals("app-icon",ModeSettings.resolve(edited,true).logo.icon.asset)
  assertTrue(ModeSettings.resolve(edited,true).logo.icon.outline)
  assertEquals(icon.lightGradient,ModeSettings.resolve(edited,false).logo.icon.lightGradient)
 }
 @Test fun outerOutlineSettingPersistsAndFollowsIconLink() {
  var c=ModeSettings.toggle(WidgetConfig(),"icon.0",true,false)
  val dark=ModeSettings.resolve(c,true)
  c=ModeSettings.edit(c,dark.copy(buttons=dark.buttons.mapIndexed {i,b->if(i==0)b.copy(icon=b.icon.copy(keepOuterOutline=true)) else b}),true)
  c=Catalog.json.decodeFromString(Catalog.json.encodeToString(c))
  assertTrue(ModeSettings.resolve(c,true).buttons[0].icon.keepOuterOutline)
  assertFalse(ModeSettings.resolve(c,false).buttons[0].icon.keepOuterOutline)
  assertFalse(Catalog.json.decodeFromString<IconSpec>("{}").keepOuterOutline)
 }

}
