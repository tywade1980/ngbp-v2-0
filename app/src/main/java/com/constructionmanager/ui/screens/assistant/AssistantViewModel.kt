package com.constructionmanager.ui.screens.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.ai.AssistantReply
import com.constructionmanager.ai.AssistantRepository
import com.constructionmanager.data.network.wade.WadeBackendConfig
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
    val live: Boolean = false
)

data class AssistantUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Hi Tyler — Caroline here. Ask me for an estimate, a briefing, a material lookup, " +
                "or anything across your projects.",
            fromUser = false
        )
    ),
    val input: String = "",
    val isSending: Boolean = false,
    // Backend config mirror for the in-screen settings sheet
    val remoteEnabled: Boolean = false,
    val orchestratorUrl: String = "",
    val memoryUrl: String = "",
    val carolineUrl: String = "",
    val userId: String = ""
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val repository: AssistantRepository,
    private val config: WadeBackendConfig
) : ViewModel() {

    private val sessionId = UUID.randomUUID().toString()
    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private fun loadState() = AssistantUiState(
        remoteEnabled = config.remoteEnabled,
        orchestratorUrl = config.orchestratorUrl,
        memoryUrl = config.memoryUrl,
        carolineUrl = config.carolineUrl,
        userId = config.userId
    )

    fun onInputChange(value: String) = _uiState.update { it.copy(input = value) }

    fun send() {
        val text = _uiState.value.input.trim()
        if (text.isEmpty() || _uiState.value.isSending) return
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage(text = text, fromUser = true),
                input = "",
                isSending = true
            )
        }
        viewModelScope.launch {
            val reply = repository.chat(text, sessionId)
            appendAssistant(reply)
        }
    }

    fun quickPrompt(prompt: String) {
        _uiState.update { it.copy(input = prompt) }
        send()
    }

    fun briefing() {
        if (_uiState.value.isSending) return
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage(text = "Give me a project briefing.", fromUser = true),
                isSending = true
            )
        }
        viewModelScope.launch {
            val result = repository.briefing()
            val text = result.getOrElse { "Couldn't generate a briefing: ${it.message}" }
            appendAssistant(AssistantReply(text, AssistantReply.Source.LIVE))
        }
    }

    private fun appendAssistant(reply: AssistantReply) {
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage(
                    text = reply.text,
                    fromUser = false,
                    live = reply.source == AssistantReply.Source.LIVE
                ),
                isSending = false
            )
        }
    }

    fun saveBackend(
        remoteEnabled: Boolean,
        orchestratorUrl: String,
        memoryUrl: String,
        carolineUrl: String,
        userId: String
    ) {
        config.remoteEnabled = remoteEnabled
        config.orchestratorUrl = orchestratorUrl
        config.memoryUrl = memoryUrl
        config.carolineUrl = carolineUrl
        if (userId.isNotBlank()) config.userId = userId
        _uiState.update {
            it.copy(
                remoteEnabled = config.remoteEnabled,
                orchestratorUrl = config.orchestratorUrl,
                memoryUrl = config.memoryUrl,
                carolineUrl = config.carolineUrl,
                userId = config.userId
            )
        }
    }
}
