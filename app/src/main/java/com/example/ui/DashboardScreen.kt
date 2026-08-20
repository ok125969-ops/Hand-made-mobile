package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ConversationEntity
import com.example.data.database.MemoryEntity
import com.example.model.EventDecision
import com.example.model.ProposedAction
import com.example.model.UserActivityState
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.components.DecisionCard
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StatusWaveform
import com.example.ui.theme.SleekAmber
import com.example.ui.theme.SleekBgDark
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekCardBg
import com.example.ui.theme.SleekCardBgDarker
import com.example.ui.theme.SleekEmerald
import com.example.ui.theme.SleekRoseAlert
import com.example.ui.theme.SleekSkyBlue
import com.example.ui.theme.SleekSkyBlueDark
import com.example.ui.theme.SleekSkyBlueLight
import com.example.ui.theme.SleekTextDarkMuted
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  viewModel: DashboardViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val systemContext by viewModel.systemContext.collectAsState()
  val settings by viewModel.myraaSettings.collectAsState()
  val decisions by viewModel.decisionHistory.collectAsState()
  val latestDecision by viewModel.latestDecision.collectAsState()
  val pendingAction by viewModel.pendingAction.collectAsState()
  val isSpeaking by viewModel.isSpeaking.collectAsState()
  val memories by viewModel.memories.collectAsState(initial = emptyList())
  val conversations by viewModel.conversations.collectAsState(initial = emptyList())
  val cursorOverlayRunning by viewModel.cursorOverlayRunning.collectAsState()
  val feedbackOverlayRunning by viewModel.feedbackOverlayRunning.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  var showSettingsDialog by remember { mutableStateOf(false) }
  var showAddMemoryDialog by remember { mutableStateOf(false) }
  var chatInputText by remember { mutableStateOf("") }
  var activeTaskInput by remember { mutableStateOf(systemContext.activeTask) }
  var isEditingTask by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(SleekBgDark)
      .testTag("dashboard_scaffold"),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = if (settings.proactiveIntelligenceEnabled) "SYSTEM ACTIVE" else "SYSTEM PASSIVE",
              color = SleekTextMuted,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 2.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "MYRAA ",
                color = SleekTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.5).sp
              )
              Text(
                text = "PRO",
                color = SleekSkyBlue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
        },
        actions = {
          if (isSpeaking) {
            IconButton(
              onClick = { viewModel.stopSpeaking() },
              modifier = Modifier.testTag("stop_speech_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop Speech",
                tint = SleekRoseAlert
              )
            }
          }

          // Sleek Status Orb Header Button
          Surface(
            shape = CircleShape,
            color = SleekCardBg,
            border = BorderStroke(1.dp, SleekBorder),
            modifier = Modifier
              .size(40.dp)
              .clickable { showSettingsDialog = true }
              .testTag("open_settings_btn")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (settings.proactiveIntelligenceEnabled) SleekSkyBlue else SleekTextDarkMuted)
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = { showSettingsDialog = true }
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = SleekTextMuted
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = SleekBgDark
        )
      )
    },
    containerColor = SleekBgDark
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // 1. Sleek Context Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("hero_status_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekCardBg.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, SleekBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Section Header Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = SleekSkyBlueDark.copy(alpha = 0.12f),
                modifier = Modifier.size(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = SleekSkyBlue,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "CURRENT CONTEXT",
                color = SleekTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = SleekCardBgDarker,
              border = BorderStroke(1.dp, SleekBorder)
            ) {
              Text(
                text = systemContext.userState.name,
                color = SleekSkyBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Active Application
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Active Application",
                color = SleekTextSecondary,
                fontSize = 12.sp
              )
              Text(
                text = systemContext.currentApp,
                color = SleekTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
              )
            }

            StatusWaveform(
              isSpeaking = isSpeaking,
              isAlert = latestDecision?.decisionType == com.example.model.DecisionType.ASK_CONFIRMATION,
              modifier = Modifier.size(44.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(SleekBorder)
          )
          Spacer(modifier = Modifier.height(8.dp))

          // Primary Task
          Text(
            text = "PRIMARY TASK",
            color = SleekTextDarkMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(2.dp))

          if (isEditingTask) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              OutlinedTextField(
                value = activeTaskInput,
                onValueChange = { activeTaskInput = it },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = SleekSkyBlue,
                  unfocusedBorderColor = SleekBorder,
                  focusedContainerColor = SleekCardBgDarker,
                  unfocusedContainerColor = SleekCardBgDarker,
                  focusedTextColor = SleekTextPrimary,
                  unfocusedTextColor = SleekTextPrimary
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = SleekTextPrimary)
              )
              IconButton(onClick = {
                viewModel.updateActiveTask(activeTaskInput)
                isEditingTask = false
              }) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Save", tint = SleekEmerald)
              }
            }
          } else {
            Text(
              text = systemContext.activeTask,
              color = SleekSkyBlueLight,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.clickable {
                activeTaskInput = systemContext.activeTask
                isEditingTask = true
              }
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Activity Switcher Chips
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            UserActivityState.entries.forEach { state ->
              FilterChip(
                selected = systemContext.userState == state,
                onClick = { viewModel.updateUserActivity(state) },
                label = { Text(state.name, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("chip_state_${state.name}"),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = SleekSkyBlue.copy(alpha = 0.15f),
                  selectedLabelColor = SleekSkyBlue,
                  containerColor = SleekCardBgDarker,
                  labelColor = SleekTextMuted
                ),
                border = BorderStroke(
                  1.dp,
                  if (systemContext.userState == state) SleekSkyBlue else SleekBorder
                ),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }
        }
      }

      // 2. Navigation Tabs
      val tabTitles = listOf("Intelligence Feed", "Event Simulator", "Memory & Knowledge", "Voice Terminal")
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = SleekBgDark,
        contentColor = SleekSkyBlue,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = SleekSkyBlue,
            height = 2.dp
          )
        },
        edgePadding = 16.dp,
        divider = {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(SleekBorder)
          )
        }
      ) {
        tabTitles.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = {
              Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                color = if (selectedTab == index) SleekSkyBlue else SleekTextMuted
              )
            },
            modifier = Modifier.testTag("tab_$index")
          )
        }
      }

      // 3. Tab Content
      Box(modifier = Modifier
        .weight(1f)
        .fillMaxWidth()) {
        when (selectedTab) {
          0 -> LiveStreamTab(
            decisions = decisions,
            onActionClick = { action -> viewModel.confirmPendingAction(action) }
          )
          1 -> EventSimulatorTab(
            viewModel = viewModel,
            cursorRunning = cursorOverlayRunning,
            feedbackRunning = feedbackOverlayRunning,
            onToggleCursor = { viewModel.toggleCursorOverlay(context) },
            onToggleFeedback = { viewModel.toggleFeedbackOverlay(context) }
          )
          2 -> MemoryTab(
            memories = memories,
            onDeleteMemory = { viewModel.deleteMemory(it) },
            onAddMemoryClick = { showAddMemoryDialog = true }
          )
          3 -> VoiceTerminalTab(
            conversations = conversations,
            isSpeaking = isSpeaking,
            inputText = chatInputText,
            onInputTextChange = { chatInputText = it },
            onSendMessage = {
              viewModel.sendUserMessage(chatInputText)
              chatInputText = ""
            }
          )
        }
      }
    }
  }

  // Dialogs
  if (showSettingsDialog) {
    SettingsDialog(
      currentSettings = settings,
      onSaveSettings = { viewModel.updateSettings(it) },
      onDismiss = { showSettingsDialog = false }
    )
  }

  pendingAction?.let { action ->
    ActionConfirmationDialog(
      action = action,
      onConfirm = { viewModel.confirmPendingAction(action) },
      onDismiss = { viewModel.dismissPendingAction() }
    )
  }

  if (showAddMemoryDialog) {
    AddMemoryDialog(
      onSave = { category, title, content, importance ->
        viewModel.addMemory(category, title, content, importance)
        showAddMemoryDialog = false
      },
      onDismiss = { showAddMemoryDialog = false }
    )
  }
}

