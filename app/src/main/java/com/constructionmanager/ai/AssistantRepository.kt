package com.constructionmanager.ai

import android.util.Log
import com.constructionmanager.data.network.wade.AddMemoryRequest
import com.constructionmanager.data.network.wade.ChatRequest
import com.constructionmanager.data.network.wade.EstimateRequest
import com.constructionmanager.data.network.wade.MemoryMessage
import com.constructionmanager.data.network.wade.ScreenRequest
import com.constructionmanager.data.network.wade.SearchMemoryRequest
import com.constructionmanager.data.network.wade.WadeBackendConfig
import com.constructionmanager.data.network.wade.WadeServiceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** A single assistant turn, with provenance so the UI can show how it was produced. */
data class AssistantReply(
    val text: String,
    val source: Source,
    val memoryContext: List<String> = emptyList()
) {
    enum class Source { LIVE, OFFLINE }
}

data class CallScreeningResult(
    val decision: String,
    val reason: String,
    val live: Boolean
)

/**
 * The hub that unifies the three Wade services behind one construction-app-facing API:
 *  - orchestrator /chat          → reasoning + persona
 *  - mem0 /search and /memories  → recall context, then persist the exchange
 *  - caroline /screen and /calls → telephony intelligence
 *
 * Every method degrades gracefully: if the backend is disabled or unreachable it falls
 * back to the on-device [OfflineAssistant] / heuristics so the app is always functional.
 */
@Singleton
class AssistantRepository @Inject constructor(
    private val services: WadeServiceFactory,
    private val config: WadeBackendConfig
) {
    suspend fun chat(message: String, sessionId: String): AssistantReply = withContext(Dispatchers.IO) {
        if (!config.remoteEnabled) {
            return@withContext AssistantReply(OfflineAssistant.reply(message), AssistantReply.Source.OFFLINE)
        }
        try {
            // 1) Recall relevant memory for context (best-effort).
            val context = runCatching {
                services.memory()
                    .search(SearchMemoryRequest(query = message, userId = config.userId))
                    .results.orEmpty().mapNotNull { it.memory }
            }.getOrDefault(emptyList())

            // 2) Ask the orchestrator.
            val response = services.orchestrator()
                .chat(ChatRequest(message = message, sessionId = sessionId))
            val text = response.response?.takeIf { it.isNotBlank() }
                ?: "I didn't get a response from the orchestrator."

            // 3) Persist the exchange to long-term memory (best-effort).
            runCatching {
                services.memory().add(
                    AddMemoryRequest(
                        messages = listOf(
                            MemoryMessage("user", message),
                            MemoryMessage("assistant", text)
                        ),
                        userId = config.userId
                    )
                )
            }

            AssistantReply(text, AssistantReply.Source.LIVE, context)
        } catch (e: Exception) {
            Log.w(TAG, "Live chat failed, using offline assistant", e)
            AssistantReply(
                OfflineAssistant.reply(message) +
                    "\n\n_(couldn't reach the orchestrator at ${config.orchestratorUrl})_",
                AssistantReply.Source.OFFLINE
            )
        }
    }

    suspend fun estimate(clientName: String, projectType: String, notes: String): Result<String> =
        withContext(Dispatchers.IO) {
            if (!config.remoteEnabled) {
                return@withContext Result.success(
                    OfflineAssistant.reply("estimate for $projectType")
                )
            }
            runCatching {
                val r = services.orchestrator()
                    .estimate(EstimateRequest(clientName, projectType, notes))
                buildString {
                    append(r.summary ?: "Estimate generated.")
                    r.estimateTotal?.let { append("\n\nTotal: $%,.2f".format(it)) }
                }
            }
        }

    suspend fun briefing(): Result<String> = withContext(Dispatchers.IO) {
        if (!config.remoteEnabled) {
            return@withContext Result.success(OfflineAssistant.reply("status briefing"))
        }
        runCatching {
            services.orchestrator().briefing().briefing ?: "No briefing available."
        }
    }

    suspend fun recentCalls(): Result<List<com.constructionmanager.data.network.wade.CallRecord>> =
        withContext(Dispatchers.IO) {
            if (!config.remoteEnabled) return@withContext Result.success(emptyList())
            runCatching { services.caroline().calls() }
        }

    /**
     * Asks the live Caroline receptionist how to handle an inbound call. Returns a result marked
     * [CallScreeningResult.live] = false when the backend is disabled or unreachable, so the caller
     * can fall back to its on-device heuristic.
     */
    suspend fun screenCall(callSid: String, number: String, name: String? = null): CallScreeningResult =
        withContext(Dispatchers.IO) {
            if (!config.remoteEnabled) {
                return@withContext CallScreeningResult("local", "On-device heuristic", live = false)
            }
            try {
                val decision = services.caroline()
                    .screen(ScreenRequest(callSid = callSid, callerNumber = number, callerName = name))
                CallScreeningResult(
                    decision = decision.decision ?: "allow",
                    reason = decision.reason ?: "",
                    live = true
                )
            } catch (e: Exception) {
                Log.w(TAG, "Live call screening failed, falling back to heuristic", e)
                CallScreeningResult("local", "Fallback after error: ${e.message}", live = false)
            }
        }

    /** Best-effort connectivity probe for the orchestrator, used by the backend settings UI. */
    suspend fun ping(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val health = services.orchestrator().health()
            val status = health["status"]?.toString() ?: "ok"
            "Orchestrator reachable ($status)"
        }
    }

    companion object {
        private const val TAG = "AssistantRepository"
    }
}
