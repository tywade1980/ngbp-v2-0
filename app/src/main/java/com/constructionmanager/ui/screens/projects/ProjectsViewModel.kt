package com.constructionmanager.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.data.cloud.CloudSync
import com.constructionmanager.domain.model.ConstructionPhase
import com.constructionmanager.domain.model.Project
import com.constructionmanager.domain.model.ProjectStatus
import com.constructionmanager.domain.model.ProjectType
import com.constructionmanager.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import javax.inject.Inject

data class ProjectsUiState(
    val isLoading: Boolean = true,
    val projects: List<Project> = emptyList(),
    val filteredProjects: List<Project> = emptyList(),
    val selectedStatus: ProjectStatus? = null,
    val error: String? = null
)

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val cloudSync: CloudSync
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        // Combine search query and selected status to filter projects
        combine(
            _searchQuery,
            _uiState.map { it.selectedStatus },
            _uiState.map { it.projects }
        ) { query, status, projects ->
            filterProjects(projects, query, status)
        }.onEach { filteredProjects ->
            _uiState.value = _uiState.value.copy(filteredProjects = filteredProjects)
        }.launchIn(viewModelScope)
    }
    
    fun loadProjects() {
        // Pull any cloud projects into the local store (survives reinstall / other devices).
        viewModelScope.launch { cloudSync.pullProjects() }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                projectRepository.getAllProjects().collect { projects ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        projects = projects,
                        filteredProjects = filterProjects(
                            projects, 
                            _searchQuery.value, 
                            _uiState.value.selectedStatus
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load projects"
                )
            }
        }
    }
    
    /** Creates a project: writes to the local DB (instant display) and mirrors it to Firestore. */
    fun createProject(
        name: String,
        clientName: String,
        projectType: ProjectType,
        totalBudget: String,
        city: String
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val budget = totalBudget.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val project = Project(
                    id = "proj_${System.currentTimeMillis()}",
                    name = name.trim(),
                    address = "",
                    city = city.trim(),
                    state = "",
                    zipCode = "",
                    clientName = clientName.trim(),
                    clientEmail = "",
                    clientPhone = "",
                    projectType = projectType,
                    currentPhase = ConstructionPhase.PRE_CONSTRUCTION,
                    startDate = today,
                    estimatedEndDate = today.plus(90, DateTimeUnit.DAY),
                    totalBudget = budget,
                    currentCost = BigDecimal.ZERO,
                    status = ProjectStatus.PLANNING,
                    notes = "",
                    createdAt = now,
                    updatedAt = now
                )
                projectRepository.insertProject(project)
                cloudSync.pushProject(project)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to create project")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectStatus(status: ProjectStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
    }
    
    private fun filterProjects(
        projects: List<Project>,
        searchQuery: String,
        selectedStatus: ProjectStatus?
    ): List<Project> {
        return projects.asSequence()
            .filter { project ->
                selectedStatus == null || project.status == selectedStatus
            }
            .filter { project ->
                if (searchQuery.isBlank()) {
                    true
                } else {
                    project.name.contains(searchQuery, ignoreCase = true) ||
                    project.address.contains(searchQuery, ignoreCase = true) ||
                    project.city.contains(searchQuery, ignoreCase = true) ||
                    project.clientName.contains(searchQuery, ignoreCase = true)
                }
            }
            .sortedByDescending { it.updatedAt }
            .toList()
    }
}