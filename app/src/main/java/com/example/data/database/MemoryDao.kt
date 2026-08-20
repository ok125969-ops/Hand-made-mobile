package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
  @Query("SELECT * FROM memories ORDER BY timestamp DESC")
  fun getAllMemoriesFlow(): Flow<List<MemoryEntity>>

  @Query("SELECT * FROM memories ORDER BY timestamp DESC")
  suspend fun getAllMemories(): List<MemoryEntity>

  @Query("SELECT * FROM memories WHERE category = :category ORDER BY timestamp DESC")
  suspend fun getMemoriesByCategory(category: String): List<MemoryEntity>

  @Query("SELECT * FROM memories WHERE category = 'ACTIVE_TASK' ORDER BY timestamp DESC LIMIT 1")
  suspend fun getLatestActiveTask(): MemoryEntity?

  @Query("SELECT * FROM memories WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY importance DESC, timestamp DESC LIMIT :limit")
  suspend fun searchMemories(query: String, limit: Int = 5): List<MemoryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemory(memory: MemoryEntity)

  @Update
  suspend fun updateMemory(memory: MemoryEntity)

  @Delete
  suspend fun deleteMemory(memory: MemoryEntity)

  @Query("DELETE FROM memories WHERE category = 'ACTIVE_TASK'")
  suspend fun clearActiveTasks()
}
