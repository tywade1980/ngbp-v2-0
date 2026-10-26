package com.constructionmanager.data.caroline

import com.google.gson.annotations.SerializedName

/*
 * Request and response shapes of Caroline's Office API, as the routes on the VM write them (office_api.py on branch
 * phone-pair). Response fields are nullable on purpose: Gson does not apply Kotlin defaults, and a missing field must
 * read as "not given", never as a made-up value.
 */

data class PairDeviceInfo(
    @SerializedName("model") val model: String?,
    @SerializedName("android") val android: String?,
    @SerializedName("app_version") val appVersion: String?
)

data class PairRequest(
    @SerializedName("pair_code") val pairCode: String,
    @SerializedName("device") val device: PairDeviceInfo,
    @SerializedName("label") val label: String = ""
)

data class PairResponse(
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("tenant") val tenant: String? = null,
    @SerializedName("label") val label: String? = null
)

/** One row of GET /api/calls (office_api.list_calls). */
data class CallSummary(
    @SerializedName("id") val id: String? = null,
    @SerializedName("started_at") val startedAt: String? = null,
    @SerializedName("ended_at") val endedAt: String? = null,
    @SerializedName("direction") val direction: String? = null,
    @SerializedName("party_number") val partyNumber: String? = null,
    @SerializedName("caller_name") val callerName: String? = null,
    @SerializedName("lead_status") val leadStatus: String? = null,
    @SerializedName("outcome") val outcome: String? = null,
    @SerializedName("summary") val summary: String? = null
)

/** GET /api/briefing: the app shows `text` (the same owner briefing the line uses). */
data class BriefingResponse(
    @SerializedName("text") val text: String? = null
)

data class AssistTextRequest(
    @SerializedName("text") val text: String,
    @SerializedName("client_msg_id") val clientMsgId: String,
    @SerializedName("said_at") val saidAt: String? = null
)

data class AssistTextResponse(
    @SerializedName("stored") val stored: Boolean? = null,
    @SerializedName("duplicate") val duplicate: Boolean? = null,
    @SerializedName("message_id") val messageId: Long? = null,
    @SerializedName("at") val at: String? = null,
    @SerializedName("thread") val thread: String? = null,
    @SerializedName("answered") val answered: Boolean? = null,
    @SerializedName("reply") val reply: String? = null,
    @SerializedName("detail") val detail: String? = null
)

/**
 * POST /api/auth/login (office_api.auth_login): email + password in, the same device token pairing returns.
 * The password crosses the wire once, on this request, over https, and is never kept by the app.
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("device") val device: PairDeviceInfo,
    @SerializedName("label") val label: String = ""
)

data class LoginResponse(
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("tenant") val tenant: String? = null,
    @SerializedName("label") val label: String? = null,
    @SerializedName("email") val email: String? = null
)

/** POST /api/auth/set-password: the owner sets their OWN password with a one-time code from their Office. */
data class SetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String,
    @SerializedName("new_password") val newPassword: String
)

data class SetPasswordResponse(
    @SerializedName("tenant") val tenant: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("password_set") val passwordSet: Boolean? = null
)

data class RevokeResponse(
    @SerializedName("revoked") val revoked: String? = null
)

/**
 * The server writes timestamps as "2026-10-08 21:05:12.345+00:00" (UTC, Python str()) or ISO with a 'T'. Shown in the
 * phone's own time zone, never as raw UTC (a UTC time read as local is a wrong time). null when it cannot be read.
 */
fun serverTimeToLocal(raw: String?, zone: java.time.ZoneId = java.time.ZoneId.systemDefault()): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        val t = java.time.OffsetDateTime.parse(raw.trim().replaceFirst(' ', 'T'))
        t.atZoneSameInstant(zone).format(java.time.format.DateTimeFormatter.ofPattern("EEE MMM d, h:mm a", java.util.Locale.US))
    } catch (e: java.time.format.DateTimeParseException) {
        null
    }
}
