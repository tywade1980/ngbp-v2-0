package com.constructionmanager.data.cloud

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Firestore-friendly project record (all fields defaulted so Firestore can deserialize it). */
data class CloudProject(
    val id: String = "",
    val name: String = "",
    val status: String = "",
    val budget: Double = 0.0,
    val updatedAt: Long = 0L
)

/**
 * Cloud persistence for projects, backed by Firestore in the nextgenbuildpro project.
 *
 * This is the concrete "data stores correctly in Firebase" path: [pushProject] writes a document,
 * [observeProjects] streams live updates, and [count] powers the dashboard connection indicator.
 * All calls degrade to a [Result] failure (rather than crashing) when offline or denied by rules.
 */
@Singleton
class CloudProjectRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun collection() = firestore.collection(COLLECTION)

    suspend fun pushProject(project: CloudProject): Result<String> = runCatching {
        val doc = if (project.id.isBlank()) collection().document() else collection().document(project.id)
        doc.set(project.copy(id = doc.id, updatedAt = System.currentTimeMillis())).await()
        doc.id
    }

    suspend fun count(): Result<Int> = runCatching {
        collection().get().await().size()
    }

    fun observeProjects(): Flow<List<CloudProject>> = callbackFlow {
        val registration = collection().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { it.toObject(CloudProject::class.java) }.orEmpty()
            trySend(items)
        }
        awaitClose { registration.remove() }
    }

    private companion object {
        const val COLLECTION = "projects"
    }
}
