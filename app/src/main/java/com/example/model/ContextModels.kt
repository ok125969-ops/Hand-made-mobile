package com.example.model

enum class UserActivityState(val description: String, val availabilityScore: Float) {
  IDLE("Idle / Ready", 0.9f),
  CODING("Active Development", 0.6f),
  READING("Reading / Reviewing", 0.5f),
  WATCHING_MEDIA("Watching Media / Presentation", 0.2f),
  IN_CONVERSATION("Conversing with MYRAA", 1.0f),
  BUSY("Do Not Disturb / Busy", 0.1f)
}

data class SystemContext(
  val currentApp: String = "IDE / Android Studio",
  val activeTask: String = "Debugging DashboardViewModel",
  val userState: UserActivityState = UserActivityState.CODING,
  val lastUserInteractionTime: Long = System.currentTimeMillis() - 45_000L,
  val lastMyraaSpeechTime: Long = 0L,
  val recentErrorsCount: Int = 0,
  val isAudioPlaying: Boolean = false,
  val batteryLevel: Int = 88,
  val isCharging: Boolean = true
)
