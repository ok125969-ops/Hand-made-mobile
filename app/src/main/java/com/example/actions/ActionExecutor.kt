package com.example.actions

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.KeyEvent

data class ExecutionResult(
  val success: Boolean,
  val message: String,
  val action: GestureAction
)

/**
 * Unified Executor for Android system actions, media controls, audio levels, and hardware utilities.
 */
class ActionExecutor(private val context: Context) {

  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
  private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
  private var isTorchOn = false

  /**
   * Executes a given GestureAction with proper fallbacks.
   */
  fun execute(
    action: GestureAction,
    hapticEnabled: Boolean = true,
    soundEnabled: Boolean = false
  ): ExecutionResult {
    if (action == GestureAction.NONE) {
      return ExecutionResult(true, "No action mapped", action)
    }

    if (hapticEnabled) {
      triggerHapticFeedback()
    }

    val a11yService = GestureAccessibilityService.getInstance()

    return when (action) {
      GestureAction.BACK -> {
        if (a11yService != null) {
          val ok = a11yService.performGlobal(AccessibilityService.GLOBAL_ACTION_BACK)
          ExecutionResult(ok, if (ok) "Navigated Back" else "Failed to navigate back", action)
        } else {
          ExecutionResult(false, "Enable Accessibility in Settings for Back action", action)
        }
      }

      GestureAction.HOME -> {
        if (a11yService != null) {
          val ok = a11yService.performGlobal(AccessibilityService.GLOBAL_ACTION_HOME)
          ExecutionResult(ok, if (ok) "Navigated Home" else "Failed to navigate home", action)
        } else {
          // Fallback via Intent
          try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
              addCategory(Intent.CATEGORY_HOME)
              flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)
            ExecutionResult(true, "Navigated Home via Launcher", action)
          } catch (e: Exception) {
            ExecutionResult(false, "Accessibility required for Home", action)
          }
        }
      }

      GestureAction.RECENTS -> {
        if (a11yService != null) {
          val ok = a11yService.performGlobal(AccessibilityService.GLOBAL_ACTION_RECENTS)
          ExecutionResult(ok, if (ok) "Opened Recent Apps" else "Failed to open recents", action)
        } else {
          ExecutionResult(false, "Enable Accessibility for Recents", action)
        }
      }

      GestureAction.NOTIFICATION_SHADE -> {
        if (a11yService != null) {
          val ok = a11yService.performGlobal(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
          ExecutionResult(ok, if (ok) "Opened Notifications" else "Failed to open notifications", action)
        } else {
          ExecutionResult(false, "Enable Accessibility for Notification shade", action)
        }
      }

      GestureAction.QUICK_SETTINGS -> {
        if (a11yService != null) {
          val ok = a11yService.performGlobal(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
          ExecutionResult(ok, if (ok) "Opened Quick Settings" else "Failed to open quick settings", action)
        } else {
          ExecutionResult(false, "Enable Accessibility for Quick Settings", action)
        }
      }

      GestureAction.SCROLL_UP -> {
        if (a11yService != null) {
          val ok = a11yService.performScroll(scrollUp = true)
          ExecutionResult(ok, if (ok) "Scrolled Up" else "Scroll failed", action)
        } else {
          ExecutionResult(false, "Accessibility required for scrolling", action)
        }
      }

      GestureAction.SCROLL_DOWN -> {
        if (a11yService != null) {
          val ok = a11yService.performScroll(scrollUp = false)
          ExecutionResult(ok, if (ok) "Scrolled Down" else "Scroll failed", action)
        } else {
          ExecutionResult(false, "Accessibility required for scrolling", action)
        }
      }

      GestureAction.MEDIA_PLAY_PAUSE -> {
        dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        ExecutionResult(true, "Toggled Play/Pause", action)
      }

      GestureAction.MEDIA_NEXT -> {
        dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        ExecutionResult(true, "Skipped to Next Track", action)
      }

      GestureAction.MEDIA_PREVIOUS -> {
        dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        ExecutionResult(true, "Skipped to Previous Track", action)
      }

      GestureAction.VOLUME_UP -> {
        audioManager?.adjustStreamVolume(
          AudioManager.STREAM_MUSIC,
          AudioManager.ADJUST_RAISE,
          AudioManager.FLAG_SHOW_UI
        )
        val vol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        ExecutionResult(true, "Volume Raised ($vol)", action)
      }

      GestureAction.VOLUME_DOWN -> {
        audioManager?.adjustStreamVolume(
          AudioManager.STREAM_MUSIC,
          AudioManager.ADJUST_LOWER,
          AudioManager.FLAG_SHOW_UI
        )
        val vol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        ExecutionResult(true, "Volume Lowered ($vol)", action)
      }

      GestureAction.MUTE_TOGGLE -> {
        audioManager?.adjustStreamVolume(
          AudioManager.STREAM_MUSIC,
          AudioManager.ADJUST_TOGGLE_MUTE,
          AudioManager.FLAG_SHOW_UI
        )
        ExecutionResult(true, "Toggled Audio Mute", action)
      }

      GestureAction.FLASHLIGHT_TOGGLE -> {
        toggleTorch()
      }

      GestureAction.TAKE_SCREENSHOT -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && a11yService != null) {
          val ok = a11yService.performGlobal(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
          ExecutionResult(ok, if (ok) "Captured Screenshot" else "Screenshot failed", action)
        } else {
          ExecutionResult(false, "Accessibility (Android 9+) required for screenshot", action)
        }
      }

      GestureAction.NONE -> ExecutionResult(true, "None", action)
    }
  }

  private fun dispatchMediaKey(keyCode: Int) {
    try {
      val eventTime = SystemClock.uptimeMillis()
      val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
      val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)

      audioManager?.dispatchMediaKeyEvent(downEvent)
      audioManager?.dispatchMediaKeyEvent(upEvent)
    } catch (e: Exception) {
      Log.e("ActionExecutor", "Failed to dispatch media key $keyCode", e)
    }
  }

  private fun toggleTorch(): ExecutionResult {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraManager != null) {
        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
          val chars = cameraManager.getCameraCharacteristics(id)
          chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
        if (cameraId != null) {
          isTorchOn = !isTorchOn
          cameraManager.setTorchMode(cameraId, isTorchOn)
          ExecutionResult(true, if (isTorchOn) "Flashlight ON" else "Flashlight OFF", GestureAction.FLASHLIGHT_TOGGLE)
        } else {
          ExecutionResult(false, "No flashlight available on device", GestureAction.FLASHLIGHT_TOGGLE)
        }
      } else {
        ExecutionResult(false, "Torch control requires Android 6.0+", GestureAction.FLASHLIGHT_TOGGLE)
      }
    } catch (e: Exception) {
      ExecutionResult(false, "Flashlight error: ${e.message}", GestureAction.FLASHLIGHT_TOGGLE)
    }
  }

  private fun triggerHapticFeedback() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(35L)
      }
    } catch (e: Exception) {
      Log.e("ActionExecutor", "Haptic error", e)
    }
  }
}