@Composable
fun LiveStreamTab(
  decisions: List<EventDecision>,
  onActionClick: (ProposedAction) -> Unit
) {
  if (decisions.isEmpty()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Surface(
        shape = CircleShape,
        color = SleekCardBg,
        border = BorderStroke(1.dp, SleekBorder),
        modifier = Modifier.size(64.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = SleekTextDarkMuted,
            modifier = Modifier.size(32.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "No proactive events observed yet",
        color = SleekTextSecondary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Trigger an event from the Event Simulator tab to observe MYRAA's reasoning pipeline in real time.",
        color = SleekTextDarkMuted,
        fontSize = 12.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
    }
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("decision_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(decisions, key = { it.id }) { decision ->
        DecisionCard(
          decision = decision,
          onActionClick = onActionClick
        )
      }
    }
  }
}

@Composable
fun EventSimulatorTab(
  viewModel: DashboardViewModel,
  cursorRunning: Boolean,
  feedbackRunning: Boolean,
  onToggleCursor: () -> Unit,
  onToggleFeedback: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("event_simulator_tab")
  ) {
    Text(
      text = "PROACTIVE EVENT INJECTOR",
      color = SleekSkyBlue,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )
    Text(
      text = "Simulate developer & environment events to verify decision filtering, anti-spam cooldowns, and proactive speech.",
      color = SleekTextSecondary,
      fontSize = 12.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 1. Build Failure Trigger
    OutlinedButton(
      onClick = { viewModel.triggerBuildFailed("DashboardViewModel.kt") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("trigger_build_fail_btn"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = SleekCardBg.copy(alpha = 0.4f)),
      border = BorderStroke(1.dp, SleekRoseAlert.copy(alpha = 0.7f))
    ) {
      Icon(Icons.Default.BugReport, contentDescription = null, tint = SleekRoseAlert)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Trigger: Build Failed (DashboardViewModel.kt)", color = SleekRoseAlert, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2. Build Success Trigger
    OutlinedButton(
      onClick = { viewModel.triggerBuildSuccess() },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("trigger_build_success_btn"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = SleekCardBg.copy(alpha = 0.4f)),
      border = BorderStroke(1.dp, SleekEmerald.copy(alpha = 0.7f))
    ) {
      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SleekEmerald)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Trigger: Build Completed (APK Ready)", color = SleekEmerald, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 3. Repeated Error Simulation (3x Escalation)
    OutlinedButton(
      onClick = { viewModel.triggerRepeatedErrorSimulation() },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("trigger_repeat_error_btn"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = SleekCardBg.copy(alpha = 0.4f)),
      border = BorderStroke(1.dp, SleekAmber.copy(alpha = 0.7f))
    ) {
      Icon(Icons.Default.Replay, contentDescription = null, tint = SleekAmber)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Trigger: Repeated Error (3x Escalation)", color = SleekAmber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 4. Reminder Due
    OutlinedButton(
      onClick = { viewModel.triggerReminderDue("GestureControl Pull Request Review") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("trigger_reminder_btn"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = SleekCardBg.copy(alpha = 0.4f)),
      border = BorderStroke(1.dp, SleekSkyBlueDark.copy(alpha = 0.7f))
    ) {
      Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SleekSkyBlue)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Trigger: Task Reminder Due", color = SleekSkyBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 5. User Returned
    OutlinedButton(
      onClick = { viewModel.triggerUserReturned() },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("trigger_user_returned_btn"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = SleekCardBg.copy(alpha = 0.4f)),
      border = BorderStroke(1.dp, SleekSkyBlue.copy(alpha = 0.7f))
    ) {
      Icon(Icons.Default.CompassCalibration, contentDescription = null, tint = SleekSkyBlue)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Trigger: User Returned to Workstation", color = SleekSkyBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 6. Critical Alert
    OutlinedButton(
      onClick = { viewModel.triggerCriticalSecurityAlert() },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("trigger_critical_btn"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = SleekCardBg.copy(alpha = 0.4f)),
      border = BorderStroke(1.dp, SleekRoseAlert)
    ) {
      Icon(Icons.Default.Warning, contentDescription = null, tint = SleekRoseAlert)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Trigger: Critical Priority Event", color = SleekRoseAlert, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 7. Low Priority Telemetry
    OutlinedButton(
      onClick = { viewModel.triggerLowPriorityTelemetry() },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("trigger_telemetry_btn"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.outlinedButtonColors(containerColor = SleekCardBg.copy(alpha = 0.4f)),
      border = BorderStroke(1.dp, SleekBorder)
    ) {
      Icon(Icons.Default.Speed, contentDescription = null, tint = SleekTextDarkMuted)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Trigger: Low-Priority Telemetry (Should Silence)", color = SleekTextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Overlay Service Controls
    Text(
      text = "SYSTEM OVERLAY SERVICES",
      color = SleekSkyBlue,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      OutlinedButton(
        onClick = onToggleCursor,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = SleekCardBg.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, if (cursorRunning) SleekEmerald else SleekBorder)
      ) {
        Icon(Icons.Default.Layers, contentDescription = null, tint = if (cursorRunning) SleekEmerald else SleekTextDarkMuted)
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (cursorRunning) "Cursor: ON" else "Cursor: OFF", fontSize = 12.sp, color = SleekTextPrimary)
      }

      OutlinedButton(
        onClick = onToggleFeedback,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = SleekCardBg.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, if (feedbackRunning) SleekEmerald else SleekBorder)
      ) {
        Icon(Icons.Default.Terminal, contentDescription = null, tint = if (feedbackRunning) SleekEmerald else SleekTextDarkMuted)
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (feedbackRunning) "HUD: ON" else "HUD: OFF", fontSize = 12.sp, color = SleekTextPrimary)
      }
    }
  }
}

@Composable
fun MemoryTab(
  memories: List<MemoryEntity>,
  onDeleteMemory: (MemoryEntity) -> Unit,
  onAddMemoryClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("memory_tab")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "MYRAA MEMORY SYSTEM",
          color = SleekSkyBlue,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Text(
          text = "Short-term context & persistent knowledge",
          color = SleekTextSecondary,
          fontSize = 12.sp
        )
      }

      Button(
        onClick = onAddMemoryClick,
        colors = ButtonDefaults.buttonColors(
          containerColor = SleekSkyBlue,
          contentColor = SleekBgDark
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("add_memory_btn")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = SleekBgDark, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Memory", color = SleekBgDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(memories, key = { it.id }) { memory ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = SleekCardBg.copy(alpha = 0.5f)),
          border = BorderStroke(1.dp, SleekBorder)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.Top
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = SleekCardBgDarker,
                  border = BorderStroke(1.dp, SleekBorder)
                ) {
                  Text(
                    text = memory.category,
                    color = SleekSkyBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = memory.title,
                  color = SleekTextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = memory.content,
                color = SleekTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
              )
            }

            IconButton(
              onClick = { onDeleteMemory(memory) },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SleekTextDarkMuted, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
fun VoiceTerminalTab(
  conversations: List<ConversationEntity>,
  isSpeaking: Boolean,
  inputText: String,
  onInputTextChange: (String) -> Unit,
  onSendMessage: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("voice_terminal_tab")
  ) {
    Text(
      text = "CONVERSATIONAL & VOICE INTERACTION",
      color = SleekSkyBlue,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      reverseLayout = true,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(conversations) { item ->
        val isUser = item.sender == "USER"
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isUser) SleekCardBgDarker else SleekCardBg.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, if (isUser) SleekSkyBlue.copy(alpha = 0.5f) else SleekBorder),
            modifier = Modifier.fillMaxWidth(0.85f)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isUser) "YOU" else if (item.isProactive) "MYRAA (PROACTIVE)" else "MYRAA",
                  color = if (isUser) SleekSkyBlue else if (item.isProactive) SleekAmber else SleekSkyBlueLight,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                if (!isUser) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = SleekSkyBlue,
                    modifier = Modifier.size(12.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = item.message,
                color = SleekTextPrimary,
                fontSize = 13.sp,
                lineHeight = 17.sp
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Input row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = inputText,
        onValueChange = onInputTextChange,
        placeholder = { Text("Speak or type to MYRAA...", color = SleekTextDarkMuted, fontSize = 13.sp) },
        modifier = Modifier
          .weight(1f)
          .testTag("chat_input_field"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = SleekSkyBlue,
          unfocusedBorderColor = SleekBorder,
          focusedTextColor = SleekTextPrimary,
          unfocusedTextColor = SleekTextPrimary,
          focusedContainerColor = SleekCardBgDarker,
          unfocusedContainerColor = SleekCardBgDarker
        )
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = onSendMessage,
        modifier = Modifier
          .size(48.dp)
          .background(SleekSkyBlue, CircleShape)
          .testTag("send_message_btn")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send",
          tint = SleekBgDark,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun AddMemoryDialog(
  onSave: (category: String, title: String, content: String, importance: Int) -> Unit,
  onDismiss: () -> Unit
) {
  var category by remember { mutableStateOf("OBSERVATION") }
  var title by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SleekCardBg,
    shape = RoundedCornerShape(24.dp),
    title = { Text("Add Memory Item", color = SleekTextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title", color = SleekTextMuted) },
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SleekSkyBlue,
            unfocusedBorderColor = SleekBorder,
            focusedTextColor = SleekTextPrimary,
            unfocusedTextColor = SleekTextPrimary,
            focusedContainerColor = SleekCardBgDarker,
            unfocusedContainerColor = SleekCardBgDarker
          ),
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Content / Fact / Note", color = SleekTextMuted) },
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SleekSkyBlue,
            unfocusedBorderColor = SleekBorder,
            focusedTextColor = SleekTextPrimary,
            unfocusedTextColor = SleekTextPrimary,
            focusedContainerColor = SleekCardBgDarker,
            unfocusedContainerColor = SleekCardBgDarker
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && content.isNotBlank()) {
            onSave(category, title, content, 3)
          }
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SleekSkyBlue)
      ) {
        Text("Save", color = SleekBgDark, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SleekBorder)
      ) {
        Text("Cancel", color = SleekTextSecondary)
      }
    }
  )
}

