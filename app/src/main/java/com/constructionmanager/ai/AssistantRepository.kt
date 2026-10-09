package com.constructionmanager.ai

import com.constructionmanager.data.caroline.AssistTextResponse
import com.constructionmanager.data.caroline.CallSummary
import com.constructionmanager.data.caroline.CarolineConnector
import com.constructionmanager.data.caroline.ConnectorResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** A single assistant turn, with provenance so the UI can show how it was produced. */
data class AssistantReply(
    val text: String,
    val source: Source
) {
    enum class Source {
        /** Caroline answered from the server. */
        CAROLINE,
        /** The message reached Caroline's record; she did not answer in the app (see [AssistantRepository.replyFor]). */
        DELIVERED,
        /** Not sent yet: in the outbox until the phone is back online. */
        QUEUED,
        /** The on-device helper answered; Caroline was not reached. */
        OFFLINE,
        /** An on-device action ran (a project, material or worker). */
        AGENT,
        WEB
    }
}

/**
 * What the Assistant and Calls screens ask for. Everything that goes to Caroline goes through [CarolineConnector];
 * nothing here talks HTTP itself. On-device actions and web search stay on the phone.
 */
@Singleton
class AssistantRepository @Inject constructor(
    private val connector: CarolineConnector,
    private val agent: ConstructionAgent,
    private val webSearch: WebSearchRepository
) {
    suspend fun chat(message: String): AssistantReply = withContext(Dispatchers.IO) {
        // 0) An explicit on-device command (create a project, add a material...) runs here and reports what happened.
        val action = try {
            agent.tryHandle(message)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            return@withContext AssistantReply(
                "That didn't finish on this phone (${e.message ?: e.javaClass.simpleName}). Check the list before trying again.",
                AssistantReply.Source.AGENT
            )
        }
        action?.let { return@withContext AssistantReply(it.text, AssistantReply.Source.AGENT) }

        // 1) Explicit web lookups, when a search key is configured.
        if (webSearch.isConfigured() && webSearch.isSearchQuery(message)) {
            webSearch.search(message).getOrNull()?.let { return@withContext AssistantReply(it, AssistantReply.Source.WEB) }
        }

        // 2) To Caroline.
        if (!connector.isPaired) {
            return@withContext AssistantReply(
                OfflineAssistant.reply(message) + "\n\n_(Caroline was not reached: this phone is not paired. Settings → Connect Caroline.)_",
                AssistantReply.Source.OFFLINE
            )
        }
        replyFor(connector.sendText(message), message)
    }

    suspend fun briefing(): Result<String> = when (val r = connector.briefing()) {
        is ConnectorResult.Ok -> Result.success(r.value.text?.takeIf { it.isNotBlank() } ?: "The briefing came back empty.")
        is ConnectorResult.Failed -> Result.failure(IllegalStateException(r.error.message))
        is ConnectorResult.Queued -> Result.failure(IllegalStateException("not sent"))
    }

    suspend fun recentCalls(): Result<List<CallSummary>> = when (val r = connector.calls()) {
        is ConnectorResult.Ok -> Result.success(r.value)
        is ConnectorResult.Failed -> Result.failure(IllegalStateException(r.error.message))
        is ConnectorResult.Queued -> Result.failure(IllegalStateException("not sent"))
    }

    /** Sends what the outbox holds; returns a sentence for the screen, or null when there was nothing to send. */
    suspend fun flushOutbox(): String? {
        val rep = connector.flushOutbox()
        if (rep.sent == 0 && rep.refused.isEmpty()) return null
        return buildString {
            if (rep.sent > 0) append("Sent ${rep.sent} message(s) that were waiting for a connection.")
            rep.refused.forEach { (item, err) -> append(" Not accepted: \"${item.text.take(40)}\" - ${err.message}") }
            if (rep.left > 0) append(" ${rep.left} still waiting.")
        }.trim()
    }

    companion object {
        /** Turns the connector's answer into words that say exactly what happened - nothing more. */
        fun replyFor(r: ConnectorResult<AssistTextResponse>, message: String): AssistantReply = when (r) {
            is ConnectorResult.Ok -> {
                val v = r.value
                val reply = v.reply
                when {
                    v.answered == true && !reply.isNullOrBlank() -> AssistantReply(reply, AssistantReply.Source.CAROLINE)
                    v.stored == true -> AssistantReply(
                        (v.detail ?: "Saved to your thread with Caroline.") + (if (v.duplicate == true) " (It was already there.)" else ""),
                        AssistantReply.Source.DELIVERED
                    )
                    else -> AssistantReply("Caroline's server answered but did not say the message was kept.", AssistantReply.Source.OFFLINE)
                }
            }
            is ConnectorResult.Queued -> AssistantReply(
                "Not sent yet - no connection. It's saved on this phone and goes to Caroline, in order, as soon as the phone is back online.",
                AssistantReply.Source.QUEUED
            )
            is ConnectorResult.Failed -> AssistantReply(
                OfflineAssistant.reply(message) + "\n\n_(Caroline was not reached: ${r.error.message})_",
                AssistantReply.Source.OFFLINE
            )
        }
    }
}
