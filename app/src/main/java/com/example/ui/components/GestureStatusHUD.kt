package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actions.GestureAction
import com.example.gesture.GestureLifecycleState
import com.example.gesture.GestureType
import com.example.gesture.RecognizedGesture
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekCardBg
import com.example.ui.theme.SleekCardBgDarker
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekRoseAlert
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekTextDarkMuted
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@Composable
fun GestureStatusHUD(
  currentGesture: RecognizedGesture?,
  mappedAction: GestureAction?,
  fps: Float,
  latencyMs: Long,
  feedbackMessage: String?,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(SleekCardBg.copy(alpha = 0.92f))
      .border(1.dp, SleekBorder, RoundedCornerShape(20.dp))
      .padding(16.dp)
  ) {
    // 1. Top Row: Status, FPS & Latency
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(if (currentGesture != null && currentGesture.type != GestureType.NONE) SleekEmerald else SleekTextDarkMuted)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentGesture != null && currentGesture.type != GestureType.NONE) "HAND DETECTED" else "WAITING FOR HAND",
          color = if (currentGesture != null && currentGesture.type != GestureType.NONE) SleekEmerald else SleekTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }

      // FPS & Latency Counter
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(SleekCardBgDarker)
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Speed,
          contentDescription = "FPS",
          tint = SleekSkyBlue,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${fps.toInt()} FPS • ${latencyMs}ms",
          color = SleekTextSecondary,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Medium
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Gesture Main Info
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Gesture Icon Bubble
      val gestureIcon = getGestureIcon(currentGesture?.type)
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(SleekCardBgDarker)
          .border(1.dp, SleekSkyBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = gestureIcon,
          contentDescription = "Gesture",
          tint = SleekSkyBlue,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = currentGesture?.type?.displayName ?: "No Gesture",
            color = SleekTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )

          // State Badge
          val stateColor = when (currentGesture?.state) {
            GestureLifecycleState.DETECTED -> SleekSkyBlue
            GestureLifecycleState.HELD -> SleekAmber
            GestureLifecycleState.REPEATED -> SleekEmerald
            GestureLifecycleState.CHANGED -> SleekSkyBlue
            GestureLifecycleState.RELEASED -> SleekTextDarkMuted
            null -> SleekTextDarkMuted
          }

          Text(
            text = (currentGesture?.state?.name ?: "IDLE"),
            color = stateColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(stateColor.copy(alpha = 0.15f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Confidence Indicator
        val confidence = currentGesture?.confidence ?: 0f
        Row(verticalAlignment = Alignment.CenterVertically) {
          LinearProgressIndicator(
            progress = { confidence },
            modifier = Modifier
              .weight(1f)
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp)),
            color = SleekSkyBlue,
            trackColor = SleekCardBgDarker,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${(confidence * 100).toInt()}%",
            color = SleekTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // 3. Mapped Action & Feedback Notification Banner
    Spacer(modifier = Modifier.height(10.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(SleekCardBgDarker)
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "ACTION:",
          color = SleekSkyBlue,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = mappedAction?.displayName ?: "None (Unassigned)",
          color = SleekTextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }

      if (feedbackMessage != null) {
        Text(
          text = feedbackMessage,
          color = SleekEmerald,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

private fun getGestureIcon(type: GestureType?): ImageVector {
  return when (type) {
    GestureType.OPEN_PALM, GestureType.OPEN_PALM_HOLD -> Icons.Default.FrontHand
    GestureType.CLOSED_FIST, GestureType.FIST_HOLD -> Icons.Default.PanTool
    GestureType.POINTING, GestureType.POINT_HOLD -> Icons.Default.TouchApp
    GestureType.THUMB_UP -> Icons.Default.ThumbUp
    GestureType.THUMB_DOWN -> Icons.Default.ThumbDown
    GestureType.VICTORY_PEACE -> Icons.Default.CheckCircle
    GestureType.OK_GESTURE -> Icons.Default.CheckCircle
    GestureType.PINCH, GestureType.PINCH_HOLD -> Icons.Default.TouchApp
    GestureType.SWIPE_LEFT -> Icons.Default.ArrowBack
    GestureType.SWIPE_RIGHT -> Icons.Default.ArrowForward
    GestureType.SWIPE_UP -> Icons.Default.ArrowUpward
    GestureType.SWIPE_DOWN -> Icons.Default.ArrowDownward
    GestureType.PUSH_FORWARD, GestureType.PULL_BACK -> Icons.Default.Navigation
    GestureType.CIRCULAR_MOTION -> Icons.Default.Refresh
    else -> Icons.Default.FrontHand
  }
}
