package com.constructionmanager

import android.app.Application
import com.constructionmanager.data.caroline.RecordingsSync
import com.constructionmanager.data.cloud.FirebaseInit
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ConstructionManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Connect to the nextgenbuildpro Firebase project before any Firebase use.
        FirebaseInit.ensureInitialized(this)
        // his phone's call recordings to the record, hourly (Tyler, Nov 3 2026); runs only once the phone is paired and allowed to read them
        runCatching { RecordingsSync.schedule(this) }
    }
}
