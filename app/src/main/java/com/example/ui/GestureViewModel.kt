package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.actions.ActionExecutor
import com.example.actions.DefaultGestureMappings
import com.example.actions.GestureAccessibilityService
import com.example.actions.GestureAction
import com.example.actions.GesturePresetProfile
import com.example.camera.PerformanceMode
import com.example.data.database.GestureCalibrationEntity
import com.example.data.database.GestureHistoryEntity
import com.example.data.database.GestureSettingsEntity
import com.example.data.database.MyraaDatabase
import com.example.data.repository.GestureRepository
import com.example.gesture.GestureLifecycleState
import com.example.gesture.GestureType
import com.example.gesture.RecognizedGesture
import com.example.vision.HandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GestureUiState(
  val isGestureControlEnabled: Boolean = true,
  val currentGesture: RecognizedGesture? = null,
  val detectedHands: List<HandResult> = emptyList(),
  val lastExecutedAction: GestureAction? = null,
  val feedbackMessage: String? = null,
  val fps: Float = 0f,
  val latencyMs: Long = 0L,
  val performanceMode: PerformanceMode = PerformanceMode.BALANCED,
  val sensitivity: Float = 1.0f,
  val showLandmarks: Boolean = true,
  val hapticFeedback: Boolean = true,
  val soundFeedback: Boolean = false,
  val useFrontCamera: Boolean = true,
  val isAccessibilityEnabled: Boolean = false,
  val mappings: Map<GestureType, GestureAction> = DefaultGestureMappings.getPreset(GesturePresetProfile.BALANCED_DEFAULT),
  val calibration: GestureCalibrationEntity = GestureCalibrationEntity(),
  val history: List<GestureHistoryEntity> = emptyList()
)

class GestureViewModel(application: Application) : AndroidViewModel(application) {

  private val database = MyraaDatabase.getInstance(application)
  private val repository = GestureRepository(database.gestureDao())
  private val actionExecutor = ActionExecutor(application)

  private val _uiState = MutableStateFlow(GestureUiState())
  val uiState: StateFlow<GestureUiState> = _uiState.asStateFlow()

  init {
    // Observe persisted settings
    viewModelScope.launch(Dispatchers.IO) {
      repository.settingsFlow.collectLatest { settings ->
        val mode = try { PerformanceMode.valueOf(settings.performanceMode) } catch (e: Exception) { PerformanceMode.BALANCED }
        _uiState.update {
          it.copy(
            isGestureControlEnabled = settings.isEnabled,
            performanceMode = mode,
            sensitivity = settings.sensitivity,
            showLandmarks = settings.showLandmarks,
            hapticFeedback = settings.hapticFeedback,
            soundFeedback = settings.soundFeedback,
            useFrontCamera = settings.useFrontCamera
          )
        }
      }
    }

    // Observe mappings
    viewModelScope.launch(Dispatchers.IO) {
      repository.mappingsFlow.collectLatest { map ->
        _uiState.update { it.copy(mappings = map) }
      }
    }

    // Observe calibration
    viewModelScope.launch(Dispatchers.IO) {
      repository.calibrationFlow.collectLatest { cal ->
        _uiState.update { it.copy(calibration = cal) }
      }
    }

    // Observe history
    viewModelScope.launch(Dispatchers.IO) {
      repository.historyFlow.collectLatest { hist ->
        _uiState.update { it.copy(history = hist) }
      }
    }

    checkAccessibilityStatus()
  }

  fun checkAccessibilityStatus() {
    val enabled = GestureAccessibilityService.isAccessibilityServiceEnabled(getApplication())
    _uiState.update { it.copy(isAccessibilityEnabled = enabled) }
  }

  fun onHandsTracked(hands: List<HandResult>, fps: Float, latencyMs: Long) {
    _uiState.update {
      it.copy(
        detectedHands = hands,
        fps = fps,
        latencyMs = latencyMs
      )
    }
  }

  fun onGestureRecognized(gesture: RecognizedGesture) {
    _uiState.update { it.copy(currentGesture = gesture) }

    if (!_uiState.value.isGestureControlEnabled) return

    // Execute mapped action on trigger states
    if (gesture.state == GestureLifecycleState.DETECTED ||
      gesture.state == GestureLifecycleState.HELD ||
      gesture.state == GestureLifecycleState.REPEATED) {

      val mappedAction = _uiState.value.mappings[gesture.type] ?: GestureAction.NONE

      if (mappedAction != GestureAction.NONE) {
        val result = actionExecutor.execute(
          action = mappedAction,
          hapticEnabled = _uiState.value.hapticFeedback,
          soundEnabled = _uiState.value.soundFeedback
        )

        _uiState.update {
          it.copy(
            lastExecutedAction = mappedAction,
            feedbackMessage = result.message
          )
        }

        viewModelScope.launch(Dispatchers.IO) {
          repository.logHistory(
            gestureType = gesture.type,
            action = mappedAction,
            state = gesture.state.name,
            confidence = gesture.confidence,
            latencyMs = _uiState.value.latencyMs
          )
        }
      }
    }
  }

  fun toggleGestureControl(enabled: Boolean) {
    _uiState.update { it.copy(isGestureControlEnabled = enabled) }
    persistSettings()
  }

  fun toggleFrontCamera() {
    val newMode = !_uiState.value.useFrontCamera
    _uiState.update { it.copy(useFrontCamera = newMode) }
    persistSettings()
  }

  fun setPerformanceMode(mode: PerformanceMode) {
    _uiState.update { it.copy(performanceMode = mode) }
    persistSettings()
  }

  fun setSensitivity(sens: Float) {
    _uiState.update { it.copy(sensitivity = sens) }
    persistSettings()
  }

  fun toggleLandmarkOverlay(show: Boolean) {
    _uiState.update { it.copy(showLandmarks = show) }
    persistSettings()
  }

  fun toggleHaptic(enabled: Boolean) {
    _uiState.update { it.copy(hapticFeedback = enabled) }
    persistSettings()
  }

  fun toggleSound(enabled: Boolean) {
    _uiState.update { it.copy(soundFeedback = enabled) }
    persistSettings()
  }

  fun updateGestureMapping(gesture: GestureType, action: GestureAction) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.updateMapping(gesture, action)
    }
  }

  fun applyPresetProfile(profile: GesturePresetProfile) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.applyPreset(profile)
    }
  }

  fun saveCalibration(restingScale: Float, reach: Float, pinchThreshold: Float) {
    viewModelScope.launch(Dispatchers.IO) {
      val entity = GestureCalibrationEntity(
        id = 1,
        restingPalmScale = restingScale,
        reachDistance = reach,
        calibratedPinchThreshold = pinchThreshold,
        isCalibrated = true,
        calibratedAt = System.currentTimeMillis()
      )
      repository.saveCalibration(entity)
    }
  }

  fun clearHistory() {
    viewModelScope.launch(Dispatchers.IO) {
      repository.clearHistory()
    }
  }

  fun dismissFeedbackMessage() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }

  private fun persistSettings() {
    val state = _uiState.value
    viewModelScope.launch(Dispatchers.IO) {
      repository.saveSettings(
        GestureSettingsEntity(
          id = 1,
          isEnabled = state.isGestureControlEnabled,
          performanceMode = state.performanceMode.name,
          sensitivity = state.sensitivity,
          showLandmarks = state.showLandmarks,
          hapticFeedback = state.hapticFeedback,
          soundFeedback = state.soundFeedback,
          useFrontCamera = state.useFrontCamera
        )
      )
    }
  }
}
