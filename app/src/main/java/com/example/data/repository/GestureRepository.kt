package com.example.data.repository

import com.example.actions.DefaultGestureMappings
import com.example.actions.GestureAction
import com.example.actions.GesturePresetProfile
import com.example.camera.PerformanceMode
import com.example.data.database.GestureCalibrationEntity
import com.example.data.database.GestureDao
import com.example.data.database.GestureHistoryEntity
import com.example.data.database.GestureMappingEntity
import com.example.data.database.GestureSettingsEntity
import com.example.gesture.GestureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GestureRepository(private val gestureDao: GestureDao) {

  val settingsFlow: Flow<GestureSettingsEntity> = gestureDao.getSettings().map {
    it ?: GestureSettingsEntity()
  }

  val calibrationFlow: Flow<GestureCalibrationEntity> = gestureDao.getCalibration().map {
    it ?: GestureCalibrationEntity()
  }

  val historyFlow: Flow<List<GestureHistoryEntity>> = gestureDao.getRecentHistory()

  val mappingsFlow: Flow<Map<GestureType, GestureAction>> = gestureDao.getAllMappings().map { list ->
    if (list.isEmpty()) {
      DefaultGestureMappings.getPreset(GesturePresetProfile.BALANCED_DEFAULT)
    } else {
      val map = mutableMapOf<GestureType, GestureAction>()
      list.forEach { entity ->
        val gesture = try { GestureType.valueOf(entity.gestureName) } catch (e: Exception) { null }
        val action = try { GestureAction.valueOf(entity.actionName) } catch (e: Exception) { null }
        if (gesture != null && action != null) {
          map[gesture] = if (entity.isEnabled) action else GestureAction.NONE
        }
      }
      map
    }
  }

  suspend fun updateMapping(gesture: GestureType, action: GestureAction) {
    gestureDao.insertMapping(
      GestureMappingEntity(
        gestureName = gesture.name,
        actionName = action.name,
        isEnabled = action != GestureAction.NONE
      )
    )
  }

  suspend fun applyPreset(profile: GesturePresetProfile) {
    val preset = DefaultGestureMappings.getPreset(profile)
    val entities = preset.map { (gesture, action) ->
      GestureMappingEntity(
        gestureName = gesture.name,
        actionName = action.name,
        isEnabled = action != GestureAction.NONE
      )
    }
    gestureDao.insertMappings(entities)
  }

  suspend fun saveSettings(settings: GestureSettingsEntity) {
    gestureDao.saveSettings(settings)
  }

  suspend fun saveCalibration(calibration: GestureCalibrationEntity) {
    gestureDao.saveCalibration(calibration)
  }

  suspend fun logHistory(
    gestureType: GestureType,
    action: GestureAction,
    state: String,
    confidence: Float,
    latencyMs: Long
  ) {
    gestureDao.insertHistory(
      GestureHistoryEntity(
        gestureType = gestureType.displayName,
        actionExecuted = action.displayName,
        state = state,
        confidence = confidence,
        latencyMs = latencyMs,
        timestamp = System.currentTimeMillis()
      )
    )
  }

  suspend fun clearHistory() {
    gestureDao.clearHistory()
  }
}
