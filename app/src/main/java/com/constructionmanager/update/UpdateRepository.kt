package com.constructionmanager.update

import android.content.Intent
import android.util.Log
import com.constructionmanager.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point for the update lifecycle. It resolves the best available update across the
 * Play and self-hosted channels, drives the APK download for the self-hosted path, and exposes a
 * single [status] stream the UI observes.
 */
@Singleton
class UpdateRepository @Inject constructor(
    private val config: UpdateConfig,
    private val installer: ApkInstaller,
    private val playUpdateManager: PlayUpdateManager,
    okHttpClient: OkHttpClient
) {
    // The manifest is fetched via an absolute @Url, so this base URL is only a Retrofit formality.
    private val api: UpdateApiService = Retrofit.Builder()
        .baseUrl("https://updates.invalid/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UpdateApiService::class.java)

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    val currentVersionName: String = BuildConfig.VERSION_NAME
    val currentVersionCode: Int = BuildConfig.VERSION_CODE

    private var lastDownloaded: File? = null

    /**
     * Checks both channels and updates [status]. Returns the available update, or null when the
     * app is up to date / the check failed. Play is consulted first when [UpdateConfig.preferPlayStore].
     */
    suspend fun check(): AvailableUpdate? {
        _status.value = UpdateStatus.Checking

        if (config.preferPlayStore) {
            val playInfo = playUpdateManager.checkForUpdate()
            if (playInfo != null) {
                val update = AvailableUpdate(
                    versionCode = playInfo.availableVersionCode(),
                    versionName = "Play update",
                    releaseNotes = "A newer version is available on Google Play.",
                    apkUrl = "",
                    mandatory = false,
                    sizeBytes = playInfo.totalBytesToDownload(),
                    source = UpdateSource.PLAY
                )
                _status.value = UpdateStatus.Available(update)
                return update
            }
        }

        return try {
            val manifest = api.fetchManifest(config.manifestUrl)
            val isNewer = manifest.versionCode > currentVersionCode && manifest.apkUrl.isNotBlank()
            if (isNewer) {
                val update = AvailableUpdate(
                    versionCode = manifest.versionCode,
                    versionName = manifest.versionName.ifBlank { manifest.versionCode.toString() },
                    releaseNotes = manifest.releaseNotes,
                    apkUrl = manifest.apkUrl,
                    mandatory = manifest.mandatory || currentVersionCode < manifest.minSupportedVersionCode,
                    sizeBytes = manifest.sizeBytes,
                    source = UpdateSource.SELF_HOSTED
                )
                _status.value = UpdateStatus.Available(update)
                update
            } else {
                _status.value = UpdateStatus.UpToDate(currentVersionName, currentVersionCode)
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Self-hosted update check failed", e)
            _status.value = UpdateStatus.Failed("Couldn't reach the update channel: ${e.message}")
            null
        }
    }

    /** Downloads the APK for a self-hosted update, streaming progress into [status]. */
    suspend fun download(update: AvailableUpdate) {
        if (update.source != UpdateSource.SELF_HOSTED || update.apkUrl.isBlank()) return
        _status.value = UpdateStatus.Downloading(-1)
        installer.download(update.apkUrl, update.versionCode).collect { progress ->
            _status.value = when (progress) {
                is DownloadProgress.Running -> UpdateStatus.Downloading(progress.percent)
                is DownloadProgress.Complete -> {
                    lastDownloaded = progress.file
                    UpdateStatus.ReadyToInstall(update)
                }
                is DownloadProgress.Failed -> UpdateStatus.Failed(progress.reason)
            }
        }
    }

    /** Launches the platform installer for the APK downloaded by [download]. */
    fun installDownloaded(): Boolean {
        val file = lastDownloaded ?: return false
        installer.install(file)
        return true
    }

    fun canInstallPackages(): Boolean = installer.canInstallPackages()

    fun unknownSourcesIntent(): Intent = installer.unknownSourcesSettingsIntent()

    fun skip(update: AvailableUpdate) {
        config.skippedVersionCode = update.versionCode
        _status.value = UpdateStatus.Idle
    }

    private companion object {
        const val TAG = "UpdateRepository"
    }
}
