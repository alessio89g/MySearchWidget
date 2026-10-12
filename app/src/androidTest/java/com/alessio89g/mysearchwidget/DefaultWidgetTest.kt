package com.alessio89g.mysearchwidget

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Parcel
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.data.*
import com.alessio89g.mysearchwidget.widget.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DefaultWidgetTest {
 private val ins=InstrumentationRegistry.getInstrumentation()
 private val context=ins.targetContext
 @Test fun presetMatchesBackupWithSystemThemeAndCenteredThirdIcon() {
  val original=ins.context.assets.open("default-reference.json").bufferedReader().use {Validation.parse(it.readText())}
  val preset=DefaultWidget.create(context)
  assertEquals(original.config.copy(theme="system",buttons=original.config.buttons.mapIndexed {i,b->if(i==2)b.copy(iconPosition=b.iconPosition.copy(x=0f)) else b}),preset)
  val layout=ElementLayout(374f,preset)
  assertEquals(layout.buttonBounds[2].centerX,layout.iconBounds[2].centerX,.001f)
  Assets.validate(context,preset.assets)
  assertEquals(Catalog.engines.first(),original.engine)
 }
 @Test fun newSlotsUsePresetWithoutSavingOrChangingReferenceWidth()=runBlocking {
  val repo=Repository(context);val id=80134
  assertFalse(repo.hasConfig(id))
  assertEquals(DefaultWidget.create(context),repo.config(id))
  assertEquals(DefaultWidget.create(context),repo.anchorWidth(id,430))
  assertFalse(repo.hasConfig(id));assertTrue(repo.library(setOf(id)).isEmpty())
 }
 @Test fun storedWidgetsAndLegacyBackupDefaultsStayUnchanged()=runBlocking {
  val repo=Repository(context);val id=80135
  // A sparse old schema keeps its historical serialization defaults.
  val legacy=Catalog.json.decodeFromString<WidgetConfig>("{}")
  assertEquals(WidgetConfig(),legacy);assertEquals(2,legacy.count);assertTrue(legacy.dynamic)
  val saved=legacy.copy(theme="dark",placeholder="My existing widget")
  try {
   repo.save(id,saved)
   assertEquals(saved,repo.config(id));repo.confirmPinned(id);assertEquals(saved,repo.config(id))
   assertEquals(saved.copy(referenceWidthDp=410),repo.anchorWidth(id,410))
   val existing=ins.context.assets.open("default-reference.json").bufferedReader().use {Validation.parse(it.readText()).config}
   repo.save(id,existing)
   assertEquals(existing,repo.config(id));assertEquals(existing,repo.anchorWidth(id,430))
   assertEquals("dark",repo.config(id).theme);assertEquals(-2f,repo.config(id).buttons[2].iconPosition.x,0f)
  }finally {repo.remove(id)}
 }
 @Test fun resetsUsePresetWithoutChangingOtherSettingsOrSaving()=runBlocking {
  val preset=DefaultWidget.create(context)
  val modified=preset.copy(heightDp=110f,sizing=preset.sizing.copy(fieldWidthDp=250f,fieldHeightDp=60f),hint=preset.hint.copy(size=30f,weight=700),query=preset.query.copy(size=26f),placement=preset.placement.copy(field=OffsetDp(40f,30f),text=OffsetDp(12f,4f)))
  val repo=Repository(context);val id=80137
  try {
   repo.save(id,modified)
   val height=modified.resetWidgetHeight(preset)
   assertEquals(68f,height.heightDp,0f);assertEquals(preset.sizing.outerWidthDp,height.sizing.outerWidthDp)
   assertEquals(modified.placement,height.placement);assertEquals(modified.buttons,height.buttons)
   val field=modified.resetElementDimensions("field",preset)
   assertEquals(193f,field.sizing.fieldWidthDp!!,0f);assertEquals(47f,field.sizing.fieldHeightDp!!,0f)
   assertEquals(modified.placement,field.placement);assertEquals(modified.heightDp,field.heightDp,0f)
   val text=modified.resetElementDimensions("text",preset)
   assertEquals(12.5f,text.hint.size,0f);assertEquals(15f,text.query.size,0f);assertEquals(700,text.hint.weight)
   assertEquals(preset.placement.field,modified.resetElementPosition("field",preset).placement.field)
   assertEquals(preset.placement.text,modified.resetElementPosition("text",preset).placement.text)
   for(layer in DEFAULT_LAYERS) {
    val dimension=modified.resetElementDimensions(layer,preset)
    assertEquals(modified.placement,dimension.placement)
    assertEquals(dimension,dimension.resetElementDimensions(layer,preset))
    Validation.config(dimension)
    val position=modified.resetElementPosition(layer,preset)
    assertEquals(modified.sizing,position.sizing);assertEquals(modified.heightDp,position.heightDp,0f)
    assertEquals(position,position.resetElementPosition(layer,preset))
   }
   assertEquals("Resets are drafts until Save",modified,repo.config(id))
  }finally {repo.remove(id)}
 }
 @Test fun hostSwitchesThemeAndExportsActualDefaultRendering() {
  ins.runOnMainSync {
   val preset=DefaultWidget.create(context)
   val rv=SearchWidget.views(context,80136,preset,374)
   val parcel=Parcel.obtain()
   val remote=try {rv.writeToParcel(parcel,0);parcel.setDataPosition(0);RemoteViews.CREATOR.createFromParcel(parcel)}finally {parcel.recycle()}
   val images=mutableListOf<Bitmap>()
   for(dark in listOf(false,true)) {
    val cfg=Configuration(context.resources.configuration).apply {
     uiMode=(uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or if(dark)Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    }
    val host=context.createConfigurationContext(cfg)
    val view=remote.apply(host,FrameLayout(host))
    val actual=(view.findViewById<ImageView>(R.id.art).drawable as BitmapDrawable).bitmap
    val expected=Renderer.render(host,preset,374)
    assertTrue(expected.sameAs(actual));images+=expected
   }
   assertFalse(images[0].sameAs(images[1]))
   File(context.filesDir,"default-widget-readme.png").outputStream().use {images[1].compress(Bitmap.CompressFormat.PNG,100,it)}
   images.forEach {it.recycle()}
  }
 }
}
