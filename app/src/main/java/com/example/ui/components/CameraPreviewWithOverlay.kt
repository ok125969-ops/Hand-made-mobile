package com.example.ui.components

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.CameraManager
import com.example.camera.GestureAnalysisListener
import com.example.camera.PerformanceMode
import com.example.gesture.RecognizedGesture
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekCardBgDarker
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekTextMuted
import com.example.vision.HandResult

@Composable
fun CameraPreviewWithOverlay(
  isGestureControlEnabled: Boolean,
  useFrontCamera: Boolean,
  performanceMode: PerformanceMode,
  sensitivity: Float,
  showLandmarks: Boolean,
  detectedHands: List<HandResult>,
  onHandsDetected: (List<HandResult>, Float, Long) -> Unit,
  onGestureRecognized: (RecognizedGesture) -> Unit,
  onSwitchCamera: () -> Unit,
  onToggleLandmarks: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  val previewView = remember {
    PreviewView(context).apply {
      implementationMode = PreviewView.ImplementationMode.COMPATIBLE
      scaleType = PreviewView.ScaleType.FILL_CENTER
    }
  }

  val cameraManager = remember(useFrontCamera, performanceMode, sensitivity) {
    CameraManager(
      context = context,
      lifecycleOwner = lifecycleOwner,
      previewView = previewView,
      analysisListener = object : GestureAnalysisListener {
        override fun onHandsDetected(hands: List<HandResult>, fps: Float, latencyMs: Long) {
          onHandsDetected(hands, fps, latencyMs)
        }

        override fun onGestureRecognized(gesture: RecognizedGesture) {
          onGestureRecognized(gesture)
        }
      }
    )
  }

  DisposableEffect(isGestureControlEnabled, useFrontCamera, performanceMode, sensitivity) {
    if (isGestureControlEnabled) {
      cameraManager.startCamera(
        useFrontCamera = useFrontCamera,
        performanceMode = performanceMode,
        sensitivity = sensitivity
      )
    } else {
      cameraManager.stopCamera()
    }

    onDispose {
      cameraManager.stopCamera()
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(4f / 3f)
      .clip(RoundedCornerShape(24.dp))
      .background(SleekCardBgDarker)
      .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
  ) {
    if (isGestureControlEnabled) {
      // Camera Stream Preview
      AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
      )

      // 21-Landmark Skeleton Canvas Overlay
      HandLandmarkOverlay(
        hands = detectedHands,
        showSkeleton = showLandmarks,
        showBoundingBox = showLandmarks,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      // Inactive State Display
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Gesture Control is Paused",
          color = SleekTextMuted,
          fontSize = 14.sp
        )
      }
    }

    // Privacy Indicator (Top-Left)
    Row(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(12.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(Color.Black.copy(alpha = 0.6f))
        .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Privacy",
        tint = SleekEmerald,
        modifier = Modifier.size(12.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "On-Device CV",
        color = Color.White,
        fontSize = 10.sp
      )
    }

    // Quick Action Bar (Top-Right)
    Row(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(8.dp)
    ) {
      // Toggle Landmarks
      IconButton(
        onClick = onToggleLandmarks,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
      ) {
        Icon(
          imageVector = if (showLandmarks) Icons.Default.Visibility else Icons.Default.VisibilityOff,
          contentDescription = "Toggle Landmarks",
          tint = if (showLandmarks) SleekSkyBlue else SleekTextMuted,
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Switch Camera
      IconButton(
        onClick = onSwitchCamera,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.6f))
      ) {
        Icon(
          imageVector = Icons.Default.Cameraswitch,
          contentDescription = "Switch Camera",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
