package com.constructionmanager.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.data.settings.SettingsStore
import com.constructionmanager.domain.model.User
import com.constructionmanager.domain.repository.AuthRepository
import com.constructionmanager.domain.repository.MaterialRepository
import com.constructionmanager.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsStore,
    private val authRepository: AuthRepository,
    private val projectRepository: ProjectRepository,
    private val materialRepository: MaterialRepository
) : ViewModel() {

    val darkTheme: StateFlow<Boolean> = store.darkTheme
    val notifications: StateFlow<Boolean> = store.notifications
    val offlineMode: StateFlow<Boolean> = store.offlineMode
    val defaultRegion: StateFlow<String> = store.defaultRegion
    val currency: StateFlow<String> = store.currency
    val emailNotifications: StateFlow<Boolean> = store.emailNotifications
    val smsNotifications: StateFlow<Boolean> = store.smsNotifications

    private val _profile = MutableStateFlow<User?>(null)
    val profile: StateFlow<User?> = _profile.asStateFlow()

    init {
        viewModelScope.launch { _profile.value = runCatching { authRepository.getCurrentUser() }.getOrNull() }
    }

    fun setDarkTheme(value: Boolean) = store.setDarkTheme(value)
    fun setNotifications(value: Boolean) = store.setNotifications(value)
    fun setOfflineMode(value: Boolean) = store.setOfflineMode(value)
    fun setDefaultRegion(value: String) = store.setDefaultRegion(value)
    fun setCurrency(value: String) = store.setCurrency(value)
    fun setEmailNotifications(value: Boolean) = store.setEmailNotifications(value)
    fun setSmsNotifications(value: Boolean) = store.setSmsNotifications(value)

    fun saveProfile(firstName: String, lastName: String, company: String) {
        viewModelScope.launch {
            val current = _profile.value ?: runCatching { authRepository.getCurrentUser() }.getOrNull() ?: return@launch
            val updated = current.copy(firstName = firstName.trim(), lastName = lastName.trim(), company = company.trim())
            _profile.value = runCatching { authRepository.updateProfile(updated) }.getOrDefault(updated)
        }
    }

    fun changePassword(newPassword: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(runCatching { authRepository.changePassword("", newPassword) })
        }
    }

    /** Builds a plain-text export of the user's data and hands it back for sharing. */
    fun exportData(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val projects = runCatching { projectRepository.getAllProjects().first() }.getOrDefault(emptyList())
            val materials = runCatching { materialRepository.getAllActiveMaterials().first() }.getOrDefault(emptyList())
            val text = buildString {
                appendLine("ConstructPro — Data Export")
                appendLine("=".repeat(40))
                appendLine()
                appendLine("PROJECTS (${projects.size})")
                projects.forEach {
                    appendLine("• ${it.name} — ${it.status.name.lowercase()} — $${it.totalBudget} — ${it.clientName}")
                }
                appendLine()
                appendLine("MATERIALS (${materials.size})")
                materials.forEach {
                    appendLine("• ${it.name} — ${it.category.name.lowercase()} @ $${it.currentPrice}/${it.unitOfMeasurement}")
                }
            }
            onReady(text)
        }
    }
}
