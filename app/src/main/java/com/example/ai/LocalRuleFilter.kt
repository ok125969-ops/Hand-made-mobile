package com.example.ai

import com.example.model.EventType
import com.example.model.MYRAAEvent
import com.example.model.Priority
import com.example.model.SystemContext

object LocalRuleFilter {

  data class FilterResult(
    val passesFilter: Boolean,
    val needsLlmReasoning: Boolean,
    val initialReason: String
  )

  fun evaluate(event: MYRAAEvent, context: SystemContext): FilterResult {
    // 1. Informational low events with no relevance to active task -> ignore directly without LLM
    if (event.priority == Priority.INFORMATIONAL && !isRelevantToTask(event, context.activeTask)) {
      return FilterResult(
        passesFilter = false,
        needsLlmReasoning = false,
        initialReason = "Filtered: Low priority informational event unrelated to active task"
      )
    }

    // 2. Telemetry noise / routine memory updates -> bypass LLM
    if (event.eventType == EventType.MEMORY_UPDATED || event.eventType == EventType.SYSTEM_STATE_CHANGED) {
      if (event.priority == Priority.LOW) {
        return FilterResult(
          passesFilter = false,
          needsLlmReasoning = false,
          initialReason = "Filtered: Routine background state change"
        )
      }
    }

    // 3. High/Critical events always pass
    if (event.priority == Priority.CRITICAL || event.priority == Priority.HIGH) {
      return FilterResult(
        passesFilter = true,
        needsLlmReasoning = true,
        initialReason = "Passed: High/Critical priority event requires reasoning"
      )
    }

    // 4. Build failed or task completed or reminder due
    if (event.eventType in listOf(EventType.BUILD_FAILED, EventType.BUILD_COMPLETED, EventType.TASK_COMPLETED, EventType.TASK_REMINDER_DUE, EventType.REPEATED_ERROR_DETECTED, EventType.USER_RETURNED)) {
      return FilterResult(
        passesFilter = true,
        needsLlmReasoning = true,
        initialReason = "Passed: Actionable workflow event"
      )
    }

    // Default medium passes, low is dropped unless relevant
    val isRelevant = isRelevantToTask(event, context.activeTask)
    return if (event.priority == Priority.MEDIUM || isRelevant) {
      FilterResult(
        passesFilter = true,
        needsLlmReasoning = true,
        initialReason = "Passed: Medium priority or relevant to current task"
      )
    } else {
      FilterResult(
        passesFilter = false,
        needsLlmReasoning = false,
        initialReason = "Filtered: Low relevance"
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
