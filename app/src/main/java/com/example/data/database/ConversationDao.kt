package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
  @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
  fun getRecentConversationsFlow(limit: Int = 30): Flow<List<ConversationEntity>>

  @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
  suspend fun getRecentConversations(limit: Int = 20): List<ConversationEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ConversationEntity)

  @Query("DELETE FROM conversations")
  suspend fun clearAll()
}
