package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.GestureCalibrationEntity
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekCardBg
import com.example.ui.theme.SleekCardBgDarker
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekTextDarkMuted
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.vision.HandResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationDialog(
  currentCalibration: GestureCalibrationEntity,
  detectedHands: List<HandResult>,
  onSaveCalibration: (Float, Float, Float) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var currentStep by remember { mutableIntStateOf(1) }

  var capturedRestingScale by remember { mutableFloatStateOf(currentCalibration.restingPalmScale) }
  var capturedReachDistance by remember { mutableFloatStateOf(currentCalibration.reachDistance) }
  var capturedPinchThreshold by remember { mutableFloatStateOf(currentCalibration.calibratedPinchThreshold) }

  val primaryHand = detectedHands.firstOrNull()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SleekCardBgDarker,
    dragHandle = null
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.80f)
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Hand Calibration Studio",
            color = SleekTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Calibrates distance, scale, and gesture sensitivity",
            color = SleekTextMuted,
            fontSize = 12.sp
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = SleekTextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Progress Steps
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        listOf("1. Neutral Palm", "2. Reach Distance", "3. Pinch Threshold", "4. Complete").forEachIndexed { index, label ->
          val stepIndex = index + 1
          val isDone = currentStep > stepIndex
          val isCurrent = currentStep == stepIndex

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                  if (isDone) SleekEmerald
                  else if (isCurrent) SleekSkyBlue
                  else SleekCardBg
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "$stepIndex",
                color = if (isDone || isCurrent) SleekCardBgDarker else SleekTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = label.split(" ").last(),
              color = if (isCurrent) SleekSkyBlue else SleekTextDarkMuted,
              fontSize = 10.sp,
              textAlign = TextAlign.Center
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Step Contents
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(SleekCardBg)
          .border(1.dp, SleekBorder, RoundedCornerShape(20.dp))
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        when (currentStep) {
          1 -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.FrontHand,
                contentDescription = "Neutral Palm",
                tint = SleekSkyBlue,
                modifier = Modifier.size(56.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Hold Open Palm at Resting Distance",
                color = SleekTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Position your hand comfortably 30-50cm from the camera.",
                color = SleekTextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = if (primaryHand != null) "Hand Detected (Scale: ${(primaryHand.palmSize * 100).toInt()}%)" else "Waiting for hand in camera...",
                color = if (primaryHand != null) SleekEmerald else SleekTextDarkMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          2 -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.PanTool,
                contentDescription = "Reach",
                tint = SleekSkyBlue,
                modifier = Modifier.size(56.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Extend Hand Slightly Closer",
                color = SleekTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "This calibrates your forward push & depth gestures.",
                color = SleekTextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = if (primaryHand != null) "Reach Scale: ${(primaryHand.palmSize * 100).toInt()}%" else "Waiting for hand...",
                color = if (primaryHand != null) SleekEmerald else SleekTextDarkMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          3 -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = "Pinch",
                tint = SleekSkyBlue,
                modifier = Modifier.size(56.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Perform a Natural Pinch Gesture",
                color = SleekTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Touch your thumb and index finger together.",
                color = SleekTextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = if (primaryHand != null) "Tracking Pinch..." else "Waiting for hand...",
                color = if (primaryHand != null) SleekEmerald else SleekTextDarkMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          4 -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = SleekEmerald,
                modifier = Modifier.size(56.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Calibration Complete!",
                color = SleekTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Your custom hand profile has been optimized for low-latency recognition.",
                color = SleekTextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Action Button
      Button(
        onClick = {
          when (currentStep) {
            1 -> {
              primaryHand?.let { capturedRestingScale = it.palmSize }
              currentStep = 2
            }
            2 -> {
              primaryHand?.let { capturedReachDistance = it.palmSize }
              currentStep = 3
            }
            3 -> {
              capturedPinchThreshold = 0.35f
              currentStep = 4
            }
            4 -> {
              onSaveCalibration(capturedRestingScale, capturedReachDistance, capturedPinchThreshold)
              onDismiss()
            }
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = SleekSkyBlue),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
      ) {
        Text(
          text = if (currentStep == 4) "Save Profile & Finish" else "Next Step",
          color = SleekCardBgDarker,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
