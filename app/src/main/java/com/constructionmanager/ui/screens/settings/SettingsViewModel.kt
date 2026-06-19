package com.constructionmanager.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.constructionmanager.data.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsStore
) : ViewModel() {

    val darkTheme: StateFlow<Boolean> = store.darkTheme
    val notifications: StateFlow<Boolean> = store.notifications
    val offlineMode: StateFlow<Boolean> = store.offlineMode
    val defaultRegion: StateFlow<String> = store.defaultRegion

    fun setDarkTheme(value: Boolean) = store.setDarkTheme(value)
    fun setNotifications(value: Boolean) = store.setNotifications(value)
    fun setOfflineMode(value: Boolean) = store.setOfflineMode(value)
    fun setDefaultRegion(value: String) = store.setDefaultRegion(value)
}
