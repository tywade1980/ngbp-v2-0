package com.constructionmanager.telephony

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.constructionmanager.ai.AssistantRepository
import com.constructionmanager.data.network.wade.WadeBackendConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.math.abs

/**
 * On-device call screening, ported and de-risked from the standalone telephony_agent app.
 *
 * Two-tier decision:
 *  1. When the Wade backend is enabled, ask the live **Caroline** receptionist (`/screen`) within a
 *     short time budget so the system isn't kept waiting.
 *  2. Otherwise (or on timeout/error) fall back to a fast deterministic heuristic against the
 *     user's risk threshold, so screening always works offline.
 */
@AndroidEntryPoint
class CarolineCallScreeningService : CallScreeningService() {

    @Inject
    lateinit var config: WadeBackendConfig

    @Inject
    lateinit var assistant: AssistantRepository

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        val allow = decide(number)

        val response = CallResponse.Builder()
            .setDisallowCall(!allow)
            .setRejectCall(!allow)
            .setSkipCallLog(false)
            .setSkipNotification(!allow)
            .build()
        respondToCall(callDetails, response)
        Log.d(TAG, "Screened $number -> ${if (allow) "allow" else "block"}")
    }

    private fun decide(number: String?): Boolean {
        if (number.isNullOrBlank()) return false // anonymous → screen out

        if (config.remoteEnabled) {
            val live = runBlocking {
                withTimeoutOrNull(LIVE_TIMEOUT_MS) {
                    assistant.screenCall(callSid = "android-${System.currentTimeMillis()}", number = number)
                }
            }
            if (live != null && live.live) {
                Log.d(TAG, "Caroline decision: ${live.decision} (${live.reason})")
                return live.decision.equals("allow", ignoreCase = true) ||
                    live.decision.equals("transfer", ignoreCase = true)
            }
        }

        return heuristicAllow(number)
    }

    /** Deterministic placeholder risk score in 0..100; allow when at or below the user threshold. */
    private fun heuristicAllow(number: String): Boolean {
        val risk = abs(number.hashCode()) % 100
        return risk <= config.screeningThreshold
    }

    private companion object {
        const val TAG = "CarolineScreening"
        const val LIVE_TIMEOUT_MS = 2500L
    }
}
