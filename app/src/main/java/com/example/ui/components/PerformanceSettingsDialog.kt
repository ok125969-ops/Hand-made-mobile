package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.PerformanceMode
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekCardBg
import com.example.ui.theme.SleekCardBgDarker
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekTextDarkMuted
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceSettingsDialog(
  performanceMode: PerformanceMode,
  sensitivity: Float,
  hapticFeedback: Boolean,
  soundFeedback: Boolean,
  isAccessibilityEnabled: Boolean,
  onSelectPerformanceMode: (PerformanceMode) -> Unit,
  onUpdateSensitivity: (Float) -> Unit,
  onToggleHaptic: (Boolean) -> Unit,
  onToggleSound: (Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SleekCardBgDarker,
    dragHandle = null
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .padding(20.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Settings",
            tint = SleekSkyBlue,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Performance & System",
            color = SleekTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
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

      Spacer(modifier = Modifier.height(18.dp))

      // 1. Accessibility Service Section
      Text(
        text = "SYSTEM ACCESSIBILITY (REQUIRED FOR GLOBAL ACTIONS)",
        color = SleekSkyBlue,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(SleekCardBg)
          .border(
            1.dp,
            if (isAccessibilityEnabled) SleekEmerald.copy(alpha = 0.5f) else SleekAmber.copy(alpha = 0.5f),
            RoundedCornerShape(16.dp)
          )
          .padding(14.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AccessibilityNew,
                contentDescription = "Accessibility",
                tint = if (isAccessibilityEnabled) SleekEmerald else SleekAmber,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isAccessibilityEnabled) "Accessibility Connected" else "Accessibility Required",
                color = SleekTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Text(
              text = if (isAccessibilityEnabled) "ACTIVE" else "DISABLED",
              color = if (isAccessibilityEnabled) SleekEmerald else SleekAmber,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                  if (isAccessibilityEnabled) SleekEmerald.copy(alpha = 0.15f)
                  else SleekAmber.copy(alpha = 0.15f)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Required to dispatch system Back, Home, Recents, and page scrolling without root.",
            color = SleekTextMuted,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = {
              val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
              }
              context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isAccessibilityEnabled) SleekCardBgDarker else SleekSkyBlue
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.OpenInNew,
              contentDescription = "Settings",
              tint = if (isAccessibilityEnabled) SleekSkyBlue else SleekCardBgDarker,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isAccessibilityEnabled) "Open Accessibility Settings" else "Enable in Android Settings",
              color = if (isAccessibilityEnabled) SleekSkyBlue else SleekCardBgDarker,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 2. Performance Mode Section
      Text(
        text = "DEVICE PERFORMANCE PROFILE",
        color = SleekSkyBlue,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PerformanceMode.entries.forEach { mode ->
          val isSelected = performanceMode == mode
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(if (isSelected) SleekSkyBlue.copy(alpha = 0.12f) else SleekCardBg)
              .border(
                1.dp,
                if (isSelected) SleekSkyBlue else SleekBorder,
                RoundedCornerShape(14.dp)
              )
              .clickable { onSelectPerformanceMode(mode) }
              .padding(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = mode.displayName,
                  color = if (isSelected) SleekSkyBlue else SleekTextPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = mode.description,
                  color = SleekTextMuted,
                  fontSize = 11.sp
                )
              }
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Selected",
                  tint = SleekSkyBlue,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 3. Sensitivity Slider
      Text(
        text = "GESTURE SENSITIVITY: ${(sensitivity * 100).toInt()}%",
        color = SleekSkyBlue,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(6.dp))

      Slider(
        value = sensitivity,
        onValueChange = onUpdateSensitivity,
        valueRange = 0.5f..1.5f,
        steps = 10,
        colors = SliderDefaults.colors(
          thumbColor = SleekSkyBlue,
          activeTrackColor = SleekSkyBlue,
          inactiveTrackColor = SleekCardBg
        )
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Feedback Toggles
      Text(
        text = "FEEDBACK & NOTIFICATION",
        color = SleekSkyBlue,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(SleekCardBg)
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Vibration,
            contentDescription = "Haptic",
            tint = SleekSkyBlue,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Haptic Feedback",
            color = SleekTextPrimary,
            fontSize = 14.sp
          )
        }
        Switch(
          checked = hapticFeedback,
          onCheckedChange = onToggleHaptic,
          colors = SwitchDefaults.colors(
            checkedThumbColor = SleekSkyBlue,
            checkedTrackColor = SleekSkyBlue.copy(alpha = 0.3f)
          )
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(SleekCardBg)
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Sound",
            tint = SleekSkyBlue,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Audio Confirmation",
            color = SleekTextPrimary,
            fontSize = 14.sp
          )
        }
        Switch(
          checked = soundFeedback,
          onCheckedChange = onToggleSound,
          colors = SwitchDefaults.colors(
            checkedThumbColor = SleekSkyBlue,
            checkedTrackColor = SleekSkyBlue.copy(alpha = 0.3f)
          )
        )
      }
    }
  }
}
