package com.constructionmanager.ui.screens.assistant

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.BuildConfig
import com.constructionmanager.ai.AssistantReply
import com.constructionmanager.ai.AssistantRepository
import com.constructionmanager.data.assistant.ChatHistoryStore
import com.constructionmanager.data.caroline.CarolineConnector
import com.constructionmanager.data.caroline.CarolineEndpoint
import com.constructionmanager.data.caroline.ConnectorResult
import com.constructionmanager.data.caroline.ConnectorSettings
import com.constructionmanager.data.settings.WebSearchSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val fromUser: Boolean,
    val provenance: String? = null
)

data class AssistantUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Caroline here. Pair this phone (Settings → Connect Caroline) and what you type here goes onto your " +
                "thread with me. On the phone itself I can also take actions: \"create a project called Maple Kitchen, " +
                "budget \$48k\", or answer ballpark questions like \"estimate drywall for 1,200 sq ft\".",
            fromUser = false,
            provenance = "on-device"
        )
    ),
    val input: String = "",
    val isSending: Boolean = false,
    // Caroline connection, for the settings sheet
    val serverUrl: String = CarolineEndpoint.DEFAULT_BASE_URL,
    val paired: Boolean = false,
    val connectionNote: String? = null,
    val isPairing: Boolean = false,
    val webSearchApiKey: String = ""
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val repository: AssistantRepository,
    private val connector: CarolineConnector,
    private val connectorSettings: ConnectorSettings,
    private val webSearchSettings: WebSearchSettings,
    private val chatHistoryStore: ChatHistoryStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { repository.flushOutbox()?.let { note -> appendSystem(note) } }
    }

    private fun loadState(): AssistantUiState {
        val persisted = chatHistoryStore.load()
        val messages = if (persisted.isEmpty()) {
            AssistantUiState().messages
        } else {
            persisted.map { ChatMessage(text = it.text, fromUser = it.fromUser, provenance = it.provenance) }
        }
        return AssistantUiState(
            messages = messages,
            serverUrl = connectorSettings.baseUrl,
            paired = connector.isPaired,
            webSearchApiKey = webSearchSettings.apiKey
        )
    }

    private fun persistHistory() {
        chatHistoryStore.save(
            _uiState.value.messages.map { ChatHistoryStore.StoredMessage(it.text, it.fromUser, it.provenance) }
        )
    }

    fun onInputChange(value: String) = _uiState.update { it.copy(input = value) }

    fun send() {
        val text = _uiState.value.input.trim()
        if (text.isEmpty() || _uiState.value.isSending) return
        _uiState.update {
            it.copy(messages = it.messages + ChatMessage(text = text, fromUser = true), input = "", isSending = true)
        }
        viewModelScope.launch {
            try {
                repository.flushOutbox()?.let { note -> appendSystem(note) }
                appendAssistant(repository.chat(text))
            } finally {
                // never leave the composer locked (audit B3: a hung call used to drop every later message)
                _uiState.update { it.copy(isSending = false, paired = connector.isPaired) }
            }
        }
    }

    fun quickPrompt(prompt: String) {
        _uiState.update { it.copy(input = prompt) }
        send()
    }

    fun briefing() {
        if (_uiState.value.isSending) return
        _uiState.update {
            it.copy(messages = it.messages + ChatMessage(text = "Give me my briefing.", fromUser = true), isSending = true)
        }
        viewModelScope.launch {
            try {
                val result = repository.briefing()
                appendAssistant(
                    result.fold(
                        onSuccess = { AssistantReply(it, AssistantReply.Source.CAROLINE) },
                        onFailure = { AssistantReply("No briefing: ${it.message}", AssistantReply.Source.OFFLINE) }
                    )
                )
            } finally {
                _uiState.update { it.copy(isSending = false, paired = connector.isPaired) }
            }
        }
    }

    private fun appendAssistant(reply: AssistantReply) {
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage(text = reply.text, fromUser = false, provenance = provenanceOf(reply.source))
            )
        }
        persistHistory()
    }

    private fun appendSystem(text: String) {
        _uiState.update { it.copy(messages = it.messages + ChatMessage(text = text, fromUser = false, provenance = "connection")) }
        persistHistory()
    }

    /** Pair this phone with a one-time code the owner made in the Office. */
    fun pair(serverUrl: String, code: String, label: String) {
        if (connector.isPaired) {
            _uiState.update { it.copy(connectionNote = "This phone is already paired. Disconnect first, then pair again.") }
            return
        }
        val checked = CarolineEndpoint.normalize(serverUrl)
        if (checked is CarolineEndpoint.Checked.Refused) {
            _uiState.update { it.copy(connectionNote = checked.reason) }
            return
        }
        val previousUrl = connectorSettings.baseUrl
        connectorSettings.baseUrl = (checked as CarolineEndpoint.Checked.Ok).url   // no token is held here (not paired)
        _uiState.update { it.copy(serverUrl = connectorSettings.baseUrl, isPairing = true, connectionNote = null) }
        viewModelScope.launch {
            val r = connector.pair(code, label, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME)
            if (r !is ConnectorResult.Ok) connectorSettings.baseUrl = previousUrl
            val note = when (r) {
                is ConnectorResult.Ok -> "Paired as \"${r.value.label}\". Messages you type here now go to Caroline."
                is ConnectorResult.Failed -> "Not paired: ${r.error.message}"
                is ConnectorResult.Queued -> "Not paired."
            }
            _uiState.update { it.copy(isPairing = false, paired = connector.isPaired, serverUrl = connectorSettings.baseUrl, connectionNote = note) }
        }
    }

    /** Unpair this phone (the token is revoked on the server when it can be reached). */
    fun disconnect() {
        if (!connector.isPaired) {
            _uiState.update { it.copy(paired = false, connectionNote = "This phone is not paired.") }
            return
        }
        viewModelScope.launch {
            val rep = connector.signOut()
            val note = buildString {
                append(if (rep.serverRevoked) "Disconnected; the server revoked this phone." else
                    "Disconnected on this phone. The server could not be told (${rep.serverMessage ?: "no answer"}); revoke it in the Office too.")
                if (rep.droppedUnsent > 0) append(" ${rep.droppedUnsent} unsent message(s) were dropped.")
            }
            _uiState.update { it.copy(paired = connector.isPaired, connectionNote = note) }
        }
    }

    fun saveWebSearchKey(key: String) {
        webSearchSettings.apiKey = key
        _uiState.update { it.copy(webSearchApiKey = webSearchSettings.apiKey) }
    }

    companion object {
        fun provenanceOf(source: AssistantReply.Source): String = when (source) {
            AssistantReply.Source.CAROLINE -> "Caroline"
            AssistantReply.Source.DELIVERED -> "delivered to Caroline • no reply in the app yet"
            AssistantReply.Source.QUEUED -> "waiting for a connection"
            AssistantReply.Source.AGENT -> "on this phone • action"
            AssistantReply.Source.OFFLINE -> "on-device • Caroline not reached"
            AssistantReply.Source.WEB -> "web • tavily"
        }
    }
}
