package com.constructionmanager.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.data.cloud.CloudProjectRepository
import com.constructionmanager.data.cloud.CloudStatus
import com.constructionmanager.domain.model.ConstructionPhase
import com.constructionmanager.domain.model.Project
import com.constructionmanager.domain.model.ProjectStatus
import com.constructionmanager.domain.repository.MaterialRepository
import com.constructionmanager.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val activeProjectsCount: Int = 0,
    val completedProjectsCount: Int = 0,
    val totalBudget: Double = 0.0,
    val currentCosts: Double = 0.0,
    val onSchedulePercentage: Double = 0.0,
    val recentProjects: List<Project> = emptyList(),
    val phaseDistribution: Map<ConstructionPhase, Int> = emptyMap(),
    val cloudConnected: Boolean = false,
    val cloudProjectId: String = "",
    val cloudProjectCount: Int? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val materialRepository: MaterialRepository,
    private val cloudStatus: CloudStatus,
    private val cloudProjectRepository: CloudProjectRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        // Initialize seed data on first run
        viewModelScope.launch {
            try {
                materialRepository.initializeSeedData()
            } catch (e: Exception) {
                // Seed data might already exist, continue
            }
        }
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Get project counts
                val activeCount = projectRepository.getProjectCountByStatus(ProjectStatus.ACTIVE)
                val completedCount = projectRepository.getProjectCountByStatus(ProjectStatus.COMPLETED)
                
                // Get financial data
                val totalBudget = projectRepository.getTotalBudgetByStatuses(
                    listOf(ProjectStatus.ACTIVE, ProjectStatus.PLANNING)
                ) / 1000.0 // Convert to thousands
                
                val currentCosts = projectRepository.getTotalCurrentCostByStatuses(
                    listOf(ProjectStatus.ACTIVE, ProjectStatus.COMPLETED)
                ) / 1000.0 // Convert to thousands
                
                // Get recent projects
                val allProjects = projectRepository.getAllProjects().first()
                val recentProjects = allProjects.take(5)
                
                // Calculate phase distribution
                val activeProjects = projectRepository.getProjectsByStatus(ProjectStatus.ACTIVE).first()
                val phaseDistribution = activeProjects.groupBy { it.currentPhase }
                    .mapValues { it.value.size }
                
                // Calculate on-schedule percentage (simplified logic)
                val onSchedulePercentage = if (activeProjects.isNotEmpty()) {
                    activeProjects.count { project ->
                        // Simple logic: if current date is before estimated end date
                        kotlinx.datetime.Clock.System.now().toEpochMilliseconds() < 
                        project.estimatedEndDate.toEpochDays() * 24 * 60 * 60 * 1000
                    }.toDouble() / activeProjects.size
                } else 0.0
                
                // Cloud (Firebase) status + a live Firestore read, best-effort.
                val cloudCount = cloudProjectRepository.count().getOrNull()

                _uiState.value = DashboardUiState(
                    isLoading = false,
                    activeProjectsCount = activeCount,
                    completedProjectsCount = completedCount,
                    totalBudget = totalBudget,
                    currentCosts = currentCosts,
                    onSchedulePercentage = onSchedulePercentage,
                    recentProjects = recentProjects,
                    phaseDistribution = phaseDistribution,
                    cloudConnected = cloudStatus.connected,
                    cloudProjectId = cloudStatus.projectId,
                    cloudProjectCount = cloudCount
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
}