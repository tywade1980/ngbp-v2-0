package com.constructionmanager.data.cloud

import com.constructionmanager.domain.model.ConstructionPhase
import com.constructionmanager.domain.model.Material
import com.constructionmanager.domain.model.MaterialCategory
import com.constructionmanager.domain.model.Project
import com.constructionmanager.domain.model.ProjectStatus
import com.constructionmanager.domain.model.ProjectType
import com.constructionmanager.domain.model.SkillLevel
import com.constructionmanager.domain.model.TradeType
import com.constructionmanager.domain.model.Worker
import com.constructionmanager.domain.repository.MaterialRepository
import com.constructionmanager.domain.repository.ProjectRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Two-way Firestore sync for the user's data, partitioned under
 * `users/{workspaceId}/{collection}`.
 *
 * Writes store the **full** record (not a summary) so data round-trips losslessly; reads upsert
 * cloud records into the local Room store (REPLACE), so in-app data survives reinstall and shows
 * up on other devices. Firestore's own on-device cache covers short-term/offline reads; Room is the
 * fast local source the UI observes. Everything is [Result]-based and never throws.
 */
@Singleton
class CloudSync @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cloudStatus: CloudStatus,
    private val projectRepository: ProjectRepository,
    private val materialRepository: MaterialRepository
) {
    private fun col(name: String) =
        firestore.collection("users").document(cloudStatus.workspaceId).collection(name)

    // ---- writes (full fidelity) ---------------------------------------------

    suspend fun pushProject(project: Project): Result<Unit> = runCatching {
        col("projects").document(project.id).set(CloudCodec.projectToMap(project)).await()
    }

    suspend fun pushMaterial(material: Material): Result<Unit> = runCatching {
        col("materials").document(material.id).set(CloudCodec.materialToMap(material)).await()
    }

    suspend fun pushWorker(worker: Worker): Result<Unit> = runCatching {
        col("workers").document(worker.id).set(CloudCodec.workerToMap(worker)).await()
    }

    // ---- reads (upsert into local store) ------------------------------------

    /** Pulls cloud projects into Room (REPLACE upsert). Returns how many were synced. */
    suspend fun pullProjects(): Result<Int> = runCatching {
        var count = 0
        col("projects").get().await().documents.forEach { doc ->
            doc.data?.let { CloudCodec.mapToProject(it) }?.let {
                projectRepository.insertProject(it)
                count++
            }
        }
        count
    }

    suspend fun pullMaterials(): Result<Int> = runCatching {
        var count = 0
        col("materials").get().await().documents.forEach { doc ->
            doc.data?.let { CloudCodec.mapToMaterial(it) }?.let {
                materialRepository.insertMaterial(it)
                count++
            }
        }
        count
    }

    /** Workers have no local table, so they're returned for the Labor screen to merge in. */
    suspend fun pullWorkers(): Result<List<Worker>> = runCatching {
        col("workers").get().await().documents.mapNotNull { doc ->
            doc.data?.let { CloudCodec.mapToWorker(it) }
        }
    }
}

/** Pure, tolerant conversions between domain models and Firestore maps. */
object CloudCodec {

    fun projectToMap(p: Project): Map<String, Any?> = mapOf(
        "id" to p.id,
        "name" to p.name,
        "address" to p.address,
        "city" to p.city,
        "state" to p.state,
        "zipCode" to p.zipCode,
        "clientName" to p.clientName,
        "clientEmail" to p.clientEmail,
        "clientPhone" to p.clientPhone,
        "projectType" to p.projectType.name,
        "currentPhase" to p.currentPhase.name,
        "startDate" to p.startDate.toString(),
        "estimatedEndDate" to p.estimatedEndDate.toString(),
        "actualEndDate" to p.actualEndDate?.toString(),
        "totalBudget" to p.totalBudget.toString(),
        "currentCost" to p.currentCost.toString(),
        "status" to p.status.name,
        "notes" to p.notes,
        "createdAt" to p.createdAt.toString(),
        "updatedAt" to p.updatedAt.toString(),
        "updatedAtMs" to System.currentTimeMillis()
    )

