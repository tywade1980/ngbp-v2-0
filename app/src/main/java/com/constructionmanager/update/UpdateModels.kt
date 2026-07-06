package com.constructionmanager.update

import com.google.gson.annotations.SerializedName

/**
 * Over-the-air (OTA) update model.
 *
 * ConstructPro AI is a native Kotlin app, so it cannot use Expo/React-Native style JS-bundle
 * OTA. Instead it ships with two real native update paths:
 *  - **Google Play In-App Updates** for Play-distributed installs.
 *  - A **self-hosted APK channel** (this manifest) for sideloaded / internal distribution, which
 *    is the relevant path for an internal pro tool that already talks to configurable backends.
 */

/** Remote update descriptor published to the self-hosted OTA channel as JSON. */
data class UpdateManifest(
    @SerializedName("versionCode") val versionCode: Int = 0,
    @SerializedName("versionName") val versionName: String = "",
    @SerializedName("apkUrl") val apkUrl: String = "",
    @SerializedName("releaseNotes") val releaseNotes: String = "",
    @SerializedName("mandatory") val mandatory: Boolean = false,
    /** Installs older than this are forced to update (treated as mandatory). */
    @SerializedName("minSupportedVersionCode") val minSupportedVersionCode: Int = 0,
    @SerializedName("sizeBytes") val sizeBytes: Long = 0L,
    @SerializedName("sha256") val sha256: String? = null
)

/** Where an available update originated. */
enum class UpdateSource { PLAY, SELF_HOSTED }

/** A concrete, actionable update resolved from one of the channels. */
data class AvailableUpdate(
    val versionCode: Int,
    val versionName: String,
    val releaseNotes: String,
    val apkUrl: String,
    val mandatory: Boolean,
    val sizeBytes: Long,
    val source: UpdateSource
)

/** UI-facing state machine covering the whole update lifecycle. */
sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data class UpToDate(val versionName: String, val versionCode: Int) : UpdateStatus
    data class Available(val update: AvailableUpdate) : UpdateStatus

    /** [percent] is 0..100, or -1 when the total size is unknown. */
    data class Downloading(val percent: Int) : UpdateStatus
    data class ReadyToInstall(val update: AvailableUpdate) : UpdateStatus
    data class Failed(val message: String) : UpdateStatus
}
