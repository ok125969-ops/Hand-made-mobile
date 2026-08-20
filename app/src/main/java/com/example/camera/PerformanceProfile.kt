package com.example.camera

import android.os.Build

/**
 * Performance profile presets adapting to low-end, mid-range, and flagship hardware.
 */
enum class PerformanceMode(val displayName: String, val description: String) {
  AUTO("Auto-Adaptive", "Automatically configures FPS and resolution based on device specs"),
  LOW("Battery Saver / Low-End", "10 FPS, lower resolution, drops frames for minimal CPU usage"),
  BALANCED("Balanced", "20 FPS, standard 480p analysis, optimal responsiveness"),
  HIGH("High Performance", "30 FPS, high fidelity tracking for smooth real-time control")
}

data class PerformanceConfig(
  val targetFps: Int,
  val frameIntervalMs: Long,
  val targetResolutionWidth: Int,
  val targetResolutionHeight: Int,
  val minConfidence: Float,
  val debounceFrames: Int
)

object PerformanceProfileManager {

  fun getConfig(mode: PerformanceMode): PerformanceConfig {
    return when (mode) {
      PerformanceMode.AUTO -> resolveAutoConfig()
      PerformanceMode.LOW -> PerformanceConfig(
        targetFps = 10,
        frameIntervalMs = 100L,
        targetResolutionWidth = 320,
        targetResolutionHeight = 240,
        minConfidence = 0.65f,
        debounceFrames = 2
      )
      PerformanceMode.BALANCED -> PerformanceConfig(
        targetFps = 20,
        frameIntervalMs = 50L,
        targetResolutionWidth = 640,
        targetResolutionHeight = 480,
        minConfidence = 0.70f,
        debounceFrames = 3
      )
      PerformanceMode.HIGH -> PerformanceConfig(
        targetFps = 30,
        frameIntervalMs = 33L,
        targetResolutionWidth = 960,
        targetResolutionHeight = 720,
        minConfidence = 0.75f,
        debounceFrames = 3
      )
    }
  }

  private fun resolveAutoConfig(): PerformanceConfig {
    val cores = Runtime.getRuntime().availableProcessors()
    val maxMemoryMb = (Runtime.getRuntime().maxMemory() / (1024 * 1024)).toInt()

    return if (cores <= 4 || maxMemoryMb < 256) {
      // Low-end device detected
      getConfig(PerformanceMode.LOW)
    } else if (cores >= 8 && maxMemoryMb >= 512) {
      // High-end device detected
      getConfig(PerformanceMode.HIGH)
    } else {
      // Mid-range device
      getConfig(PerformanceMode.BALANCED)
    }
  }
}
