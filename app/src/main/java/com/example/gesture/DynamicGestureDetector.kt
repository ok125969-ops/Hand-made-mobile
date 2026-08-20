package com.example.gesture

import com.example.vision.HandResult
import com.example.vision.LandmarkIndex
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Historical track point for trajectory analysis.
 */
data class TrackPoint(
  val x: Float,
  val y: Float,
  val z: Float,
  val palmSize: Float,
  val timestamp: Long
)

/**
 * Analyzes temporal movement of hand landmarks over sliding windows (200ms - 800ms).
 * Detects dynamic gestures: Swipes, Continuous Movement, Push/Pull, and Circular motion.
 */
class DynamicGestureDetector {

  private val history = mutableListOf<TrackPoint>()
  private val maxHistoryDurationMs = 800L
  private var lastDynamicTriggerTime = 0L
  private val dynamicCooldownMs = 400L

  /**
   * Feeds the latest HandResult and evaluates whether a dynamic motion gesture occurred.
   */
  fun processMotion(hand: HandResult?, sensitivity: Float = 1.0f): Pair<GestureType, Float>? {
    val now = System.currentTimeMillis()

    if (now - lastDynamicTriggerTime < dynamicCooldownMs) {
      return null
    }

    if (hand == null) {
      // Clear old history if hand lost for more than 300ms
      if (history.isNotEmpty() && now - history.last().timestamp > 300) {
        history.clear()
      }
      return null
    }

    val wrist = hand.getLandmark(LandmarkIndex.WRIST)
    val point = TrackPoint(
      x = hand.palmCenter.x,
      y = hand.palmCenter.y,
      z = wrist.z,
      palmSize = hand.palmSize,
      timestamp = now
    )

    history.add(point)

    // Prune history older than window
    history.removeAll { now - it.timestamp > maxHistoryDurationMs }

    if (history.size < 4) {
      return null
    }

    val first = history.first()
    val last = history.last()
    val dt = (last.timestamp - first.timestamp).toFloat()

    if (dt < 100f) return null // Need at least 100ms for motion estimation

    val dx = last.x - first.x
    val dy = last.y - first.y
    val dz = last.z - first.z
    val scaleChange = (last.palmSize - first.palmSize) / (first.palmSize.coerceAtLeast(0.01f))

    val distance = sqrt(dx * dx + dy * dy)
    val velocity = (distance / (dt / 1000f)) // units per second

    val minSwipeDistance = 0.18f / sensitivity
    val minSwipeVelocity = 0.45f / sensitivity

    // 1. PUSH FORWARD / PULL BACK (Rapid scale or Z depth change)
    if (scaleChange > 0.35f * (1f / sensitivity)) {
      lastDynamicTriggerTime = now
      history.clear()
      return Pair(GestureType.PUSH_FORWARD, 0.92f)
    } else if (scaleChange < -0.30f * (1f / sensitivity)) {
      lastDynamicTriggerTime = now
      history.clear()
      return Pair(GestureType.PULL_BACK, 0.90f)
    }

    // 2. CIRCULAR MOTION (Accumulate angular changes around center)
    if (history.size >= 8 && distance < 0.25f) {
      val isCircle = detectCircularTrajectory(history)
      if (isCircle) {
        lastDynamicTriggerTime = now
        history.clear()
        return Pair(GestureType.CIRCULAR_MOTION, 0.88f)
      }
    }

    // 3. FAST SWIPES (High velocity + directional linearity)
    if (distance > minSwipeDistance && velocity > minSwipeVelocity) {
      val isHorizontal = abs(dx) > abs(dy) * 1.3f
      val isVertical = abs(dy) > abs(dx) * 1.3f

      if (isHorizontal) {
        lastDynamicTriggerTime = now
        history.clear()
        return if (dx > 0) {
          Pair(GestureType.SWIPE_RIGHT, 0.94f)
        } else {
          Pair(GestureType.SWIPE_LEFT, 0.94f)
        }
      } else if (isVertical) {
        lastDynamicTriggerTime = now
        history.clear()
        return if (dy > 0) {
          Pair(GestureType.SWIPE_DOWN, 0.94f)
        } else {
          Pair(GestureType.SWIPE_UP, 0.94f)
        }
      }
    }

    // 4. SMOOTH CONTINUOUS MOVEMENT (Slower velocity shifts)
    val minMoveDist = 0.10f / sensitivity
    if (distance > minMoveDist && velocity in 0.15f..0.60f) {
      if (abs(dx) > abs(dy) * 1.5f) {
        return if (dx > 0) Pair(GestureType.HAND_MOVE_RIGHT, 0.82f) else Pair(GestureType.HAND_MOVE_LEFT, 0.82f)
      } else if (abs(dy) > abs(dx) * 1.5f) {
        return if (dy > 0) Pair(GestureType.HAND_MOVE_DOWN, 0.82f) else Pair(GestureType.HAND_MOVE_UP, 0.82f)
      }
    }

    return null
  }

  private fun detectCircularTrajectory(points: List<TrackPoint>): Boolean {
    // Calculate mean center
    val avgX = points.map { it.x }.average().toFloat()
    val avgY = points.map { it.y }.average().toFloat()

    var totalAngle = 0.0
    var lastAngle = atan2((points[0].y - avgY).toDouble(), (points[0].x - avgX).toDouble())

    for (i in 1 until points.size) {
      val currentAngle = atan2((points[i].y - avgY).toDouble(), (points[i].x - avgX).toDouble())
      var dAngle = currentAngle - lastAngle

      // Normalize to [-pi, pi]
      while (dAngle > Math.PI) dAngle -= 2 * Math.PI
      while (dAngle < -Math.PI) dAngle += 2 * Math.PI

      totalAngle += dAngle
      lastAngle = currentAngle
    }

    // A circle sweeps at least 250 degrees (4.36 radians) in one direction
    return abs(totalAngle) > 4.36
  }

  fun reset() {
    history.clear()
    lastDynamicTriggerTime = 0L
  }
}
