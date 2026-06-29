package com.constructionmanager.data.assistant

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Short-term assistant memory: persists the recent conversation locally (SharedPreferences as JSON)
 * so the chat survives app restarts. Long-term semantic memory lives in mem0 via the Wade backend;
 * this is the on-device transcript that's always available, even offline.
 */
@Singleton
class ChatHistoryStore @Inject constructor(
    private val prefs: SharedPreferences
) {
    @Serializable
    data class StoredMessage(
        val text: String,
        val fromUser: Boolean,
        val provenance: String? = null
    )

    private val json = Json { ignoreUnknownKeys = true }

    fun load(): List<StoredMessage> = runCatching {
        prefs.getString(KEY, null)?.let { json.decodeFromString<List<StoredMessage>>(it) }
    }.getOrNull() ?: emptyList()

    fun save(messages: List<StoredMessage>) {
        runCatching {
            prefs.edit { putString(KEY, json.encodeToString(messages.takeLast(MAX_MESSAGES))) }
        }
    }

    fun clear() {
        prefs.edit { remove(KEY) }
    }

    private companion object {
        const val KEY = "assistant_chat_history"
        const val MAX_MESSAGES = 100
    }
}
