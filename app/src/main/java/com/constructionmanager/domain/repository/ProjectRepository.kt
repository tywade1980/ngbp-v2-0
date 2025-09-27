package com.constructionmanager.domain.repository

import com.constructionmanager.domain.model.Project
import com.constructionmanager.domain.model.ProjectStatus
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    
    fun getAllProjects(): Flow<List<Project>>
    
    fun getProjectsByStatus(status: ProjectStatus): Flow<List<Project>>
    
    suspend fun getProjectById(projectId: String): Project?
    
    fun searchProjects(searchQuery: String): Flow<List<Project>>
    
    suspend fun insertProject(project: Project)
    
    suspend fun updateProject(project: Project)
    
    suspend fun deleteProject(project: Project)
    
    suspend fun getProjectCountByStatus(status: ProjectStatus): Int
    
    suspend fun getTotalBudgetByStatuses(statuses: List<ProjectStatus>): Double
    
    suspend fun getTotalCurrentCostByStatuses(statuses: List<ProjectStatus>): Double
}