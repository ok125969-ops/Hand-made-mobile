package com.example.data.repository

import com.example.data.database.ConversationDao
import com.example.data.database.ConversationEntity
import com.example.data.database.EventLogDao
import com.example.data.database.EventLogEntity
import com.example.data.database.MemoryDao
import com.example.data.database.MemoryEntity
import com.example.model.EventDecision
import com.example.model.MYRAAEvent
import kotlinx.coroutines.flow.Flow

class MemoryRepository(
  private val memoryDao: MemoryDao,
  private val eventLogDao: EventLogDao,
  private val conversationDao: ConversationDao
) {
  val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemoriesFlow()
  val recentEventLogs: Flow<List<EventLogEntity>> = eventLogDao.getRecentEventLogsFlow(50)
  val recentConversations: Flow<List<ConversationEntity>> = conversationDao.getRecentConversationsFlow(30)

  suspend fun getActiveTask(): String? {
    return memoryDao.getLatestActiveTask()?.content
  }

  suspend fun setActiveTask(taskDescription: String, title: String = "Active Task") {
    memoryDao.clearActiveTasks()
    memoryDao.insertMemory(
      MemoryEntity(
        category = "ACTIVE_TASK",
        title = title,
        content = taskDescription,
        importance = 5,
        tags = "task,focus,active"
      )
    )
  }

  suspend fun addMemory(category: String, title: String, content: String, importance: Int = 3, tags: String = "") {
    memoryDao.insertMemory(
      MemoryEntity(
        category = category,
        title = title,
        content = content,
        importance = importance,
        tags = tags
      )
    )
  }

  suspend fun deleteMemory(memory: MemoryEntity) {
    memoryDao.deleteMemory(memory)
  }

  suspend fun findRelevantMemories(query: String, limit: Int = 3): List<MemoryEntity> {
    if (query.isBlank()) return emptyList()
    val words = query.split(" ", "_", "-", ".").filter { it.length > 2 }
    val results = mutableListOf<MemoryEntity>()
    for (word in words.take(3)) {
      results.addAll(memoryDao.searchMemories(word, limit))
    }
    return results.distinctBy { it.id }.take(limit)
  }

  suspend fun recordDecision(event: MYRAAEvent, decision: EventDecision) {
    eventLogDao.insertEventLog(
      EventLogEntity(
        eventId = event.eventId,
        eventType = event.eventType.name,
        priority = event.priority.name,
        title = event.title,
        message = event.message,
        decisionType = decision.decisionType.name,
        reason = decision.reason,
        speechText = decision.speechText,
        relevanceScore = decision.relevanceScore,
        interruptScore = decision.interruptScore,
        timestamp = decision.timestamp
      )
    )
  }

  suspend fun countRecentErrors(sinceMinutes: Int = 10): Int {
    val cutoff = System.currentTimeMillis() - (sinceMinutes * 60 * 1000L)
    return eventLogDao.countEventsSince("BUILD_FAILED", cutoff) +
      eventLogDao.countEventsSince("TOOL_EXECUTION_FAILED", cutoff)
  }

  suspend fun addConversation(sender: String, message: String, isProactive: Boolean = false) {
    conversationDao.insertMessage(
      ConversationEntity(
        sender = sender,
        message = message,
        isProactive = isProactive,
        timestamp = System.currentTimeMillis()
      )
    )
  }
}
