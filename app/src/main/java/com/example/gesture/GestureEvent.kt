package com.example.gesture

import com.example.vision.Handedness

/**
 * Lifecycle state transitions for gestures over time.
 */
enum class GestureLifecycleState {
  DETECTED,
  HELD,
  REPEATED,
  CHANGED,
  RELEASED
}

/**
 * Encapsulates a recognized gesture event with confidence, timing, and handedness.
 */
data class RecognizedGesture(
  val type: GestureType,
  val confidence: Float,
  val state: GestureLifecycleState,
  val handedness: Handedness = Handedness.UNKNOWN,
  val timestamp: Long = System.currentTimeMillis(),
  val holdDurationMs: Long = 0L,
  val details: String = ""
)
