package com.constructionmanager.data.network.wade

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central configuration for the Wade agentic backend that the master app talks to.
 *
 * The Wade ecosystem exposes three cooperating services (see the companion repos):
 *  - Orchestrator   (unified-agentic-ai-foundation) — chat, reasoning, WCC construction tools
 *  - Memory         (mem0)                           — persistent semantic memory
 *  - Caroline       (telephony_voice_ai)             — call screening + receptionist
 *
 * All endpoints are user-configurable at runtime so the same APK works against an
 * emulator loopback host, a LAN dev box, or a deployed gateway. When the backend is
 * unreachable the app degrades gracefully to an on-device assistant.
 */
@Singleton
class WadeBackendConfig @Inject constructor(
    private val prefs: SharedPreferences
) {
    var orchestratorUrl: String
        get() = prefs.getString(KEY_ORCH, DEFAULT_ORCH) ?: DEFAULT_ORCH
        set(value) = prefs.edit { putString(KEY_ORCH, normalize(value)) }

    var memoryUrl: String
        get() = prefs.getString(KEY_MEM, DEFAULT_MEM) ?: DEFAULT_MEM
        set(value) = prefs.edit { putString(KEY_MEM, normalize(value)) }

    var carolineUrl: String
        get() = prefs.getString(KEY_CAROLINE, DEFAULT_CAROLINE) ?: DEFAULT_CAROLINE
        set(value) = prefs.edit { putString(KEY_CAROLINE, normalize(value)) }

    /** Identity used as user_id / agent routing key across the ecosystem. */
    var userId: String
        get() = prefs.getString(KEY_USER, DEFAULT_USER) ?: DEFAULT_USER
        set(value) = prefs.edit { putString(KEY_USER, value.trim()) }

    /** When false the app never reaches out and runs the on-device assistant only. */
    var remoteEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMOTE, false)
        set(value) = prefs.edit { putBoolean(KEY_REMOTE, value) }

    /** On-device call-screening risk threshold (0 = block all, 100 = allow all). */
    var screeningThreshold: Int
        get() = prefs.getInt(KEY_SCREEN, 70)
        set(value) = prefs.edit { putInt(KEY_SCREEN, value.coerceIn(0, 100)) }

    private fun normalize(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return trimmed
        val withScheme = if (trimmed.startsWith("http")) trimmed else "http://$trimmed"
        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }

    companion object {
        // 10.0.2.2 is the host loopback as seen from the Android emulator.
        const val DEFAULT_ORCH = "http://10.0.2.2:8000/"
        const val DEFAULT_MEM = "http://10.0.2.2:8888/"
        const val DEFAULT_CAROLINE = "http://10.0.2.2:8001/"
        const val DEFAULT_USER = "tyler"

        private const val KEY_ORCH = "wade_orchestrator_url"
        private const val KEY_MEM = "wade_memory_url"
        private const val KEY_CAROLINE = "wade_caroline_url"
        private const val KEY_USER = "wade_user_id"
        private const val KEY_REMOTE = "wade_remote_enabled"
        private const val KEY_SCREEN = "wade_screening_threshold"
    }
}
