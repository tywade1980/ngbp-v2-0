@file:OptIn(ExperimentalMaterial3Api::class)

package com.constructionmanager.ui.screens.labor

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
import com.constructionmanager.domain.model.SkillLevel
import com.constructionmanager.domain.model.TradeType
import com.constructionmanager.domain.model.Worker
import com.constructionmanager.ui.components.LaborEntryCard
import com.constructionmanager.ui.components.WorkerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaborManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: LaborManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddWorker by remember { mutableStateOf(false) }
    val tabs = listOf("Time Tracking", "Workers", "Labor Costs")

    LaunchedEffect(Unit) {
        viewModel.loadLaborData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Labor Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddWorker = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Worker")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> if (uiState.isTimeTracking) viewModel.stopTimeTracking() else viewModel.startTimeTracking()
                        else -> showAddWorker = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = when (selectedTab) {
                        0 -> if (uiState.isTimeTracking) Icons.Default.Stop else Icons.Default.AccessTime
                        1 -> Icons.Default.PersonAdd
                        else -> Icons.Default.Add
                    },
                    contentDescription = "Add"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> TimeTrackingTab(uiState, viewModel)
                1 -> WorkersTab(uiState, viewModel)
                2 -> LaborCostsTab(uiState, viewModel)
            }
        }
    }

    if (showAddWorker) {
        AddWorkerDialog(
            onDismiss = { showAddWorker = false },
            onAdd = { first, last, trade, skill, rate, phone, email ->
                viewModel.addWorker(first, last, trade, skill, rate, phone, email)
                showAddWorker = false
            }
        )
    }
}

