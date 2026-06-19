package com.constructionmanager.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.domain.model.Material
import com.constructionmanager.domain.model.Project
import com.constructionmanager.domain.model.ProjectStatus
import com.constructionmanager.domain.repository.MaterialRepository
import com.constructionmanager.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class ReportsUiState(
    val isLoading: Boolean = true,
    val totalProjects: Int = 0,
    val activeProjects: Int = 0,
    val totalBudget: String = "$0",
    val onSchedule: String = "—"
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val materialRepository: MaterialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private var projects: List<Project> = emptyList()
    private var materials: List<Material> = emptyList()

    init {
        viewModelScope.launch {
            projectRepository.getAllProjects().collect { list ->
                projects = list
                recomputeSummary()
            }
        }
        viewModelScope.launch {
            materialRepository.getAllActiveMaterials().collect { list ->
                materials = list
            }
        }
    }

    private fun recomputeSummary() {
        val active = projects.count { it.status == ProjectStatus.ACTIVE }
        val totalBudget = projects.fold(BigDecimal.ZERO) { acc, p -> acc + p.totalBudget }
        val onTrack = projects.count { it.status == ProjectStatus.ACTIVE || it.status == ProjectStatus.COMPLETED }
        _uiState.value = ReportsUiState(
            isLoading = false,
            totalProjects = projects.size,
            activeProjects = active,
            totalBudget = compactMoney(totalBudget),
            onSchedule = if (projects.isEmpty()) "—" else "${onTrack * 100 / projects.size}%"
        )
    }

    /** Builds a plain-text report from the live project/material data for sharing/export. */
    fun buildReport(type: String): String {
        val header = "ConstructPro — $type\nGenerated ${java.util.Date()}\n" + "=".repeat(40)
        val body = when (type) {
            "Cost Analysis", "Budget vs Actual" -> costAnalysis()
            "Material Usage" -> materialUsage()
            "Project Timeline", "Phase Progress" -> timeline()
            "Labor Summary" -> laborSummary()
            else -> fullSummary()
        }
        return "$header\n\n$body\n"
    }

    fun exportAll(): String = buildReport("Full Summary")

    private fun fullSummary(): String = buildString {
        appendLine("Projects: ${projects.size}")
        appendLine("Active: ${projects.count { it.status == ProjectStatus.ACTIVE }}")
        appendLine("Completed: ${projects.count { it.status == ProjectStatus.COMPLETED }}")
        val totalBudget = projects.fold(BigDecimal.ZERO) { acc, p -> acc + p.totalBudget }
        val totalCost = projects.fold(BigDecimal.ZERO) { acc, p -> acc + p.currentCost }
        appendLine("Total budget: ${money(totalBudget)}")
        appendLine("Total spent:  ${money(totalCost)}")
        appendLine("Remaining:    ${money(totalBudget - totalCost)}")
        appendLine("Materials tracked: ${materials.size}")
    }

    private fun costAnalysis(): String = buildString {
        if (projects.isEmpty()) { append("No projects yet."); return@buildString }
        projects.forEach { p ->
            val remaining = p.totalBudget - p.currentCost
            appendLine("${p.name} (${p.status.name.lowercase()})")
            appendLine("  Budget ${money(p.totalBudget)} | Spent ${money(p.currentCost)} | Left ${money(remaining)}")
        }
    }

    private fun materialUsage(): String = buildString {
        if (materials.isEmpty()) { append("No materials in the catalog yet."); return@buildString }
        materials.sortedBy { it.category.name }.forEach { m ->
            appendLine("${m.name} — ${m.category.name.lowercase()} @ ${money(m.currentPrice)} / ${m.unitOfMeasurement}")
        }
    }

    private fun timeline(): String = buildString {
        if (projects.isEmpty()) { append("No projects yet."); return@buildString }
        projects.forEach { p ->
            appendLine("${p.name}: ${p.currentPhase.name.replace("_", " ").lowercase()} — ${p.status.name.lowercase()}")
            appendLine("  ${p.startDate} → ${p.estimatedEndDate}")
        }
    }

    private fun laborSummary(): String =
        "Labor reporting reads from the Labor module. " +
            "Add workers and time entries there to populate detailed labor costs."

    private fun money(value: BigDecimal): String = "$%,.0f".format(value)

    private fun compactMoney(value: BigDecimal): String {
        val d = value.toDouble()
        return when {
            d >= 1_000_000 -> "$%.1fM".format(d / 1_000_000)
            d >= 1_000 -> "$%.0fK".format(d / 1_000)
            else -> "$%,.0f".format(d)
        }
    }
}
