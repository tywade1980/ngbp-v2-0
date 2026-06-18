package com.constructionmanager.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.constructionmanager.domain.model.ConstructionPhase
import com.constructionmanager.domain.model.ProjectStatus
import com.constructionmanager.ui.components.QuickActionCard
import com.constructionmanager.ui.components.StatCard
import com.constructionmanager.ui.components.RecentProjectCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToProjects: () -> Unit,
    onNavigateToMaterials: () -> Unit,
    onNavigateToLabor: () -> Unit = {},
    onNavigateToWorkflows: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAssistant: () -> Unit = {},
    onNavigateToVoice: () -> Unit = {},
    onNavigateToUpdates: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "ConstructPro AI",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Welcome Back!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Here's your construction overview",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Cloud (Firebase) connection status
            item {
                CloudStatusCard(
                    connected = uiState.cloudConnected,
                    projectId = uiState.cloudProjectId,
                    projectCount = uiState.cloudProjectCount
                )
            }

            // Quick Actions
            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    item {
                        QuickActionCard(
                            title = "Ask Caroline",
                            icon = Icons.Default.SmartToy,
                            onClick = onNavigateToAssistant
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "Calls",
                            icon = Icons.Default.Call,
                            onClick = onNavigateToVoice
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "New Project",
                            icon = Icons.Default.Add,
                            onClick = onNavigateToProjects
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "Materials",
                            icon = Icons.Default.Build,
                            onClick = onNavigateToMaterials
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "Labor",
                            icon = Icons.Default.People,
                            onClick = onNavigateToLabor
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "Workflows",
                            icon = Icons.Default.Timeline,
                            onClick = onNavigateToWorkflows
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "Reports",
                            icon = Icons.Default.Assessment,
                            onClick = onNavigateToReports
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "Updates",
                            icon = Icons.Default.SystemUpdate,
                            onClick = onNavigateToUpdates
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "Settings",
                            icon = Icons.Default.Settings,
                            onClick = onNavigateToSettings
                        )
                    }
                }
            }
            
            // Project Statistics
            item {
                Text(
                    "Project Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Active Projects",
                        value = uiState.activeProjectsCount.toString(),
                        icon = Icons.Default.Business
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Budget",
                        value = "$${String.format("%.0f", uiState.totalBudget)}K",
                        icon = Icons.Default.AttachMoney
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Completed",
                        value = uiState.completedProjectsCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        isPositive = true
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "On Schedule",
                        value = "${(uiState.onSchedulePercentage * 100).toInt()}%",
                        icon = Icons.Default.TrendingUp,
                        isPositive = uiState.onSchedulePercentage > 0.8
                    )
                }
            }
            
            // Recent Projects
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Projects",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToProjects) {
                        Text("View All")
                    }
                }
            }
            
            items(uiState.recentProjects) { project ->
                RecentProjectCard(
                    project = project,
                    onClick = { /* Navigate to project details */ }
                )
            }
            
            // Construction Phase Progress
            item {
                Text(
                    "Current Phase Distribution",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        uiState.phaseDistribution.forEach { (phase, count) ->
                            PhaseProgressRow(
                                phase = phase,
                                count = count,
                                total = uiState.activeProjectsCount
                            )
                            if (phase != uiState.phaseDistribution.keys.last()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhaseProgressRow(
    phase: ConstructionPhase,
    count: Int,
    total: Int
) {
    val progress = if (total > 0) count.toFloat() / total.toFloat() else 0f
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = phase.name.replace("_", " "),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$count projects",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CloudStatusCard(
    connected: Boolean,
    projectId: String,
    projectCount: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (connected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (connected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                contentDescription = null,
                tint = if (connected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (connected) "Firebase connected" else "Firebase not connected",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = buildString {
                        append("Project: ")
                        append(projectId)
                        if (projectCount != null) append(" · $projectCount cloud projects")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}