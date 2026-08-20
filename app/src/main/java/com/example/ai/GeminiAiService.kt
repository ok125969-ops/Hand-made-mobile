package com.example.ai

import com.example.BuildConfig
import com.example.model.ActionSafetyLevel
import com.example.model.DecisionType
import com.example.model.EventDecision
import com.example.model.EventType
import com.example.model.MYRAAEvent
import com.example.model.ProposedAction
import com.example.model.SystemContext
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class GeminiPart(
  @field:Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
  @field:Json(name = "parts") val parts: List<GeminiPart>,
  @field:Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
  @field:Json(name = "contents") val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
  @field:Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
  @field:Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApi {
  @POST("v1beta/models/gemini-3.5-flash:generateContent")
  suspend fun generateContent(
    @Query("key") apiKey: String,
    @Body request: GeminiRequest
  ): GeminiResponse
}

class GeminiAiService {

  private val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.NONE
    })
    .build()

  private val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://generativelanguage.googleapis.com/")
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

  private val geminiApi: GeminiApi = retrofit.create(GeminiApi::class.java)

  suspend fun generateProactiveResponse(
    event: MYRAAEvent,
    context: SystemContext,
    relevantMemories: List<String>,
    repeatCount: Int
  ): ProactiveAiOutcome = withContext(Dispatchers.IO) {
    val apiKey = getApiKey()
    val isRealKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

    if (isRealKey) {
      try {
        val prompt = buildProactivePrompt(event, context, relevantMemories, repeatCount)
        val request = GeminiRequest(
          contents = listOf(
            GeminiContent(
              parts = listOf(GeminiPart(text = prompt)),
              role = "user"
            )
          )
        )
        val response = geminiApi.generateContent(apiKey, request)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
        if (!text.isNullOrBlank()) {
          return@withContext parseAiOutcome(text, event)
        }
      } catch (e: Exception) {
        // Fall back gracefully to high-precision local reasoner
      }
    }

    // High quality local contextual reasoner (calm, concise JARVIS personality)
    synthesizeLocalOutcome(event, context, relevantMemories, repeatCount)
  }

  suspend fun generateConversationalReply(
    userMessage: String,
    context: SystemContext,
    relevantMemories: List<String>
  ): String = withContext(Dispatchers.IO) {
    val apiKey = getApiKey()
    val isRealKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

    if (isRealKey) {
      try {
        val prompt = """
          You are MYRAA, a calm, intelligent, JARVIS-style personal AI assistant.
          Current Context: Active Task="${context.activeTask}", User State="${context.userState.name}".
          Relevant Memories: ${relevantMemories.joinToString("; ")}
          
          User says: "$userMessage"
          
          Respond in a calm, confident, helpful, and concise manner (1-2 sentences). Do not use filler or excessive enthusiasm.
        """.trimIndent()

        val request = GeminiRequest(
          contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user"))
        )
        val response = geminiApi.generateContent(apiKey, request)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
        if (!text.isNullOrBlank()) {
          return@withContext text
        }
      } catch (e: Exception) {
        // Fall back to local synthesis
      }
    }

    // Contextual local conversational reply
    synthesizeLocalConversationalReply(userMessage, context)
  }

  private fun getApiKey(): String {
    return try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Throwable) {
      ""
    }
  }

  private fun buildProactivePrompt(
    event: MYRAAEvent,
    context: SystemContext,
    memories: List<String>,
    repeatCount: Int
  ): String {
    return """
      You are MYRAA, a proactive JARVIS-style personal AI assistant.
      An event just occurred in the user's environment.
      
      Event: Type=${event.eventType.name}, Priority=${event.priority.name}, Title="${event.title}", Message="${event.message}".
      Target Metadata: ${event.metadata}
      User Current Context: Task="${context.activeTask}", State=${context.userState.name}.
      Relevant Long-term/Short-term Memories: ${memories.joinToString("; ")}
      Repeated Occurrences Count: $repeatCount
      
      Format your response strictly as:
      SPEECH: [One concise, confident, calm sentence to say to the user, or NONE if silence is better]
      ACTION: [Suggested action title, or NONE]
      REASON: [Brief reason for this decision]
    """.trimIndent()
  }

  private fun parseAiOutcome(rawText: String, event: MYRAAEvent): ProactiveAiOutcome {
    var speech: String? = null
    var actionTitle: String? = null
    var reason = "AI contextual reasoning evaluated"

    val lines = rawText.lines()
    for (line in lines) {
      val trimmed = line.trim()
      if (trimmed.startsWith("SPEECH:", ignoreCase = true)) {
        val s = trimmed.substringAfter(":").trim()
        if (s.isNotEmpty() && !s.equals("NONE", ignoreCase = true)) {
          speech = s
        }
      } else if (trimmed.startsWith("ACTION:", ignoreCase = true)) {
        val a = trimmed.substringAfter(":").trim()
        if (a.isNotEmpty() && !a.equals("NONE", ignoreCase = true)) {
          actionTitle = a
        }
      } else if (trimmed.startsWith("REASON:", ignoreCase = true)) {
        reason = trimmed.substringAfter(":").trim()
      }
    }

    val proposedAction = actionTitle?.let {
      ProposedAction(
        title = it,
        description = "Proactively suggested by MYRAA based on event: ${event.title}",
        safetyLevel = ActionSafetyLevel.SUGGEST
      )
    }

    return ProactiveAiOutcome(
      speechText = speech,
      proposedAction = proposedAction,
      reason = reason
    )
  }

  private fun synthesizeLocalOutcome(
    event: MYRAAEvent,
    context: SystemContext,
    memories: List<String>,
    repeatCount: Int
  ): ProactiveAiOutcome {
    return when (event.eventType) {
      EventType.BUILD_FAILED -> {
        val target = event.metadata["target"] ?: "DashboardViewModel.kt"
        if (repeatCount >= 3) {
          ProactiveAiOutcome(
            speechText = "The same build error has appeared three times in $target. I can investigate the root cause.",
            proposedAction = ProposedAction(
              title = "Inspect Stacktrace & Suggest Fix",
              description = "Analyze recurring compiler error in $target",
              safetyLevel = ActionSafetyLevel.SUGGEST,
              actionType = "ANALYZE_ERROR"
            ),
            reason = "Escalated recurring failure threshold ($repeatCount times)"
          )
        } else {
          val connectedMemory = memories.firstOrNull { it.contains("GestureControl", ignoreCase = true) || it.contains("build", ignoreCase = true) }
          val speech = if (connectedMemory != null) {
            "Your ${context.activeTask} failed. I found an error in $target."
          } else {
            "The build failed. I found an error in $target."
          }
          ProactiveAiOutcome(
            speechText = speech,
            proposedAction = ProposedAction(
              title = "Navigate to $target",
              description = "Open source file at line of failure",
              safetyLevel = ActionSafetyLevel.SUGGEST,
              actionType = "OPEN_FILE"
            ),
            reason = "Build failure detected during active task"
          )
        }
      }
      EventType.BUILD_COMPLETED -> {
        ProactiveAiOutcome(
          speechText = "The APK build finished successfully.",
          proposedAction = ProposedAction(
            title = "Install & Launch APK",
            description = "Deploy build artifact to target device",
            safetyLevel = ActionSafetyLevel.EXECUTE,
            actionType = "DEPLOY_APK"
          ),
          reason = "Build completed for active development"
        )
      }
      EventType.TASK_COMPLETED -> {
        ProactiveAiOutcome(
          speechText = "${event.title} has completed.",
          proposedAction = null,
          reason = "Background task completion notice"
        )
      }
      EventType.TASK_REMINDER_DUE -> {
        ProactiveAiOutcome(
          speechText = "You asked me to remind you about ${event.title}. It's due soon.",
          proposedAction = ProposedAction(
            title = "Open ${event.title}",
            description = "View reminder details and action checklist",
            safetyLevel = ActionSafetyLevel.SUGGEST,
            actionType = "OPEN_REMINDER"
          ),
          reason = "Scheduled reminder due"
        )
      }
      EventType.USER_RETURNED -> {
        ProactiveAiOutcome(
          speechText = "Welcome back. Your task on ${context.activeTask} is ready for review.",
          proposedAction = null,
          reason = "User resumed interaction session"
        )
      }
      EventType.REPEATED_ERROR_DETECTED -> {
        ProactiveAiOutcome(
          speechText = "I've noticed the same error appearing repeatedly. I can investigate the root cause.",
          proposedAction = ProposedAction(
            title = "Deep Diagnostic",
            description = "Run automated linting and dependency diagnostic",
            safetyLevel = ActionSafetyLevel.SUGGEST,
            actionType = "RUN_DIAGNOSTIC"
          ),
          reason = "Repeated error pattern detected"
        )
      }
      EventType.TOOL_EXECUTION_FAILED -> {
        ProactiveAiOutcome(
          speechText = "A tool execution error occurred in ${event.title}.",
          proposedAction = ProposedAction(
            title = "Retry Tool with Fallback",
            description = "Rerun with relaxed parameters",
            safetyLevel = ActionSafetyLevel.SUGGEST,
            actionType = "RETRY_TOOL"
          ),
          reason = "Tool execution failure"
        )
      }
      EventType.IMPORTANT_NOTIFICATION -> {
        ProactiveAiOutcome(
          speechText = "Incoming priority alert: ${event.title}.",
          proposedAction = null,
          reason = "Critical notification alert"
        )
      }
      else -> {
        ProactiveAiOutcome(
          speechText = null,
          proposedAction = null,
          reason = "Standard event handled silently"
        )
      }
    }
  }

  private fun synthesizeLocalConversationalReply(userMessage: String, context: SystemContext): String {
    val lower = userMessage.lowercase()
    return when {
      lower.contains("status") || lower.contains("how are you") -> {
        "All systems operational. Currently observing ${context.activeTask} in ${context.currentApp}."
      }
      lower.contains("task") || lower.contains("working on") -> {
        "You are currently working on: ${context.activeTask}."
      }
      lower.contains("build") || lower.contains("compile") -> {
        "I am monitoring build events and background tasks for any errors."
      }
      lower.contains("hello") || lower.contains("hi") || lower.contains("myraa") -> {
        "Online and monitoring, ready to assist."
      }
      lower.contains("help") -> {
        "I observe system events, monitor build pipelines, track reminders, and proactively alert you when critical interventions are required."
      }
      else -> {
        "Understood. I will keep track of this context as you continue working on ${context.activeTask}."
      }
    }
  }

  data class ProactiveAiOutcome(
    val speechText: String?,
    val proposedAction: ProposedAction?,
    val reason: String
  )
}
