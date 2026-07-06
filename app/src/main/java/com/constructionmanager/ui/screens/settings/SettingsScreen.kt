package com.constructionmanager.ui.screens.settings

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUpdates: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.darkTheme.collectAsState()
    val isNotificationsEnabled by viewModel.notifications.collectAsState()
    val defaultRegion by viewModel.defaultRegion.collectAsState()
    val isOfflineMode by viewModel.offlineMode.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val emailNotifications by viewModel.emailNotifications.collectAsState()
    val smsNotifications by viewModel.smsNotifications.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current
    var showRegionDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showNotifDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    val regionOptions = listOf("Northeast", "Southeast", "Midwest", "Southwest", "West")
    val currencyOptions = listOf("USD ($)", "EUR (€)", "GBP (£)", "CAD (C$)", "AUD (A$)")

    val settingsSections =
        listOf(
            SettingsSection(
                title = "Appearance",
                items = listOf(
                    SettingsItem.Switch("Dark Theme", "Use dark color scheme", isDarkTheme) { viewModel.setDarkTheme(it) }
                )
            ),
            SettingsSection(
                title = "Notifications",
                items = listOf(
                    SettingsItem.Switch("Push Notifications", "Receive project updates", isNotificationsEnabled) { viewModel.setNotifications(it) },
                    SettingsItem.Navigation("Notification Settings", "Email, SMS and push", onClick = { showNotifDialog = true })
                )
            ),
            SettingsSection(
                title = "Regional Settings",
                items = listOf(
                    SettingsItem.Selection("Default Region", "Set pricing region", defaultRegion, regionOptions, onClick = { showRegionDialog = true }) { viewModel.setDefaultRegion(it) },
                    SettingsItem.Navigation("Currency Format", currency, onClick = { showCurrencyDialog = true })
                )
            ),
            SettingsSection(
                title = "Data & Storage",
                items = listOf(
                    SettingsItem.Switch("Offline Mode", "Store data locally", isOfflineMode) { viewModel.setOfflineMode(it) },
                    SettingsItem.Navigation("Data Export", "Export projects & materials", onClick = {
                        viewModel.exportData { shareData(context, it) }
                    }),
                    SettingsItem.Navigation("Backup & Sync", "Cloud backup settings")
                )
            ),
            SettingsSection(
                title = "About",
                items = listOf(
                    SettingsItem.Action("App Updates", "Check for new versions over the air", onNavigateToUpdates)
                )
            ),
            SettingsSection(
                title = "Account",
                items = listOf(
                    SettingsItem.Navigation("Profile Settings", "Edit personal information", onClick = { showProfileDialog = true }),
                    SettingsItem.Navigation("Security", "Password and authentication", onClick = { showSecurityDialog = true }),
                    SettingsItem.Action("Sign Out", "Log out of your account", onLogout)
                )
            )
        )

    if (showRegionDialog) {
        AlertDialog(
            onDismissRequest = { showRegionDialog = false },
            title = { Text("Default Region") },
            text = {
                Column {
                    regionOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDefaultRegion(option)
                                    showRegionDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = option == defaultRegion, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(option, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRegionDialog = false }) { Text("Close") }
            }
        )
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Currency Format") },
            text = {
                Column {
                    currencyOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrency(option)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = option == currency, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(option, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Close") }
            }
        )
    }

    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            title = { Text("Notification Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    NotificationToggleRow("Push notifications", isNotificationsEnabled) { viewModel.setNotifications(it) }
                    NotificationToggleRow("Email notifications", emailNotifications) { viewModel.setEmailNotifications(it) }
                    NotificationToggleRow("SMS notifications", smsNotifications) { viewModel.setSmsNotifications(it) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifDialog = false }) { Text("Done") }
            }
        )
    }

    if (showProfileDialog) {
        var firstName by remember { mutableStateOf(profile?.firstName.orEmpty()) }
        var lastName by remember { mutableStateOf(profile?.lastName.orEmpty()) }
        var company by remember { mutableStateOf(profile?.company.orEmpty()) }
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Profile Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!profile?.email.isNullOrBlank()) {
                        Text(
                            profile!!.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedTextField(firstName, { firstName = it }, label = { Text("First name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(lastName, { lastName = it }, label = { Text("Last name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(company, { company = it }, label = { Text("Company") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveProfile(firstName, lastName, company)
                    showProfileDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showSecurityDialog) {
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var message by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            title = { Text("Security") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Change your password", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        newPassword, { newPassword = it }, label = { Text("New password") },
                        singleLine = true, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        confirmPassword, { confirmPassword = it }, label = { Text("Confirm password") },
                        singleLine = true, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    message?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = newPassword.length >= 6 && newPassword == confirmPassword,
                    onClick = {
                        viewModel.changePassword(newPassword) { result ->
                            message = result.fold(
                                onSuccess = { "Password updated." },
                                onFailure = { "Couldn't update: ${it.message}" }
                            )
                        }
                    }
                ) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { showSecurityDialog = false }) { Text("Close") }
            }
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
                            val displayName = listOfNotNull(profile?.firstName, profile?.lastName)
                                .joinToString(" ").trim().ifBlank { "ConstructPro User" }
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = profile?.email ?: "Not signed in",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = (profile?.subscriptionTier?.name ?: "FREE")
                                    .lowercase().replaceFirstChar { it.uppercase() } + " Tier",
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
                    .clickable { item.onClick() }
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
                    .clickable { item.onClick() }
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

@Composable
private fun NotificationToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

private fun shareData(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "ConstructPro data export")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Export data"))
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
        val subtitle: String = "",
        val onClick: () -> Unit = {}
    ) : SettingsItem()
    
    data class Selection(
        val title: String,
        val subtitle: String,
        val selectedValue: String,
        val options: List<String>,
        val onClick: () -> Unit = {},
        val onSelectionChange: (String) -> Unit
    ) : SettingsItem()
    
    data class Action(
        val title: String,
        val description: String,
        val action: () -> Unit
    ) : SettingsItem()
}