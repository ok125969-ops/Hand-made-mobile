package com.example.gesture

import com.example.vision.HandResult
import com.example.vision.Handedness

/**
 * Configuration parameters for gesture stabilization and temporal state machine.
 */
data class StabilizerConfig(
  val debounceFrames: Int = 3,
  val minConfidence: Float = 0.70f,
  val holdDurationThresholdMs: Long = 350L,
  val repeatIntervalMs: Long = 250L,
  val releaseCooldownMs: Long = 200L,
  val actionCooldownMs: Long = 450L
)

/**
 * Gesture State Machine & Temporal Stabilizer.
 * Eliminates single-frame classification flicker and manages lifecycle states.
 */
class GestureStabilizer(
  private val config: StabilizerConfig = StabilizerConfig()
) {

  private var candidateGesture: GestureType = GestureType.NONE
  private var candidateCount: Int = 0
  private var candidateConfidence: Float = 0f

  private var currentActiveGesture: GestureType = GestureType.NONE
  private var activeGestureStartTime: Long = 0L
  private var lastRepeatTime: Long = 0L
  private var lastActionTriggerTime: Long = 0L
  private var isHeldEmitted: Boolean = false

  /**
   * Processes a raw recognized gesture from current frame and produces a stabilized lifecycle event.
   */
  fun stabilize(
    rawGesture: GestureType,
    rawConfidence: Float,
    handedness: Handedness = Handedness.UNKNOWN,
    isDynamic: Boolean = false
  ): RecognizedGesture? {
    val now = System.currentTimeMillis()

    // 1. Dynamic gestures bypass hold state machine and trigger with cooldown
    if (isDynamic && rawGesture.category == GestureCategory.DYNAMIC) {
      if (now - lastActionTriggerTime >= config.actionCooldownMs) {
        lastActionTriggerTime = now
        return RecognizedGesture(
          type = rawGesture,
          confidence = rawConfidence,
          state = GestureLifecycleState.DETECTED,
          handedness = handedness,
          timestamp = now,
          details = "Dynamic motion gesture triggered"
        )
      }
      return null
    }

    // 2. Debounce and Hysteresis filtering for static gestures
    if (rawGesture == candidateGesture && rawConfidence >= config.minConfidence) {
      candidateCount++
    } else {
      candidateGesture = rawGesture
      candidateCount = 1
      candidateConfidence = rawConfidence
    }

    // Hand released or lost
    if (rawGesture == GestureType.NONE) {
      if (currentActiveGesture != GestureType.NONE) {
        val releasedGesture = currentActiveGesture
        currentActiveGesture = GestureType.NONE
        candidateGesture = GestureType.NONE
        candidateCount = 0
        isHeldEmitted = false

        return RecognizedGesture(
          type = releasedGesture,
          confidence = 1.0f,
          state = GestureLifecycleState.RELEASED,
          handedness = handedness,
          timestamp = now,
          holdDurationMs = now - activeGestureStartTime,
          details = "Gesture released"
        )
      }
      return null
    }

    // Candidate has passed debounce threshold
    if (candidateCount >= config.debounceFrames) {
      if (currentActiveGesture != candidateGesture) {
        // Gesture Changed or Initial Detect
        val previous = currentActiveGesture
        currentActiveGesture = candidateGesture
        activeGestureStartTime = now
        lastRepeatTime = now
        isHeldEmitted = false

        val state = if (previous == GestureType.NONE) {
          GestureLifecycleState.DETECTED
        } else {
          GestureLifecycleState.CHANGED
        }

        return RecognizedGesture(
          type = currentActiveGesture,
          confidence = candidateConfidence,
          state = state,
          handedness = handedness,
          timestamp = now,
          details = "Gesture detected and stabilized"
        )
      } else {
        // Same gesture continues to be held
        val holdDuration = now - activeGestureStartTime

        // Check if hold threshold reached
        if (!isHeldEmitted && holdDuration >= config.holdDurationThresholdMs) {
          isHeldEmitted = true
          lastRepeatTime = now

          val holdGestureType = mapToHoldType(currentActiveGesture)
          return RecognizedGesture(
            type = holdGestureType,
            confidence = candidateConfidence,
            state = GestureLifecycleState.HELD,
            handedness = handedness,
            timestamp = now,
            holdDurationMs = holdDuration,
            details = "Gesture hold threshold reached"
          )
        }

        // Check for continuous repeat ticks (e.g. for continuous volume or scroll)
        if (isHeldEmitted && (now - lastRepeatTime) >= config.repeatIntervalMs) {
          lastRepeatTime = now
          val holdGestureType = mapToHoldType(currentActiveGesture)
          return RecognizedGesture(
            type = holdGestureType,
            confidence = candidateConfidence,
            state = GestureLifecycleState.REPEATED,
            handedness = handedness,
            timestamp = now,
            holdDurationMs = holdDuration,
            details = "Hold repeat tick"
          )
        }
      }
    }

    return null
  }

  private fun mapToHoldType(gesture: GestureType): GestureType {
    return when (gesture) {
      GestureType.PINCH -> GestureType.PINCH_HOLD
      GestureType.OPEN_PALM -> GestureType.OPEN_PALM_HOLD
      GestureType.CLOSED_FIST -> GestureType.FIST_HOLD
      GestureType.POINTING -> GestureType.POINT_HOLD
      else -> gesture
    }
  }

  fun reset() {
    candidateGesture = GestureType.NONE
    candidateCount = 0
    candidateConfidence = 0f
    currentActiveGesture = GestureType.NONE
    activeGestureStartTime = 0L
    lastRepeatTime = 0L
    isHeldEmitted = false
  }
}
