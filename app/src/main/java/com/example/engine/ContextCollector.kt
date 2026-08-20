package com.example.engine

import com.example.data.repository.MemoryRepository
import com.example.model.SystemContext
import com.example.model.UserActivityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ContextCollector(
  private val memoryRepository: MemoryRepository
) {
  private val _contextState = MutableStateFlow(
    SystemContext(
      currentApp = "Android Studio / Proactive Project",
      activeTask = "GestureControl Build & Debug",
      userState = UserActivityState.CODING,
      lastUserInteractionTime = System.currentTimeMillis() - 20_000L,
      lastMyraaSpeechTime = 0L,
      recentErrorsCount = 0,
      isAudioPlaying = false,
      batteryLevel = 92,
      isCharging = true
    )
  )
  val contextState: StateFlow<SystemContext> = _contextState.asStateFlow()

  fun getCurrentContext(): SystemContext = _contextState.value

  fun updateUserState(newState: UserActivityState) {
    _contextState.update { it.copy(userState = newState, lastUserInteractionTime = System.currentTimeMillis()) }
  }

  fun updateActiveTask(taskName: String) {
    _contextState.update { it.copy(activeTask = taskName) }
  }

  fun updateCurrentApp(appName: String) {
    _contextState.update { it.copy(currentApp = appName) }
  }

  fun recordUserInteraction() {
    _contextState.update { it.copy(lastUserInteractionTime = System.currentTimeMillis()) }
  }

  fun recordMyraaSpeech() {
    _contextState.update { it.copy(lastMyraaSpeechTime = System.currentTimeMillis()) }
  }

  suspend fun refreshRecentErrors() {
    val count = memoryRepository.countRecentErrors(10)
    _contextState.update { it.copy(recentErrorsCount = count) }
  }
}
