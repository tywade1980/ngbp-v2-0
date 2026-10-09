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
import androidx.compose.runtime.saveable.rememberSaveable
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
                            if (state.paired) "Paired with Caroline" else "Not paired: on-device only",
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
                        imageVector = if (state.paired) Icons.Default.Cloud else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (state.paired) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Connect Caroline")
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
        ConnectCarolineSheet(
            state = state,
            onDismiss = { showSettings = false },
            onPair = viewModel::pair,
            onDisconnect = viewModel::disconnect,
            onSaveWebKey = viewModel::saveWebSearchKey
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
                text = msg.provenance ?: "on-device",
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


/**
 * Connect Caroline: the server address (https, default the VM), a one-time pairing code from the Office, and
 * disconnect. The phone never holds the owner's Office password; it holds only its own revocable device token.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectCarolineSheet(
    state: AssistantUiState,
    onDismiss: () -> Unit,
    onPair: (String, String, String) -> Unit,
    onDisconnect: () -> Unit,
    onSaveWebKey: (String) -> Unit
) {
    var url by rememberSaveable { mutableStateOf(state.serverUrl) }
    var code by rememberSaveable { mutableStateOf("") }
    var label by rememberSaveable { mutableStateOf("") }
    var webKey by rememberSaveable { mutableStateOf(state.webSearchApiKey) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Connect Caroline", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                if (state.paired) "This phone is paired." else
                    "In the Office, make a pairing code (it works once, for ten minutes) and type it here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = url, onValueChange = { url = it },
                label = { Text("Caroline's server (https)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (!state.paired) {
                OutlinedTextField(
                    value = code, onValueChange = { code = it },
                    label = { Text("Pairing code, like ABCD-EFGH") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = label, onValueChange = { label = it },
                    label = { Text("Name for this phone (optional)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onPair(url, code, label) },
                    enabled = !state.isPairing && code.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (state.isPairing) "Pairing…" else "Pair this phone") }
            } else {
                OutlinedButton(onClick = onDisconnect, modifier = Modifier.fillMaxWidth()) { Text("Disconnect this phone") }
            }
            state.connectionNote?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Divider()
            Text("Web Search (Tavily)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Paste a free Tavily API key to let the app search the web (\"search …\", \"look up …\"). " +
                    "Leave blank to use bundled knowledge only. Web search does not go through Caroline.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = webKey, onValueChange = { webKey = it },
                label = { Text("Tavily API key") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { onSaveWebKey(webKey) }, modifier = Modifier.fillMaxWidth()) { Text("Save web search key") }
        }
    }
}
