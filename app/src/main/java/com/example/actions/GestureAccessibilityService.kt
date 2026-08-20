package com.example.actions

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

/**
 * Official Android Accessibility Service for hands-free system action execution.
 */
class GestureAccessibilityService : AccessibilityService() {

  companion object {
    private const val TAG = "GestureA11yService"
    private var instanceRef: WeakReference<GestureAccessibilityService>? = null

    val isRunning: Boolean
      get() = instanceRef?.get() != null

    fun getInstance(): GestureAccessibilityService? = instanceRef?.get()

    /**
     * Checks if the Accessibility Service is currently enabled in Android Settings.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
      val expectedServiceName = "${context.packageName}/${GestureAccessibilityService::class.java.canonicalName}"
      val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
      ) ?: return false

      val colonSplitter = TextUtils.SimpleStringSplitter(':')
      colonSplitter.setString(enabledServices)
      while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedServiceName, ignoreCase = true)) {
          return true
        }
      }
      return false
    }
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    instanceRef = WeakReference(this)
    Log.d(TAG, "GestureAccessibilityService connected and ready")
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    // Passive monitoring if needed
  }

  override fun onInterrupt() {
    Log.d(TAG, "GestureAccessibilityService interrupted")
  }

  override fun onDestroy() {
    super.onDestroy()
    instanceRef = null
    Log.d(TAG, "GestureAccessibilityService destroyed")
  }

  /**
   * Dispatches a global navigation action (Back, Home, Recents, etc.)
   */
  fun performGlobal(action: Int): Boolean {
    return try {
      performGlobalAction(action)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to perform global action $action", e)
      false
    }
  }

  /**
   * Performs an automated scroll gesture (up or down).
   */
  fun performScroll(scrollUp: Boolean): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false

    return try {
      val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
      val metrics = DisplayMetrics()
      @Suppress("DEPRECATION")
      windowManager.defaultDisplay.getMetrics(metrics)

      val startX = metrics.widthPixels / 2f
      val startY: Float
      val endY: Float

      if (scrollUp) {
        // Scroll Up: Swipe top to bottom
        startY = metrics.heightPixels * 0.35f
        endY = metrics.heightPixels * 0.75f
      } else {
        // Scroll Down: Swipe bottom to top
        startY = metrics.heightPixels * 0.75f
        endY = metrics.heightPixels * 0.35f
      }

      val path = Path().apply {
        moveTo(startX, startY)
        lineTo(startX, endY)
      }

      val gestureBuilder = GestureDescription.Builder()
      gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0L, 250L))
      dispatchGesture(gestureBuilder.build(), null, null)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to dispatch scroll gesture", e)
      false
    }
  }
}
