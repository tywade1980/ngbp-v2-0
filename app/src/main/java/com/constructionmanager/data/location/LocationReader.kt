package com.constructionmanager.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/** One reading of where the phone is. [atMillis] is the fix's own time, never the time it was asked for. */
data class LocationFix(
    val lat: Double,
    val lng: Double,
    val accuracyM: Float?,
    val atMillis: Long,
    val provider: String?
)

/**
 * The phone's own location, read with the framework only (no Play services): the permission is the app's,
 * requested the Android way by the screen; a live fix is tried first and the freshest last-known fix is the
 * fallback, so a slow GPS never reads as "no location" when the phone already knows where it is.
 */
object LocationReader {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun current(context: Context, timeoutMs: Long = 15_000): LocationFix? {
        if (!hasPermission(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val live = withTimeoutOrNull(timeoutMs) { requestOne(lm) }
        if (live != null) return live
        val last = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .mapNotNull { p -> runCatching { lm.getLastKnownLocation(p) }.getOrNull() }
            .maxByOrNull { it.time }
        return last?.toFix()
    }

    private fun Location.toFix(): LocationFix =
        LocationFix(latitude, longitude, if (hasAccuracy()) accuracy else null, time, provider)

    @SuppressLint("MissingPermission")
    private suspend fun requestOne(lm: LocationManager): LocationFix? = suspendCancellableCoroutine { cont ->
        val provider = when {
            runCatching { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) -> LocationManager.GPS_PROVIDER
            runCatching { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
        if (provider == null) {
            if (cont.isActive) cont.resume(null)
            return@suspendCancellableCoroutine
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val exec = Executors.newSingleThreadExecutor()
            try {
                lm.getCurrentLocation(provider, null, exec) { loc ->
                    exec.shutdown()
                    if (cont.isActive) cont.resume(loc?.toFix())
                }
            } catch (e: Exception) {
                exec.shutdown()
                if (cont.isActive) cont.resume(null)
            }
            cont.invokeOnCancellation { runCatching { exec.shutdownNow() } }
        } else {
            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    runCatching { lm.removeUpdates(this) }
                    if (cont.isActive) cont.resume(loc.toFix())
                }
                override fun onStatusChanged(p: String?, s: Int, extras: Bundle?) {}
                override fun onProviderEnabled(p: String) {}
                override fun onProviderDisabled(p: String) {}
            }
            try {
                lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            } catch (e: Exception) {
                if (cont.isActive) cont.resume(null)
            }
            cont.invokeOnCancellation { runCatching { lm.removeUpdates(listener) } }
        }
    }
}
