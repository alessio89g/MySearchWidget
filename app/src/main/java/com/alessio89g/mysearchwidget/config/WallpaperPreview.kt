package com.alessio89g.mysearchwidget.config

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Environment
import com.alessio89g.mysearchwidget.R
import com.alessio89g.mysearchwidget.i18n.tr

object WallpaperPreview {
 fun allowed(context:Context)=if(Build.VERSION.SDK_INT>=33)context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)==PackageManager.PERMISSION_GRANTED
  else context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
 fun load(context:Context):Bitmap {
  check(allowed(context))
  val manager=WallpaperManager.getInstance(context)
  check(manager.wallpaperInfo==null){tr(R.string.wallpaper_live)}
  // Read only the current Home wallpaper. No storage scan, copies or network access.
  return manager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)?.use {file->
   ImageDecoder.decodeBitmap(ImageDecoder.createSource(java.util.concurrent.Callable {android.content.res.AssetFileDescriptor(file,0,android.content.res.AssetFileDescriptor.UNKNOWN_LENGTH)})) {decoder,info,_->
    val scale=1200f/maxOf(info.size.width,info.size.height).coerceAtLeast(1200)
    decoder.setTargetSize((info.size.width*scale).toInt().coerceAtLeast(1),(info.size.height*scale).toInt().coerceAtLeast(1))
    decoder.allocator=ImageDecoder.ALLOCATOR_SOFTWARE
   }
  } ?: run {
   val drawable=manager.drawable ?: error(tr(R.string.wallpaper_unavailable))
   val width=drawable.intrinsicWidth.coerceAtLeast(1);val height=drawable.intrinsicHeight.coerceAtLeast(1)
   val scale=1200f/maxOf(width,height).coerceAtLeast(1200)
   Bitmap.createBitmap((width*scale).toInt().coerceAtLeast(1),(height*scale).toInt().coerceAtLeast(1),Bitmap.Config.ARGB_8888).also {
    drawable.setBounds(0,0,it.width,it.height);drawable.draw(android.graphics.Canvas(it));manager.forgetLoadedWallpaper()
   }
  }
 }
}
