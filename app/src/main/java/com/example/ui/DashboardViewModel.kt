package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiService
import com.example.data.database.ConversationEntity
import com.example.data.database.MemoryEntity
import com.example.data.database.MyraaDatabase
import com.example.data.repository.MemoryRepository
import com.example.engine.AntiSpamEngine
import com.example.engine.ContextCollector
import com.example.engine.InterruptibilityEvaluator
import com.example.engine.MYRAAEventBus
import com.example.engine.ProactiveIntelligenceEngine
import com.example.model.DecisionType
import com.example.model.EventDecision
import com.example.model.EventSource
import com.example.model.EventType
import com.example.model.MYRAAEvent
import com.example.model.MyraaSettings
import com.example.model.Priority
import com.example.model.ProposedAction
import com.example.model.SystemContext
import com.example.model.UserActivityState
import com.example.service.CursorOverlayService
import com.example.service.FeedbackOverlayService
import com.example.voice.MyraaVoiceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

  private val database = MyraaDatabase.getInstance(application)
  val memoryRepository = MemoryRepository(
    database.memoryDao(),
    database.eventLogDao(),
    database.conversationDao()
  )

  val eventBus = MYRAAEventBus()
  val contextCollector = ContextCollector(memoryRepository)
  val antiSpamEngine = AntiSpamEngine()
  val interruptibilityEvaluator = InterruptibilityEvaluator()
  val geminiAiService = GeminiAiService()
  val voiceManager = MyraaVoiceManager(application)

  val engine = ProactiveIntelligenceEngine(
    eventBus = eventBus,
    contextCollector = contextCollector,
    memoryRepository = memoryRepository,
    antiSpamEngine = antiSpamEngine,
    interruptibilityEvaluator = interruptibilityEvaluator,
    geminiAiService = geminiAiService,
    voiceManager = voiceManager,
    scope = viewModelScope
  )

  val systemContext: StateFlow<SystemContext> = contextCollector.contextState
  val myraaSettings: StateFlow<MyraaSettings> = engine.settings
  val latestDecision: StateFlow<EventDecision?> = engine.latestDecision
  val decisionHistory: StateFlow<List<EventDecision>> = engine.decisionHistory
  val pendingAction: StateFlow<ProposedAction?> = engine.pendingActionForConfirmation
  val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking

  val memories: Flow<List<MemoryEntity>> = memoryRepository.allMemories
  val conversations: Flow<List<ConversationEntity>> = memoryRepository.recentConversations

  val cursorOverlayRunning: StateFlow<Boolean> = CursorOverlayService.isRunning
  val feedbackOverlayRunning: StateFlow<Boolean> = FeedbackOverlayService.isRunning

  private val _simulatedBuildErrorCount = MutableStateFlow(0)
  val simulatedBuildErrorCount: StateFlow<Int> = _simulatedBuildErrorCount.asStateFlow()

  init {
    seedInitialMemories()
  }

  private fun seedInitialMemories() {
    viewModelScope.launch {
      val existing = memoryRepository.getActiveTask()
      if (existing == null) {
        memoryRepository.setActiveTask(
          "Debugging GestureControl build and integrating proactive intelligence",
          "Active Sprint Task"
        )
        memoryRepository.addMemory(
          "LONG_TERM_FACT",
          "Developer Preference",
          "Prefers concise speech feedback during active coding sessions; minimize interruptions unless compiler errors occur.",
          4,
          "preference,dev"
        )
        memoryRepository.addMemory(
          "OBSERVATION",
          "GestureControl Pipeline",
          "Previous build failed on gesture mapper callback logic in DashboardViewModel.kt",
          3,
          "build,gesture,error"
        )
        memoryRepository.addConversation("MYRAA", "MYRAA online. Proactive contextual intelligence initialized.", isProactive = true)
      }
    }
  }

  fun sendUserMessage(text: String) {
    if (text.isBlank()) return
    viewModelScope.launch {
      memoryRepository.addConversation("USER", text, isProactive = false)
      contextCollector.recordUserInteraction()

      // Post user interaction event to intelligence pipeline
      val event = MYRAAEvent(
        eventType = EventType.USER_INTERACTION,
        priority = Priority.HIGH,
        source = EventSource.USER_INPUT,
        title = "User Conversational Turn",
        message = text
      )
      eventBus.postEvent(event)

      // Generate conversational reply
      val reply = geminiAiService.generateConversationalReply(
        userMessage = text,
        context = systemContext.value,
        relevantMemories = listOf("Active Task: ${systemContext.value.activeTask}")
      )

      memoryRepository.addConversation("MYRAA", reply, isProactive = false)
      voiceManager.speak(reply, isProactive = false)
    }
  }

  fun updateSettings(newSettings: MyraaSettings) {
    engine.updateSettings(newSettings)
  }

  fun updateUserActivity(newState: UserActivityState) {
    contextCollector.updateUserState(newState)
  }

  fun updateActiveTask(taskName: String) {
    viewModelScope.launch {
      contextCollector.updateActiveTask(taskName)
      memoryRepository.setActiveTask(taskName)
    }
  }

  fun addMemory(category: String, title: String, content: String, importance: Int) {
    viewModelScope.launch {
      memoryRepository.addMemory(category, title, content, importance)
    }
  }

  fun deleteMemory(memory: MemoryEntity) {
    viewModelScope.launch {
      memoryRepository.deleteMemory(memory)
    }
  }

  fun confirmPendingAction(action: ProposedAction) {
    engine.confirmPendingAction(action)
  }

  fun dismissPendingAction() {
    engine.dismissPendingAction()
  }

  fun stopSpeaking() {
    voiceManager.stopSpeaking()
  }

  // --- Quick Proactive Event Simulation Triggers ---

  fun triggerBuildFailed(file: String = "DashboardViewModel.kt") {
    viewModelScope.launch {
      _simulatedBuildErrorCount.value += 1
      val event = MYRAAEvent(
        eventType = EventType.BUILD_FAILED,
        priority = Priority.HIGH,
        source = EventSource.BUILD_SYSTEM,
        title = "Build Failed: Compilation Error",
        message = "Type mismatch and unresolved reference in $file: line 42",
        metadata = mapOf("target" to file, "error" to "TypeMismatch", "iteration" to "${_simulatedBuildErrorCount.value}")
      )
      eventBus.postEvent(event)
    }
  }

  fun triggerBuildSuccess() {
    viewModelScope.launch {
      val event = MYRAAEvent(
        eventType = EventType.BUILD_COMPLETED,
        priority = Priority.HIGH,
        source = EventSource.BUILD_SYSTEM,
        title = "Build Succeeded",
        message = "AssembleDebug generated app-debug.apk in 12.4s",
        metadata = mapOf("artifact" to "app-debug.apk", "target" to "GestureControl")
      )
      eventBus.postEvent(event)
    }
  }

  fun triggerReminderDue(task: String = "GestureControl Pull Request Review") {
    viewModelScope.launch {
      val event = MYRAAEvent(
        eventType = EventType.TASK_REMINDER_DUE,
        priority = Priority.MEDIUM,
        source = EventSource.REMINDER_SCHEDULER,
        title = task,
        message = "Scheduled review reminder due at 06:30",
        metadata = mapOf("task" to task)
      )
      eventBus.postEvent(event)
    }
  }

  fun triggerRepeatedErrorSimulation() {
    viewModelScope.launch {
      // Trigger multiple build fails to demonstrate recurrence escalation
      repeat(3) {
        _simulatedBuildErrorCount.value += 1
      }
      val event = MYRAAEvent(
        eventType = EventType.BUILD_FAILED,
        priority = Priority.HIGH,
        source = EventSource.BUILD_SYSTEM,
        title = "Recurring Build Failure",
        message = "Unresolved reference in DashboardViewModel.kt (Occurred 3 times)",
        metadata = mapOf("target" to "DashboardViewModel.kt", "error" to "RecurringError"),
        deduplicationKey = "BUILD_FAILED_DashboardViewModel.kt"
      )
      eventBus.postEvent(event)
    }
  }

  fun triggerUserReturned() {
    viewModelScope.launch {
      contextCollector.updateUserState(UserActivityState.IDLE)
      val event = MYRAAEvent(
        eventType = EventType.USER_RETURNED,
        priority = Priority.MEDIUM,
        source = EventSource.SYSTEM_TELEMETRY,
        title = "User Returned to Workstation",
        message = "Resumed session after 25 minutes of inactivity",
        metadata = mapOf("duration_min" to "25")
      )
      eventBus.postEvent(event)
    }
  }

  fun triggerCriticalSecurityAlert() {
    viewModelScope.launch {
      val event = MYRAAEvent(
        eventType = EventType.IMPORTANT_NOTIFICATION,
        priority = Priority.CRITICAL,
        source = EventSource.SYSTEM_TELEMETRY,
        title = "Critical Security Alert",
        message = "Unauthorized token refresh attempt blocked",
        metadata = mapOf("severity" to "CRITICAL")
      )
      eventBus.postEvent(event)
    }
  }

  fun triggerLowPriorityTelemetry() {
    viewModelScope.launch {
      val event = MYRAAEvent(
        eventType = EventType.SYSTEM_STATE_CHANGED,
        priority = Priority.INFORMATIONAL,
        source = EventSource.SYSTEM_TELEMETRY,
        title = "Background Memory Sync",
        message = "Indexed 4 new cache blocks",
        metadata = mapOf("target" to "telemetry")
      )
      eventBus.postEvent(event)
    }
  }

  fun toggleCursorOverlay(context: Context) {
    val intent = Intent(context, CursorOverlayService::class.java)
    if (CursorOverlayService.isRunning.value) {
      context.stopService(intent)
    } else {
      context.startService(intent)
    }
  }

  fun toggleFeedbackOverlay(context: Context) {
    val intent = Intent(context, FeedbackOverlayService::class.java)
    if (FeedbackOverlayService.isRunning.value) {
      context.stopService(intent)
    } else {
      context.startService(intent)
    }
  }

  override fun onCleared() {
    super.onCleared()
    voiceManager.shutdown()
  }
}
