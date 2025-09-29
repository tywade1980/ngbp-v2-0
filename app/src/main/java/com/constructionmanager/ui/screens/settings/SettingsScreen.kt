package com.constructionmanager.ui.screens.settings

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    var isDarkTheme by remember { mutableStateOf(false) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var defaultRegion by remember { mutableStateOf("Midwest") }
    var isOfflineMode by remember { mutableStateOf(false) }
    
    val settingsSections = remember {
        listOf(
            SettingsSection(
                title = "Appearance",
                items = listOf(
                    SettingsItem.Switch("Dark Theme", "Use dark color scheme", isDarkTheme) { isDarkTheme = it }
                )
            ),
            SettingsSection(
                title = "Notifications", 
                items = listOf(
                    SettingsItem.Switch("Push Notifications", "Receive project updates", isNotificationsEnabled) { isNotificationsEnabled = it },
                    SettingsItem.Navigation("Notification Settings", "Customize notification preferences")
                )
            ),
            SettingsSection(
                title = "Regional Settings",
                items = listOf(
                    SettingsItem.Selection("Default Region", "Set pricing region", defaultRegion, listOf("Northeast", "Southeast", "Midwest", "Southwest", "West")) { defaultRegion = it },
                    SettingsItem.Navigation("Currency Format", "USD ($)")
                )
            ),
            SettingsSection(
                title = "Data & Storage",
                items = listOf(
                    SettingsItem.Switch("Offline Mode", "Store data locally", isOfflineMode) { isOfflineMode = it },
                    SettingsItem.Navigation("Data Export", "Export project data"),
                    SettingsItem.Navigation("Backup & Sync", "Cloud backup settings")
                )
            ),
            SettingsSection(
                title = "Account",
                items = listOf(
                    SettingsItem.Navigation("Profile Settings", "Edit personal information"),
                    SettingsItem.Navigation("Security", "Password and authentication"),
                    SettingsItem.Action("Sign Out", "Log out of your account", onLogout)
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // Profile Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Demo Manager",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "demo@constructionmanager.com",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Professional Tier",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            items(settingsSections) { section ->
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            section.items.forEachIndexed { index, item ->
                                SettingsItemRow(item = item)
                                
                                if (index < section.items.size - 1) {
                                    Divider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsItemRow(item: SettingsItem) {
    when (item) {
        is SettingsItem.Switch -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = item.checked,
                    onCheckedChange = item.onCheckedChange
                )
            }
        }
        
        is SettingsItem.Navigation -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Navigate */ }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (item.subtitle.isNotEmpty()) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        is SettingsItem.Selection -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Show selection dialog */ }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.selectedValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        is SettingsItem.Action -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { item.action() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (item.title == "Sign Out") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

private sealed class SettingsItem {
    data class Switch(
        val title: String,
        val description: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingsItem()
    
    data class Navigation(
        val title: String,
        val subtitle: String = ""
    ) : SettingsItem()
    
    data class Selection(
        val title: String,
        val subtitle: String,
        val selectedValue: String,
        val options: List<String>,
        val onSelectionChange: (String) -> Unit
    ) : SettingsItem()
    
    data class Action(
        val title: String,
        val description: String,
        val action: () -> Unit
    ) : SettingsItem()
}