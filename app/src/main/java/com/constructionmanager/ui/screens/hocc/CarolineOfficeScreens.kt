package com.constructionmanager.ui.screens.hocc

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.constructionmanager.BuildConfig
import com.constructionmanager.data.caroline.CallSummary
import com.constructionmanager.data.caroline.serverTimeToLocal
import java.io.File

// ------------------------------------------------------------------ Caroline (the chat + the calls)

@Composable
fun CarolineScreen(vm: HoccViewModel) {
    val chatState by vm.chat.collectAsState()
    val calls by vm.chatCalls.collectAsState()
    val sending by vm.sending.collectAsState()
    val note by vm.chatNote.collectAsState()
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val messages = chatState.data?.messages.orEmpty()
    LaunchedEffect(Unit) { vm.loadCaroline() }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Caroline", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { vm.loadCaroline() }) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") }
        }
        chatState.error?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        if (chatState.loading && messages.isEmpty()) LoadingLine("Reading your thread with Caroline…")
        if (!chatState.loading && messages.isEmpty() && chatState.error == null) {
            EmptyLine("No messages yet - say the first thing below and Caroline answers here.")
        }
        LazyColumn(
            state = listState, modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { m -> ChatBubble(m) }
            if (calls.isNotEmpty()) {
                item {
                    Text("Recent calls", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
                }
                items(calls) { c -> CallRow(c) }
            }
        }
        note?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
        }
        if (sending) {
            Text("Caroline is working on it…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = draft, onValueChange = { draft = it },
                placeholder = { Text("Message Caroline") },
                maxLines = 4, modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { val t = draft; draft = ""; vm.sendChat(t) },
                enabled = draft.isNotBlank() && !sending
            ) { Text("Send") }
        }
    }
}

@Composable
private fun ChatBubble(m: ChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromOwner) Arrangement.End else Arrangement.Start) {
        Surface(
            color = if (m.fromOwner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    m.text, style = MaterialTheme.typography.bodyMedium,
                    color = if (m.fromOwner) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                m.at?.let { serverTimeToLocal(it) }?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CallRow(c: CallSummary) {
    Column(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(c.callerName ?: c.partyNumber ?: "Unknown caller", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        val sub = listOfNotNull(serverTimeToLocal(c.startedAt), c.outcome ?: c.leadStatus, c.summary).joinToString(" · ")
        if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ------------------------------------------------------------------ Office

@Composable
fun OfficeScreen(vm: HoccViewModel) {
    val state by vm.office.collectAsState()
    val upd by vm.updateInfo.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.loadOffice() }
    ScreenColumn(title = "Office", onRefresh = { vm.loadOffice() }) {
        if (state.loading && state.data == null) LoadingLine()
        state.error?.let { ErrorLine(it) { vm.loadOffice() } }
        val d = state.data

        HoccCard("This app") {
            Text("HOCC ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE}) is what's installed on this phone.", style = MaterialTheme.typography.bodyMedium)
            val u = upd
            when {
                u == null -> TextButton(onClick = { vm.checkUpdate() }) { Text("Check for a newer build") }
                u.error != null -> {
                    Text(u.error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { vm.checkUpdate() }) { Text("Check again") }
                }
                u.isNewer -> {
                    Text("Version ${u.name ?: ""} (build ${u.code ?: ""}) is ready.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    u.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    u.size?.let { Text("${String.format("%.1f", it / 1_000_000.0)} MB", style = MaterialTheme.typography.bodySmall) }
                    when {
                        u.downloading -> LoadingLine("Downloading the new build…")
                        u.downloadedPath != null -> { val path = u.downloadedPath; Button(onClick = { if (path != null) installApk(context, File(path)) }) { Text("Install it now") } }
                        else -> Button(onClick = { vm.downloadUpdate(context) }) { Text("Download & install") }
                    }
                }
                else -> Text("You're on the newest build Caroline has published.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (d != null) {
            val texts = d.messages.objects()
            HoccCard("Texts (${texts.size})") {
                if (texts.isEmpty()) EmptyLine("No texts on the record.")
                texts.take(25).forEach { m ->
                    val party = m.str("contact_name", "name") ?: m.str("from_number", "party") ?: "Text"
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(party, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        m.str("direction")?.let { dir -> humanStatus(dir)?.let { StatusChip(it) } }
                    }
                    m.str("body", "text")?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    m.str("created_at", "at", "sent_at")?.let { at ->
                        Text(serverTimeToLocal(at) ?: at, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            val estimates = (d.estimates?.rowsOf("estimates", "rows", "items") ?: com.google.gson.JsonArray()).objects()
            HoccCard("Estimates (${estimates.size})") {
                if (estimates.isEmpty()) EmptyLine("No estimates on the record.")
                estimates.take(25).forEach { e ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(e.str("client_name", "client", "label") ?: "Estimate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        e.str("status")?.let { s -> humanStatus(s)?.let { StatusChip(it) } }
                    }
                    val sub = listOfNotNull(e.str("number", "estimate_number"), money(e.num("total", "grand_total", "amount"))).joinToString(" · ")
                    if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            val contacts = d.contacts.objects()
            HoccCard("Contacts (${contacts.size})") {
                if (contacts.isEmpty()) EmptyLine("No contacts on the record.")
                contacts.take(30).forEach { c ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(c.str("name") ?: "Contact", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        c.str("lead_status", "status")?.let { s -> humanStatus(s)?.let { StatusChip(it) } }
                    }
                    val sub = listOfNotNull(c.str("phone"), c.str("address")).joinToString(" · ")
                    if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            val issues = d.issues.objects()
            HoccCard("Open items (${issues.size})") {
                if (issues.isEmpty()) EmptyLine("Nothing open - the list is clear.")
                issues.take(25).forEach { i ->
                    val title = i.str("title", "summary", "what", "kind") ?: "Open item"
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        i.str("severity", "priority")?.let { s -> humanStatus(s)?.let { StatusChip(it) } }
                    }
                    i.str("created_at", "at")?.let { at ->
                        Text(serverTimeToLocal(at) ?: at, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        val activity = context as? Activity
        Button(
            onClick = { activity?.let { vm.signOut(it) } },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) { Text("Sign out of Caroline on this phone") }
    }
}

/** Hand the downloaded build to Android's installer, through the app's own file provider. */
private fun installApk(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, "application/vnd.android.package-archive")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
