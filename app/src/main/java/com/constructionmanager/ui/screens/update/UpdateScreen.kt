package com.constructionmanager.ui.screens.update

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.constructionmanager.update.AvailableUpdate
import com.constructionmanager.update.UpdateSource
import com.constructionmanager.update.UpdateStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    onNavigateBack: () -> Unit,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (status is UpdateStatus.Idle) viewModel.check()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Updates") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::check) {
                        Icon(Icons.Default.Refresh, contentDescription = "Check for updates")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VersionCard(
                versionName = viewModel.currentVersionName,
                versionCode = viewModel.currentVersionCode
            )

            StatusCard(
                status = status,
                onCheck = viewModel::check,
                onDownload = { viewModel.download(it) },
                onInstall = {
                    if (viewModel.canInstallPackages()) {
                        viewModel.install()
                    } else {
                        runCatching { context.startActivity(viewModel.unknownSourcesIntent()) }
                    }
                },
                onOpenPlay = {
                    val pkg = context.packageName.removeSuffix(".debug")
                    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        context.startActivity(market)
                    } catch (e: ActivityNotFoundException) {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                },
                onSkip = { viewModel.skip(it) }
            )

            UpdateSettingsCard(
                manifestUrl = settings.manifestUrl,
                autoCheck = settings.autoCheckEnabled,
                preferPlay = settings.preferPlayStore,
                onSave = viewModel::saveSettings
            )
        }
    }
}

@Composable
private fun VersionCard(versionName: String, versionCode: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text("ConstructPro AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Installed: v$versionName (build $versionCode)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    status: UpdateStatus,
    onCheck: () -> Unit,
    onDownload: (AvailableUpdate) -> Unit,
    onInstall: () -> Unit,
    onOpenPlay: () -> Unit,
    onSkip: (AvailableUpdate) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (val s = status) {
                is UpdateStatus.Idle, is UpdateStatus.Checking -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Checking for updates…", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                is UpdateStatus.UpToDate -> {
                    StatusHeader(Icons.Default.CheckCircle, "You're up to date", MaterialTheme.colorScheme.primary)
                    Text(
                        "v${s.versionName} (build ${s.versionCode}) is the latest version.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onCheck) { Text("Check again") }
                }

                is UpdateStatus.Available -> {
                    val u = s.update
                    StatusHeader(Icons.Default.CloudDownload, "Update available", MaterialTheme.colorScheme.primary)
                    Text(
                        "Version ${u.versionName}" +
                            (if (u.sizeBytes > 0) " · ${formatSize(u.sizeBytes)}" else "") +
                            if (u.mandatory) " · required" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (u.releaseNotes.isNotBlank()) {
                        Text(u.releaseNotes, style = MaterialTheme.typography.bodyMedium)
                    }
                    when (u.source) {
                        UpdateSource.PLAY -> Button(onClick = onOpenPlay, modifier = Modifier.fillMaxWidth()) {
                            Text("Update via Google Play")
                        }
                        UpdateSource.SELF_HOSTED -> {
                            Button(onClick = { onDownload(u) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Download update")
                            }
                            if (!u.mandatory) {
                                TextButton(onClick = { onSkip(u) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Skip this version")
                                }
                            }
                        }
                    }
                }

                is UpdateStatus.Downloading -> {
                    Text("Downloading update…", style = MaterialTheme.typography.bodyLarge)
                    if (s.percent in 0..100) {
                        LinearProgressIndicator(
                            progress = s.percent / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("${s.percent}%", style = MaterialTheme.typography.bodySmall)
                    } else {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }

                is UpdateStatus.ReadyToInstall -> {
                    StatusHeader(Icons.Default.SystemUpdate, "Ready to install", MaterialTheme.colorScheme.primary)
                    Text(
                        "Version ${s.update.versionName} has been downloaded.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = onInstall, modifier = Modifier.fillMaxWidth()) { Text("Install now") }
                }

                is UpdateStatus.Failed -> {
                    StatusHeader(Icons.Default.CloudDownload, "Update problem", MaterialTheme.colorScheme.error)
                    Text(s.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onCheck) { Text("Try again") }
                }
            }
        }
    }
}

@Composable
private fun StatusHeader(icon: ImageVector, title: String, tint: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun UpdateSettingsCard(
    manifestUrl: String,
    autoCheck: Boolean,
    preferPlay: Boolean,
    onSave: (String, Boolean, Boolean) -> Unit
) {
    var url by remember(manifestUrl) { mutableStateOf(manifestUrl) }
    var auto by remember(autoCheck) { mutableStateOf(autoCheck) }
    var play by remember(preferPlay) { mutableStateOf(preferPlay) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Update channel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Update manifest URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            ToggleRow("Check automatically on launch", auto) { auto = it }
            ToggleRow("Prefer Google Play when available", play) { play = it }
            Button(onClick = { onSave(url, auto, play) }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1) "%.1f MB".format(mb) else "%.0f KB".format(bytes / 1024.0)
}
