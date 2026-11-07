package com.constructionmanager.ui.screens.hocc

import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.input.TextFieldValue
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
import com.constructionmanager.BuildConfig
import com.constructionmanager.data.caroline.CallSummary
import com.constructionmanager.data.caroline.serverTimeToLocal
import java.io.File

// ------------------------------------------------------------------ Caroline (the chat + the calls)

@Composable
fun CarolineScreen(vm: HoccViewModel) {
    val chatState by vm.chat.collectAsState()
    // (her line's calls with other people used to list here; they are in the Call and text log now, under that person - Tyler, Nov 3 2026)
    val sending by vm.sending.collectAsState()
    val note by vm.chatNote.collectAsState()
    // TextFieldValue, not a String: with a String the keyboard's half-typed (composing) word came back after Send
    // (Tyler, Nov 4 2026: "chat input doesnt clear after send... carries over to the next message")
    var draft by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    val messages = chatState.data?.messages.orEmpty()
    LaunchedEffect(Unit) { vm.loadCaroline() }
    LaunchedEffect(messages.size, sending) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }
    // imePadding: the app is edge-to-edge, so the keyboard sat over the input ("the keyboard covers the chat block")
    Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 14.dp, vertical = 12.dp)) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(messages) { m -> ChatBubble(m) }
            // only the two of them here (Tyler, Nov 3 2026): calls on her line with other people are in the Call and text log, under that person
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
                maxLines = 5, modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { val t = draft.text; draft = TextFieldValue(""); vm.sendChat(t) },
                enabled = draft.text.isNotBlank() && !sending
            ) { Text("Send") }
        }
    }
}

/** Her replies come with **bold** and ### headings; the screen shows the words with the weight, not the markup. */
fun plainWords(text: String): androidx.compose.ui.text.AnnotatedString = androidx.compose.ui.text.buildAnnotatedString {
    val bold = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)
    text.lines().forEachIndexed { li, raw ->
        if (li > 0) append("\n")
        var line = raw
        val heading = Regex("^#{1,6}\\s+").containsMatchIn(line)
        if (heading) line = line.replace(Regex("^#{1,6}\\s+"), "")
        line = line.replace(Regex("^(\\s*)[-*]\\s+"), "$1• ")
        if (heading) pushStyle(bold)
        var i = 0
        while (i < line.length) {
            val open = line.indexOf("**", i)
            if (open < 0) { append(line.substring(i)); break }
            val close = line.indexOf("**", open + 2)
            if (close < 0) { append(line.substring(i)); break }
            append(line.substring(i, open)); pushStyle(bold); append(line.substring(open + 2, close)); pop()
            i = close + 2
        }
        if (heading) pop()
    }
}

