package com.constructionmanager.data.cloud

import android.content.Context
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
    private const val API_KEY = "AIzaSyA6Chd4wCiD3ID7FLbefWrwoMaSxVRXQHA"
    private const val SENDER_ID = "945923797256"
    private const val STORAGE_BUCKET = "nextgenbuildpro.firebasestorage.app"

    /** Idempotently creates the default [FirebaseApp]. Safe to call more than once. */
    fun ensureInitialized(context: Context): FirebaseApp {
        FirebaseApp.getApps(context).firstOrNull()?.let { return it }
        val options = FirebaseOptions.Builder()
            .setProjectId(PROJECT_ID)
            .setApplicationId(APP_ID)
            .setApiKey(API_KEY)
            .setGcmSenderId(SENDER_ID)
            .setStorageBucket(STORAGE_BUCKET)
            .build()
        return FirebaseApp.initializeApp(context.applicationContext, options)
    }
}
