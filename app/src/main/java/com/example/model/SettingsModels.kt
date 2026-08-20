package com.example.model

enum class NotificationMode {
  OFF,
  NOTIFICATION,
  VOICE,
  BOTH
}

enum class InterruptionLevel(val threshold: Float, val label: String) {
  LOW(0.75f, "Low (Interrupt for Critical / High only)"),
  BALANCED(0.45f, "Balanced (Recommended JARVIS Mode)"),
  HIGH(0.20f, "High (Active updates & frequent observations)")
}

data class MyraaSettings(
  val proactiveIntelligenceEnabled: Boolean = true,
  val proactiveVoiceEnabled: Boolean = true,
  val notificationMode: NotificationMode = NotificationMode.BOTH,
  val interruptionLevel: InterruptionLevel = InterruptionLevel.BALANCED,
  val quietHoursEnabled: Boolean = false,
  val quietHoursStartHour: Int = 22,
  val quietHoursEndHour: Int = 7,
  val cooldownSeconds: Int = 20,
  val allowSensitiveActionAutoExecute: Boolean = false
) {
  fun isQuietHoursActive(currentHour: Int): Boolean {
    if (!quietHoursEnabled) return false
    return if (quietHoursStartHour > quietHoursEndHour) {
      currentHour >= quietHoursStartHour || currentHour < quietHoursEndHour
    } else {
      currentHour in quietHoursStartHour until quietHoursEndHour
    }
  }
}
