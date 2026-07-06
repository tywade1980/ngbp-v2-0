package com.constructionmanager.update

import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper over Google Play In-App Updates.
 *
 * When the app was not installed from Play (e.g. a sideloaded internal build) the Play APIs
 * simply report no update / throw, and callers fall back to the self-hosted channel.
 */
@Singleton
class PlayUpdateManager @Inject constructor(
    @ApplicationContext context: Context
) {
    val manager = AppUpdateManagerFactory.create(context)

    /** Returns Play [AppUpdateInfo] when a flexible update is available, otherwise null. */
    suspend fun checkForUpdate(): AppUpdateInfo? = try {
        val info = manager.requestAppUpdateInfo()
        if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        ) {
            info
        } else {
            null
        }
    } catch (e: Exception) {
        Log.d(TAG, "Play update check unavailable (likely not a Play install): ${e.message}")
        null
    }

    private companion object {
        const val TAG = "PlayUpdateManager"
    }
}
