package com.example.vision

import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Geometric analysis tools for 21-landmark hand topologies.
 * Uses normalized ratios relative to palm size to guarantee distance-, scale-, and resolution-invariance.
 */
object FingerGeometry {

  /**
   * Euclidean 2D distance between two landmarks.
   */
  fun distance2D(p1: HandLandmark, p2: HandLandmark): Float {
    val dx = p1.x - p2.x
    val dy = p1.y - p2.y
    return sqrt(dx * dx + dy * dy)
  }

  /**
   * Euclidean 3D distance between two landmarks.
   */
  fun distance3D(p1: HandLandmark, p2: HandLandmark): Float {
    val dx = p1.x - p2.x
    val dy = p1.y - p2.y
    val dz = p1.z - p2.z
    return sqrt(dx * dx + dy * dy + dz * dz)
  }

  /**
   * Computes the scale of the palm (distance between Wrist and Middle MCP).
   * This baseline normalizes hand size across varying camera distances.
   */
  fun computePalmScale(hand: HandResult): Float {
    val wrist = hand.getLandmark(LandmarkIndex.WRIST)
    val middleMcp = hand.getLandmark(LandmarkIndex.MIDDLE_MCP)
    val dist = distance2D(wrist, middleMcp)
    return if (dist > 0.01f) dist else 0.2f
  }

  /**
   * Normalized distance relative to palm scale.
   */
  fun normalizedDistance(p1: HandLandmark, p2: HandLandmark, palmScale: Float): Float {
    if (palmScale <= 0.001f) return distance2D(p1, p2)
    return distance2D(p1, p2) / palmScale
  }

  /**
   * Angle in degrees at vertex joint B formed by points A - B - C.
   */
  fun angleBetweenJoints(a: HandLandmark, b: HandLandmark, c: HandLandmark): Float {
    val v1x = a.x - b.x
    val v1y = a.y - b.y
    val v2x = c.x - b.x
    val v2y = c.y - b.y

    val dot = v1x * v2x + v1y * v2y
    val mag1 = sqrt(v1x * v1x + v1y * v1y)
    val mag2 = sqrt(v2x * v2x + v2y * v2y)

    if (mag1 < 1e-6f || mag2 < 1e-6f) return 0f
    val cosTheta = (dot / (mag1 * mag2)).coerceIn(-1.0f, 1.0f)
    return Math.toDegrees(acos(cosTheta.toDouble())).toFloat()
  }

  /**
   * Checks if a finger (Index, Middle, Ring, or Pinky) is extended.
   * A finger is extended when its tip is further from the wrist than its PIP joint,
   * and the joint angle at PIP is sufficiently straight (> 140 degrees).
   */
  fun isFingerExtended(
    hand: HandResult,
    mcpIndex: LandmarkIndex,
    pipIndex: LandmarkIndex,
    dipIndex: LandmarkIndex,
    tipIndex: LandmarkIndex
  ): Boolean {
    val wrist = hand.getLandmark(LandmarkIndex.WRIST)
    val mcp = hand.getLandmark(mcpIndex)
    val pip = hand.getLandmark(pipIndex)
    val tip = hand.getLandmark(tipIndex)

    val distWristToTip = distance2D(wrist, tip)
    val distWristToPip = distance2D(wrist, pip)
    val angle = angleBetweenJoints(mcp, pip, tip)

    return distWristToTip > distWristToPip * 1.05f && angle > 135f
  }

  /**
   * Checks if a finger is curled (folded towards palm).
   */
  fun isFingerCurled(
    hand: HandResult,
    mcpIndex: LandmarkIndex,
    pipIndex: LandmarkIndex,
    tipIndex: LandmarkIndex
  ): Boolean {
    val wrist = hand.getLandmark(LandmarkIndex.WRIST)
    val mcp = hand.getLandmark(mcpIndex)
    val pip = hand.getLandmark(pipIndex)
    val tip = hand.getLandmark(tipIndex)

    val distWristToTip = distance2D(wrist, tip)
    val distWristToPip = distance2D(wrist, pip)
    val distMcpToTip = distance2D(mcp, tip)

    return distWristToTip <= distWristToPip * 1.05f || distMcpToTip < distance2D(mcp, pip) * 0.9f
  }

  /**
   * Checks if Thumb is extended.
   */
  fun isThumbExtended(hand: HandResult): Boolean {
    val cmc = hand.getLandmark(LandmarkIndex.THUMB_CMC)
    val mcp = hand.getLandmark(LandmarkIndex.THUMB_MCP)
    val ip = hand.getLandmark(LandmarkIndex.THUMB_IP)
    val tip = hand.getLandmark(LandmarkIndex.THUMB_TIP)
    val indexMcp = hand.getLandmark(LandmarkIndex.INDEX_MCP)

    val palmScale = computePalmScale(hand)
    val thumbSpread = normalizedDistance(tip, indexMcp, palmScale)
    val angle = angleBetweenJoints(mcp, ip, tip)

    return thumbSpread > 0.45f && angle > 130f
  }

  /**
   * Checks if Thumb and Index finger are in a Pinch configuration.
   */
  fun isPinching(hand: HandResult, threshold: Float = 0.35f): Boolean {
    val thumbTip = hand.getLandmark(LandmarkIndex.THUMB_TIP)
    val indexTip = hand.getLandmark(LandmarkIndex.INDEX_TIP)
    val palmScale = computePalmScale(hand)

    val normDist = normalizedDistance(thumbTip, indexTip, palmScale)
    return normDist < threshold
  }

  /**
   * Computes Palm Center coordinate.
   */
  fun computePalmCenter(hand: HandResult): Pair<Float, Float> {
    val wrist = hand.getLandmark(LandmarkIndex.WRIST)
    val indexMcp = hand.getLandmark(LandmarkIndex.INDEX_MCP)
    val pinkyMcp = hand.getLandmark(LandmarkIndex.PINKY_MCP)
    val middleMcp = hand.getLandmark(LandmarkIndex.MIDDLE_MCP)

    val centerX = (wrist.x + indexMcp.x + pinkyMcp.x + middleMcp.x) / 4f
    val centerY = (wrist.y + indexMcp.y + pinkyMcp.y + middleMcp.y) / 4f
    return Pair(centerX, centerY)
  }

  /**
   * Evaluates if palm is facing upward, downward, left, or right based on wrist-to-middle vector.
   */
  fun computeHandOrientation(hand: HandResult): Float {
    val wrist = hand.getLandmark(LandmarkIndex.WRIST)
    val middleMcp = hand.getLandmark(LandmarkIndex.MIDDLE_MCP)
    val dx = middleMcp.x - wrist.x
    val dy = middleMcp.y - wrist.y
    return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
  }
}
