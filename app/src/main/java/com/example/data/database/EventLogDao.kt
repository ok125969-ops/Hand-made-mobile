package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventLogDao {
  @Query("SELECT * FROM event_logs ORDER BY timestamp DESC LIMIT :limit")
  fun getRecentEventLogsFlow(limit: Int = 50): Flow<List<EventLogEntity>>

  @Query("SELECT * FROM event_logs ORDER BY timestamp DESC LIMIT :limit")
  suspend fun getRecentEventLogs(limit: Int = 50): List<EventLogEntity>

  @Query("SELECT * FROM event_logs WHERE eventType = :eventType ORDER BY timestamp DESC LIMIT :limit")
  suspend fun getLogsByEventType(eventType: String, limit: Int = 10): List<EventLogEntity>

  @Query("SELECT COUNT(*) FROM event_logs WHERE eventType = :eventType AND timestamp >= :sinceTimestamp")
  suspend fun countEventsSince(eventType: String, sinceTimestamp: Long): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEventLog(eventLog: EventLogEntity)

  @Query("DELETE FROM event_logs")
  suspend fun clearAll()
}
