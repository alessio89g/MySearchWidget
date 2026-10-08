package com.alessio89g.mysearchwidget.overlay

import android.content.Context
import android.os.*
import android.media.AudioAttributes
import android.provider.Settings
import com.alessio89g.mysearchwidget.data.*

object WidgetHaptics {
 fun tap(context:Context,config:WidgetConfig,element:String) {
  if(!config.hapticEnabled(element))return
  if(Settings.System.getInt(context.contentResolver,Settings.System.HAPTIC_FEEDBACK_ENABLED,1)==0)return
  val vibrator=context.getSystemService(VibratorManager::class.java)?.defaultVibrator ?: return
  if(!vibrator.hasVibrator())return
  val effect=VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
  runCatching {
   if(Build.VERSION.SDK_INT>=33)vibrator.vibrate(effect,VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_TOUCH).build())
   else vibrator.vibrate(effect,AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build())
  }
 }
}
