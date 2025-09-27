package com.constructionmanager.ui.screens.workflows

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.constructionmanager.domain.model.ConstructionPhase
import com.constructionmanager.ui.components.PhaseWorkflowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowScreen(
    onNavigateBack: () -> Unit,
    projectId: String? = null
) {
    val constructionPhases = remember {
        listOf(
            WorkflowPhase(
                phase = ConstructionPhase.PRE_CONSTRUCTION,
                title = "Pre-Construction",
                description = "Planning, permits, and preparation",
                tasks = listOf(
                    "Obtain building permits",
                    "Site survey and analysis",
                    "Final design approval",
                    "Material procurement planning",
                    "Contractor scheduling"
                ),
                estimatedDays = 14,
                icon = Icons.Default.Assignment
            ),
            WorkflowPhase(
                phase = ConstructionPhase.SITE_PREPARATION,
                title = "Site Preparation",
                description = "Clear and prepare the construction site",
                tasks = listOf(
                    "Site clearing and grubbing",
                    "Soil testing and analysis",
                    "Utility marking and protection",
                    "Temporary facilities setup",
                    "Access road preparation"
                ),
                estimatedDays = 5,
                icon = Icons.Default.Landscape
            ),
            WorkflowPhase(
                phase = ConstructionPhase.FOUNDATION,
                title = "Foundation Work",
                description = "Excavation and foundation construction",
                tasks = listOf(
                    "Excavation and grading",
                    "Footings installation",
                    "Foundation walls construction",
                    "Waterproofing application",
                    "Backfill and compaction"
                ),
                estimatedDays = 10,
                icon = Icons.Default.Foundation
            ),
            WorkflowPhase(
                phase = ConstructionPhase.FRAMING,
                title = "Framing",
                description = "Structural framework construction",
                tasks = listOf(
                    "Floor system installation",
                    "Wall framing",
                    "Roof framing",
                    "Sheathing installation",
                    "Structural inspections"
                ),
                estimatedDays = 12,
                icon = Icons.Default.Construction
            ),
            WorkflowPhase(
                phase = ConstructionPhase.ROOFING,
                title = "Roofing",
                description = "Roof system installation",
                tasks = listOf(
                    "Roof decking installation",
                    "Underlayment application",
                    "Shingle/material installation",
                    "Flashing and sealing",
                    "Gutter installation"
                ),
                estimatedDays = 7,
                icon = Icons.Default.Roofing
            ),
            WorkflowPhase(
                phase = ConstructionPhase.SIDING_EXTERIOR,
                title = "Siding & Exterior",
                description = "Exterior finishing and weatherproofing",
                tasks = listOf(
                    "House wrap installation",
                    "Siding installation",
                    "Exterior trim work",
                    "Window and door installation",
                    "Exterior painting/staining"
                ),
                estimatedDays = 10,
                icon = Icons.Default.HomeWork
            ),
            WorkflowPhase(
                phase = ConstructionPhase.MEP_ROUGH_IN,
                title = "MEP Rough-In",
                description = "Mechanical, Electrical, and Plumbing rough installation",
                tasks = listOf(
                    "Electrical rough-in",
                    "Plumbing rough-in",
                    "HVAC installation",
                    "Low voltage wiring",
                    "MEP inspections"
                ),
                estimatedDays = 8,
                icon = Icons.Default.ElectricalServices
            ),
            WorkflowPhase(
                phase = ConstructionPhase.INSULATION,
                title = "Insulation",
                description = "Thermal and acoustic insulation installation",
                tasks = listOf(
                    "Wall insulation installation",
                    "Ceiling insulation",
                    "Vapor barrier installation",
                    "Air sealing",
                    "Insulation inspection"
                ),
                estimatedDays = 3,
                icon = Icons.Default.Thermostat
            ),
            WorkflowPhase(
                phase = ConstructionPhase.DRYWALL,
                title = "Drywall",
                description = "Interior wall and ceiling finishing",
                tasks = listOf(
                    "Drywall installation",
                    "Joint taping and mudding",
                    "Sanding and prep",
                    "Prime and texture",
                    "Quality inspection"
                ),
                estimatedDays = 8,
                icon = Icons.Default.Construction
            ),
            WorkflowPhase(
                phase = ConstructionPhase.FLOORING,
                title = "Flooring",
                description = "Floor covering installation",
                tasks = listOf(
                    "Subfloor preparation",
                    "Hardwood/tile installation",
                    "Carpet installation",
                    "Trim and transition strips",
                    "Final floor protection"
                ),
                estimatedDays = 6,
                icon = Icons.Default.Layers
            ),
            WorkflowPhase(
                phase = ConstructionPhase.INTERIOR_FINISH,
                title = "Interior Finishing",
                description = "Interior trim, painting, and fixtures",
                tasks = listOf(
                    "Interior painting",
                    "Trim and millwork installation",
                    "Cabinet installation",
                    "Countertop installation",
                    "Fixture installation"
                ),
                estimatedDays = 12,
                icon = Icons.Default.Brush
            ),
            WorkflowPhase(
                phase = ConstructionPhase.FINAL_INSPECTION,
                title = "Final Inspections",
                description = "Final inspections and corrections",
                tasks = listOf(
                    "Final building inspection",
                    "Punch list creation",
                    "Correction work",
                    "Systems testing",
                    "Certificate of occupancy"
                ),
                estimatedDays = 5,
                icon = Icons.Default.CheckCircle
            ),
            WorkflowPhase(
                phase = ConstructionPhase.HANDOVER,
                title = "Project Handover",
                description = "Final delivery and documentation",
                tasks = listOf(
                    "Final walk-through",
                    "Key delivery",
                    "Warranty documentation",
                    "Maintenance schedules",
                    "Project closeout"
                ),
                estimatedDays = 2,
                icon = Icons.Default.Key
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Construction Workflows") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                            "Construction Lifecycle Workflows",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Natural flow from planning to handover with minimal taps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            items(constructionPhases) { workflowPhase ->
                PhaseWorkflowCard(
                    workflowPhase = workflowPhase,
                    onClick = { /* Navigate to phase details */ },
                    projectId = projectId
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "Total Project Timeline",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Estimated: ${constructionPhases.sumOf { it.estimatedDays }} working days",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "Typical project duration: ${(constructionPhases.sumOf { it.estimatedDays } * 1.2).toInt()} calendar days including weather delays",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

data class WorkflowPhase(
    val phase: ConstructionPhase,
    val title: String,
    val description: String,
    val tasks: List<String>,
    val estimatedDays: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)