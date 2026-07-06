package com.constructionmanager.data.cloud

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reusable, schema-light cloud mirror for user-created records. Each item is written to
 * `users/{workspaceId}/{collection}/{id}` so data is partitioned per signed-in user (or the demo
 * workspace). Calls return a [Result] and never throw, so a write failing offline or under strict
 * security rules never blocks the local-first write that already succeeded.
 */
@Singleton
class CloudMirror @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cloudStatus: CloudStatus
) {
    private fun collection(name: String) =
        firestore.collection("users").document(cloudStatus.workspaceId).collection(name)

    suspend fun push(collection: String, id: String, data: Map<String, Any?>): Result<Unit> = runCatching {
        collection(collection)
            .document(id)
            .set(data + ("updatedAt" to System.currentTimeMillis()))
            .await()
    }

    suspend fun count(collection: String): Result<Int> = runCatching {
        collection(collection).get().await().size()
    }
}
