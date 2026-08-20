package com.example.engine

import com.example.model.DecisionType
import com.example.model.EventType
import com.example.model.InterruptionLevel
import com.example.model.MYRAAEvent
import com.example.model.MyraaSettings
import com.example.model.NotificationMode
import com.example.model.Priority
import com.example.model.SystemContext
import com.example.model.UserActivityState
import java.util.Calendar

class InterruptibilityEvaluator {

  data class EvaluationResult(
    val decisionType: DecisionType,
    val interruptScore: Float,
    val relevanceScore: Float,
    val reason: String
  )

  fun evaluate(
    event: MYRAAEvent,
    context: SystemContext,
    settings: MyraaSettings,
    repeatCount: Int,
    relevantMemoriesCount: Int
  ): EvaluationResult {
    // 1. Settings check
    if (!settings.proactiveIntelligenceEnabled) {
      return EvaluationResult(
        decisionType = DecisionType.SILENT,
        interruptScore = 0.0f,
        relevanceScore = 0.0f,
        reason = "Proactive intelligence disabled in settings"
      )
    }

    // 2. Quiet Hours check
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isQuietHours = settings.isQuietHoursActive(currentHour)
    if (isQuietHours && event.priority != Priority.CRITICAL) {
      return EvaluationResult(
        decisionType = DecisionType.SILENT,
        interruptScore = 0.1f,
        relevanceScore = 0.2f,
        reason = "Quiet hours active ($currentHour:00) - event remembered silently"
      )
    }

    // 3. User explicit conversational interaction
    if (event.eventType == EventType.USER_INTERACTION) {
      return EvaluationResult(
        decisionType = DecisionType.SPEAK,
        interruptScore = 1.0f,
        relevanceScore = 1.0f,
        reason = "User initiated conversational interaction"
      )
    }

    // 4. Calculate Relevance Score (0.0 to 1.0)
    val isTaskRelated = isRelevantToTask(event, context.activeTask)
    var relevance = when {
      event.priority == Priority.CRITICAL -> 1.0f
      event.eventType == EventType.BUILD_FAILED || event.eventType == EventType.BUILD_COMPLETED -> if (isTaskRelated) 0.95f else 0.8f
      event.eventType == EventType.TASK_REMINDER_DUE -> 0.9f
      event.eventType == EventType.REPEATED_ERROR_DETECTED -> 0.85f
      event.eventType == EventType.USER_RETURNED -> 0.7f
      isTaskRelated -> 0.75f
      else -> 0.35f
    }
    if (relevantMemoriesCount > 0) {
      relevance = (relevance + 0.15f).coerceAtMost(1.0f)
    }

    // 5. Calculate Interruptibility Model:
    // interruptScore = eventImportance + urgency + relevance + userAvailability - interruptionCost - repetitionPenalty
    val eventImportance = event.priority.weight * 0.35f
    val urgency = when (event.priority) {
      Priority.CRITICAL -> 0.30f
      Priority.HIGH -> 0.20f
      Priority.MEDIUM -> 0.10f
      Priority.LOW -> 0.05f
      Priority.INFORMATIONAL -> 0.0f
    }
    val relevanceComponent = relevance * 0.25f
    val userAvailability = context.userState.availabilityScore * 0.20f

    // Interruption cost based on user state
    val interruptionCost = when (context.userState) {
      UserActivityState.BUSY -> 0.35f
      UserActivityState.WATCHING_MEDIA -> 0.30f
      UserActivityState.READING -> 0.15f
      UserActivityState.CODING -> 0.08f
      UserActivityState.IDLE -> 0.0f
      UserActivityState.IN_CONVERSATION -> 0.05f
    }

    // Repetition penalty if repeated many times without escalation
    val repetitionPenalty = if (repeatCount > 1 && repeatCount != 3) 0.15f else 0.0f

    val interruptScore = (eventImportance + urgency + relevanceComponent + userAvailability - interruptionCost - repetitionPenalty)
      .coerceIn(0.0f, 1.0f)

    // 6. Map Interrupt Score to Decision Type using User Configured Interruption Level
    val threshold = settings.interruptionLevel.threshold
    val notificationMode = settings.notificationMode

    // Critical always alerts
    if (event.priority == Priority.CRITICAL) {
      val decision = when (notificationMode) {
        NotificationMode.OFF -> DecisionType.SILENT
        NotificationMode.NOTIFICATION -> DecisionType.NOTIFY
        NotificationMode.VOICE, NotificationMode.BOTH -> if (settings.proactiveVoiceEnabled) DecisionType.SPEAK else DecisionType.NOTIFY
      }
      return EvaluationResult(
        decisionType = decision,
        interruptScore = interruptScore,
        relevanceScore = relevance,
        reason = "Critical priority override"
      )
    }

    // User Busy + Low/Medium priority check
    if ((context.userState == UserActivityState.BUSY || context.userState == UserActivityState.WATCHING_MEDIA) && event.priority != Priority.HIGH) {
      return EvaluationResult(
        decisionType = DecisionType.SILENT,
        interruptScore = interruptScore,
        relevanceScore = relevance,
        reason = "User is ${context.userState.description} - low/medium priority event silenced"
      )
    }

    // Low / Informational priority check
    if (event.priority == Priority.LOW || event.priority == Priority.INFORMATIONAL) {
      return EvaluationResult(
        decisionType = DecisionType.SILENT,
        interruptScore = interruptScore,
        relevanceScore = relevance,
        reason = "Low priority event stored silently without interruption"
      )
    }

    // Compare with threshold
    return if (interruptScore >= threshold) {
      val decision = when (notificationMode) {
        NotificationMode.OFF -> DecisionType.SILENT
        NotificationMode.NOTIFICATION -> DecisionType.NOTIFY
        NotificationMode.VOICE, NotificationMode.BOTH -> {
          if (settings.proactiveVoiceEnabled) DecisionType.SPEAK else DecisionType.NOTIFY
        }
      }
      EvaluationResult(
        decisionType = decision,
        interruptScore = interruptScore,
        relevanceScore = relevance,
        reason = "Interrupt score (${String.format("%.2f", interruptScore)}) exceeded threshold (${String.format("%.2f", threshold)})"
      )
    } else if (interruptScore >= threshold - 0.20f && notificationMode != NotificationMode.OFF) {
      EvaluationResult(
        decisionType = DecisionType.NOTIFY,
        interruptScore = interruptScore,
        relevanceScore = relevance,
        reason = "Moderate interrupt score - non-intrusive notification"
      )
    } else {
      EvaluationResult(
        decisionType = DecisionType.SILENT,
        interruptScore = interruptScore,
        relevanceScore = relevance,
        reason = "Interrupt score below threshold (${String.format("%.2f", interruptScore)} < ${String.format("%.2f", threshold)}) - remaining silent"
      )
    }
  }

  private fun isRelevantToTask(event: MYRAAEvent, activeTask: String): Boolean {
    if (activeTask.isBlank()) return false
    val taskTokens = activeTask.lowercase().split(" ", "_", "-", ".").filter { it.length > 2 }
    val eventText = "${event.title} ${event.message} ${event.metadata.values.joinToString(" ")}".lowercase()
    return taskTokens.any { token -> eventText.contains(token) }
  }
}
