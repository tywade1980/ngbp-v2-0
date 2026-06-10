package com.constructionmanager.ui.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.ai.AssistantRepository
import com.constructionmanager.data.network.wade.CallRecord
import com.constructionmanager.data.network.wade.WadeBackendConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceUiState(
    val screeningThreshold: Int = 70,
    val remoteEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val calls: List<CallRecord> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val repository: AssistantRepository,
    private val config: WadeBackendConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VoiceUiState(
            screeningThreshold = config.screeningThreshold,
            remoteEnabled = config.remoteEnabled
        )
    )
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    fun setThreshold(value: Int) {
        config.screeningThreshold = value
        _uiState.update { it.copy(screeningThreshold = config.screeningThreshold) }
    }

    fun refreshCalls() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.recentCalls()
            _uiState.update {
                result.fold(
                    onSuccess = { calls -> it.copy(isLoading = false, calls = calls) },
                    onFailure = { e -> it.copy(isLoading = false, error = e.message) }
                )
            }
        }
    }
}
