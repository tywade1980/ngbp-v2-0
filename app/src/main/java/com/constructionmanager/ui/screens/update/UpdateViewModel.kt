package com.constructionmanager.ui.screens.update

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.update.AvailableUpdate
import com.constructionmanager.update.UpdateConfig
import com.constructionmanager.update.UpdateRepository
import com.constructionmanager.update.UpdateStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateSettingsUi(
    val manifestUrl: String = "",
    val autoCheckEnabled: Boolean = true,
    val preferPlayStore: Boolean = true
)

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val repository: UpdateRepository,
    private val config: UpdateConfig
) : ViewModel() {

    val status: StateFlow<UpdateStatus> = repository.status

    val currentVersionName: String = repository.currentVersionName
    val currentVersionCode: Int = repository.currentVersionCode

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UpdateSettingsUi> = _settings.asStateFlow()

    private fun loadSettings() = UpdateSettingsUi(
        manifestUrl = config.manifestUrl,
        autoCheckEnabled = config.autoCheckEnabled,
        preferPlayStore = config.preferPlayStore
    )

    fun check() {
        viewModelScope.launch { repository.check() }
    }

    fun download(update: AvailableUpdate) {
        viewModelScope.launch { repository.download(update) }
    }

    fun install(): Boolean = repository.installDownloaded()

    fun canInstallPackages(): Boolean = repository.canInstallPackages()

    fun unknownSourcesIntent(): Intent = repository.unknownSourcesIntent()

    fun skip(update: AvailableUpdate) = repository.skip(update)

    fun saveSettings(manifestUrl: String, autoCheck: Boolean, preferPlay: Boolean) {
        config.manifestUrl = manifestUrl
        config.autoCheckEnabled = autoCheck
        config.preferPlayStore = preferPlay
        _settings.update {
            it.copy(
                manifestUrl = config.manifestUrl,
                autoCheckEnabled = config.autoCheckEnabled,
                preferPlayStore = config.preferPlayStore
            )
        }
    }
}
