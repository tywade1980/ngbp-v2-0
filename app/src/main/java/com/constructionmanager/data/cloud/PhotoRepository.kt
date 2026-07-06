package com.constructionmanager.data.cloud

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads documentation photos to Firebase Storage under
 * `users/{workspaceId}/projects/{projectId}/photos/{id}.jpg` and records the resulting download
 * URL + metadata in Firestore (via [CloudMirror]). Returns a [Result] so the UI can show per-photo
 * upload status without crashing when offline or storage rules deny the write.
 */
@Singleton
class PhotoRepository @Inject constructor(
    private val storage: FirebaseStorage,
    private val cloudStatus: CloudStatus,
    private val cloudMirror: CloudMirror
) {
    suspend fun upload(localUri: Uri, projectId: String, category: String): Result<String> = runCatching {
        val id = "photo_${System.currentTimeMillis()}"
        val ref = storage.reference.child(
            "users/${cloudStatus.workspaceId}/projects/$projectId/photos/$id.jpg"
        )
        ref.putFile(localUri).await()
        val url = ref.downloadUrl.await().toString()
        cloudMirror.push(
            collection = "photos",
            id = id,
            data = mapOf(
                "projectId" to projectId,
                "category" to category,
                "url" to url
            )
        )
        url
    }
}
