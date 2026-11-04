package com.constructionmanager.data.caroline

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/*
 * HIS PHONE'S CALL RECORDINGS, TO THE RECORD (Tyler, Nov 3 2026: "she's supposed to get those call transcripts... if she
 * receives a call transcript of all of my telephone calls, she now knows the information that was given in that telephone
 * call and is supposed to extract it as a lead"). The phone's own call recorder writes Recordings/Call (m4a files). This reads that
 * folder through MediaStore, asks the record which files it already has, and sends the rest to POST /api/calls/recordings -
 * the Office transcribes and mines them. Hourly in the background (WorkManager); on demand from the Office tab; "send
 * everything" carries the backlog once. Nothing is deleted or changed on the phone.
 */
object RecordingsSync {

    data class Found(val uri: Uri, val name: String, val size: Long, val addedAt: Long)
    data class Outcome(val found: Int, val sent: Int, val duplicates: Int, val failed: Int, val skipped: Int, val note: String)

    const val WORK = "call-recordings-sync"
    private val json = "application/json".toMediaType()

    fun permission(): String = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, permission()) == PackageManager.PERMISSION_GRANTED

    /** Every audio file the phone filed under Recordings/Call, newest first. */
    fun findRecordings(context: Context): List<Found> {
        if (!hasPermission(context)) return emptyList()
        val out = mutableListOf<Found>()
        val coll = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val proj = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.RELATIVE_PATH)
        val sel = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
        context.contentResolver.query(coll, proj, sel, arrayOf("%Recordings/Call%"), "${MediaStore.Audio.Media.DATE_ADDED} DESC")?.use { c ->
            val iId = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val iName = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val iSize = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val iAdded = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            while (c.moveToNext()) {
                val id = c.getLong(iId)
                out += Found(ContentUris.withAppendedId(coll, id), c.getString(iName) ?: "recording-$id.m4a", c.getLong(iSize), c.getLong(iAdded))
            }
        }
        return out
    }

    /** Send what the record does not have yet. [everything] = the whole backlog; otherwise only the newest [recentLimit]. */
    suspend fun sync(context: Context, everything: Boolean = false, recentLimit: Int = 25): Outcome = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PrefsConnectorSettings.FILE, Context.MODE_PRIVATE)
        val settings = PrefsConnectorSettings(prefs)
        val token = settings.deviceToken ?: return@withContext Outcome(0, 0, 0, 0, 0, "This phone is not paired to Caroline yet.")
        if (!hasPermission(context)) return@withContext Outcome(0, 0, 0, 0, 0, "No permission to read the phone's call recordings yet.")
        val base = settings.baseUrl.trimEnd('/')
        val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).build()
        val all = findRecordings(context)
        if (all.isEmpty()) return@withContext Outcome(0, 0, 0, 0, 0, "The phone has no call recordings in Recordings/Call.")
        // what the record already holds, by file name
        val landed: Set<String> = try {
            client.newCall(Request.Builder().url("$base/calls/recordings/landed").header("Authorization", "Bearer $token").get().build()).execute().use { r ->
                if (!r.isSuccessful) return@withContext Outcome(all.size, 0, 0, 0, 0, "The record did not answer (HTTP ${r.code}).")
                JsonParser.parseString(r.body?.string() ?: "{}").asJsonObject.getAsJsonArray("filenames")?.map { it.asString }?.toSet() ?: emptySet()
            }
        } catch (e: Exception) { return@withContext Outcome(all.size, 0, 0, 0, 0, "No connection to the record: ${e.message ?: e.javaClass.simpleName}") }
        val todo = all.filter { it.name !in landed }.let { if (everything) it else it.take(recentLimit) }
        var sent = 0; var dup = 0; var failed = 0
        val notes = mutableListOf<String>()
        for (f in todo) {
            try {
                val bytes = context.contentResolver.openInputStream(f.uri)?.use { it.readBytes() } ?: continue
                if (bytes.size < 1024) continue
                val url = "$base/calls/recordings?filename=" + Uri.encode(f.name)
                val resp = client.newCall(Request.Builder().url(url).header("Authorization", "Bearer $token").post(bytes.toRequestBody("audio/mp4".toMediaType())).build()).execute()
                val code = resp.code
                val body = resp.use { it.body?.string() ?: "" }
                if (code in 200..299) {
                    val o = runCatching { JsonParser.parseString(body).asJsonObject }.getOrNull()
                    if (o?.get("duplicate")?.asBoolean == true) dup++ else sent++
                } else {
                    failed++
                    if (notes.size < 3) notes += "${f.name}: HTTP $code ${body.take(80)}"
                }
            } catch (e: Exception) {
                failed++
                if (notes.size < 3) notes += "${f.name}: ${e.message ?: e.javaClass.simpleName}"
            }
        }
        Outcome(all.size, sent, dup, failed, maxOf(0, all.size - todo.size), notes.joinToString("; ").ifBlank { if (todo.isEmpty()) "Everything on the phone is already on the record." else "" })
    }

    /** Hourly, new recordings only, when the phone has a network. Idempotent: the record dedupes by content. */
    fun schedule(context: Context) {
        val req = PeriodicWorkRequestBuilder<RecordingsWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP, req)
    }
}

class RecordingsWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        runCatching { CallLogSync.sync(applicationContext) }      // the log first, so each recording lands on its number
        val o = RecordingsSync.sync(applicationContext, everything = false)
        return if (o.failed > 0 && o.sent == 0 && o.duplicates == 0) Result.retry() else Result.success()
    }
}