    fun mapToProject(m: Map<String, Any?>): Project? {
        val id = m["id"] as? String ?: return null
        val name = m["name"] as? String ?: return null
        val now = nowDateTime()
        val today = todayDate()
        return Project(
            id = id,
            name = name,
            address = str(m, "address"),
            city = str(m, "city"),
            state = str(m, "state"),
            zipCode = str(m, "zipCode"),
            clientName = str(m, "clientName"),
            clientEmail = str(m, "clientEmail"),
            clientPhone = str(m, "clientPhone"),
            projectType = enumOr(m["projectType"] as? String, ProjectType.RESIDENTIAL),
            currentPhase = enumOr(m["currentPhase"] as? String, ConstructionPhase.PRE_CONSTRUCTION),
            startDate = parseDate(m["startDate"] as? String) ?: today,
            estimatedEndDate = parseDate(m["estimatedEndDate"] as? String) ?: today,
            actualEndDate = parseDate(m["actualEndDate"] as? String),
            totalBudget = bigDecimal(m["totalBudget"]) ?: bigDecimal(m["budget"]) ?: BigDecimal.ZERO,
            currentCost = bigDecimal(m["currentCost"]) ?: BigDecimal.ZERO,
            status = enumOr(m["status"] as? String, ProjectStatus.PLANNING),
            notes = str(m, "notes"),
            createdAt = parseDateTime(m["createdAt"] as? String) ?: now,
            updatedAt = parseDateTime(m["updatedAt"] as? String) ?: now
        )
    }

    fun materialToMap(mat: Material): Map<String, Any?> = mapOf(
        "id" to mat.id,
        "name" to mat.name,
        "category" to mat.category.name,
        "subcategory" to mat.subcategory,
        "unitOfMeasurement" to mat.unitOfMeasurement,
        "currentPrice" to mat.currentPrice.toString(),
        "supplier" to mat.supplier,
        "supplierSku" to mat.supplierSku,
        "description" to mat.description,
        "lastPriceUpdate" to mat.lastPriceUpdate.toString(),
        "updatedAtMs" to System.currentTimeMillis()
    )

    fun mapToMaterial(m: Map<String, Any?>): Material? {
        val id = m["id"] as? String ?: return null
        val name = m["name"] as? String ?: return null
        return Material(
            id = id,
            name = name,
            category = enumOr(m["category"] as? String, MaterialCategory.OTHER),
            subcategory = str(m, "subcategory"),
            unitOfMeasurement = (m["unitOfMeasurement"] as? String)?.ifBlank { "each" } ?: "each",
            currentPrice = bigDecimal(m["currentPrice"]) ?: bigDecimal(m["price"]) ?: BigDecimal.ZERO,
            supplier = str(m, "supplier"),
            supplierSku = m["supplierSku"] as? String,
            description = str(m, "description"),
            lastPriceUpdate = parseDateTime(m["lastPriceUpdate"] as? String) ?: nowDateTime()
        )
    }

    fun workerToMap(w: Worker): Map<String, Any?> = mapOf(
        "id" to w.id,
        "firstName" to w.firstName,
        "lastName" to w.lastName,
        "email" to w.email,
        "phone" to w.phone,
        "trade" to (w.tradeTypes.firstOrNull()?.name ?: TradeType.GENERAL_LABOR.name),
        "tradeTypes" to w.tradeTypes.map { it.name },
        "skillLevel" to w.skillLevel.name,
        "hourlyRate" to w.hourlyRate.toString(),
        "hireDate" to w.hireDate.toString(),
        "updatedAtMs" to System.currentTimeMillis()
    )

    fun mapToWorker(m: Map<String, Any?>): Worker? {
        val id = m["id"] as? String ?: return null
        @Suppress("UNCHECKED_CAST")
        val tradeNames = (m["tradeTypes"] as? List<String>)
            ?: listOfNotNull(m["trade"] as? String)
        val trades = tradeNames.mapNotNull { runCatching { TradeType.valueOf(it) }.getOrNull() }
            .ifEmpty { listOf(TradeType.GENERAL_LABOR) }
        return Worker(
            id = id,
            firstName = str(m, "firstName"),
            lastName = str(m, "lastName"),
            email = str(m, "email"),
            phone = str(m, "phone"),
            tradeTypes = trades,
            skillLevel = enumOr(m["skillLevel"] as? String, SkillLevel.JOURNEYMAN),
            certifications = emptyList(),
            hourlyRate = bigDecimal(m["hourlyRate"]) ?: BigDecimal.ZERO,
            hireDate = parseDate(m["hireDate"] as? String) ?: todayDate()
        )
    }

    // ---- helpers -------------------------------------------------------------

    private fun str(m: Map<String, Any?>, key: String): String = m[key] as? String ?: ""

    private inline fun <reified T : Enum<T>> enumOr(name: String?, default: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default

    private fun parseDate(s: String?): LocalDate? =
        s?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    private fun parseDateTime(s: String?): LocalDateTime? =
        s?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

    private fun bigDecimal(v: Any?): BigDecimal? = when (v) {
        is String -> v.toBigDecimalOrNull()
        is Number -> runCatching { BigDecimal.valueOf(v.toDouble()) }.getOrNull()
        else -> null
    }

    private fun nowDateTime(): LocalDateTime =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private fun todayDate(): LocalDate =
        Clock.System.todayIn(TimeZone.currentSystemDefault())
}
