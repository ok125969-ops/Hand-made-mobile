package com.example

import com.example.actions.DefaultGestureMappings
import com.example.actions.GestureAction
import com.example.actions.GesturePresetProfile
import com.example.gesture.DynamicGestureDetector
import com.example.gesture.GestureLifecycleState
import com.example.gesture.GestureRecognizer
import com.example.gesture.GestureStabilizer
import com.example.gesture.GestureType
import com.example.gesture.StabilizerConfig
import com.example.vision.FingerGeometry
import com.example.vision.HandLandmark
import com.example.vision.HandResult
import com.example.vision.Handedness
import com.example.vision.LandmarkIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GestureRecognitionTest {

  private lateinit var recognizer: GestureRecognizer
  private lateinit var dynamicDetector: DynamicGestureDetector
  private lateinit var stabilizer: GestureStabilizer

  @Before
  fun setUp() {
    recognizer = GestureRecognizer()
    dynamicDetector = DynamicGestureDetector()
    stabilizer = GestureStabilizer(
      StabilizerConfig(
        debounceFrames = 3,
        minConfidence = 0.7f,
        holdDurationThresholdMs = 150L,
        repeatIntervalMs = 100L,
        actionCooldownMs = 100L
      )
    )
  }

  /**
   * Helper to construct a synthetic 21-landmark HandResult.
   */
  private fun createSyntheticHand(
    isThumbExtended: Boolean = true,
    isIndexExtended: Boolean = true,
    isMiddleExtended: Boolean = true,
    isRingExtended: Boolean = true,
    isPinkyExtended: Boolean = true,
    isPinch: Boolean = false,
    thumbUp: Boolean = false
  ): HandResult {
    val wrist = HandLandmark(LandmarkIndex.WRIST, 0.5f, 0.7f, 0f)
    val landmarks = ArrayList<HandLandmark>(21)
    landmarks.add(wrist)

    // Thumb (1..4)
    if (isPinch) {
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_CMC, 0.48f, 0.65f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_MCP, 0.46f, 0.60f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_IP, 0.44f, 0.55f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_TIP, 0.42f, 0.50f, 0f)) // Close to index tip
    } else if (thumbUp) {
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_CMC, 0.45f, 0.65f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_MCP, 0.40f, 0.58f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_IP, 0.38f, 0.48f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_TIP, 0.36f, 0.38f, 0f)) // High above MCP
    } else if (isThumbExtended) {
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_CMC, 0.45f, 0.65f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_MCP, 0.40f, 0.60f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_IP, 0.35f, 0.55f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_TIP, 0.30f, 0.50f, 0f))
    } else {
      // Curled thumb over palm
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_CMC, 0.48f, 0.65f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_MCP, 0.46f, 0.62f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_IP, 0.45f, 0.66f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.THUMB_TIP, 0.52f, 0.68f, 0f)) // Over middle/palm
    }

    // Index (5..8)
    landmarks.add(HandLandmark(LandmarkIndex.INDEX_MCP, 0.45f, 0.55f, 0f))
    if (isPinch) {
      landmarks.add(HandLandmark(LandmarkIndex.INDEX_PIP, 0.44f, 0.53f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.INDEX_DIP, 0.43f, 0.51f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.INDEX_TIP, 0.42f, 0.50f, 0f)) // Touching thumb
    } else if (isIndexExtended) {
      landmarks.add(HandLandmark(LandmarkIndex.INDEX_PIP, 0.45f, 0.45f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.INDEX_DIP, 0.45f, 0.38f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.INDEX_TIP, 0.45f, 0.30f, 0f))
    } else {
      landmarks.add(HandLandmark(LandmarkIndex.INDEX_PIP, 0.45f, 0.58f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.INDEX_DIP, 0.46f, 0.60f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.INDEX_TIP, 0.47f, 0.62f, 0f))
    }

    // Middle (9..12)
    landmarks.add(HandLandmark(LandmarkIndex.MIDDLE_MCP, 0.50f, 0.54f, 0f))
    if (isMiddleExtended) {
      landmarks.add(HandLandmark(LandmarkIndex.MIDDLE_PIP, 0.50f, 0.43f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.MIDDLE_DIP, 0.50f, 0.35f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.MIDDLE_TIP, 0.50f, 0.27f, 0f))
    } else {
      landmarks.add(HandLandmark(LandmarkIndex.MIDDLE_PIP, 0.50f, 0.58f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.MIDDLE_DIP, 0.50f, 0.60f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.MIDDLE_TIP, 0.50f, 0.62f, 0f))
    }

    // Ring (13..16)
    landmarks.add(HandLandmark(LandmarkIndex.RING_MCP, 0.55f, 0.55f, 0f))
    if (isRingExtended) {
      landmarks.add(HandLandmark(LandmarkIndex.RING_PIP, 0.55f, 0.45f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.RING_DIP, 0.55f, 0.38f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.RING_TIP, 0.55f, 0.31f, 0f))
    } else {
      landmarks.add(HandLandmark(LandmarkIndex.RING_PIP, 0.55f, 0.58f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.RING_DIP, 0.55f, 0.60f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.RING_TIP, 0.55f, 0.62f, 0f))
    }

    // Pinky (17..20)
    landmarks.add(HandLandmark(LandmarkIndex.PINKY_MCP, 0.60f, 0.57f, 0f))
    if (isPinkyExtended) {
      landmarks.add(HandLandmark(LandmarkIndex.PINKY_PIP, 0.60f, 0.48f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.PINKY_DIP, 0.60f, 0.42f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.PINKY_TIP, 0.60f, 0.36f, 0f))
    } else {
      landmarks.add(HandLandmark(LandmarkIndex.PINKY_PIP, 0.60f, 0.59f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.PINKY_DIP, 0.60f, 0.61f, 0f))
      landmarks.add(HandLandmark(LandmarkIndex.PINKY_TIP, 0.60f, 0.63f, 0f))
    }

    return HandResult(
      id = 1,
      landmarks = landmarks,
      handedness = Handedness.RIGHT,
      confidence = 0.95f
    )
  }

  @Test
  fun testFingerGeometryCalculations() {
    val hand = createSyntheticHand(isThumbExtended = true, isIndexExtended = true)
    val scale = FingerGeometry.computePalmScale(hand)
    assertTrue("Palm scale must be positive", scale > 0.05f)

    val isIndexExt = FingerGeometry.isFingerExtended(
      hand, LandmarkIndex.INDEX_MCP, LandmarkIndex.INDEX_PIP, LandmarkIndex.INDEX_DIP, LandmarkIndex.INDEX_TIP
    )
    assertTrue("Index should be extended", isIndexExt)

    val p1 = HandLandmark(LandmarkIndex.WRIST, 0f, 0f)
    val p2 = HandLandmark(LandmarkIndex.INDEX_TIP, 3f, 4f)
    assertEquals(5f, FingerGeometry.distance2D(p1, p2), 0.001f)
  }

  @Test
  fun testOpenPalmRecognition() {
    val hand = createSyntheticHand(
      isThumbExtended = true,
      isIndexExtended = true,
      isMiddleExtended = true,
      isRingExtended = true,
      isPinkyExtended = true
    )
    val (gesture, confidence) = recognizer.recognizeStaticGesture(hand)
    assertEquals(GestureType.OPEN_PALM, gesture)
    assertTrue("Confidence should be high", confidence > 0.8f)
  }

  @Test
  fun testClosedFistRecognition() {
    val hand = createSyntheticHand(
      isThumbExtended = false,
      isIndexExtended = false,
      isMiddleExtended = false,
      isRingExtended = false,
      isPinkyExtended = false
    )
    val (gesture, confidence) = recognizer.recognizeStaticGesture(hand)
    assertEquals(GestureType.CLOSED_FIST, gesture)
    assertTrue(confidence > 0.8f)
  }

  @Test
  fun testPointingRecognition() {
    val hand = createSyntheticHand(
      isThumbExtended = false,
      isIndexExtended = true,
      isMiddleExtended = false,
      isRingExtended = false,
      isPinkyExtended = false
    )
    val (gesture, _) = recognizer.recognizeStaticGesture(hand)
    assertEquals(GestureType.POINTING, gesture)
  }

  @Test
  fun testPeaceVictoryRecognition() {
    val hand = createSyntheticHand(
      isThumbExtended = false,
      isIndexExtended = true,
      isMiddleExtended = true,
      isRingExtended = false,
      isPinkyExtended = false
    )
    val (gesture, _) = recognizer.recognizeStaticGesture(hand)
    assertEquals(GestureType.VICTORY_PEACE, gesture)
  }

  @Test
  fun testThumbUpRecognition() {
    val hand = createSyntheticHand(
      isThumbExtended = true,
      isIndexExtended = false,
      isMiddleExtended = false,
      isRingExtended = false,
      isPinkyExtended = false,
      thumbUp = true
    )
    val (gesture, _) = recognizer.recognizeStaticGesture(hand)
    assertEquals(GestureType.THUMB_UP, gesture)
  }

  @Test
  fun testPinchRecognition() {
    val hand = createSyntheticHand(
      isThumbExtended = false,
      isIndexExtended = false,
      isMiddleExtended = false,
      isRingExtended = false,
      isPinkyExtended = false,
      isPinch = true
    )
    val (gesture, _) = recognizer.recognizeStaticGesture(hand)
    assertEquals(GestureType.PINCH, gesture)
  }

  @Test
  fun testGestureStabilizerDebounceAndHold() {
    // Frame 1: Not enough debounce frames (needs 3)
    val g1 = stabilizer.stabilize(GestureType.OPEN_PALM, 0.9f)
    assertNull(g1)

    // Frame 2: Still debouncing
    val g2 = stabilizer.stabilize(GestureType.OPEN_PALM, 0.9f)
    assertNull(g2)

    // Frame 3: Debounce passed -> DETECTED state emitted
    val g3 = stabilizer.stabilize(GestureType.OPEN_PALM, 0.9f)
    assertNotNull(g3)
    assertEquals(GestureType.OPEN_PALM, g3?.type)
    assertEquals(GestureLifecycleState.DETECTED, g3?.state)

    // Simulate hold time elapse
    Thread.sleep(170L)

    // Frame 4: Hold threshold reached -> HELD state emitted
    val g4 = stabilizer.stabilize(GestureType.OPEN_PALM, 0.9f)
    assertNotNull(g4)
    assertEquals(GestureType.OPEN_PALM_HOLD, g4?.type)
    assertEquals(GestureLifecycleState.HELD, g4?.state)

    // Release hand
    val g5 = stabilizer.stabilize(GestureType.NONE, 0f)
    assertNotNull(g5)
    assertEquals(GestureLifecycleState.RELEASED, g5?.state)
  }

  @Test
  fun testDefaultGesturePresets() {
    val mediaPreset = DefaultGestureMappings.getPreset(GesturePresetProfile.MEDIA_CONTROLLER)
    assertEquals(GestureAction.MEDIA_PLAY_PAUSE, mediaPreset[GestureType.OPEN_PALM])
    assertEquals(GestureAction.MEDIA_NEXT, mediaPreset[GestureType.SWIPE_RIGHT])
    assertEquals(GestureAction.MEDIA_PREVIOUS, mediaPreset[GestureType.SWIPE_LEFT])

    val readerPreset = DefaultGestureMappings.getPreset(GesturePresetProfile.READER_BROWSER)
    assertEquals(GestureAction.SCROLL_DOWN, readerPreset[GestureType.SWIPE_UP])
  }
}
