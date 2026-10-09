package com.constructionmanager.data.cloud

import android.content.Context
import com.constructionmanager.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Connects ConstructPro AI to the existing **nextgenbuildpro** Firebase project.
 *
 * The project's registered Android package (`com.nextgenbuildpro`) differs from this app's
 * applicationId (`com.constructionmanager`), so instead of the google-services Gradle plugin
 * (which matches on package name) we initialize Firebase manually with [FirebaseOptions]. This
 * keeps the integration additive and reversible — no applicationId change, no console edits —
 * while still talking to the real project. Values mirror the project's google-services.json.
 */
object FirebaseInit {

    const val PROJECT_ID = "nextgenbuildpro"

    private const val APP_ID = "1:945923797256:android:178251cead17eb55333cbd"
    private const val SENDER_ID = "945923797256"
    private const val STORAGE_BUCKET = "nextgenbuildpro.firebasestorage.app"

    /**
     * The Web API key is no longer in the source (audit B12: it sat in a public repo). It comes from the build:
     * the Gradle property `firebaseApiKey` or the environment variable FIREBASE_API_KEY (a CI secret), into
     * BuildConfig.FIREBASE_API_KEY. Without it the app still starts; cloud sync, sign-in and photo upload then fail with
     * an error the screens show, and [configured] says why.
     */
    val configured: Boolean get() = FirebaseKey.usable(BuildConfig.FIREBASE_API_KEY)

    /** Idempotently creates the default [FirebaseApp]. Safe to call more than once. */
    fun ensureInitialized(context: Context): FirebaseApp {
        FirebaseApp.getApps(context).firstOrNull()?.let { return it }
        val options = FirebaseOptions.Builder()
            .setProjectId(PROJECT_ID)
            .setApplicationId(APP_ID)
            .setApiKey(FirebaseKey.orPlaceholder(BuildConfig.FIREBASE_API_KEY))
            .setGcmSenderId(SENDER_ID)
            .setStorageBucket(STORAGE_BUCKET)
            .build()
        return FirebaseApp.initializeApp(context.applicationContext, options)
    }
}

/** Kept apart from Android so a JVM test can check it. */
object FirebaseKey {
    /** Firebase refuses an empty key at start-up; this placeholder starts the app and makes every Firebase call fail. */
    const val NOT_CONFIGURED = "not-configured-in-this-build"

    fun usable(key: String?): Boolean = !key.isNullOrBlank() && key != NOT_CONFIGURED

    fun orPlaceholder(key: String?): String = if (usable(key)) key!!.trim() else NOT_CONFIGURED
}
