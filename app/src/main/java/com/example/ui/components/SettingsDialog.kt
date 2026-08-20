package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InterruptionLevel
import com.example.model.MyraaSettings
import com.example.model.NotificationMode
import com.example.ui.theme.SleekBgDark
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekCardBg
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekSkyBlueDark
import com.example.ui.theme.SleekTextDarkMuted
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@Composable
fun SettingsDialog(
  currentSettings: MyraaSettings,
  onSaveSettings: (MyraaSettings) -> Unit,
  onDismiss: () -> Unit
) {
  var proactiveEnabled by remember { mutableStateOf(currentSettings.proactiveIntelligenceEnabled) }
  var voiceEnabled by remember { mutableStateOf(currentSettings.proactiveVoiceEnabled) }
  var notifMode by remember { mutableStateOf(currentSettings.notificationMode) }
  var interruptionLevel by remember { mutableStateOf(currentSettings.interruptionLevel) }
  var quietHoursEnabled by remember { mutableStateOf(currentSettings.quietHoursEnabled) }
  var cooldownSeconds by remember { mutableFloatStateOf(currentSettings.cooldownSeconds.toFloat()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SleekCardBg,
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = null,
          tint = SleekSkyBlue,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("MYRAA System Settings", color = SleekTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Master Proactive Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Proactive Intelligence", color = SleekTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("Allow MYRAA to observe context & decide when to intervene", color = SleekTextSecondary, fontSize = 11.sp)
          }
          Switch(
            checked = proactiveEnabled,
            onCheckedChange = { proactiveEnabled = it },
            modifier = Modifier.testTag("toggle_proactive_intelligence"),
            colors = SwitchDefaults.colors(
              checkedThumbColor = SleekSkyBlue,
              checkedTrackColor = SleekSkyBlueDark.copy(alpha = 0.5f)
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Proactive Voice Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Proactive Speech", color = SleekTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("Permit MYRAA to speak aloud without prior voice trigger", color = SleekTextSecondary, fontSize = 11.sp)
          }
          Switch(
            checked = voiceEnabled,
            onCheckedChange = { voiceEnabled = it },
            modifier = Modifier.testTag("toggle_proactive_voice"),
            colors = SwitchDefaults.colors(
              checkedThumbColor = SleekSkyBlue,
              checkedTrackColor = SleekSkyBlueDark.copy(alpha = 0.5f)
            )
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interruption Level
        Text("Interruption Threshold", color = SleekSkyBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        InterruptionLevel.entries.forEach { level ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { interruptionLevel = level }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = interruptionLevel == level,
              onClick = { interruptionLevel = level },
              colors = RadioButtonDefaults.colors(selectedColor = SleekSkyBlue)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
              Text(level.name, color = SleekTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
              Text(level.label, color = SleekTextMuted, fontSize = 11.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification Mode
        Text("Notification Mode", color = SleekSkyBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        NotificationMode.entries.forEach { mode ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { notifMode = mode }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = notifMode == mode,
              onClick = { notifMode = mode },
              colors = RadioButtonDefaults.colors(selectedColor = SleekSkyBlue)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(mode.name, color = SleekTextPrimary, fontSize = 13.sp)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cooldown Slider
        Text(
          "Event Cooldown: ${cooldownSeconds.toInt()}s",
          color = SleekSkyBlue,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Slider(
          value = cooldownSeconds,
          onValueChange = { cooldownSeconds = it },
          valueRange = 10f..60f,
          steps = 5,
          colors = SliderDefaults.colors(
            thumbColor = SleekSkyBlue,
            activeTrackColor = SleekSkyBlueDark
          ),
          modifier = Modifier.testTag("cooldown_slider")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quiet Hours Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Quiet Hours (22:00 - 07:00)", color = SleekTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("Silently remember events during sleep hours", color = SleekTextSecondary, fontSize = 11.sp)
          }
          Switch(
            checked = quietHoursEnabled,
            onCheckedChange = { quietHoursEnabled = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = SleekSkyBlue,
              checkedTrackColor = SleekSkyBlueDark.copy(alpha = 0.5f)
            )
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSaveSettings(
            currentSettings.copy(
              proactiveIntelligenceEnabled = proactiveEnabled,
              proactiveVoiceEnabled = voiceEnabled,
              notificationMode = notifMode,
              interruptionLevel = interruptionLevel,
              quietHoursEnabled = quietHoursEnabled,
              cooldownSeconds = cooldownSeconds.toInt()
            )
          )
          onDismiss()
        },
        modifier = Modifier.testTag("save_settings_btn"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SleekSkyBlue)
      ) {
        Text("Apply Changes", color = SleekBgDark, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SleekBorder)
      ) {
        Text("Close", color = SleekTextSecondary)
      }
    }
  )
}

