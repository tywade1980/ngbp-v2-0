package com.constructionmanager.data.caroline

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/*
 * HIS PHONE'S CALL LOG, TO THE RECORD (Tyler, Nov 4 2026: "every call should have a number unless marked blocked"). The phone's
 * call recorder names a file by the caller-ID label ("Spam Risk", "WIRELESS CALLER"), not the number; the phone's own call log has
 * the number, the direction and the length for the same minute. This reads the call log (READ_CALL_LOG), asks the record where it
 * left off, and sends what is newer to POST /api/calls/log - the record lands each call once and gives every waiting recording its
 * number from the log entry it sits on. Runs before the recordings in the hourly worker and on demand from the Office tab.
 * Nothing is changed on the phone.
 */
object CallLogSync {

    data class Outcome(val found: Int, val landed: Int, val duplicates: Int, val matched: Int, val note: String)

    private val json = "application/json".toMediaType()

    fun permission(): String = Manifest.permission.READ_CALL_LOG

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, permission()) == PackageManager.PERMISSION_GRANTED

    private fun typeWord(t: Int): String? = when (t) {
        CallLog.Calls.INCOMING_TYPE -> "incoming"
        CallLog.Calls.OUTGOING_TYPE -> "outgoing"
        CallLog.Calls.MISSED_TYPE -> "missed"
        CallLog.Calls.VOICEMAIL_TYPE -> "voicemail"
        CallLog.Calls.REJECTED_TYPE -> "rejected"
        CallLog.Calls.BLOCKED_TYPE -> "blocked"
        CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> "answered_externally"
        else -> null
    }

    /** The phone's calls newer than [sinceMs] (all of them when 0), oldest first, as the record's rows. */
    fun findCalls(context: Context, sinceMs: Long, limit: Int = 2000): JsonArray {
        val out = JsonArray()
        if (!hasPermission(context)) return out
        val proj = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION)
        val sel = if (sinceMs > 0) "${CallLog.Calls.DATE} > ?" else null
        val args = if (sinceMs > 0) arrayOf(sinceMs.toString()) else null
        context.contentResolver.query(CallLog.Calls.CONTENT_URI, proj, sel, args, "${CallLog.Calls.DATE} ASC")?.use { c ->
            val iNum = c.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val iName = c.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
            val iType = c.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val iDate = c.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val iDur = c.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            while (c.moveToNext() && out.size() < limit) {
                val type = typeWord(c.getInt(iType)) ?: continue
                out.add(JsonObject().apply {
                    addProperty("number", c.getString(iNum) ?: "")
                    addProperty("name", c.getString(iName) ?: "")
                    addProperty("type", type)
                    addProperty("started_ms", c.getLong(iDate))
                    addProperty("duration_s", c.getLong(iDur))
                })
            }
        }
        return out
    }

    /** Send what is newer than the record's newest phone-log call. [everything] sends the whole log (the record dedupes by id). */
    suspend fun sync(context: Context, everything: Boolean = false): Outcome = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PrefsConnectorSettings.FILE, Context.MODE_PRIVATE)
        val settings = PrefsConnectorSettings(prefs)
        val token = settings.deviceToken ?: return@withContext Outcome(0, 0, 0, 0, "This phone is not paired to Caroline yet.")
        if (!hasPermission(context)) return@withContext Outcome(0, 0, 0, 0, "No permission to read the phone's call log yet.")
        val base = settings.baseUrl.trimEnd('/')
        val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).build()
        val since: Long = if (everything) 0L else try {
            client.newCall(Request.Builder().url("$base/calls/log/latest").header("Authorization", "Bearer $token").get().build()).execute().use { r ->
                if (!r.isSuccessful) return@withContext Outcome(0, 0, 0, 0, "The record did not answer (HTTP ${r.code}).")
                JsonParser.parseString(r.body?.string() ?: "{}").asJsonObject.get("latest_started_ms")?.asLong ?: 0L
            }
        } catch (e: Exception) { return@withContext Outcome(0, 0, 0, 0, "No connection to the record: ${e.message ?: e.javaClass.simpleName}") }
        // a minute of overlap so a call that started before the newest one but was logged after it is not skipped
        val rows = findCalls(context, if (since > 60_000) since - 60_000 else 0L)
        if (rows.size() == 0) return@withContext Outcome(0, 0, 0, 0, "The record already has every call on this phone.")
        var landed = 0; var dup = 0; var matched = 0
        var i = 0
        while (i < rows.size()) {
            val batch = JsonArray().also { b -> for (j in i until minOf(i + 500, rows.size())) b.add(rows.get(j)) }
            i += 500
            val body = JsonObject().apply { add("calls", batch) }
            try {
                client.newCall(Request.Builder().url("$base/calls/log").header("Authorization", "Bearer $token").post(body.toString().toRequestBody(json)).build()).execute().use { r ->
                    val text = r.body?.string() ?: ""
                    if (!r.isSuccessful) return@withContext Outcome(rows.size(), landed, dup, matched, "The record refused a batch (HTTP ${r.code} ${text.take(80)}).")
                    val o = JsonParser.parseString(text).asJsonObject
                    landed += o.get("landed")?.asInt ?: 0; dup += o.get("duplicates")?.asInt ?: 0; matched += o.get("matched")?.asInt ?: 0
                }
            } catch (e: Exception) {
                return@withContext Outcome(rows.size(), landed, dup, matched, "Lost the connection part way: ${e.message ?: e.javaClass.simpleName}")
            }
        }
        Outcome(rows.size(), landed, dup, matched, "")
    }
}
