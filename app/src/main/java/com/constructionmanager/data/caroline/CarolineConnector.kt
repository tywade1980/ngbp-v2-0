package com.constructionmanager.data.caroline

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * The ONE way this app talks to Caroline (NGBP-CONNECT brief, Oct 8 2026: "Connect Caroline").
 *
 * - One base URL ([ConnectorSettings.baseUrl], https only, default the VM).
 * - One auth header: `Authorization: Bearer <device token>`. The token comes from pairing (a one-time code made in the
 *   Office) and is never put in a URL. The phone never holds the owner's APP_TOKEN.
 * - Typed requests and responses ([CarolineModels]), fixed timeouts, retry with backoff for requests that are safe to
 *   repeat, plain errors ([ConnectorError]), and an offline queue ([Outbox]) for messages to Caroline.
 *
 * Pure JVM (OkHttp + Gson + coroutines): no Android class, so it is unit-tested against a mock server.
 */
class CarolineConnector(
    baseClient: OkHttpClient,
    private val settings: ConnectorSettings,
    private val outbox: Outbox,
    private val sleep: suspend (Long) -> Unit = { delay(it) },
    private val clock: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() }
) {
    private val gson = Gson()
    private val json = "application/json; charset=utf-8".toMediaType()
    private val client: OkHttpClient = baseClient.newBuilder()
        .connectTimeout(CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
        .writeTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
        .callTimeout(CALL_TIMEOUT_S, TimeUnit.SECONDS)
        .build()

    val isPaired: Boolean get() = !settings.deviceToken.isNullOrBlank()

    // ------------------------------------------------------------------ calls the app makes

    /** GET health - no token needed; tells whether the server answers at all. */
    suspend fun health(): ConnectorResult<HealthResponse> =
        exchange("GET", "health", body = null, auth = false, retry = true, HealthResponse::class.java)

    /**
     * POST pair - trade the one-time code for this phone's token, and keep it. Not retried: the code works once, so a
     * resend after a lost answer would read "already used" (the message then says to make a new code).
     */
    suspend fun pair(code: String, label: String, model: String?, android: String?, appVersion: String?): ConnectorResult<PairResponse> {
        val req = PairRequest(code.trim(), PairDeviceInfo(model, android, appVersion), label.trim())
        val r = exchange("POST", "pair", gson.toJson(req), auth = false, retry = false, PairResponse::class.java)
        if (r is ConnectorResult.Ok) {
            val token = r.value.token
            if (token.isNullOrBlank() || r.value.deviceId.isNullOrBlank()) {
                return ConnectorResult.Failed(ConnectorError.BadResponse("The server said paired but sent no token; nothing was saved."))
            }
            settings.deviceToken = token
            settings.deviceId = r.value.deviceId
        }
        return r
    }

    /** GET devices/self - which device and business this phone is paired as. */
    suspend fun whoAmI(): ConnectorResult<DeviceSelf> =
        exchange("GET", "devices/self", null, auth = true, retry = true, DeviceSelf::class.java)

    /** GET calls - the calls Caroline handled (owner's business line). */
    suspend fun calls(limit: Int = 50): ConnectorResult<List<CallSummary>> {
        val r = exchange("GET", "calls?limit=${limit.coerceIn(1, 200)}", null, auth = true, retry = true, Array<CallSummary>::class.java)
        return when (r) {
            is ConnectorResult.Ok -> ConnectorResult.Ok(r.value.toList())
            is ConnectorResult.Queued -> r
            is ConnectorResult.Failed -> r
        }
    }

    /** GET briefing - the same owner briefing the phone line reads from the record. */
    suspend fun briefing(): ConnectorResult<BriefingResponse> =
        exchange("GET", "briefing", null, auth = true, retry = true, BriefingResponse::class.java)

    /**
     * POST assist/text - a typed message to Caroline. The server stores it once per [clientMsgId], so it is safe to
     * retry; when the network is down after the retries it goes to the outbox and the answer is [ConnectorResult.Queued].
     * A refusal (4xx) is never queued: sending it again would be refused again.
     */
    suspend fun sendText(text: String, clientMsgId: String = newId()): ConnectorResult<AssistTextResponse> {
        if (!isPaired) return ConnectorResult.Failed(ConnectorError.NotPaired())
        val body = gson.toJson(AssistTextRequest(text, clientMsgId))
        val r = exchange("POST", "assist/text", body, auth = true, retry = true, AssistTextResponse::class.java)
        if (r is ConnectorResult.Failed && r.error is ConnectorError.Network) {
            if (outbox.add(OutboxItem(clientMsgId, text, clock()))) return ConnectorResult.Queued(clientMsgId)
            return ConnectorResult.Failed(ConnectorError.Network("No connection, and the outbox is full (${PrefsOutbox.MAX_ITEMS}); this message was not kept."))
        }
        return r
    }

    data class FlushReport(val sent: Int, val refused: List<Pair<OutboxItem, ConnectorError>>, val left: Int)

    /** Send what the outbox holds, oldest first, with each message's original id. Stops at the first network failure. */
    suspend fun flushOutbox(): FlushReport {
        var sent = 0
        val refused = mutableListOf<Pair<OutboxItem, ConnectorError>>()
        for (item in outbox.items()) {
            val r = exchange("POST", "assist/text", gson.toJson(AssistTextRequest(item.text, item.clientMsgId)),
                auth = true, retry = true, AssistTextResponse::class.java)
            when {
                r is ConnectorResult.Ok -> { outbox.remove(item.clientMsgId); sent++ }
                r is ConnectorResult.Failed && r.error is ConnectorError.Network -> break
                r is ConnectorResult.Failed && r.error is ConnectorError.NotPaired -> break
                r is ConnectorResult.Failed -> { outbox.remove(item.clientMsgId); refused += item to r.error }
            }
        }
        return FlushReport(sent, refused, outbox.items().size)
    }

    data class SignOutReport(val serverRevoked: Boolean, val serverMessage: String?, val droppedUnsent: Int)

    /**
     * Sign out on this phone: ask the server to revoke this token for good, then forget it here - always, even when the
     * server could not be reached (the report says so, so the owner can revoke it from the Office). Unsent messages
     * belong to this pairing and are dropped; the report counts them.
     */
    suspend fun signOut(): SignOutReport {
        var revoked = false
        var msg: String? = null
        if (isPaired) {
            when (val r = exchange("POST", "devices/self/revoke", "{}", auth = true, retry = true, RevokeResponse::class.java)) {
                is ConnectorResult.Ok -> revoked = true
                is ConnectorResult.Failed -> msg = r.error.message
                is ConnectorResult.Queued -> Unit
            }
        }
        settings.deviceToken = null
        settings.deviceId = null
        return SignOutReport(revoked, msg, outbox.clear())
    }

    // ------------------------------------------------------------------ the one request path

    private suspend fun <T> exchange(method: String, path: String, body: String?, auth: Boolean, retry: Boolean, type: Class<T>): ConnectorResult<T> {
        val url = resolve(path) ?: return ConnectorResult.Failed(ConnectorError.BadResponse("The server address \"${settings.baseUrl}\" is not usable."))
        if (!url.isHttps) return ConnectorResult.Failed(ConnectorError.BadResponse("Only https addresses are allowed."))
        val token = settings.deviceToken
        if (auth && token.isNullOrBlank()) return ConnectorResult.Failed(ConnectorError.NotPaired())
        val builder = Request.Builder().url(url).header("Accept", "application/json")
        if (auth) builder.header("Authorization", "Bearer $token")
        builder.method(method, body?.toRequestBody(json))
        val request = builder.build()

        val attempts = if (retry) MAX_ATTEMPTS else 1
        var last: ConnectorError = ConnectorError.Network("No answer from Caroline's server.")
        for (attempt in 1..attempts) {
            if (attempt > 1) sleep(BACKOFF_BASE_MS shl (attempt - 2))
            val outcome: Pair<Int, String>? = try {
                withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { resp -> resp.code to (resp.body?.string() ?: "") }
                }
            } catch (e: IOException) {
                last = ConnectorError.Network("Can't reach Caroline's server right now (${e.javaClass.simpleName}).")
                null
            }
            if (outcome == null) continue
            val (code, text) = outcome
            if (code in 200..299) {
                return try {
                    val v = gson.fromJson(text, type) ?: throw IllegalStateException("empty body")
                    ConnectorResult.Ok(v)
                } catch (e: Exception) {
                    ConnectorResult.Failed(ConnectorError.BadResponse("Caroline's server answered in a shape this app does not read ($path)."))
                }
            }
            val detail = detailOf(text) ?: "HTTP $code"
            if (code == 401 && auth) {
                settings.deviceToken = null          // the server no longer knows this token: say "not paired", don't keep retrying it
                settings.deviceId = null
                return ConnectorResult.Failed(ConnectorError.NotPaired("This phone is no longer paired ($detail). Pair it again with a new code from the Office."))
            }
            if (code == 403) return ConnectorResult.Failed(ConnectorError.Forbidden(detail))
            if (code == 429 || code >= 500) {
                last = if (code >= 500) ConnectorError.Network("Caroline's server had a problem (HTTP $code).") else ConnectorError.Http(code, detail)
                if (code == 429 && !retry) return ConnectorResult.Failed(last)
                continue
            }
            return ConnectorResult.Failed(ConnectorError.Http(code, detail))
        }
        return ConnectorResult.Failed(last)
    }

    private fun resolve(path: String): HttpUrl? {
        val base = settings.baseUrl.toHttpUrlOrNull() ?: return null
        return base.resolve(path)
    }

    /** FastAPI errors: {"detail": "text"} or {"detail": [{"msg": ...}, ...]} (validation). */
    private fun detailOf(text: String): String? = try {
        val d = gson.fromJson(text, JsonObject::class.java)?.get("detail")
        when {
            d == null || d.isJsonNull -> null
            d.isJsonPrimitive -> d.asString
            d.isJsonArray -> d.asJsonArray.mapNotNull { it.asJsonObject.get("msg")?.asString }.joinToString("; ").ifBlank { null }
            else -> d.toString()
        }
    } catch (e: Exception) {
        null
    }

    companion object {
        const val CONNECT_TIMEOUT_S = 10L
        const val READ_TIMEOUT_S = 20L
        const val CALL_TIMEOUT_S = 30L
        const val MAX_ATTEMPTS = 3
        const val BACKOFF_BASE_MS = 500L
    }
}
