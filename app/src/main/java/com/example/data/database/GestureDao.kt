package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GestureDao {

  @Query("SELECT * FROM gesture_mappings")
  fun getAllMappings(): Flow<List<GestureMappingEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMappings(mappings: List<GestureMappingEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMapping(mapping: GestureMappingEntity)

  @Query("SELECT * FROM gesture_settings WHERE id = 1")
  fun getSettings(): Flow<GestureSettingsEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveSettings(settings: GestureSettingsEntity)

  @Query("SELECT * FROM gesture_calibration WHERE id = 1")
  fun getCalibration(): Flow<GestureCalibrationEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveCalibration(calibration: GestureCalibrationEntity)

  @Query("SELECT * FROM gesture_history ORDER BY timestamp DESC LIMIT 50")
  fun getRecentHistory(): Flow<List<GestureHistoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHistory(history: GestureHistoryEntity)

  @Query("DELETE FROM gesture_history")
  suspend fun clearHistory()
}
