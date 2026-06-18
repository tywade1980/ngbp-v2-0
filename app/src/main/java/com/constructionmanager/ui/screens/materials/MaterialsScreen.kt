package com.constructionmanager.ui.screens.materials

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.constructionmanager.domain.model.MaterialCategory
import com.constructionmanager.ui.components.MaterialCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MaterialsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadMaterials()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Materials Catalog") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Material")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Search materials...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Category Filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("All") },
                        selected = uiState.selectedCategory == null
                    )
                }
                items(uiState.categories) { category ->
                    FilterChip(
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category.name.replace("_", " ")) },
                        selected = uiState.selectedCategory == category
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Materials List
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = { viewModel.loadMaterials() },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.filteredMaterials.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No materials found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredMaterials) { material ->
                            MaterialCard(
                                material = material,
                                onClick = { /* Material detail view */ }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMaterialDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, category, unit, price, supplier, description ->
                viewModel.addMaterial(name, category, unit, price, supplier, description)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMaterialDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, category: MaterialCategory, unit: String, price: String, supplier: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MaterialCategory.LUMBER) }
    var categoryMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Material") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Material name *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = categoryMenuOpen,
                    onExpandedChange = { categoryMenuOpen = it }
                ) {
                    OutlinedTextField(
                        value = category.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuOpen) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuOpen,
                        onDismissRequest = { categoryMenuOpen = false }
                    ) {
                        MaterialCategory.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name.replace("_", " ")) },
                                onClick = { category = option; categoryMenuOpen = false }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price, onValueChange = { price = it },
                        label = { Text("Price ($)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit, onValueChange = { unit = it },
                        label = { Text("Unit") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = supplier, onValueChange = { supplier = it },
                    label = { Text("Supplier") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, category, unit, price, supplier, description) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}