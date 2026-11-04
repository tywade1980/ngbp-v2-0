package com.constructionmanager.data.caroline

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * The room's writers (HOCC 0.6, Nov 2 2026) - the same Office doors the web room rides, reached with this
 * phone's token. #1180 (Oct 30): a paired phone bound to the owner passes every door, so nothing new stands
 * on the server; these are the verbs the record already carries, sent from the phone.
 *
 * A write is sent ONCE and never retried: a lost answer can never land the same hours, line or photo twice.
 * After a write the screen reads the record again and shows what stands - the answer is never trusted
 * to be the state.
 *
 * Kept beside [CarolineConnector] as an extension so the connector's own contract (its retry policy, its
 * outbox, its logging rule) stays exactly as reviewed on Oct 8.
 */
class RoomWriters(
    private val connector: CarolineConnector,
    baseClient: OkHttpClient,
    private val settings: ConnectorSettings
) {
    private val gson = Gson()
    private val json = "application/json; charset=utf-8".toMediaType()
    private val client: OkHttpClient = baseClient.newBuilder()
        .apply { interceptors().clear(); networkInterceptors().clear() }
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    suspend fun post(path: String, body: JsonObject): ConnectorResult<JsonElement> = once("POST", path, gson.toJson(body).toRequestBody(json))
    suspend fun patch(path: String, body: JsonObject): ConnectorResult<JsonElement> = once("PATCH", path, gson.toJson(body).toRequestBody(json))
    suspend fun delete(path: String): ConnectorResult<JsonElement> = once("DELETE", path, null)

    /** Raw bytes (a photo) to attachments?kind=project&ref=<id>&filename=<name> - the attachments door. */
    suspend fun upload(path: String, bytes: ByteArray, contentType: String): ConnectorResult<JsonElement> =
        once("POST", path, bytes.toRequestBody(contentType.toMediaType()))

    private suspend fun once(method: String, path: String, body: okhttp3.RequestBody?): ConnectorResult<JsonElement> {
        val base = settings.baseUrl.toHttpUrlOrNull()
            ?: return ConnectorResult.Failed(ConnectorError.BadResponse("The server address \"${settings.baseUrl}\" is not usable."))
        val url = base.resolve(path) ?: return ConnectorResult.Failed(ConnectorError.BadResponse("The server address is not usable."))
        if (!url.isHttps) return ConnectorResult.Failed(ConnectorError.BadResponse("Only https addresses are allowed."))
        val token = settings.deviceToken
        if (token.isNullOrBlank()) return ConnectorResult.Failed(ConnectorError.NotPaired())
        val req = Request.Builder().url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .method(method, body)
            .build()
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(req).execute().use { resp ->
                    val text = resp.body?.string() ?: ""
                    if (resp.code == 401) {
                        settings.deviceToken = null
                        settings.deviceId = null
                        return@withContext ConnectorResult.Failed(ConnectorError.NotPaired("This phone's sign-in is no longer accepted. Sign in again."))
                    }
                    if (resp.code == 403) return@withContext ConnectorResult.Failed(ConnectorError.Forbidden(detailOf(text) ?: "This phone may not use that door."))
                    if (!resp.isSuccessful) return@withContext ConnectorResult.Failed(ConnectorError.Http(resp.code, detailOf(text) ?: "HTTP ${resp.code}"))
                    try {
                        ConnectorResult.Ok(gson.fromJson(text.ifBlank { "{}" }, JsonElement::class.java))
                    } catch (e: Exception) {
                        ConnectorResult.Failed(ConnectorError.BadResponse("The answer to $method $path was not readable."))
                    }
                }
            } catch (e: IOException) {
                ConnectorResult.Failed(ConnectorError.Network("No connection to Caroline (" + (e.message ?: "network error") + "). Nothing was written; try again in coverage."))
            }
        }
    }

    /** FastAPI errors: {"detail": "text"} or {"detail": [{"msg": ...}]} - the refusal in the door's own words. */
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
}
