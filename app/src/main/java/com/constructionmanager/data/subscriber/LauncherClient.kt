package com.constructionmanager.data.subscriber

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class LauncherResult<out T> {
    data class Ok<T>(val value: T) : LauncherResult<T>()
    data class Failed(val message: String) : LauncherResult<Nothing>()
}

/**
 * The phone's one call to the PLATFORM (not to any tenant): submit the
 * subscriber intake and read the launch back, over the launcher's JSON API
 * (POST /subscribe/api, GET /subscribe/api/status/<token> on the host's
 * caroline-launcher.service — the same pipeline the subscribe web page fires).
 *
 * [PLATFORM_BASE_URL] is the platform's own front door — the launcher every
 * install talks to exactly once, to be born. It is NOT a tenant backend: the
 * moment a launch finishes, the app binds to the tenant's own VM (the office
 * URL in the launch confirmation) and never asks this address for office data.
 *
 * Pure JVM (OkHttp + Gson), same hygiene as CarolineConnector: the shared
 * client's interceptors are stripped (the intake carries an EIN; nothing of
 * it may reach a log), fixed timeouts, https only, no auth — a launch token
 * is the only credential, and it stays in the request path the server issued.
 */
class LauncherClient(
    baseClient: OkHttpClient,
    private val platformBaseUrl: String = PLATFORM_BASE_URL
) {
    private val gson = Gson()
    private val json = "application/json; charset=utf-8".toMediaType()
    private val client: OkHttpClient = baseClient.newBuilder()
        .apply { interceptors().clear(); networkInterceptors().clear() }
        .connectTimeout(CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
        .writeTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
        .callTimeout(CALL_TIMEOUT_S, TimeUnit.SECONDS)
        .build()

    /** Submit the intake. A 422 answer is the launcher's own field errors, shown verbatim. */
    suspend fun submit(intake: SubscriberIntake): LauncherResult<LaunchStarted> {
        val body = gson.toJson(intake.toWire())
        val (code, text) = exchange("POST", "/subscribe/api", body)
            ?: return LauncherResult.Failed("Can't reach the platform right now. Check your connection and try again.")
        val obj = parseObject(text)
        return when {
            code in 200..299 && obj?.get("ok")?.asBoolean == true -> {
                val token = obj.get("token")?.asString
                if (token.isNullOrBlank()) LauncherResult.Failed("The platform answered without a launch token; nothing was started.")
                else LauncherResult.Ok(LaunchStarted(token, obj.get("status_path")?.asString))
            }
            obj?.has("errors") == true -> LauncherResult.Failed(
                obj.getAsJsonArray("errors").mapNotNull { it.asString }.joinToString("\n").ifBlank { "The platform refused the intake (HTTP $code)." }
            )
            else -> LauncherResult.Failed("The platform answered HTTP $code in a shape this app does not read.")
        }
    }

    /** Read one launch back. Polling this is how the subscriber watches their system being built. */
    suspend fun status(token: String): LauncherResult<LaunchStatus> {
        val (code, text) = exchange("GET", "/subscribe/api/status/$token", null)
            ?: return LauncherResult.Failed("Can't reach the platform right now.")
        val obj = parseObject(text)
        if (code == 404) return LauncherResult.Failed("That launch is not on the platform's record.")
        if (code !in 200..299 || obj?.get("ok")?.asBoolean != true) {
            return LauncherResult.Failed("The platform answered HTTP $code in a shape this app does not read.")
        }
        val launch = obj.getAsJsonObject("launch") ?: return LauncherResult.Failed("The platform's answer had no launch in it.")
        return LauncherResult.Ok(parseStatus(launch))
    }

    private fun parseStatus(launch: JsonObject): LaunchStatus {
        val steps = launch.getAsJsonArray("steps")?.mapNotNull { el ->
            val o = el.asJsonObject
            LaunchStep(
                key = o.get("key")?.asString ?: "",
                label = o.get("label")?.asString ?: "",
                status = o.get("status")?.asString ?: "",
                error = o.get("error")?.takeIf { !it.isJsonNull }?.asString,
                reasons = o.getAsJsonArray("reasons")?.mapNotNull { it.asString } ?: emptyList()
            )
        } ?: emptyList()
        val conf = launch.get("confirmation")?.takeIf { !it.isJsonNull }?.asJsonObject?.let { c ->
            LaunchConfirmation(
                businessNumber = c.get("business_number")?.asString ?: "",
                crmUrl = c.get("crm_url")?.asString ?: "",
                appToken = c.get("app_token")?.takeIf { !it.isJsonNull }?.asString,
                voice = c.get("voice")?.takeIf { !it.isJsonNull }?.asString,
                texting = c.get("texting")?.takeIf { !it.isJsonNull }?.asString,
                facilitation = c.get("facilitation")?.takeIf { !it.isJsonNull }?.asString,
                admin = c.get("admin")?.takeIf { !it.isJsonNull }?.asString
            )
        }
        return LaunchStatus(
            token = launch.get("token")?.asString ?: "",
            slug = launch.get("slug")?.asString ?: "",
            status = launch.get("status")?.asString ?: "",
            businessName = launch.get("business_name")?.asString ?: "",
            steps = steps,
            confirmation = conf
        )
    }

    private suspend fun exchange(method: String, path: String, body: String?): Pair<Int, String>? {
        val base = platformBaseUrl.trimEnd('/')
        val builder = Request.Builder().url(base + path).header("Accept", "application/json")
        builder.method(method, body?.toRequestBody(json))
        return try {
            withContext(Dispatchers.IO) {
                client.newCall(builder.build()).execute().use { resp -> resp.code to (resp.body?.string() ?: "") }
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun parseObject(text: String): JsonObject? = try {
        gson.fromJson(text, JsonObject::class.java)
    } catch (e: Exception) {
        null
    }

    companion object {
        /**
         * The platform launcher's public door. This is the platform host (where the launcher and,
         * today, the platform's first tenant happen to live) — used ONLY for /subscribe/api calls.
         * A subscriber's office data never comes from here: their app binds to their own VM.
         */
        const val PLATFORM_BASE_URL = "https://136-108-70-191.nip.io"
        const val CONNECT_TIMEOUT_S = 10L
        const val READ_TIMEOUT_S = 20L
        const val CALL_TIMEOUT_S = 30L
    }
}
