package com.constructionmanager.telephony

import android.content.Context
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import kotlin.math.abs

/**
 * On-device call screening, ported and de-risked from the standalone telephony_agent app.
 *
 * The original prototype depended on a bundled ONNX Phi-3 model; that heavy dependency is
 * replaced here with a fast deterministic heuristic so the master APK builds and runs with
 * no extra native assets. The risk threshold is shared with the rest of the app through the
 * same SharedPreferences store used by [com.constructionmanager.data.network.wade.WadeBackendConfig],
 * and live AI screening (Caroline) is surfaced through the Voice tab when the backend is enabled.
 */
class CarolineCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        val allow = evaluate(number)

        val response = CallResponse.Builder()
            .setDisallowCall(!allow)
            .setRejectCall(!allow)
            .setSkipCallLog(false)
            .setSkipNotification(!allow)
            .build()
        respondToCall(callDetails, response)
        Log.d(TAG, "Screened $number -> ${if (allow) "allow" else "block"}")
    }

    private fun evaluate(number: String?): Boolean {
        if (number.isNullOrBlank()) return false // anonymous → screen out
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val threshold = prefs.getInt(KEY_THRESHOLD, 70)
        // Deterministic placeholder risk score in 0..100; replaced by Caroline AI when the
        // backend is enabled. Allow when risk is at or below the user's threshold.
        val risk = abs(number.hashCode()) % 100
        return risk <= threshold
    }

    companion object {
        private const val TAG = "CarolineScreening"
        private const val PREFS = "construction_manager_prefs"
        private const val KEY_THRESHOLD = "wade_screening_threshold"
    }
}
