package com.constructionmanager.data.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persisted app preferences, backed by SharedPreferences and exposed as StateFlows so the UI
 * (and the theme in MainActivity) react immediately and survive restarts.
 */
@Singleton
class SettingsStore @Inject constructor(
    private val prefs: SharedPreferences
) {
    private val _darkTheme = MutableStateFlow(prefs.getBoolean(KEY_DARK, false))
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    private val _notifications = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS, true))
    val notifications: StateFlow<Boolean> = _notifications.asStateFlow()

    private val _offlineMode = MutableStateFlow(prefs.getBoolean(KEY_OFFLINE, false))
    val offlineMode: StateFlow<Boolean> = _offlineMode.asStateFlow()

    private val _defaultRegion = MutableStateFlow(prefs.getString(KEY_REGION, "Midwest") ?: "Midwest")
    val defaultRegion: StateFlow<String> = _defaultRegion.asStateFlow()

    private val _currency = MutableStateFlow(prefs.getString(KEY_CURRENCY, "USD ($)") ?: "USD ($)")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _emailNotifications = MutableStateFlow(prefs.getBoolean(KEY_EMAIL_NOTIF, false))
    val emailNotifications: StateFlow<Boolean> = _emailNotifications.asStateFlow()

    private val _smsNotifications = MutableStateFlow(prefs.getBoolean(KEY_SMS_NOTIF, false))
    val smsNotifications: StateFlow<Boolean> = _smsNotifications.asStateFlow()

    fun setDarkTheme(value: Boolean) {
        prefs.edit { putBoolean(KEY_DARK, value) }
        _darkTheme.value = value
    }

    fun setNotifications(value: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFICATIONS, value) }
        _notifications.value = value
    }

    fun setOfflineMode(value: Boolean) {
        prefs.edit { putBoolean(KEY_OFFLINE, value) }
        _offlineMode.value = value
    }

    fun setDefaultRegion(value: String) {
        prefs.edit { putString(KEY_REGION, value) }
        _defaultRegion.value = value
    }

    fun setCurrency(value: String) {
        prefs.edit { putString(KEY_CURRENCY, value) }
        _currency.value = value
    }

    fun setEmailNotifications(value: Boolean) {
        prefs.edit { putBoolean(KEY_EMAIL_NOTIF, value) }
        _emailNotifications.value = value
    }

    fun setSmsNotifications(value: Boolean) {
        prefs.edit { putBoolean(KEY_SMS_NOTIF, value) }
        _smsNotifications.value = value
    }

    private companion object {
        const val KEY_DARK = "settings_dark_theme"
        const val KEY_NOTIFICATIONS = "settings_notifications"
        const val KEY_OFFLINE = "settings_offline_mode"
        const val KEY_REGION = "settings_default_region"
        const val KEY_CURRENCY = "settings_currency"
        const val KEY_EMAIL_NOTIF = "settings_email_notifications"
        const val KEY_SMS_NOTIF = "settings_sms_notifications"
    }
}
