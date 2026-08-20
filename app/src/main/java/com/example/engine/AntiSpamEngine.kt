package com.example.engine

import com.example.model.EventType
import com.example.model.MYRAAEvent
import com.example.model.MyraaSettings
import com.example.model.Priority
import com.example.model.SystemContext
import com.example.model.UserActivityState
import java.util.concurrent.ConcurrentHashMap

class AntiSpamEngine {

  data class AntiSpamResult(
    val isAllowed: Boolean,
    val reason: String,
    val repetitionCount: Int,
    val isEscalation: Boolean
  )

  private val eventHistory = ConcurrentHashMap<String, Long>()
  private val eventCounts = ConcurrentHashMap<String, Int>()
  private var lastSpeechTimestamp: Long = 0L

  fun evaluate(
    event: MYRAAEvent,
    context: SystemContext,
    settings: MyraaSettings
  ): AntiSpamResult {
    val now = System.currentTimeMillis()
    val deduplicationKey = event.deduplicationKey

    // 1. Check if proactive intelligence is completely disabled
    if (!settings.proactiveIntelligenceEnabled) {
      return AntiSpamResult(
        isAllowed = false,
        reason = "Proactive intelligence disabled in settings",
        repetitionCount = 0,
        isEscalation = false
      )
    }

    // 2. Critical events bypass general cooldowns
    val isCritical = event.priority == Priority.CRITICAL

    // 3. User actively conversing with MYRAA -> Proactive interruptions should not talk over conversation
    if (context.userState == UserActivityState.IN_CONVERSATION && !isCritical && event.eventType != EventType.USER_INTERACTION) {
      return AntiSpamResult(
        isAllowed = false,
        reason = "User is currently in conversation with MYRAA",
        repetitionCount = 0,
        isEscalation = false
      )
    }

    // 4. Duplicate event detection and recurrence counting
    val lastOccurrence = eventHistory[deduplicationKey] ?: 0L
    val count = (eventCounts[deduplicationKey] ?: 0) + 1
    eventCounts[deduplicationKey] = count
    eventHistory[deduplicationKey] = now

    val timeSinceLastSameEvent = now - lastOccurrence
    val duplicateCooldownMs = (settings.cooldownSeconds * 1000L).coerceAtLeast(10_000L)

    // Repeated occurrence escalation check (e.g. 3rd repetition of same failure)
    val isEscalation = count == 3 && event.eventType == EventType.BUILD_FAILED

    if (lastOccurrence > 0L && timeSinceLastSameEvent < duplicateCooldownMs && !isCritical) {
      if (isEscalation) {
        return AntiSpamResult(
          isAllowed = true,
          reason = "Escalated: Same error detected $count times",
          repetitionCount = count,
          isEscalation = true
        )
      }
      return AntiSpamResult(
        isAllowed = false,
        reason = "Suppressed: Duplicate event received within ${duplicateCooldownMs / 1000}s window (occurrence #$count)",
        repetitionCount = count,
        isEscalation = false
      )
    }

    // 5. Global Speech Cooldown check (prevent talking every 2 seconds)
    val timeSinceLastSpeech = now - lastSpeechTimestamp
    val globalSpeechCooldownMs = 8_000L
    if (timeSinceLastSpeech < globalSpeechCooldownMs && !isCritical && event.priority != Priority.HIGH) {
      return AntiSpamResult(
        isAllowed = false,
        reason = "Suppressed: Global speech cooldown active (${timeSinceLastSpeech / 1000}s since last speech)",
        repetitionCount = count,
        isEscalation = false
      )
    }

    return AntiSpamResult(
      isAllowed = true,
      reason = "Allowed by Anti-Spam engine",
      repetitionCount = count,
      isEscalation = isEscalation
    )
  }

  fun recordSpeech() {
    lastSpeechTimestamp = System.currentTimeMillis()
  }

  fun reset() {
    eventHistory.clear()
    eventCounts.clear()
    lastSpeechTimestamp = 0L
  }
}
