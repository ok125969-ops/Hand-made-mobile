package com.example.model

import java.util.UUID

enum class Priority(val weight: Float) {
  CRITICAL(1.0f),
  HIGH(0.8f),
  MEDIUM(0.5f),
  LOW(0.25f),
  INFORMATIONAL(0.1f)
}

enum class EventType {
  BUILD_FAILED,
  BUILD_COMPLETED,
  TASK_COMPLETED,
  TASK_REMINDER_DUE,
  IMPORTANT_NOTIFICATION,
  USER_RETURNED,
  SYSTEM_STATE_CHANGED,
  AI_ACTION_COMPLETED,
  TOOL_EXECUTION_FAILED,
  MEMORY_UPDATED,
  REPEATED_ERROR_DETECTED,
  USER_INTERACTION
}

enum class EventSource {
  BUILD_SYSTEM,
  BACKGROUND_TASK,
  REMINDER_SCHEDULER,
  SYSTEM_TELEMETRY,
  TOOL_RUNNER,
  USER_INPUT,
  CONVERSATION_MANAGER
}

data class MYRAAEvent(
  val eventId: String = UUID.randomUUID().toString(),
  val eventType: EventType,
  val priority: Priority,
  val source: EventSource,
  val title: String,
  val message: String,
  val metadata: Map<String, String> = emptyMap(),
  val deduplicationKey: String = "${eventType.name}_${metadata["target"] ?: title}",
  val timestamp: Long = System.currentTimeMillis()
)
