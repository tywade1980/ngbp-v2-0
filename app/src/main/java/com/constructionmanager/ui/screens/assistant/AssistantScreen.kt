package com.constructionmanager.ui.screens.assistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AssistantScreen(
    onNavigateBack: () -> Unit,
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Caroline — AI Assistant")
                        Text(
                            if (state.remoteEnabled) "Connected to Wade backend" else "On-device (offline) mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Icon(
                        imageVector = if (state.remoteEnabled) Icons.Default.Cloud else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (state.remoteEnabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Backend settings")
                    }
                }
            )
        },
        bottomBar = {
            MessageComposer(
                value = state.input,
                enabled = !state.isSending,
                onValueChange = viewModel::onInputChange,
                onSend = viewModel::send
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            QuickPromptRow(
                onPrompt = viewModel::quickPrompt,
                onBriefing = viewModel::briefing,
                enabled = !state.isSending
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.messages, key = { it.id }) { msg -> MessageBubble(msg) }
                if (state.isSending) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Caroline is thinking…", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        BackendSettingsSheet(
            remoteEnabled = state.remoteEnabled,
            orchestratorUrl = state.orchestratorUrl,
            memoryUrl = state.memoryUrl,
            carolineUrl = state.carolineUrl,
            userId = state.userId,
            onDismiss = { showSettings = false },
            onSave = { remote, orch, mem, caroline, user ->
                viewModel.saveBackend(remote, orch, mem, caroline, user)
                showSettings = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickPromptRow(
    onPrompt: (String) -> Unit,
    onBriefing: () -> Unit,
    enabled: Boolean
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(enabled = enabled, onClick = onBriefing, label = { Text("Daily briefing") })
        AssistChip(
            enabled = enabled,
            onClick = { onPrompt("Draft an estimate for a kitchen remodel.") },
            label = { Text("Estimate") }
        )
        AssistChip(
            enabled = enabled,
            onClick = { onPrompt("What materials have long lead times?") },
            label = { Text("Materials") }
        )
        AssistChip(
            enabled = enabled,
            onClick = { onPrompt("Help me schedule the next project phase.") },
            label = { Text("Schedule") }
        )
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val alignment = if (msg.fromUser) Alignment.End else Alignment.Start
    val container = if (msg.fromUser) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.secondaryContainer
    val content = if (msg.fromUser) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSecondaryContainer
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = container,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = msg.text,
                color = content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
        if (!msg.fromUser) {
            Text(
                text = msg.provenance ?: if (msg.live) "live • orchestrator + memory" else "on-device",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageComposer(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message Caroline…") },
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = onSend, enabled = enabled && value.isNotBlank()) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackendSettingsSheet(
    remoteEnabled: Boolean,
    orchestratorUrl: String,
    memoryUrl: String,
    carolineUrl: String,
    userId: String,
    onDismiss: () -> Unit,
    onSave: (Boolean, String, String, String, String) -> Unit
) {
    var remote by remember { mutableStateOf(remoteEnabled) }
    var orch by remember { mutableStateOf(orchestratorUrl) }
    var mem by remember { mutableStateOf(memoryUrl) }
    var caroline by remember { mutableStateOf(carolineUrl) }
    var user by remember { mutableStateOf(userId) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Wade Backend", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Use live backend", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Off = on-device assistant only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = remote, onCheckedChange = { remote = it })
            }
            OutlinedTextField(
                value = orch, onValueChange = { orch = it },
                label = { Text("Orchestrator URL") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = mem, onValueChange = { mem = it },
                label = { Text("Memory (mem0) URL") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = caroline, onValueChange = { caroline = it },
                label = { Text("Caroline (calls) URL") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = user, onValueChange = { user = it },
                label = { Text("User ID") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSave(remote, orch, mem, caroline, user) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }
}
