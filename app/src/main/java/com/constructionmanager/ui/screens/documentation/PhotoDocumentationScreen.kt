package com.constructionmanager.ui.screens.documentation

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
import com.constructionmanager.domain.model.ConstructionPhase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDocumentationScreen(
    onNavigateBack: () -> Unit,
    projectId: String
) {
    val photoCategories = remember {
        listOf(
            PhotoCategory.PROGRESS,
            PhotoCategory.SAFETY,
            PhotoCategory.QUALITY_CONTROL,
            PhotoCategory.MATERIALS,
            PhotoCategory.ISSUES,
            PhotoCategory.BEFORE_AFTER
        )
    }
    
    var selectedCategory by remember { mutableStateOf<PhotoCategory?>(null) }
    var selectedPhase by remember { mutableStateOf<ConstructionPhase?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Documentation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Open camera */ }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo")
                    }
                    IconButton(onClick = { /* Import from gallery */ }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Import Photo")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Quick camera capture */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Quick Photo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Photo summary cards
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(photoCategories) { category ->
                    PhotoCategorySummaryCard(
                        category = category,
                        photoCount = when (category) {
                            PhotoCategory.PROGRESS -> 45
                            PhotoCategory.SAFETY -> 12
                            PhotoCategory.QUALITY_CONTROL -> 8
                            PhotoCategory.MATERIALS -> 23
                            PhotoCategory.ISSUES -> 3
                            PhotoCategory.BEFORE_AFTER -> 16
                        },
                        isSelected = selectedCategory == category,
                        onClick = { 
                            selectedCategory = if (selectedCategory == category) null else category
                        }
                    )
                }
            }
            
            // Phase filter
            if (selectedCategory == PhotoCategory.PROGRESS || selectedCategory == null) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = { selectedPhase = null },
                            label = { Text("All Phases") },
                            selected = selectedPhase == null
                        )
                    }
                    items(ConstructionPhase.values()) { phase ->
                        FilterChip(
                            onClick = { selectedPhase = phase },
                            label = { 
                                Text(
                                    phase.name.replace("_", " ").lowercase()
                                        .split(" ")
                                        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
                                ) 
                            },
                            selected = selectedPhase == phase
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Photo documentation content
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick capture section
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
                                "Quick Documentation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Capture progress with just one tap",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { /* Daily progress photo */ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Today,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Daily Progress")
                                }
                                
                                FilledTonalButton(
                                    onClick = { /* Safety documentation */ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Safety Check")
                                }
                            }
                        }
                    }
                }
                
                // Photo timeline
                item {
                    Text(
                        "Recent Documentation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Sample photo entries
                items(getSamplePhotoEntries()) { photoEntry ->
                    PhotoDocumentationCard(
                        photoEntry = photoEntry,
                        onClick = { /* View photo details */ }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoCategorySummaryCard(
    category: PhotoCategory,
    photoCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (icon, title, color) = when (category) {
        PhotoCategory.PROGRESS -> Triple(Icons.Default.Timeline, "Progress", MaterialTheme.colorScheme.primary)
        PhotoCategory.SAFETY -> Triple(Icons.Default.Security, "Safety", MaterialTheme.colorScheme.error)
        PhotoCategory.QUALITY_CONTROL -> Triple(Icons.Default.VerifiedUser, "Quality", MaterialTheme.colorScheme.tertiary)
        PhotoCategory.MATERIALS -> Triple(Icons.Default.Inventory, "Materials", MaterialTheme.colorScheme.secondary)
        PhotoCategory.ISSUES -> Triple(Icons.Default.Warning, "Issues", MaterialTheme.colorScheme.error)
        PhotoCategory.BEFORE_AFTER -> Triple(Icons.Default.Compare, "Before/After", MaterialTheme.colorScheme.primary)
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                color.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(width = 2.dp)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$photoCount photos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoDocumentationCard(
    photoEntry: PhotoEntry,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = photoEntry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${photoEntry.phase.name.replace("_", " ")} • ${photoEntry.category.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = photoEntry.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (photoEntry.description.isNotEmpty()) {
                Text(
                    text = photoEntry.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Photo count indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${photoEntry.photoCount} photos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

enum class PhotoCategory {
    PROGRESS,
    SAFETY,
    QUALITY_CONTROL,
    MATERIALS,
    ISSUES,
    BEFORE_AFTER
}

data class PhotoEntry(
    val id: String,
    val title: String,
    val description: String,
    val category: PhotoCategory,
    val phase: ConstructionPhase,
    val timestamp: String,
    val photoCount: Int
)

private fun getSamplePhotoEntries(): List<PhotoEntry> = listOf(
    PhotoEntry(
        id = "1",
        title = "Foundation Wall Progress",
        description = "West wall foundation completed, ready for waterproofing",
        category = PhotoCategory.PROGRESS,
        phase = ConstructionPhase.FOUNDATION,
        timestamp = "2 hours ago",
        photoCount = 4
    ),
    PhotoEntry(
        id = "2",
        title = "Safety Equipment Check",
        description = "Daily safety equipment inspection and worker PPE verification",
        category = PhotoCategory.SAFETY,
        phase = ConstructionPhase.FOUNDATION,
        timestamp = "Today 8:00 AM",
        photoCount = 6
    ),
    PhotoEntry(
        id = "3",
        title = "Concrete Pour Quality",
        description = "Quality control documentation for foundation concrete pour",
        category = PhotoCategory.QUALITY_CONTROL,
        phase = ConstructionPhase.FOUNDATION,
        timestamp = "Yesterday",
        photoCount = 8
    ),
    PhotoEntry(
        id = "4",
        title = "Material Delivery",
        description = "Lumber delivery for framing phase - quality verification",
        category = PhotoCategory.MATERIALS,
        phase = ConstructionPhase.FRAMING,
        timestamp = "2 days ago",
        photoCount = 3
    )
)