@Composable
private fun ChatBubble(m: ChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromOwner) Arrangement.End else Arrangement.Start) {
        Surface(
            color = if (m.fromOwner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(if (m.fromOwner) 0.82f else 0.92f)
        ) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    plainWords(m.text), style = MaterialTheme.typography.bodyMedium,
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

        var installNote by remember { mutableStateOf<String?>(null) }
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
                        u.downloadedPath != null -> {
                            val path = u.downloadedPath
                            Button(onClick = { if (path != null) installNote = UpdateInstall.install(context, File(path)) }) { Text("Install it now") }
                            installNote?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                        }
                        else -> Button(onClick = { vm.downloadUpdate(context) }) { Text("Download & install") }
                    }
                }
                else -> Text("You're on the newest build Caroline has published.", style = MaterialTheme.typography.bodyMedium)
            }
        }


        // edit, delete, archive, restore (0.7.4, Tyler Nov 6 2026): the client cards, the archived jobs, the trash
        ClientsBookCard(vm)
        ArchivedJobsCard(vm)
        TrashCard(vm)

        // his phone's call recordings -> the record (Tyler, Nov 3 2026)
        LocationCard()
        RecordingsCard(vm)
        if (d != null) {
            val estimates = (d.estimates?.rowsOf("estimates", "rows", "items") ?: com.google.gson.JsonArray()).objects()
            HoccCard("Estimates (${estimates.size})") {
                if (estimates.isEmpty()) EmptyLine("No estimates on file.")
                estimates.take(25).forEach { e ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(e.str("client_name", "client", "label") ?: "Estimate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        e.str("status")?.let { s -> humanStatus(s)?.let { StatusChip(it) } }
                    }
                    val sub = listOfNotNull(e.str("number", "estimate_number"), money(e.num("total", "grand_total", "amount"))).joinToString(" · ")
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


@Composable
private fun RecordingsCard(vm: HoccViewModel) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var status by remember { mutableStateOf<com.google.gson.JsonObject?>(null) }
    var outcome by remember { mutableStateOf<com.constructionmanager.data.caroline.RecordingsSync.Outcome?>(null) }
    var running by remember { mutableStateOf(false) }
    var granted by remember { mutableStateOf(com.constructionmanager.data.caroline.RecordingsSync.hasPermission(context)) }
    val ask = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted = it }
    var logGranted by remember { mutableStateOf(com.constructionmanager.data.caroline.CallLogSync.hasPermission(context)) }
    val askLog = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { logGranted = it }
    var logStatus by remember { mutableStateOf<com.google.gson.JsonObject?>(null) }
    var logOutcome by remember { mutableStateOf<com.constructionmanager.data.caroline.CallLogSync.Outcome?>(null) }
    val refresh: () -> Unit = { scope.launch { status = vm.readJson("calls/recordings/status"); logStatus = vm.readJson("calls/log/latest") } }
    LaunchedEffect(Unit) { refresh() }
    HoccCard("Your phone's calls") {
        Text("The phone's call log gives every call its number, direction and length; every recording goes to the office, Caroline writes it out and files the lead, the meeting and what was said. You audit it on the board.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!logGranted) {
            Text("This phone has not let the app read its call log yet - without it a recording named \"Spam Risk\" has no number.", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { askLog.launch(com.constructionmanager.data.caroline.CallLogSync.permission()) }) { Text("Allow reading the call log") }
        } else {
            logStatus?.let { Text("Call log on file: ${it.num("calls")?.toInt() ?: 0} calls" + (it.num("latest_started_ms")?.toLong()?.takeIf { ms -> ms > 0 }?.let { ms -> ", newest " + java.text.SimpleDateFormat("EEE MMM d, h:mm a", java.util.Locale.US).format(java.util.Date(ms)) } ?: ""), style = MaterialTheme.typography.bodyMedium) }
            logOutcome?.let { o -> Text("Last send: ${o.landed} landed, ${o.duplicates} already there, ${o.matched} recordings given their number." + (if (o.note.isNotBlank()) " ${o.note}" else ""), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = !running, onClick = { running = true; scope.launch { logOutcome = com.constructionmanager.data.caroline.CallLogSync.sync(context); running = false; refresh() } }) { Text("Send new calls now") }
                TextButton(enabled = !running, onClick = { running = true; scope.launch { logOutcome = com.constructionmanager.data.caroline.CallLogSync.sync(context, everything = true); running = false; refresh() } }) { Text("Send the whole log") }
            }
        }
        Text("Recordings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        if (!granted) {
            Text("This phone has not let the app read its call recordings yet.", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { ask.launch(com.constructionmanager.data.caroline.RecordingsSync.permission()) }) { Text("Allow reading the recordings") }
        } else {
            val onPhone = remember(granted) { com.constructionmanager.data.caroline.RecordingsSync.findRecordings(context).size }
            Text("On this phone: $onPhone recordings in Recordings/Call.", style = MaterialTheme.typography.bodyMedium)
        }
        status?.let { s ->
            Text("On file: ${s.num("recordings")?.toInt() ?: 0} · transcribed ${s.num("transcribed")?.toInt() ?: 0} · read by Caroline ${s.num("mined")?.toInt() ?: 0}" +
                (s.num("failed")?.toInt()?.takeIf { it > 0 }?.let { " · $it failed" } ?: ""), style = MaterialTheme.typography.bodyMedium)
            s.objOf("kinds")?.let { k -> Text(k.entrySet().joinToString(" · ") { (key, v) -> "${humanStatus(key)} ${v.asInt}" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        outcome?.let { o ->
            Text("Last send: ${o.sent} sent, ${o.duplicates} already there, ${o.failed} failed of ${o.found} on the phone." + (if (o.note.isNotBlank()) " ${o.note}" else ""),
                style = MaterialTheme.typography.bodySmall, color = if (o.failed > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = granted && !running, onClick = { running = true; scope.launch { outcome = com.constructionmanager.data.caroline.RecordingsSync.sync(context, everything = false); running = false; refresh() } }) { Text(if (running) "Sending…" else "Send new now") }
            TextButton(enabled = granted && !running, onClick = { running = true; scope.launch { outcome = com.constructionmanager.data.caroline.RecordingsSync.sync(context, everything = true); running = false; refresh() } }) { Text("Send everything from the start") }
        }
        Text("New recordings also go on their own, hourly, whenever the phone has a connection.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/*
 * WHERE YOU ARE (0.7.3, Tyler Nov 6 2026). Three of his taps, in Android's order: location while using the app, then "Allow all the
 * time" on the app's permission page (Android 11+ will not ask that in a dialog), then "don't put this app to sleep". The card reads
 * back what Android says is granted - it never claims a grant it cannot see.
 */
@Composable
private fun LocationCard() {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val LS = com.constructionmanager.data.location.LocationSync
    var fg by remember { mutableStateOf(LS.hasForeground(context)) }
    var bg by remember { mutableStateOf(LS.hasBackground(context)) }
    var awake by remember { mutableStateOf(LS.ignoresBatteryLimits(context)) }
    var note by remember { mutableStateOf<String?>(null) }
    val askFg = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) { fg = LS.hasForeground(context) }
    val lifecycle = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycle) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) { fg = LS.hasForeground(context); bg = LS.hasBackground(context); awake = LS.ignoresBatteryLimits(context) }
        }
        lifecycle.lifecycle.addObserver(obs)
        onDispose { lifecycle.lifecycle.removeObserver(obs) }
    }
    HoccCard("Where you are") {
        Text("Caroline uses your phone's location to know which job you're at and the weather where you'll be. With all three on, the phone sends a fix every 15 minutes even with the app closed.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text((if (fg) "✓ " else "1. ") + "Location while using the app", style = MaterialTheme.typography.bodyMedium)
        if (!fg) Button(onClick = { askFg.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)) }) { Text("Allow location") }
        Text((if (bg) "✓ " else "2. ") + "Location all the time", style = MaterialTheme.typography.bodyMedium)
        if (fg && !bg) {
            Text("Android asks this one on the app's own page: tap Permissions, then Location, then Allow all the time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { context.startActivity(LS.appSettingsIntent(context)) }) { Text("Open the app's settings") }
        }
        Text((if (awake) "✓ " else "3. ") + "Don't put the app to sleep", style = MaterialTheme.typography.bodyMedium)
        if (!awake) Button(onClick = { runCatching { context.startActivity(LS.batteryIntent(context)) }.onFailure { context.startActivity(LS.appSettingsIntent(context)) } }) { Text("Keep the app awake") }
        if (fg) TextButton(onClick = { scope.launch { note = LS.sendOnce(context).let { if (it.sent) "Sent your location to Caroline just now." else it.note } } }) { Text("Send my location now") }
        note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
