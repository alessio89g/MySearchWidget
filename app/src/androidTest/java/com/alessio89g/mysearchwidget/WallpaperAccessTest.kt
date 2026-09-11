package com.alessio89g.mysearchwidget

import androidx.test.platform.app.InstrumentationRegistry
import com.alessio89g.mysearchwidget.config.WallpaperPreview
import org.junit.Assert.*
import org.junit.Test

/** Run twice externally with access denied and allowed: revoking the app-op kills this UID. */
class WallpaperAccessTest {
 @Test fun wallpaperReadRespectsPermissionAndBoundsDecodedBitmap() {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  when(InstrumentationRegistry.getArguments().getString("expectedWallpaperAccess")) {
   "allowed"->assertTrue(WallpaperPreview.allowed(context))
   "denied"->assertFalse(WallpaperPreview.allowed(context))
  }
  if(!WallpaperPreview.allowed(context)) {
   assertTrue(runCatching {WallpaperPreview.load(context)}.isFailure)
  } else {
   val bitmap=WallpaperPreview.load(context)
   assertTrue(bitmap.width in 1..1200 && bitmap.height in 1..1200)
   bitmap.recycle()
  }
 }
}
