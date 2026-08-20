package com.example.gesture

import com.example.vision.FingerGeometry
import com.example.vision.HandLandmark
import com.example.vision.HandResult
import com.example.vision.LandmarkIndex
import kotlin.math.abs

/**
 * Geometric Landmark Gesture Classifier.
 * Analyzes the 21 normalized landmarks, finger extension angles, and relative distances.
 */
class GestureRecognizer {

  /**
   * Classifies a static hand posture from a single frame's HandResult.
   */
  fun recognizeStaticGesture(hand: HandResult, sensitivity: Float = 1.0f): Pair<GestureType, Float> {
    if (hand.landmarks.size < 21) {
      return Pair(GestureType.NONE, 0.0f)
    }

    val palmScale = FingerGeometry.computePalmScale(hand)

    // Evaluate extension state of each finger
    val isThumbExt = FingerGeometry.isThumbExtended(hand)
    val isIndexExt = FingerGeometry.isFingerExtended(
      hand, LandmarkIndex.INDEX_MCP, LandmarkIndex.INDEX_PIP, LandmarkIndex.INDEX_DIP, LandmarkIndex.INDEX_TIP
    )
    val isMiddleExt = FingerGeometry.isFingerExtended(
      hand, LandmarkIndex.MIDDLE_MCP, LandmarkIndex.MIDDLE_PIP, LandmarkIndex.MIDDLE_DIP, LandmarkIndex.MIDDLE_TIP
    )
    val isRingExt = FingerGeometry.isFingerExtended(
      hand, LandmarkIndex.RING_MCP, LandmarkIndex.RING_PIP, LandmarkIndex.RING_DIP, LandmarkIndex.RING_TIP
    )
    val isPinkyExt = FingerGeometry.isFingerExtended(
      hand, LandmarkIndex.PINKY_MCP, LandmarkIndex.PINKY_PIP, LandmarkIndex.PINKY_DIP, LandmarkIndex.PINKY_TIP
    )

    // Evaluate curl state
    val isIndexCurled = FingerGeometry.isFingerCurled(hand, LandmarkIndex.INDEX_MCP, LandmarkIndex.INDEX_PIP, LandmarkIndex.INDEX_TIP)
    val isMiddleCurled = FingerGeometry.isFingerCurled(hand, LandmarkIndex.MIDDLE_MCP, LandmarkIndex.MIDDLE_PIP, LandmarkIndex.MIDDLE_TIP)
    val isRingCurled = FingerGeometry.isFingerCurled(hand, LandmarkIndex.RING_MCP, LandmarkIndex.RING_PIP, LandmarkIndex.RING_TIP)
    val isPinkyCurled = FingerGeometry.isFingerCurled(hand, LandmarkIndex.PINKY_MCP, LandmarkIndex.PINKY_PIP, LandmarkIndex.PINKY_TIP)

    // Check Pinch
    val pinchThreshold = 0.35f * sensitivity
    val isPinch = FingerGeometry.isPinching(hand, pinchThreshold)

    val thumbTip = hand.getLandmark(LandmarkIndex.THUMB_TIP)
    val indexTip = hand.getLandmark(LandmarkIndex.INDEX_TIP)
    val middleTip = hand.getLandmark(LandmarkIndex.MIDDLE_TIP)
    val wrist = hand.getLandmark(LandmarkIndex.WRIST)

    // 1. PINCH / OK GESTURE
    if (isPinch) {
      if (isMiddleExt && isRingExt) {
        return Pair(GestureType.OK_GESTURE, 0.94f)
      } else if (isMiddleCurled && isRingCurled && isPinkyCurled) {
        return Pair(GestureType.PINCH, 0.92f)
      }
    }

    // 2. OPEN PALM (all 5 fingers extended)
    if (isThumbExt && isIndexExt && isMiddleExt && isRingExt && isPinkyExt) {
      return Pair(GestureType.OPEN_PALM, 0.96f)
    }

    // 3. CLOSED FIST (all 4 fingers curled)
    if (isIndexCurled && isMiddleCurled && isRingCurled && isPinkyCurled) {
      // Check Thumb Up / Down orientation
      val thumbMcp = hand.getLandmark(LandmarkIndex.THUMB_MCP)
      val dy = thumbTip.y - thumbMcp.y

      if (isThumbExt && abs(dy) > 0.05f) {
        if (dy < -0.05f) {
          return Pair(GestureType.THUMB_UP, 0.95f)
        } else if (dy > 0.05f) {
          return Pair(GestureType.THUMB_DOWN, 0.95f)
        }
      }

      return Pair(GestureType.CLOSED_FIST, 0.93f)
    }

    // 4. POINTING (Index extended, others curled)
    if (isIndexExt && isMiddleCurled && isRingCurled && isPinkyCurled) {
      return Pair(GestureType.POINTING, 0.95f)
    }

    // 5. VICTORY / PEACE (Index and Middle extended in V-shape, others curled)
    if (isIndexExt && isMiddleExt && isRingCurled && isPinkyCurled) {
      val tipDist = FingerGeometry.normalizedDistance(indexTip, middleTip, palmScale)
      if (tipDist > 0.3f) {
        return Pair(GestureType.VICTORY_PEACE, 0.95f)
      }
    }

    // Fallback: Check if majority fingers extended
    val extendedCount = (if (isThumbExt) 1 else 0) +
      (if (isIndexExt) 1 else 0) +
      (if (isMiddleExt) 1 else 0) +
      (if (isRingExt) 1 else 0) +
      (if (isPinkyExt) 1 else 0)

    if (extendedCount >= 4) {
      return Pair(GestureType.OPEN_PALM, 0.82f)
    }

    return Pair(GestureType.NONE, 0.40f)
  }
}
