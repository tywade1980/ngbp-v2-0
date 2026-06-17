package com.constructionmanager.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Progress events emitted while downloading an APK for the self-hosted update channel. */
sealed interface DownloadProgress {
    /** [percent] is 0..100, or -1 when the total size isn't yet known. */
    data class Running(val percent: Int) : DownloadProgress
    data class Complete(val file: File) : DownloadProgress
    data class Failed(val reason: String) : DownloadProgress
}

/**
 * Downloads an update APK with the system [DownloadManager] and hands it to the platform
 * package installer through a [FileProvider]. No storage permission is required because the
 * APK is written to the app-specific external files directory.
 */
@Singleton
class ApkInstaller @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val downloadManager: DownloadManager
        get() = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    /** On O+ the user must grant "install unknown apps" to this package before we can install. */
    fun canInstallPackages(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }

    /** Intent that opens the system screen where the user grants the install-unknown-apps right. */
    fun unknownSourcesSettingsIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun download(apkUrl: String, versionCode: Int): Flow<DownloadProgress> = flow {
        val dir = File(context.getExternalFilesDir(null), "updates").apply { mkdirs() }
        val dest = File(dir, "constructpro-$versionCode.apk")
        if (dest.exists()) dest.delete()

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("ConstructPro AI update")
            .setDescription("Downloading version $versionCode")
            .setDestinationUri(Uri.fromFile(dest))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val id = downloadManager.enqueue(request)
        while (true) {
            val progress = queryProgress(id, dest)
            emit(progress)
            if (progress is DownloadProgress.Complete || progress is DownloadProgress.Failed) break
            delay(POLL_INTERVAL_MS)
        }
    }

    private fun queryProgress(id: Long, dest: File): DownloadProgress {
        downloadManager.query(DownloadManager.Query().setFilterById(id)).use { cursor ->
            if (cursor == null || !cursor.moveToFirst()) {
                return DownloadProgress.Failed("Download could not be tracked")
            }
            return when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_SUCCESSFUL -> DownloadProgress.Complete(dest)
                DownloadManager.STATUS_FAILED -> {
                    val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                    DownloadProgress.Failed("Download failed (code $reason)")
                }
                else -> {
                    val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val done = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val percent = if (total > 0) ((done * 100) / total).toInt() else -1
                    DownloadProgress.Running(percent)
                }
            }
        }
    }

    /** Launches the platform installer for a previously-downloaded APK. */
    fun install(file: File) {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private companion object {
        const val POLL_INTERVAL_MS = 500L
    }
}
