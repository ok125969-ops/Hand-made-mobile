package com.example.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.actions.GestureAction
import com.example.gesture.GestureType
import com.example.ui.components.CalibrationDialog
import com.example.ui.components.CameraPreviewWithOverlay
import com.example.ui.components.GestureMappingSheet
import com.example.ui.components.GestureStatusHUD
import com.example.ui.components.PerformanceSettingsDialog
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekBgDark
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GestureControlScreen(
  viewModel: GestureViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    hasCameraPermission = granted
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    viewModel.checkAccessibilityStatus()
  }

  var showMappingSheet by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }
  var showCalibrationDialog by remember { mutableStateOf(false) }
  var selectedTab by remember { mutableIntStateOf(0) }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(SleekBgDark),
    containerColor = SleekBgDark
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // 1. App Top Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (uiState.isGestureControlEnabled) SleekEmerald else SleekTextDarkMuted)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "GESTURE CONTROL",
              color = SleekTextPrimary,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }
          Text(
            text = "${uiState.performanceMode.displayName} • ${if (uiState.useFrontCamera) "Front Cam" else "Back Cam"}",
            color = SleekTextMuted,
            fontSize = 11.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Calibration Quick Icon
          IconButton(
            onClick = { showCalibrationDialog = true },
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(SleekCardBg)
          ) {
            Icon(
              imageVector = Icons.Default.Build,
              contentDescription = "Calibration",
              tint = SleekSkyBlue,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Settings Quick Icon
          IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(SleekCardBg)
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = SleekTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Master Power Switch
          Switch(
            checked = uiState.isGestureControlEnabled,
            onCheckedChange = { viewModel.toggleGestureControl(it) },
            colors = SwitchDefaults.colors(
              checkedThumbColor = SleekSkyBlue,
              checkedTrackColor = SleekSkyBlue.copy(alpha = 0.35f),
              uncheckedThumbColor = SleekTextDarkMuted,
              uncheckedTrackColor = SleekCardBgDarker
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Accessibility Notice Banner (if disabled)
      if (!uiState.isAccessibilityEnabled) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SleekAmber.copy(alpha = 0.12f))
            .border(1.dp, SleekAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable {
              val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
              }
              context.startActivity(intent)
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = SleekAmber,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Tap to enable Accessibility for Back/Home/Scroll",
                color = SleekAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
            Text(
              text = "ENABLE",
              color = SleekAmber,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
      }

      // Camera Permission Banner (if not granted)
      if (!hasCameraPermission) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SleekRoseAlert.copy(alpha = 0.15f))
            .border(1.dp, SleekRoseAlert, RoundedCornerShape(16.dp))
            .padding(16.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Camera Permission",
              tint = SleekRoseAlert,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Camera Permission Required",
              color = SleekTextPrimary,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Camera is processed 100% locally on-device to track hand gestures in real time.",
              color = SleekTextMuted,
              fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
              colors = ButtonDefaults.buttonColors(containerColor = SleekRoseAlert)
            ) {
              Text("Grant Camera Access", fontWeight = FontWeight.Bold)
            }
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      // 2. Navigation Tabs
      val tabs = listOf("Live HUD", "Action Mapping", "Event Log")
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = SleekBgDark,
        contentColor = SleekSkyBlue,
        edgePadding = 0.dp,
        divider = {}
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = {
              Text(
                text = title,
                color = if (selectedTab == index) SleekSkyBlue else SleekTextMuted,
                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
              )
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Tab Contents
      when (selectedTab) {
        0 -> {
          // LIVE HUD TAB
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            item {
              if (hasCameraPermission) {
                CameraPreviewWithOverlay(
                  isGestureControlEnabled = uiState.isGestureControlEnabled,
                  useFrontCamera = uiState.useFrontCamera,
                  performanceMode = uiState.performanceMode,
                  sensitivity = uiState.sensitivity,
                  showLandmarks = uiState.showLandmarks,
                  detectedHands = uiState.detectedHands,
                  onHandsDetected = { hands, fps, latency ->
                    viewModel.onHandsTracked(hands, fps, latency)
                  },
                  onGestureRecognized = { gesture ->
                    viewModel.onGestureRecognized(gesture)
                  },
                  onSwitchCamera = { viewModel.toggleFrontCamera() },
                  onToggleLandmarks = { viewModel.toggleLandmarkOverlay(!uiState.showLandmarks) }
                )
              }
            }

            item {
              GestureStatusHUD(
                currentGesture = uiState.currentGesture,
                mappedAction = uiState.currentGesture?.type?.let { uiState.mappings[it] },
                fps = uiState.fps,
                latencyMs = uiState.latencyMs,
                feedbackMessage = uiState.feedbackMessage
              )
            }

            item {
              // Quick action buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Button(
                  onClick = { showMappingSheet = true },
                  colors = ButtonDefaults.buttonColors(containerColor = SleekCardBg),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Mappings",
                    tint = SleekSkyBlue,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Edit Mappings", color = SleekTextPrimary, fontSize = 12.sp)
                }

                Button(
                  onClick = { showCalibrationDialog = true },
                  colors = ButtonDefaults.buttonColors(containerColor = SleekCardBg),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Calibrate",
                    tint = SleekEmerald,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Calibrate Hand", color = SleekTextPrimary, fontSize = 12.sp)
                }
              }
            }
          }
        }

        1 -> {
          // ACTION MAPPING TAB
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "ACTIVE GESTURE BINDINGS",
                  color = SleekSkyBlue,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
                Text(
                  text = "Tap any to edit",
                  color = SleekTextDarkMuted,
                  fontSize = 11.sp
                )
              }
            }

            items(GestureType.entries.filter { it != GestureType.NONE }) { gesture ->
              val action = uiState.mappings[gesture] ?: GestureAction.NONE
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(SleekCardBg)
                  .border(1.dp, SleekBorder, RoundedCornerShape(14.dp))
                  .clickable { showMappingSheet = true }
                  .padding(14.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = gesture.displayName,
                      color = SleekTextPrimary,
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = gesture.description,
                      color = SleekTextMuted,
                      fontSize = 11.sp
                    )
                  }

                  Text(
                    text = action.displayName,
                    color = if (action != GestureAction.NONE) SleekSkyBlue else SleekTextDarkMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (action != GestureAction.NONE) SleekSkyBlue.copy(alpha = 0.15f) else SleekCardBgDarker)
                      .padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }

        2 -> {
          // EVENT LOG TAB
          Column(modifier = Modifier.fillMaxSize()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "GESTURE ACTIVITY LOG (${uiState.history.size})",
                color = SleekSkyBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
              IconButton(onClick = { viewModel.clearHistory() }) {
                Icon(
                  imageVector = Icons.Default.DeleteSweep,
                  contentDescription = "Clear",
                  tint = SleekTextMuted,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            if (uiState.history.isEmpty()) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth(),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No gesture events recorded yet.\nPerform gestures in front of the camera.",
                  color = SleekTextMuted,
                  fontSize = 13.sp,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            } else {
              val dateFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

              LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                items(uiState.history) { item ->
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(SleekCardBg)
                      .border(1.dp, SleekBorder, RoundedCornerShape(12.dp))
                      .padding(12.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                            text = item.gestureType,
                            color = SleekTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Text(
                            text = "→ ${item.actionExecuted}",
                            color = SleekSkyBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                          )
                        }
                        Text(
                          text = "State: ${item.state} • ${(item.confidence * 100).toInt()}% conf • ${item.latencyMs}ms",
                          color = SleekTextMuted,
                          fontSize = 10.sp,
                          fontFamily = FontFamily.Monospace
                        )
                      }

                      Text(
                        text = dateFormat.format(Date(item.timestamp)),
                        color = SleekTextDarkMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Dialogs & Sheets
  if (showMappingSheet) {
    GestureMappingSheet(
      mappings = uiState.mappings,
      onUpdateMapping = { g, a -> viewModel.updateGestureMapping(g, a) },
      onApplyPreset = { profile -> viewModel.applyPresetProfile(profile) },
      onDismiss = { showMappingSheet = false }
    )
  }

  if (showSettingsDialog) {
    PerformanceSettingsDialog(
      performanceMode = uiState.performanceMode,
      sensitivity = uiState.sensitivity,
      hapticFeedback = uiState.hapticFeedback,
      soundFeedback = uiState.soundFeedback,
      isAccessibilityEnabled = uiState.isAccessibilityEnabled,
      onSelectPerformanceMode = { viewModel.setPerformanceMode(it) },
      onUpdateSensitivity = { viewModel.setSensitivity(it) },
      onToggleHaptic = { viewModel.toggleHaptic(it) },
      onToggleSound = { viewModel.toggleSound(it) },
      onDismiss = { showSettingsDialog = false }
    )
  }

  if (showCalibrationDialog) {
    CalibrationDialog(
      currentCalibration = uiState.calibration,
      detectedHands = uiState.detectedHands,
      onSaveCalibration = { scale, reach, pinch ->
        viewModel.saveCalibration(scale, reach, pinch)
      },
      onDismiss = { showCalibrationDialog = false }
    )
  }
}
