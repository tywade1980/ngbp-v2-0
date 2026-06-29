package com.constructionmanager.ai

import com.constructionmanager.data.cloud.CloudSync
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device agent that gives the assistant *control* of the app, not just conversation.
 *
 * It maps natural-language requests ("create a project called Maple Kitchen for the Reyes
 * family, budget $48k", "add a worker named Sam, electrician at $45/hr", "how many projects
 * do I have?") to concrete actions against the real repositories — writing to the local
 * database and mirroring to Firestore exactly like the on-screen buttons do.
 *
 * [tryHandle] returns a confirmation when it recognises and performs an action, or null when
 * the message is conversational — in which case the caller defers to the LLM orchestrator (or
 * the [OfflineAssistant]) for reasoning. So the agent handles "do X" and the LLM handles "what
 * about X?", and both work together.
 */
@Singleton
class ConstructionAgent @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val materialRepository: MaterialRepository,
    private val cloudSync: CloudSync
) {
    data class Outcome(val text: String)

    suspend fun tryHandle(message: String): Outcome? = withContext(Dispatchers.IO) {
        val m = message.lowercase()
        when {
            isCreate(m) && m.contains("project") -> createProject(message)
            isCreate(m) && m.contains("material") -> addMaterial(message)
            isCreate(m) && mentionsWorker(m) -> addWorker(message)
            mentionsList(m) && m.contains("project") -> listProjects()
            m.contains("budget") && m.contains("project") -> budgetSummary()
            else -> null
        }
    }

    private fun isCreate(m: String) = CREATE_VERBS.any { m.contains(it) }
    private fun mentionsWorker(m: String) =
        listOf("worker", "laborer", "employee", "crew member", "tradesman").any { m.contains(it) }
    private fun mentionsList(m: String) =
        listOf("how many", "list", "show me", "what projects", "which projects").any { m.contains(it) }

    private suspend fun createProject(message: String): Outcome {
        val name = extractName(message) ?: "New Project"
        val client = extractClient(message)
        val budget = extractMoney(message) ?: BigDecimal.ZERO
        val type = detectProjectType(message.lowercase())

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val project = Project(
            id = "proj_${System.currentTimeMillis()}",
            name = name,
            address = "", city = "", state = "", zipCode = "",
            clientName = client.orEmpty(),
            clientEmail = "", clientPhone = "",
            projectType = type,
            currentPhase = ConstructionPhase.PRE_CONSTRUCTION,
            startDate = today,
            estimatedEndDate = today.plus(90, DateTimeUnit.DAY),
            totalBudget = budget,
            currentCost = BigDecimal.ZERO,
            status = ProjectStatus.PLANNING,
            notes = "Created by Caroline (AI agent).",
            createdAt = now,
            updatedAt = now
        )
        projectRepository.insertProject(project)
        cloudSync.pushProject(project)

        val details = buildString {
            append("✅ Created project \"$name\"")
            if (!client.isNullOrBlank()) append(" for $client")
            append(" (${type.name.replace("_", " ").lowercase()}")
            if (budget > BigDecimal.ZERO) append(", budget ${money(budget)}")
            append(").")
            append(" It's in your Projects list and synced to Firebase (nextgenbuildpro).")
        }
        return Outcome(details)
    }

    private suspend fun addMaterial(message: String): Outcome {
        val name = extractName(message) ?: "New Material"
        val price = extractMoney(message) ?: BigDecimal.ZERO
        val category = detectMaterialCategory(message.lowercase())
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val material = Material(
            id = "mat_${System.currentTimeMillis()}",
            name = name,
            category = category,
            subcategory = "",
            unitOfMeasurement = "each",
            currentPrice = price,
            supplier = "",
            supplierSku = null,
            description = "Added by Caroline (AI agent).",
            lastPriceUpdate = now
        )
        materialRepository.insertMaterial(material)
        cloudSync.pushMaterial(material)
        return Outcome(
            "✅ Added \"$name\" (${category.name.lowercase()}" +
                (if (price > BigDecimal.ZERO) " at ${money(price)}" else "") +
                ") to the materials catalog and synced it to Firebase."
        )
    }

    private suspend fun addWorker(message: String): Outcome {
        val name = extractName(message) ?: "New Worker"
        val rate = extractMoney(message) ?: BigDecimal.ZERO
        val trade = detectTrade(message.lowercase())
        val parts = name.split(" ").filter { it.isNotBlank() }
        val worker = Worker(
            id = "worker_${System.currentTimeMillis()}",
            firstName = parts.firstOrNull() ?: name,
            lastName = parts.drop(1).joinToString(" "),
            email = "",
            phone = "",
            tradeTypes = listOf(trade),
            skillLevel = SkillLevel.JOURNEYMAN,
            certifications = emptyList(),
            hourlyRate = rate,
            hireDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        )
        cloudSync.pushWorker(worker)
        return Outcome(
            "✅ Added $name as a ${trade.name.replace("_", " ").lowercase()}" +
                (if (rate > BigDecimal.ZERO) " at ${money(rate)}/hr" else "") +
                " and synced to Firebase. Open Labor → Workers to see the roster."
        )
    }

    private suspend fun listProjects(): Outcome {
        val projects = projectRepository.getAllProjects().first()
        if (projects.isEmpty()) {
            return Outcome("You don't have any projects yet. Say \"create a project called …\" and I'll add one.")
        }
        val top = projects.sortedByDescending { it.updatedAt }.take(8)
        val lines = top.joinToString("\n") { "• ${it.name} — ${it.status.name.lowercase()} (${money(it.totalBudget)})" }
        val more = if (projects.size > top.size) "\n…and ${projects.size - top.size} more." else ""
        return Outcome("You have ${projects.size} project(s):\n$lines$more")
    }

    private suspend fun budgetSummary(): Outcome {
        val projects = projectRepository.getAllProjects().first()
        if (projects.isEmpty()) return Outcome("No projects yet, so total budget is $0.")
        val totalBudget = projects.fold(BigDecimal.ZERO) { acc, p -> acc + p.totalBudget }
        val totalCost = projects.fold(BigDecimal.ZERO) { acc, p -> acc + p.currentCost }
        val remaining = totalBudget - totalCost
        return Outcome(
            "Across ${projects.size} project(s): total budget ${money(totalBudget)}, " +
                "spent ${money(totalCost)}, remaining ${money(remaining)}."
        )
    }

    // --- extraction helpers ---------------------------------------------------

    private fun extractName(text: String): String? {
        QUOTED.find(text)?.let { mr ->
            val q = mr.groupValues[1].ifBlank { mr.groupValues[2] }.trim()
            if (q.isNotEmpty()) return q.titlecaseWords()
        }
        NAMED.find(text)?.let { return it.groupValues[1].trim().titlecaseWords() }
        return null
    }

    private fun extractClient(text: String): String? {
        FOR_CLIENT.find(text)?.let { return it.groupValues[1].trim().titlecaseWords() }
        return null
    }

    /** Pulls a dollar amount, honouring k/m suffixes; only when the text actually implies money. */
    private fun extractMoney(text: String): BigDecimal? {
        val lower = text.lowercase()
        val impliesMoney = text.contains('$') || lower.contains("budget") ||
            lower.contains("/hr") || lower.contains("per hour") || lower.contains("price") ||
            Regex("""\d\s?(k|m|thousand|million)\b""").containsMatchIn(lower)
        if (!impliesMoney) return null
        val ctx = if (lower.contains("budget")) text.substring(lower.indexOf("budget")) else text
        val match = MONEY.find(ctx) ?: MONEY.find(text) ?: return null
        val base = match.groupValues[1].replace(",", "").toBigDecimalOrNull() ?: return null
        val mult = when (match.groupValues[2].lowercase()) {
            "k", "thousand" -> BigDecimal(1_000)
            "m", "million" -> BigDecimal(1_000_000)
            else -> BigDecimal.ONE
        }
        return base.multiply(mult)
    }

    private fun detectProjectType(m: String): ProjectType = when {
        m.contains("renovation") || m.contains("remodel") -> ProjectType.RENOVATION
        m.contains("new construction") || m.contains("new build") -> ProjectType.NEW_CONSTRUCTION
        m.contains("addition") -> ProjectType.ADDITION
        m.contains("repair") -> ProjectType.REPAIR
        m.contains("commercial") -> ProjectType.COMMERCIAL
        else -> ProjectType.RESIDENTIAL
    }

    private fun detectMaterialCategory(m: String): MaterialCategory =
        MaterialCategory.values().firstOrNull { m.contains(it.name.replace("_", " ").lowercase()) }
            ?: MaterialCategory.OTHER

    private fun detectTrade(m: String): TradeType =
        TradeType.values().firstOrNull { m.contains(it.name.replace("_", " ").lowercase()) }
            ?: TradeType.GENERAL_LABOR

    private fun money(value: BigDecimal): String = "$%,.0f".format(value)

    private fun String.titlecaseWords(): String =
        split(" ").filter { it.isNotBlank() }.joinToString(" ") { w ->
            w.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

    private companion object {
        val CREATE_VERBS = listOf("create", "add", "new", "make", "start", "set up", "hire", "register")
        val QUOTED = Regex("\"([^\"]+)\"|'([^']+)'")
        val NAMED = Regex("""(?:called|named|titled)\s+(.+?)(?:\s+for\b|\s+with\b|\s+budget\b|\s+at\b|[.,!?]|$)""", RegexOption.IGNORE_CASE)
        val FOR_CLIENT = Regex("""(?:for (?:client|the)?\s*|client\s+)(?:the\s+)?([A-Za-z][\w'.\- ]+?)(?:\s+budget\b|\s+with\b|\s+at\b|[.,!?]|$)""", RegexOption.IGNORE_CASE)
        val MONEY = Regex("""\$?\s?([0-9][0-9,]*(?:\.[0-9]+)?)\s?(k|m|thousand|million)?""", RegexOption.IGNORE_CASE)
    }
}
