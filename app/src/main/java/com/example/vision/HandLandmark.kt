package com.example.vision

import android.graphics.RectF
import androidx.compose.ui.geometry.Offset

/**
 * 21 Hand Landmark indices corresponding to standard hand anatomical topology:
 * 0: WRIST
 * 1-4: THUMB (CMC, MCP, IP, TIP)
 * 5-8: INDEX (MCP, PIP, DIP, TIP)
 * 9-12: MIDDLE (MCP, PIP, DIP, TIP)
 * 13-16: RING (MCP, PIP, DIP, TIP)
 * 17-20: PINKY (MCP, PIP, DIP, TIP)
 */
enum class LandmarkIndex(val id: Int, val label: String) {
  WRIST(0, "Wrist"),
  THUMB_CMC(1, "Thumb CMC"),
  THUMB_MCP(2, "Thumb MCP"),
  THUMB_IP(3, "Thumb IP"),
  THUMB_TIP(4, "Thumb Tip"),
  INDEX_MCP(5, "Index MCP"),
  INDEX_PIP(6, "Index PIP"),
  INDEX_DIP(7, "Index DIP"),
  INDEX_TIP(8, "Index Tip"),
  MIDDLE_MCP(9, "Middle MCP"),
  MIDDLE_PIP(10, "Middle PIP"),
  MIDDLE_DIP(11, "Middle DIP"),
  MIDDLE_TIP(12, "Middle Tip"),
  RING_MCP(13, "Ring MCP"),
  RING_PIP(14, "Ring PIP"),
  RING_DIP(15, "Ring DIP"),
  RING_TIP(16, "Ring Tip"),
  PINKY_MCP(17, "Pinky MCP"),
  PINKY_PIP(18, "Pinky PIP"),
  PINKY_DIP(19, "Pinky DIP"),
  PINKY_TIP(20, "Pinky Tip");

  companion object {
    fun fromId(id: Int): LandmarkIndex = entries.firstOrNull { it.id == id } ?: WRIST
  }
}

/**
 * Normalized 3D Hand Landmark coordinate (0.0 to 1.0) with visibility.
 */
data class HandLandmark(
  val index: LandmarkIndex,
  val x: Float, // Normalized horizontal position [0.0, 1.0]
  val y: Float, // Normalized vertical position [0.0, 1.0]
  val z: Float = 0f, // Normalized depth relative to wrist
  val visibility: Float = 1.0f
) {
  fun toOffset(viewWidth: Float, viewHeight: Float): Offset {
    return Offset(x * viewWidth, y * viewHeight)
  }
}

/**
 * Handedness classification.
 */
enum class Handedness {
  LEFT,
  RIGHT,
  UNKNOWN
}

/**
 * Complete result of hand tracking for a single detected hand.
 */
data class HandResult(
  val id: Int,
  val landmarks: List<HandLandmark>,
  val handedness: Handedness = Handedness.UNKNOWN,
  val confidence: Float = 0.9f,
  val boundingBox: RectF = RectF(),
  val palmCenter: Offset = Offset.Zero,
  val palmSize: Float = 0.2f
) {
  fun getLandmark(index: LandmarkIndex): HandLandmark {
    return landmarks.firstOrNull { it.index == index }
      ?: HandLandmark(index, 0.5f, 0.5f, 0f, 0f)
  }
}

/**
 * Skeletal connection between two hand landmarks for rendering bone structures.
 */
data class SkeletalBone(
  val start: LandmarkIndex,
  val end: LandmarkIndex,
  val fingerType: FingerType
)

enum class FingerType {
  PALM,
  THUMB,
  INDEX,
  MIDDLE,
  RING,
  PINKY
}

object HandTopology {
  val BONES: List<SkeletalBone> = listOf(
    // Palm Base
    SkeletalBone(LandmarkIndex.WRIST, LandmarkIndex.THUMB_CMC, FingerType.PALM),
    SkeletalBone(LandmarkIndex.WRIST, LandmarkIndex.INDEX_MCP, FingerType.PALM),
    SkeletalBone(LandmarkIndex.INDEX_MCP, LandmarkIndex.MIDDLE_MCP, FingerType.PALM),
    SkeletalBone(LandmarkIndex.MIDDLE_MCP, LandmarkIndex.RING_MCP, FingerType.PALM),
    SkeletalBone(LandmarkIndex.RING_MCP, LandmarkIndex.PINKY_MCP, FingerType.PALM),
    SkeletalBone(LandmarkIndex.WRIST, LandmarkIndex.PINKY_MCP, FingerType.PALM),

    // Thumb
    SkeletalBone(LandmarkIndex.THUMB_CMC, LandmarkIndex.THUMB_MCP, FingerType.THUMB),
    SkeletalBone(LandmarkIndex.THUMB_MCP, LandmarkIndex.THUMB_IP, FingerType.THUMB),
    SkeletalBone(LandmarkIndex.THUMB_IP, LandmarkIndex.THUMB_TIP, FingerType.THUMB),

    // Index Finger
    SkeletalBone(LandmarkIndex.INDEX_MCP, LandmarkIndex.INDEX_PIP, FingerType.INDEX),
    SkeletalBone(LandmarkIndex.INDEX_PIP, LandmarkIndex.INDEX_DIP, FingerType.INDEX),
    SkeletalBone(LandmarkIndex.INDEX_DIP, LandmarkIndex.INDEX_TIP, FingerType.INDEX),

    // Middle Finger
    SkeletalBone(LandmarkIndex.MIDDLE_MCP, LandmarkIndex.MIDDLE_PIP, FingerType.MIDDLE),
    SkeletalBone(LandmarkIndex.MIDDLE_PIP, LandmarkIndex.MIDDLE_DIP, FingerType.MIDDLE),
    SkeletalBone(LandmarkIndex.MIDDLE_DIP, LandmarkIndex.MIDDLE_TIP, FingerType.MIDDLE),

    // Ring Finger
    SkeletalBone(LandmarkIndex.RING_MCP, LandmarkIndex.RING_PIP, FingerType.RING),
    SkeletalBone(LandmarkIndex.RING_PIP, LandmarkIndex.RING_DIP, FingerType.RING),
    SkeletalBone(LandmarkIndex.RING_DIP, LandmarkIndex.RING_TIP, FingerType.RING),

    // Pinky Finger
    SkeletalBone(LandmarkIndex.PINKY_MCP, LandmarkIndex.PINKY_PIP, FingerType.PINKY),
    SkeletalBone(LandmarkIndex.PINKY_PIP, LandmarkIndex.PINKY_DIP, FingerType.PINKY),
    SkeletalBone(LandmarkIndex.PINKY_DIP, LandmarkIndex.PINKY_TIP, FingerType.PINKY)
  )
}
