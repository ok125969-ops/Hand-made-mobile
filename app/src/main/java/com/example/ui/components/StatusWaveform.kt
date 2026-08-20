package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekRoseAlert
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekSkyBlueDark

@Composable
fun StatusWaveform(
  isSpeaking: Boolean,
  isAlert: Boolean = false,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  
  val pulse1 by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (isSpeaking) 500 else 2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse1"
  )

  val pulse2 by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.55f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (isSpeaking) 800 else 3000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse2"
  )

  val primaryColor = when {
    isAlert -> SleekRoseAlert
    isSpeaking -> SleekSkyBlue
    else -> SleekSkyBlueDark
  }

  val secondaryColor = when {
    isAlert -> SleekRoseAlert.copy(alpha = 0.25f)
    isSpeaking -> SleekEmerald.copy(alpha = 0.35f)
    else -> SleekSkyBlue.copy(alpha = 0.2f)
  }

  Box(
    modifier = modifier.size(100.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val radius = size.minDimension / 4f

      // Outer animated ripple ring
      drawCircle(
        color = secondaryColor,
        radius = radius * pulse2,
        style = Stroke(width = 1.5.dp.toPx())
      )

      // Middle animated ripple ring
      drawCircle(
        color = primaryColor.copy(alpha = 0.5f),
        radius = radius * pulse1,
        style = Stroke(width = 2.5.dp.toPx())
      )

      // Core Glowing Orb
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(primaryColor, primaryColor.copy(alpha = 0.2f), Color.Transparent),
          radius = radius
        ),
        radius = radius
      )

      // Solid central core
      drawCircle(
        color = primaryColor,
        radius = radius * 0.45f
      )
    }
  }
}

