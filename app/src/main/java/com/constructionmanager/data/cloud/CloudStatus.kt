package com.constructionmanager.data.cloud

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

/** Reports whether the app is wired to its Firebase project, for display in the UI. */
@Singleton
class CloudStatus @Inject constructor(
    private val app: FirebaseApp,
    private val auth: FirebaseAuth
) {
    val projectId: String get() = app.options.projectId ?: "unknown"
    val connected: Boolean get() = !app.options.projectId.isNullOrBlank()
    val userId: String? get() = auth.currentUser?.uid

    /** Firestore partition for the current user; falls back to the demo workspace when signed out. */
    val workspaceId: String get() = auth.currentUser?.uid ?: DEMO_WORKSPACE

    companion object {
        const val DEMO_WORKSPACE = "demo_user_001"
    }
}