@Composable
private fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onAdd: (first: String, last: String, trade: TradeType, skill: SkillLevel, rate: String, phone: String, email: String) -> Unit
) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var trade by remember { mutableStateOf(TradeType.CARPENTER) }
    var skill by remember { mutableStateOf(SkillLevel.JOURNEYMAN) }
    var tradeMenuOpen by remember { mutableStateOf(false) }
    var skillMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Worker") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = first, onValueChange = { first = it },
                        label = { Text("First name *") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = last, onValueChange = { last = it },
                        label = { Text("Last name") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                ExposedDropdownMenuBox(
                    expanded = tradeMenuOpen,
                    onExpandedChange = { tradeMenuOpen = it }
                ) {
                    OutlinedTextField(
                        value = trade.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Trade") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tradeMenuOpen) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = tradeMenuOpen,
                        onDismissRequest = { tradeMenuOpen = false }
                    ) {
                        TradeType.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name.replace("_", " ")) },
                                onClick = { trade = option; tradeMenuOpen = false }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = skillMenuOpen,
                    onExpandedChange = { skillMenuOpen = it }
                ) {
                    OutlinedTextField(
                        value = skill.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Skill level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = skillMenuOpen) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = skillMenuOpen,
                        onDismissRequest = { skillMenuOpen = false }
                    ) {
                        SkillLevel.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = { skill = option; skillMenuOpen = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = rate, onValueChange = { rate = it },
                    label = { Text("Hourly rate ($)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(first, last, trade, skill, rate, phone, email) },
                enabled = first.isNotBlank() || last.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TimeTrackingTab(
    uiState: LaborManagementUiState,
    viewModel: LaborManagementViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick time entry
        item {
            QuickTimeEntryCard(
                workers = uiState.workers,
                selectedWorkerId = uiState.selectedWorkerId,
                onSelectWorker = { viewModel.selectWorker(it) },
                onStartTimer = { viewModel.startTimeTracking() },
                onStopTimer = { viewModel.stopTimeTracking() },
                isTracking = uiState.isTimeTracking,
                currentDuration = uiState.currentTrackingDuration
            )
        }

        // Today's summary
        item {
            TodaysSummaryCard(
                totalHours = uiState.todaysTotalHours,
                totalCost = uiState.todaysTotalCost,
                activeWorkers = uiState.activeWorkersCount
            )
        }

        // Recent time entries
        item {
            Text(
                text = "Recent Time Entries",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(uiState.recentLaborEntries) { entry ->
            LaborEntryCard(
                entry = entry,
                onClick = { /* Navigate to entry details */ }
            )
        }
    }
}

@Composable
private fun WorkersTab(
    uiState: LaborManagementUiState,
    viewModel: LaborManagementViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Trade type filter
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        onClick = { viewModel.filterByTradeType(null) },
                        label = { Text("All Trades") },
                        selected = uiState.selectedTradeType == null
                    )
                }
                items(TradeType.values()) { tradeType ->
                    FilterChip(
                        onClick = { viewModel.filterByTradeType(tradeType) },
                        label = { Text(tradeType.name.replace("_", " ")) },
                        selected = uiState.selectedTradeType == tradeType
                    )
                }
            }
        }

        // Workers list
        items(uiState.filteredWorkers) { worker ->
            WorkerCard(
                worker = worker,
                onClick = { /* Navigate to worker details */ }
            )
        }
    }
}

@Composable
private fun LaborCostsTab(
    uiState: LaborManagementUiState,
    viewModel: LaborManagementViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cost summary cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CostSummaryCard(
                    title = "This Week",
                    amount = uiState.weeklyLaborCost,
                    modifier = Modifier.weight(1f)
                )
                CostSummaryCard(
                    title = "This Month",
                    amount = uiState.monthlyLaborCost,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Labor cost breakdown by trade
        item {
            LaborCostBreakdownCard(costsByTrade = uiState.laborCostsByTrade)
        }

        // Hourly rates by trade
        item {
            HourlyRatesCard(hourlyRates = uiState.hourlyRatesByTrade)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickTimeEntryCard(
    workers: List<Worker>,
    selectedWorkerId: String?,
    onSelectWorker: (String?) -> Unit,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit,
    isTracking: Boolean,
    currentDuration: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isTracking) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTracking) "Time Tracking Active" else "Quick Time Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (isTracking) {
                    Text(
                        text = currentDuration,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (workers.isNotEmpty()) {
                var menuOpen by remember { mutableStateOf(false) }
                val selected = workers.firstOrNull { it.id == selectedWorkerId } ?: workers.first()
                ExposedDropdownMenuBox(
                    expanded = menuOpen,
                    onExpandedChange = { if (!isTracking) menuOpen = it }
                ) {
                    OutlinedTextField(
                        value = "${selected.firstName} ${selected.lastName}".trim(),
                        onValueChange = {},
                        readOnly = true,
                        enabled = !isTracking,
                        label = { Text("Worker") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        workers.forEach { w ->
                            DropdownMenuItem(
                                text = { Text("${w.firstName} ${w.lastName}".trim()) },
                                onClick = { onSelectWorker(w.id); menuOpen = false }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = if (isTracking) onStopTimer else onStartTimer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isTracking) "Stop Timer & Log" else "Start Timer")
            }
        }
    }
}

@Composable
private fun TodaysSummaryCard(
    totalHours: Double,
    totalCost: Double,
    activeWorkers: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    icon = Icons.Default.Schedule,
                    value = String.format("%.1f hrs", totalHours),
                    label = "Total Hours"
                )
                SummaryItem(
                    icon = Icons.Default.AttachMoney,
                    value = String.format("$%.0f", totalCost),
                    label = "Total Cost"
                )
                SummaryItem(
                    icon = Icons.Default.People,
                    value = activeWorkers.toString(),
                    label = "Active Workers"
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CostSummaryCard(
    title: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = String.format("$%.0f", amount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun LaborCostBreakdownCard(costsByTrade: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Labor Costs by Trade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            costsByTrade.forEach { (trade, cost) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = trade.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = String.format("$%.0f", cost),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyRatesCard(hourlyRates: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Average Hourly Rates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            hourlyRates.forEach { (trade, rate) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = trade.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = String.format("$%.2f/hr", rate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}