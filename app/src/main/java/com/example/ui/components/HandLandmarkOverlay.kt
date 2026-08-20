package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekRoseAlert
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekSkyBlueLight
import com.example.vision.FingerType
import com.example.vision.HandResult
import com.example.vision.HandTopology

/**
 * High-performance Jetpack Compose Canvas overlay that renders the 21-landmark hand skeleton.
 */
@Composable
fun HandLandmarkOverlay(
  hands: List<HandResult>,
  modifier: Modifier = Modifier,
  showSkeleton: Boolean = true,
  showBoundingBox: Boolean = true
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    if (hands.isEmpty() || !showSkeleton) return@Canvas

    hands.forEach { hand ->
      val landmarks = hand.landmarks
      if (landmarks.isEmpty()) return@forEach

      // 1. Draw Bounding Box
      if (showBoundingBox) {
        val box = hand.boundingBox
        val left = box.left * canvasWidth
        val top = box.top * canvasHeight
        val width = (box.right - box.left) * canvasWidth
        val height = (box.bottom - box.top) * canvasHeight

        drawRect(
          color = SleekSkyBlue.copy(alpha = 0.4f),
          topLeft = Offset(left, top),
          size = Size(width, height),
          style = Stroke(
            width = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
          )
        )
      }

      // 2. Draw Skeletal Bone Connections
      HandTopology.BONES.forEach { bone ->
        val startLandmark = hand.getLandmark(bone.start)
        val endLandmark = hand.getLandmark(bone.end)

        val startOffset = startLandmark.toOffset(canvasWidth, canvasHeight)
        val endOffset = endLandmark.toOffset(canvasWidth, canvasHeight)

        val boneColor = when (bone.fingerType) {
          FingerType.PALM -> SleekSkyBlue.copy(alpha = 0.7f)
          FingerType.THUMB -> SleekAmber.copy(alpha = 0.85f)
          FingerType.INDEX -> SleekSkyBlueLight.copy(alpha = 0.9f)
          FingerType.MIDDLE -> SleekEmerald.copy(alpha = 0.85f)
          FingerType.RING -> Color(0xFFC084FC).copy(alpha = 0.85f) // Purple
          FingerType.PINKY -> SleekRoseAlert.copy(alpha = 0.85f)
        }

        drawLine(
          color = boneColor,
          start = startOffset,
          end = endOffset,
          strokeWidth = 5f,
          cap = StrokeCap.Round
        )
      }

      // 3. Draw Joint Landmark Nodes
      landmarks.forEach { landmark ->
        val offset = landmark.toOffset(canvasWidth, canvasHeight)

        // Outer glow
        drawCircle(
          color = SleekSkyBlue.copy(alpha = 0.35f),
          radius = 9f,
          center = offset
        )

        // Joint point
        drawCircle(
          color = Color.White,
          radius = 5f,
          center = offset
        )
      }

      // 4. Draw Palm Centroid Reticle
      val palmOffset = Offset(hand.palmCenter.x * canvasWidth, hand.palmCenter.y * canvasHeight)
      drawCircle(
        color = SleekEmerald,
        radius = 8f,
        center = palmOffset,
        style = Stroke(width = 3f)
      )
      drawCircle(
        color = SleekEmerald.copy(alpha = 0.7f),
        radius = 3f,
        center = palmOffset
      )
    }
  }
}
