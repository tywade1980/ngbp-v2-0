package com.constructionmanager.data.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.constructionmanager.data.caroline.CallLogSync
import com.constructionmanager.data.caroline.PrefsConnectorSettings
import com.constructionmanager.data.caroline.RecordingsSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/*
 * WHERE HE IS, WITHOUT OPENING THE APP (Tyler, Nov 6 2026: "We need location permission because my location data. She's supposed
 * to have my location data."). Until 0.7.3 the phone sent a fix only while Home or Site was open on screen, so the record went dark
 * whenever the app was closed (Nov 2: 5 fixes, Nov 3: 5, nothing after Nov 4 7:47 PM). This sends one fix every 15 minutes (the
 * shortest period Android's WorkManager allows) to the same door the screen uses (POST owner/location), and rides the same tick to
 * carry the call log and new call recordings, whose hourly job had stopped firing on his phone.
 *
 * Android's rules, kept: background location is its own grant ("Allow all the time"), asked only after the foreground grant, on
 * Android 11+ in the system's own settings screen - the app can open that screen but the choice is his tap. Nothing is sent without
 * the grant; a phone that is not paired sends nothing.
 */
object LocationSync {

    const val WORK = "owner-location-sync"
    private val json = "application/json".toMediaType()

    fun hasForeground(context: Context): Boolean = LocationReader.hasPermission(context)

    fun hasBackground(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** True when Android will not put the app to sleep (Samsung's "Sleeping apps" / battery optimization would stop the 15-minute job). */
    fun ignoresBatteryLimits(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /** Android's own screen for "Allow all the time" is the app's permission page; this opens the app's settings page that leads to it. */
    fun appSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Android's own question: let this app run unrestricted in the background. */
    fun batteryIntent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + context.packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    data class Outcome(val sent: Boolean, val note: String)

    /** One fix to the record now. */
    suspend fun sendOnce(context: Context): Outcome = withContext(Dispatchers.IO) {
        val settings = PrefsConnectorSettings(context.getSharedPreferences(PrefsConnectorSettings.FILE, Context.MODE_PRIVATE))
        val token = settings.deviceToken ?: return@withContext Outcome(false, "This phone is not paired to Caroline yet.")
        if (!hasForeground(context)) return@withContext Outcome(false, "Location permission is not granted.")
        val fix = LocationReader.current(context, timeoutMs = 20_000) ?: return@withContext Outcome(false, "The phone gave no location. Check that Location is on.")
        val body = JSONObject().apply {
            put("lat", fix.lat); put("lng", fix.lng)
            fix.accuracyM?.let { put("accuracy_m", it.toDouble()) }
            put("at", java.time.Instant.ofEpochMilli(fix.atMillis).toString())
            put("id", java.util.UUID.randomUUID().toString())
            put("label", "HOCC app (background)")
        }
        val client = OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
        try {
            client.newCall(
                Request.Builder().url(settings.baseUrl.trimEnd('/') + "/owner/location")
                    .header("Authorization", "Bearer $token").post(body.toString().toRequestBody(json)).build()
            ).execute().use { r ->
                if (r.isSuccessful) Outcome(true, "") else Outcome(false, "The record answered HTTP ${r.code}.")
            }
        } catch (e: Exception) {
            Outcome(false, "No connection to the record: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    /** Every 15 minutes while the phone has a network. KEEP: one job, however often the app starts. */
    fun schedule(context: Context) {
        val req = PeriodicWorkRequestBuilder<LocationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP, req)
    }
}

class LocationWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val loc = LocationSync.sendOnce(applicationContext)
        // the same tick carries his calls: the log first so each recording lands on its number (both skip without their grant)
        runCatching { CallLogSync.sync(applicationContext) }
        runCatching { RecordingsSync.sync(applicationContext, everything = false) }
        return if (!loc.sent && loc.note.startsWith("No connection")) Result.retry() else Result.success()
    }
}
