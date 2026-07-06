package com.constructionmanager

import android.app.Application
import com.constructionmanager.data.cloud.FirebaseInit
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ConstructionManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Connect to the nextgenbuildpro Firebase project before any Firebase use.
        FirebaseInit.ensureInitialized(this)
    }
}
