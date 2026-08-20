package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CursorOverlayService : Service() {

  companion object {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _cursorState = MutableStateFlow("IDLE")
    val cursorState: StateFlow<String> = _cursorState.asStateFlow()

    fun updateState(state: String) {
      _cursorState.value = state
    }
  }

  private var windowManager: WindowManager? = null
  private var overlayView: View? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    startForegroundServiceNotification()
    initOverlay()
    _isRunning.value = true
  }

  private fun startForegroundServiceNotification() {
    val channelId = "myraa_cursor_channel"
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        channelId,
        "MYRAA Cursor Overlay",
        NotificationManager.IMPORTANCE_LOW
      )
      notificationManager.createNotificationChannel(channel)
    }

    val notification: Notification = NotificationCompat.Builder(this, channelId)
      .setContentTitle("MYRAA Focus Overlay")
      .setContentText("Monitoring UI focus and proactive indicators")
      .setSmallIcon(android.R.drawable.ic_menu_compass)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()

    startForeground(1001, notification)
  }

  private fun initOverlay() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
      return
    }

    try {
      windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
      val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
      } else {
        @Suppress("DEPRECATION")
        WindowManager.LayoutParams.TYPE_PHONE
      }

      val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        layoutType,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
      ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 24
        y = 120
      }

      val frameLayout = FrameLayout(this)
      val icon = ImageView(this).apply {
        setImageResource(android.R.drawable.ic_menu_compass)
        setColorFilter(0xFF00E5FF.toInt())
        alpha = 0.85f
      }
      frameLayout.addView(icon)
      overlayView = frameLayout

      windowManager?.addView(overlayView, params)
    } catch (e: Exception) {
      // Ignored if permissions not granted in test
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    try {
      overlayView?.let { windowManager?.removeView(it) }
    } catch (e: Exception) {
      // ignore
    }
    _isRunning.value = false
  }
}
