package com.constructionmanager.ui.screens.hocc

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

/**
 * Where the Office's newest build is saved before it is installed, and the hand-off to Android's installer.
 *
 * Nov 7 2026: Tyler's "the in app update doesnt work". Since 0.5.0 the build was saved straight into cacheDir, a folder
 * res/xml/file_paths.xml never shared, so "Install it now" asked FileProvider for a path it has no root for and the
 * install never opened. The build now lives in cacheDir/updates/, which file_paths.xml shares as cache-path "hocc_updates".
 */
object UpdateInstall {
    /** The folder under cacheDir that file_paths.xml shares (cache-path name="hocc_updates" path="updates/"). */
    const val DIR = "updates"
    const val FILE = "hocc-update.apk"

    fun apkFile(context: Context): File = File(File(context.cacheDir, DIR).apply { mkdirs() }, FILE)

    /**
     * Opens Android's installer on [file]. Returns null when the installer opened, or the words to show when it did not:
     * the "install unknown apps" switch is off for HOCC (its settings screen is opened for him), or the installer
     * could not be reached. Never throws, so a failed hand-off is a line on the card, not a crash.
     */
    fun install(context: Context, file: File): String? {
        if (!file.exists()) return "The downloaded build is gone from this phone. Tap Download & install again."
        if (!context.packageManager.canRequestPackageInstalls()) {
            return try {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + context.packageName))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                "Turn on \"Allow from this source\" for HOCC, then come back and tap Install it now."
            } catch (e: ActivityNotFoundException) {
                "Android won't let HOCC install updates yet. In Settings, allow HOCC to install unknown apps, then tap Install it now."
            }
        }
        return try {
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            context.startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            null
        } catch (e: IllegalArgumentException) {
            "The installer could not open the downloaded build (" + (e.message ?: "no file root") + ")."
        } catch (e: ActivityNotFoundException) {
            "This phone has no app installer to open the downloaded build."
        }
    }
}
