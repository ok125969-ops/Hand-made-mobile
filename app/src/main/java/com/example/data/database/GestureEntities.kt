package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gesture_mappings")
data class GestureMappingEntity(
  @PrimaryKey val gestureName: String,
  val actionName: String,
  val isEnabled: Boolean = true,
  val customThreshold: Float = 1.0f
)

@Entity(tableName = "gesture_history")
data class GestureHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val gestureType: String,
  val actionExecuted: String,
  val state: String,
  val confidence: Float,
  val latencyMs: Long,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "gesture_settings")
data class GestureSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val isEnabled: Boolean = true,
  val performanceMode: String = "BALANCED",
  val sensitivity: Float = 1.0f,
  val holdDurationMs: Long = 350L,
  val cooldownMs: Long = 450L,
  val showLandmarks: Boolean = true,
  val hapticFeedback: Boolean = true,
  val soundFeedback: Boolean = false,
  val handPreference: String = "BOTH",
  val useFrontCamera: Boolean = true
)

@Entity(tableName = "gesture_calibration")
data class GestureCalibrationEntity(
  @PrimaryKey val id: Int = 1,
  val restingPalmScale: Float = 0.20f,
  val reachDistance: Float = 0.50f,
  val calibratedPinchThreshold: Float = 0.35f,
  val isCalibrated: Boolean = false,
  val calibratedAt: Long = 0L
)
