package com.constructionmanager.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.domain.model.Project
import com.constructionmanager.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectDetailsUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val phaseProgress: Float = 0f,
    val recentActivities: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProjectDetailsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProjectDetailsUiState())
    val uiState: StateFlow<ProjectDetailsUiState> = _uiState.asStateFlow()
    
    fun loadProject(projectId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val project = projectRepository.getProjectById(projectId)
                if (project != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        project = project,
                        phaseProgress = calculatePhaseProgress(project),
                        recentActivities = getRecentActivities(project)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Project not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load project"
                )
            }
        }
    }
    
    private fun calculatePhaseProgress(project: Project): Float {
        // Calculate progress based on current phase
        val phases = com.constructionmanager.domain.model.ConstructionPhase.values()
        val currentPhaseIndex = phases.indexOf(project.currentPhase)
        return if (currentPhaseIndex >= 0) {
            (currentPhaseIndex + 1).toFloat() / phases.size.toFloat()
        } else 0f
    }
    
    private fun getRecentActivities(project: Project): List<String> {
        return listOf(
            "Phase updated to ${project.currentPhase.name.replace("_", " ")}",
            "Budget updated: $${project.totalBudget}",
            "Project status: ${project.status.name}",
            "Last updated: ${project.updatedAt}"
        )
    }
}