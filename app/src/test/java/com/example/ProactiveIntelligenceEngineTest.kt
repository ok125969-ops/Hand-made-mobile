package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiAiService
import com.example.data.database.MyraaDatabase
import com.example.data.repository.MemoryRepository
import com.example.engine.AntiSpamEngine
import com.example.engine.ContextCollector
import com.example.engine.InterruptibilityEvaluator
import com.example.engine.MYRAAEventBus
import com.example.engine.ProactiveIntelligenceEngine
import com.example.model.DecisionType
import com.example.model.EventSource
import com.example.model.EventType
import com.example.model.MYRAAEvent
import com.example.model.MyraaSettings
import com.example.model.Priority
import com.example.model.UserActivityState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProactiveIntelligenceEngineTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  private lateinit var database: MyraaDatabase
  private lateinit var repository: MemoryRepository
  private lateinit var eventBus: MYRAAEventBus
  private lateinit var contextCollector: ContextCollector
  private lateinit var antiSpamEngine: AntiSpamEngine
  private lateinit var interruptibilityEvaluator: InterruptibilityEvaluator
  private lateinit var geminiAiService: GeminiAiService
  private lateinit var engine: ProactiveIntelligenceEngine

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, MyraaDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    repository = MemoryRepository(
      database.memoryDao(),
      database.eventLogDao(),
      database.conversationDao()
    )

    eventBus = MYRAAEventBus()
    contextCollector = ContextCollector(repository)
    antiSpamEngine = AntiSpamEngine()
    interruptibilityEvaluator = InterruptibilityEvaluator()
    geminiAiService = GeminiAiService()

    engine = ProactiveIntelligenceEngine(
      eventBus = eventBus,
      contextCollector = contextCollector,
      memoryRepository = repository,
      antiSpamEngine = antiSpamEngine,
      interruptibilityEvaluator = interruptibilityEvaluator,
      geminiAiService = geminiAiService,
      voiceManager = null, // headless in test
      scope = testScope
    )
    // Stop background flow collection loop so unit tests execute synchronously
    engine.stopListening()
  }

  @After
  fun tearDown() {
    engine.stopListening()
    database.close()
  }

  @Test
  fun test1_lowPriorityTelemetryEvent_expectedSilent() = testScope.runTest {
    // Low priority telemetry event unrelated to active task
    val event = MYRAAEvent(
      eventType = EventType.SYSTEM_STATE_CHANGED,
      priority = Priority.INFORMATIONAL,
      source = EventSource.SYSTEM_TELEMETRY,
      title = "Background Memory Cache Cleaned",
      message = "Reclaimed 4MB cache space",
      metadata = mapOf("target" to "telemetry")
    )

    val decision = engine.processEvent(event)
    assertEquals(DecisionType.SILENT, decision.decisionType)
  }

  @Test
  fun test2_lowPriorityRoutineEvent_expectedSilent() = testScope.runTest {
    val event = MYRAAEvent(
      eventType = EventType.MEMORY_UPDATED,
      priority = Priority.LOW,
      source = EventSource.CONVERSATION_MANAGER,
      title = "Routine Preference Synced",
      message = "Synced user layout preference",
      metadata = mapOf("target" to "preferences")
    )

    val decision = engine.processEvent(event)
    assertEquals(DecisionType.SILENT, decision.decisionType)
  }

  @Test
  fun test3_importantBuildFailureEvent_expectedSpeakOrSuggest() = testScope.runTest {
    contextCollector.updateActiveTask("GestureControl Build")
    contextCollector.updateUserState(UserActivityState.CODING)

    val event = MYRAAEvent(
      eventType = EventType.BUILD_FAILED,
      priority = Priority.HIGH,
      source = EventSource.BUILD_SYSTEM,
      title = "Build Failed: Compilation Error",
      message = "Unresolved reference in DashboardViewModel.kt",
      metadata = mapOf("target" to "DashboardViewModel.kt")
    )

    val decision = engine.processEvent(event)
    assertTrue(
      decision.decisionType == DecisionType.SPEAK ||
        decision.decisionType == DecisionType.SUGGEST ||
        decision.decisionType == DecisionType.ASK_CONFIRMATION
    )
    assertNotNull(decision.speechText)
    assertTrue(decision.speechText!!.contains("DashboardViewModel.kt"))
  }

  @Test
  fun test4_duplicateEvent_expectedSuppressed() = testScope.runTest {
    val event1 = MYRAAEvent(
      eventType = EventType.BUILD_FAILED,
      priority = Priority.HIGH,
      source = EventSource.BUILD_SYSTEM,
      title = "Build Failed: Compilation Error",
      message = "Type mismatch in DashboardViewModel.kt",
      metadata = mapOf("target" to "DashboardViewModel.kt"),
      deduplicationKey = "BUILD_FAILED_DashboardViewModel.kt"
    )

    // First occurrence -> passes
    val decision1 = engine.processEvent(event1)
    assertTrue(decision1.decisionType != DecisionType.SILENT)

    // Immediate duplicate occurrence -> suppressed by Anti-Spam engine
    val event2 = event1.copy(eventId = "duplicate_id_2")
    val decision2 = engine.processEvent(event2)
    assertEquals(DecisionType.SILENT, decision2.decisionType)
    assertTrue(decision2.reason.contains("Suppressed"))
  }

  @Test
  fun test5_userInteractingWithMyraa_expectedConversationalResponse() = testScope.runTest {
    contextCollector.updateUserState(UserActivityState.IN_CONVERSATION)

    val event = MYRAAEvent(
      eventType = EventType.USER_INTERACTION,
      priority = Priority.HIGH,
      source = EventSource.USER_INPUT,
      title = "User Conversational Turn",
      message = "What is my current active task?"
    )

    val decision = engine.processEvent(event)
    assertEquals(DecisionType.SPEAK, decision.decisionType)
  }

  @Test
  fun test6_userBusyWithLowPriority_expectedSilent() = testScope.runTest {
    contextCollector.updateUserState(UserActivityState.BUSY)

    val event = MYRAAEvent(
      eventType = EventType.TASK_COMPLETED,
      priority = Priority.LOW,
      source = EventSource.BACKGROUND_TASK,
      title = "Background Cache Indexing",
      message = "Finished background cache indexing"
    )

    val decision = engine.processEvent(event)
    assertEquals(DecisionType.SILENT, decision.decisionType)
  }

  @Test
  fun test7_criticalEvent_expectedImmediateAlert() = testScope.runTest {
    contextCollector.updateUserState(UserActivityState.BUSY)

    val event = MYRAAEvent(
      eventType = EventType.IMPORTANT_NOTIFICATION,
      priority = Priority.CRITICAL,
      source = EventSource.SYSTEM_TELEMETRY,
      title = "Critical Security Alert",
      message = "Security token leak detected"
    )

    val decision = engine.processEvent(event)
    assertTrue(decision.decisionType == DecisionType.SPEAK || decision.decisionType == DecisionType.NOTIFY)
    assertTrue(decision.interruptScore >= 0.5f)
  }

  @Test
  fun test8_proactiveIntelligenceDisabled_expectedSilent() = testScope.runTest {
    engine.updateSettings(
      MyraaSettings(proactiveIntelligenceEnabled = false)
    )

    val event = MYRAAEvent(
      eventType = EventType.BUILD_FAILED,
      priority = Priority.HIGH,
      source = EventSource.BUILD_SYSTEM,
      title = "Build Failed",
      message = "Compilation failed in DashboardViewModel.kt"
    )

    val decision = engine.processEvent(event)
    assertEquals(DecisionType.SILENT, decision.decisionType)
    assertTrue(decision.reason.contains("disabled"))
  }
}
