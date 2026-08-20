package com.example.model

import java.util.UUID

enum class DecisionType {
  SILENT,
  NOTIFY,
  SPEAK,
  SUGGEST,
  ACT,
  ASK_CONFIRMATION
}

enum class ActionSafetyLevel {
  READ,
  SUGGEST,
  EXECUTE,
  SENSITIVE_EXECUTE
}

data class ProposedAction(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val description: String,
  val safetyLevel: ActionSafetyLevel = ActionSafetyLevel.SUGGEST,
  val actionType: String = "GENERIC_ACTION",
  val payload: Map<String, String> = emptyMap()
)

data class EventDecision(
  val id: String = UUID.randomUUID().toString(),
  val eventId: String,
  val eventType: EventType,
  val decisionType: DecisionType,
  val speechText: String? = null,
  val notificationTitle: String? = null,
  val notificationBody: String? = null,
  val proposedAction: ProposedAction? = null,
  val relevanceScore: Float = 0.0f,
  val interruptScore: Float = 0.0f,
  val reason: String = "",
  val timestamp: Long = System.currentTimeMillis()
)
