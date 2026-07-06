package com.constructionmanager.ui.screens.voice

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    onNavigateBack: () -> Unit,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refreshCalls() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice & Call Screening") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refreshCalls) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreeningRoleCard() }

            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("On-device screening", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Caroline screens inbound calls before they ring. Lower the threshold to block " +
                                "more aggressively. Grant the call-screening role in system settings to activate.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("Risk threshold: ${state.screeningThreshold} (allow ≤ ${state.screeningThreshold})")
                        Slider(
                            value = state.screeningThreshold.toFloat(),
                            onValueChange = { viewModel.setThreshold(it.toInt()) },
                            valueRange = 0f..100f
                        )
                    }
                }
            }

            item {
                Text(
                    if (state.remoteEnabled) "Recent screened calls (Caroline)" else "Recent calls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.isLoading) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }

            if (!state.remoteEnabled) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(
                            "Live call records come from the Caroline receptionist service. Enable the Wade " +
                                "backend in the Assistant tab to see qualified leads and transcripts here.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            items(state.calls) { call ->
                Card {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                call.callerName ?: call.callerNumber ?: "Unknown caller",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            call.intent?.let {
                                Text("Intent: $it", style = MaterialTheme.typography.bodySmall)
                            }
                            call.transcript?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                        }
                        call.status?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                    }
                }
            }

            state.error?.let {
                item {
                    Text(
                        "Couldn't load calls: $it",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/** Lets the user grant Caroline the system call-screening role so it can answer calls first. */
@Composable
private fun ScreeningRoleCard() {
    val context = LocalContext.current
    var roleHeld by remember { mutableStateOf(isScreeningRoleHeld(context)) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { roleHeld = isScreeningRoleHeld(context) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (roleHeld) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (roleHeld) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (roleHeld) "Caroline is screening your calls" else "Activate call screening",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!roleHeld) {
                Text(
                    "Set Caroline as your call screening app so it can answer first, qualify leads, and " +
                        "block spam before your phone rings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        val intent = screeningRoleRequestIntent(context)
                        if (intent != null) {
                            launcher.launch(intent)
                        } else {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    }
                ) { Text("Set as call screening app") }
            }
        }
    }
}

private fun isScreeningRoleHeld(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager ?: return false
    return roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
        roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
}

private fun screeningRoleRequestIntent(context: Context): Intent? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager ?: return null
    if (!roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) return null
    return roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
}
