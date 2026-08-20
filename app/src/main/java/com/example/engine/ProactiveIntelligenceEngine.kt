package com.example.engine

import android.util.Log
import com.example.ai.GeminiAiService
import com.example.ai.LocalRuleFilter
import com.example.data.repository.MemoryRepository
import com.example.model.ActionSafetyLevel
import com.example.model.DecisionType
import com.example.model.EventDecision
import com.example.model.EventType
import com.example.model.MYRAAEvent
import com.example.model.MyraaSettings
import com.example.model.NotificationMode
import com.example.model.Priority
import com.example.model.ProposedAction
import com.example.service.CursorOverlayService
import com.example.service.FeedbackOverlayService
import com.example.voice.MyraaVoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProactiveIntelligenceEngine(
  private val eventBus: MYRAAEventBus,
  private val contextCollector: ContextCollector,
  private val memoryRepository: MemoryRepository,
  private val antiSpamEngine: AntiSpamEngine,
  private val interruptibilityEvaluator: InterruptibilityEvaluator,
  private val geminiAiService: GeminiAiService,
  private val voiceManager: MyraaVoiceManager?,
  private val scope: CoroutineScope
) {

  private val _settings = MutableStateFlow(MyraaSettings())
  val settings: StateFlow<MyraaSettings> = _settings.asStateFlow()

  private val _latestDecision = MutableStateFlow<EventDecision?>(null)
  val latestDecision: StateFlow<EventDecision?> = _latestDecision.asStateFlow()

  private val _decisionHistory = MutableStateFlow<List<EventDecision>>(emptyList())
  val decisionHistory: StateFlow<List<EventDecision>> = _decisionHistory.asStateFlow()

  private val _pendingActionForConfirmation = MutableStateFlow<ProposedAction?>(null)
  val pendingActionForConfirmation: StateFlow<ProposedAction?> = _pendingActionForConfirmation.asStateFlow()

  private var eventSubscriptionJob: Job? = null

  init {
    startListening()
  }

  fun updateSettings(newSettings: MyraaSettings) {
    _settings.value = newSettings
  }

  fun startListening() {
    eventSubscriptionJob?.cancel()
    eventSubscriptionJob = eventBus.events
      .onEach { event ->
        processEvent(event)
      }
      .launchIn(scope)
  }

  fun stopListening() {
    eventSubscriptionJob?.cancel()
    eventSubscriptionJob = null
  }

  suspend fun processEvent(event: MYRAAEvent): EventDecision {
    val currentSettings = _settings.value
    val context = contextCollector.getCurrentContext()

    // Log structured ingress
    Log.d("MYRAA_PROACTIVE", "Incoming event: ${event.eventType.name} [${event.priority.name}] - ${event.title}")

    // Step 0: Check global master toggle
    if (!currentSettings.proactiveIntelligenceEnabled) {
      val decision = EventDecision(
        eventId = event.eventId,
        eventType = event.eventType,
        decisionType = DecisionType.SILENT,
        reason = "Proactive intelligence disabled in settings",
        relevanceScore = 0.0f,
        interruptScore = 0.0f
      )
      logDecision(event, decision)
      recordAndEmitDecision(event, decision)
      return decision
    }

    // Step 1: Cheap Local Filter (AI Cost Control)
    val localFilterResult = LocalRuleFilter.evaluate(event, context)
    if (!localFilterResult.passesFilter && event.eventType != EventType.USER_INTERACTION) {
      val decision = EventDecision(
        eventId = event.eventId,
        eventType = event.eventType,
        decisionType = DecisionType.SILENT,
        reason = localFilterResult.initialReason,
        relevanceScore = 0.1f,
        interruptScore = 0.05f
      )
      logDecision(event, decision)
      recordAndEmitDecision(event, decision)
      return decision
    }

    // Step 2: Anti-Spam & Deduplication Filter
    val antiSpamResult = antiSpamEngine.evaluate(event, context, currentSettings)
    if (!antiSpamResult.isAllowed && event.eventType != EventType.USER_INTERACTION) {
      val decision = EventDecision(
        eventId = event.eventId,
        eventType = event.eventType,
        decisionType = DecisionType.SILENT,
        reason = antiSpamResult.reason,
        relevanceScore = 0.4f,
        interruptScore = 0.1f
      )
      logDecision(event, decision)
      recordAndEmitDecision(event, decision)
      return decision
    }

    // Step 3: Context & Memory Retrieval
    val relevantMemories = memoryRepository.findRelevantMemories(event.title + " " + event.message)
    val memoryContents = relevantMemories.map { "${it.title}: ${it.content}" }

    // Step 4: Interruptibility Evaluation
    val evalResult = interruptibilityEvaluator.evaluate(
      event = event,
      context = context,
      settings = currentSettings,
      repeatCount = antiSpamResult.repetitionCount,
      relevantMemoriesCount = relevantMemories.size
    )

    // Step 5: Decision Resolution & AI Generation
    var finalDecisionType = evalResult.decisionType
    var speechText: String? = null
    var proposedAction: ProposedAction? = null
    var finalReason = evalResult.reason

    if (finalDecisionType == DecisionType.SPEAK || finalDecisionType == DecisionType.NOTIFY || antiSpamResult.isEscalation) {
      // Generate concise proactive speech & suggested action
      val aiOutcome = geminiAiService.generateProactiveResponse(
        event = event,
        context = context,
        relevantMemories = memoryContents,
        repeatCount = antiSpamResult.repetitionCount
      )

      speechText = aiOutcome.speechText
      proposedAction = aiOutcome.proposedAction
      finalReason = "${evalResult.reason} | ${aiOutcome.reason}"

      if (proposedAction != null) {
        if (proposedAction.safetyLevel == ActionSafetyLevel.SENSITIVE_EXECUTE ||
          (proposedAction.safetyLevel == ActionSafetyLevel.EXECUTE && !currentSettings.allowSensitiveActionAutoExecute)
        ) {
          finalDecisionType = DecisionType.ASK_CONFIRMATION
        } else if (proposedAction.safetyLevel == ActionSafetyLevel.SUGGEST) {
          finalDecisionType = DecisionType.SUGGEST
        }
      }
    }

    val finalDecision = EventDecision(
      eventId = event.eventId,
      eventType = event.eventType,
      decisionType = finalDecisionType,
      speechText = speechText,
      notificationTitle = if (finalDecisionType != DecisionType.SILENT) "MYRAA: ${event.title}" else null,
      notificationBody = speechText ?: event.message,
      proposedAction = proposedAction,
      relevanceScore = evalResult.relevanceScore,
      interruptScore = evalResult.interruptScore,
      reason = finalReason
    )

    // Step 6: Dispatch Speech / Overlays / Actions
    executeDecisionActions(event, finalDecision)

    // Step 7: Structured Logging
    logDecision(event, finalDecision)

    // Step 8: Persistence & Stream Update
    recordAndEmitDecision(event, finalDecision)

    return finalDecision
  }

  private suspend fun executeDecisionActions(event: MYRAAEvent, decision: EventDecision) {
    when (decision.decisionType) {
      DecisionType.SPEAK -> {
        decision.speechText?.let { text ->
          voiceManager?.speak(text, isProactive = true)
          antiSpamEngine.recordSpeech()
          contextCollector.recordMyraaSpeech()
          FeedbackOverlayService.showFeedback(text)
          CursorOverlayService.updateState("SPEAKING")
          memoryRepository.addConversation("MYRAA", text, isProactive = true)
        }
      }
      DecisionType.SUGGEST -> {
        decision.speechText?.let { text ->
          if (_settings.value.proactiveVoiceEnabled) {
            voiceManager?.speak(text, isProactive = true)
            antiSpamEngine.recordSpeech()
            contextCollector.recordMyraaSpeech()
          }
          FeedbackOverlayService.showFeedback(text)
          CursorOverlayService.updateState("SUGGEST")
          memoryRepository.addConversation("MYRAA", text, isProactive = true)
        }
      }
      DecisionType.ASK_CONFIRMATION -> {
        decision.speechText?.let { text ->
          if (_settings.value.proactiveVoiceEnabled) {
            voiceManager?.speak(text, isProactive = true)
            antiSpamEngine.recordSpeech()
            contextCollector.recordMyraaSpeech()
          }
          FeedbackOverlayService.showFeedback(text)
          memoryRepository.addConversation("MYRAA", text, isProactive = true)
        }
        _pendingActionForConfirmation.value = decision.proposedAction
      }
      DecisionType.NOTIFY -> {
        FeedbackOverlayService.showFeedback(decision.notificationBody ?: event.title)
        CursorOverlayService.updateState("NOTIFY")
      }
      DecisionType.ACT -> {
        CursorOverlayService.updateState("ACTING")
      }
      DecisionType.SILENT -> {
        // Silent observation - intentionally not interrupting user
      }
    }
  }

  private fun logDecision(event: MYRAAEvent, decision: EventDecision) {
    val logOutput = """
      [MYRAA][PROACTIVE]
      Event: ${event.eventType.name}
      Priority: ${event.priority.name}
      Relevance: ${String.format("%.2f", decision.relevanceScore)}
      Interruptibility: ${String.format("%.2f", decision.interruptScore)}
      Decision: ${decision.decisionType.name}
      Reason: ${decision.reason}
      ${if (decision.speechText != null) "Speech: \"${decision.speechText}\"" else ""}
    """.trimIndent()

    Log.i("MYRAA_PROACTIVE", logOutput)
  }

  private suspend fun recordAndEmitDecision(event: MYRAAEvent, decision: EventDecision) {
    _latestDecision.value = decision
    _decisionHistory.update { listOf(decision) + it.take(49) }
    memoryRepository.recordDecision(event, decision)
  }

  fun confirmPendingAction(action: ProposedAction) {
    scope.launch {
      _pendingActionForConfirmation.value = null
      val message = "Executed: ${action.title}"
      FeedbackOverlayService.showFeedback(message)
      voiceManager?.speak(message, isProactive = true)
      memoryRepository.addConversation("MYRAA", message, isProactive = true)
      memoryRepository.addMemory("OBSERVATION", "Executed Action", "${action.title} - ${action.description}", 4)
    }
  }

  fun dismissPendingAction() {
    _pendingActionForConfirmation.value = null
  }
}
