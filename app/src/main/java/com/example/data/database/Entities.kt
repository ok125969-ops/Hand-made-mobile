package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "memories")
data class MemoryEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val category: String, // "ACTIVE_TASK", "OBSERVATION", "PREFERENCE", "LONG_TERM_FACT"
  val title: String,
  val content: String,
  val importance: Int = 1, // 1 to 5
  val timestamp: Long = System.currentTimeMillis(),
  val tags: String = "" // comma-separated
)

@Entity(tableName = "event_logs")
data class EventLogEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val eventId: String,
  val eventType: String,
  val priority: String,
  val title: String,
  val message: String,
  val decisionType: String,
  val reason: String,
  val speechText: String? = null,
  val relevanceScore: Float = 0.0f,
  val interruptScore: Float = 0.0f,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val sender: String, // "USER" or "MYRAA"
  val message: String,
  val isProactive: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
)
