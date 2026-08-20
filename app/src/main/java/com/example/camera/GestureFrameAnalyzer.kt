package com.example.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.gesture.DynamicGestureDetector
import com.example.gesture.GestureRecognizer
import com.example.gesture.GestureStabilizer
import com.example.gesture.GestureType
import com.example.gesture.RecognizedGesture
import com.example.vision.HandResult
import com.example.vision.HandTracker

/**
 * Real-time frame analyzer callback listener.
 */
interface GestureAnalysisListener {
  fun onHandsDetected(hands: List<HandResult>, fps: Float, latencyMs: Long)
  fun onGestureRecognized(gesture: RecognizedGesture)
}

/**
 * CameraX ImageAnalysis analyzer running local hand tracking and gesture recognition.
 */
class GestureFrameAnalyzer(
  private val isFrontCamera: Boolean = true,
  private var performanceConfig: PerformanceConfig = PerformanceProfileManager.getConfig(PerformanceMode.BALANCED),
  private var sensitivity: Float = 1.0f,
  private val listener: GestureAnalysisListener
) : ImageAnalysis.Analyzer {

  private val handTracker = HandTracker()
  private val gestureRecognizer = GestureRecognizer()
  private val dynamicGestureDetector = DynamicGestureDetector()
  private val gestureStabilizer = GestureStabilizer()

  private var lastAnalyzedTimestamp: Long = 0L
  private var fpsCounterTimestamp: Long = 0L
  private var frameCountSinceFps: Int = 0
  private var currentFps: Float = 0f

  fun updateConfig(config: PerformanceConfig, newSensitivity: Float) {
    this.performanceConfig = config
    this.sensitivity = newSensitivity
  }

  override fun analyze(imageProxy: ImageProxy) {
    val startTime = System.currentTimeMillis()

    // Adaptive Frame Throttling
    if (startTime - lastAnalyzedTimestamp < performanceConfig.frameIntervalMs) {
      imageProxy.close()
      return
    }
    lastAnalyzedTimestamp = startTime

    try {
      // 1. Hand Region & 21-Landmark Tracking
      val rotationDegrees = imageProxy.imageInfo.rotationDegrees
      val detectedHands = handTracker.processFrame(
        imageProxy = imageProxy,
        isFrontCamera = isFrontCamera,
        rotationDegrees = rotationDegrees
      )

      val primaryHand = detectedHands.firstOrNull()

      // 2. Gesture Classification
      if (detectedHands.size >= 2) {
        // Multi-hand detection
        val multiGesture = gestureStabilizer.stabilize(
          rawGesture = GestureType.TWO_HANDS,
          rawConfidence = 0.95f,
          isDynamic = false
        )
        multiGesture?.let { listener.onGestureRecognized(it) }
      } else if (primaryHand != null) {
        // First check dynamic gesture trajectory
        val dynamicResult = dynamicGestureDetector.processMotion(primaryHand, sensitivity)

        if (dynamicResult != null) {
          val (dynamicGesture, dynamicConf) = dynamicResult
          val stabilizedDynamic = gestureStabilizer.stabilize(
            rawGesture = dynamicGesture,
            rawConfidence = dynamicConf,
            handedness = primaryHand.handedness,
            isDynamic = true
          )
          stabilizedDynamic?.let { listener.onGestureRecognized(it) }
        } else {
          // Static geometric gesture
          val (staticGesture, staticConf) = gestureRecognizer.recognizeStaticGesture(primaryHand, sensitivity)
          val stabilizedStatic = gestureStabilizer.stabilize(
            rawGesture = staticGesture,
            rawConfidence = staticConf,
            handedness = primaryHand.handedness,
            isDynamic = false
          )
          stabilizedStatic?.let { listener.onGestureRecognized(it) }
        }
      } else {
        // Hand lost or absent
        dynamicGestureDetector.processMotion(null, sensitivity)
        val releasedGesture = gestureStabilizer.stabilize(
          rawGesture = GestureType.NONE,
          rawConfidence = 0f,
          isDynamic = false
        )
        releasedGesture?.let { listener.onGestureRecognized(it) }
      }

      val latency = System.currentTimeMillis() - startTime

      // Calculate actual FPS
      frameCountSinceFps++
      if (startTime - fpsCounterTimestamp >= 1000L) {
        currentFps = (frameCountSinceFps * 1000f) / (startTime - fpsCounterTimestamp).coerceAtLeast(1)
        frameCountSinceFps = 0
        fpsCounterTimestamp = startTime
      }

      listener.onHandsDetected(detectedHands, currentFps, latency)
    } catch (e: Exception) {
      // Graceful error recovery
    } finally {
      // Always release image buffer back to camera pipeline
      imageProxy.close()
    }
  }

  fun reset() {
    handTracker.reset()
    dynamicGestureDetector.reset()
    gestureStabilizer.reset()
    lastAnalyzedTimestamp = 0L
  }
}
