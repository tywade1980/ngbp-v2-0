package com.constructionmanager.update

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runtime configuration for the OTA update system, backed by the same SharedPreferences store
 * used by the rest of the app. The manifest URL is user-editable so the same APK can point at a
 * production release channel, a staging channel, or a LAN dev box.
 */
@Singleton
class UpdateConfig @Inject constructor(
    private val prefs: SharedPreferences
) {
    /** URL of the JSON update manifest for the self-hosted channel. */
    var manifestUrl: String
        get() = prefs.getString(KEY_URL, DEFAULT_MANIFEST_URL) ?: DEFAULT_MANIFEST_URL
        set(value) = prefs.edit { putString(KEY_URL, normalize(value)) }

    /** Whether the app checks for updates automatically on launch. */
    var autoCheckEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO, true)
        set(value) = prefs.edit { putBoolean(KEY_AUTO, value) }

    /** Prefer Google Play In-App Updates when the app was installed from Play. */
    var preferPlayStore: Boolean
        get() = prefs.getBoolean(KEY_PLAY, true)
        set(value) = prefs.edit { putBoolean(KEY_PLAY, value) }

    /** A non-mandatory version the user chose to skip; we won't nag about it again. */
    var skippedVersionCode: Int
        get() = prefs.getInt(KEY_SKIPPED, 0)
        set(value) = prefs.edit { putInt(KEY_SKIPPED, value) }

    private fun normalize(url: String): String = url.trim()

    companion object {
        /** Default points at this repository's published OTA manifest on the main branch. */
        const val DEFAULT_MANIFEST_URL =
            "https://raw.githubusercontent.com/tywade1980/ngbp-v2-0/main/ota/update.json"

        private const val KEY_URL = "ota_manifest_url"
        private const val KEY_AUTO = "ota_auto_check"
        private const val KEY_PLAY = "ota_prefer_play"
        private const val KEY_SKIPPED = "ota_skipped_version"
    }
}
