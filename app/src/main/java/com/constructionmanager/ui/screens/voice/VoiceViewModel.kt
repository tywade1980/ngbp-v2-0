package com.constructionmanager.ui.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.ai.AssistantRepository
import com.constructionmanager.data.caroline.CallSummary
import com.constructionmanager.data.caroline.CarolineConnector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceUiState(
    val paired: Boolean = false,
    val isLoading: Boolean = false,
    val calls: List<CallSummary> = emptyList(),
    val error: String? = null
)

/** The Calls screen: the record of calls Caroline handled, read from the VM through the connector. */
@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val repository: AssistantRepository,
    private val connector: CarolineConnector
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState(paired = connector.isPaired))
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    fun refreshCalls() {
        if (!connector.isPaired) {
            _uiState.update { it.copy(paired = false, isLoading = false, calls = emptyList(), error = null) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.recentCalls()
            _uiState.update {
                result.fold(
                    onSuccess = { calls -> it.copy(isLoading = false, calls = calls, paired = connector.isPaired) },
                    onFailure = { e -> it.copy(isLoading = false, error = e.message, paired = connector.isPaired) }
                )
            }
        }
    }
}
