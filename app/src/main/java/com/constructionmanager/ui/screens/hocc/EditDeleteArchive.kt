package com.constructionmanager.ui.screens.hocc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.constructionmanager.data.caroline.serverTimeToLocal
import com.google.gson.JsonObject

/*
 * EDIT, DELETE, ARCHIVE, RESTORE (HOCC 0.7.4, Tyler Nov 6 2026: "Editing, deleting, archiving. Can't do any of it from
 * anywhere yet. I've said that at least 5 or 6 times."). His rules: archive keeps a record forever and takes it out of the
 * default view; delete is soft - it goes to the record's trash and comes back from the Trash card - and the screen asks
 * before it sends one; nothing blocks a save for a missing field.
 */

/** The one question before any delete or cancel: what goes, and that it can be brought back. */
@Composable
fun ConfirmDelete(what: String, verb: String = "Delete", onYes: () -> Unit, onNo: () -> Unit,
                  body: String = "It goes to the trash - you can restore it from Office › Trash.") {
    AlertDialog(
        onDismissRequest = onNo,
        title = { Text("$verb $what?") },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = { onYes() }) { Text(verb, color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onNo) { Text("Keep it") } }
    )
}

/** The office's last answer, in its words (a refusal shows as an error). */
@Composable
fun OfficeNoteLine(vm: HoccViewModel) {
    val note by vm.officeNote.collectAsState()
    note?.let { n ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(n.text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f),
                color = if (n.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            TextButton(onClick = { vm.clearOfficeNote() }) { Text("ok") }
        }
    }
}

// ------------------------------------------------------------------ the client book: find a card, edit it, archive it, delete it

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ClientsBookCard(vm: HoccViewModel) {
    val state by vm.book.collectAsState()
    val busy by vm.officeBusy.collectAsState()
    var q by remember { mutableStateOf("") }
    var showArchived by remember { mutableStateOf(false) }
    var open by remember { mutableStateOf<String?>(null) }
    var confirm by remember { mutableStateOf<JsonObject?>(null) }
    LaunchedEffect(Unit) { vm.loadBook("") }
    HoccCard("Client cards") {
        Text("Find a card to edit it, archive it (off the active cards, kept on file) or delete it (to the trash).",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(q, { q = it }, placeholder = { Text("Name or number") }, singleLine = true, modifier = Modifier.weight(1f))
            TextButton(onClick = { vm.loadBook(q) }) { Text("Find") }
        }
        FilterChip(selected = showArchived, onClick = { showArchived = !showArchived }, label = { Text("Show archived") })
        if (state.loading && state.data == null) LoadingLine()
        state.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        val rows = state.data.orEmpty().filter { showArchived || it.str("lead_status") != "archived" }
        if (!state.loading && rows.isEmpty()) EmptyLine(if (q.isBlank()) "No cards to show." else "No card matches that.")
        rows.forEach { c ->
            val phone = c.str("phone") ?: return@forEach
            val archived = c.str("lead_status") == "archived"
            Divider()
            Row(Modifier.fillMaxWidth().clickable { open = if (open == phone) null else phone }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(c.str("name") ?: prettyPhone(phone) ?: phone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(listOfNotNull(prettyPhone(phone), c.str("address")).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (archived) StatusChip("Archived")
            }
            if (open == phone) {
                var name by remember(c) { mutableStateOf(c.str("name") ?: "") }
                var email by remember(c) { mutableStateOf(c.str("email") ?: "") }
                var address by remember(c) { mutableStateOf(c.str("address") ?: "") }
                var need by remember(c) { mutableStateOf(c.str("need") ?: "") }
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(address, { address = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(need, { need = it }, label = { Text("What they need") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tool("Save", !busy) { vm.editContact(phone, name, email, address, need) }
                    Tool(if (archived) "Unarchive" else "Archive", !busy) { vm.archiveContact(phone, !archived) }
                    Tool("Delete", !busy) { confirm = c }
                }
                c.objOf("writers")?.objOf("delete")?.takeIf { it.boolOf("allowed") == false }?.str("reason")?.let {
                    Text("Delete isn't open for this card: $it Archive keeps it off the active cards instead.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        OfficeNoteLine(vm)
    }
    confirm?.let { c ->
        val phone = c.str("phone") ?: ""
        ConfirmDelete("the card for ${c.str("name") ?: prettyPhone(phone) ?: phone}", onYes = { vm.deleteContact(phone); confirm = null; open = null }, onNo = { confirm = null })
    }
}

// ------------------------------------------------------------------ archived jobs: kept on the record; Reopen puts one back as it was

@Composable
fun ArchivedJobsCard(vm: HoccViewModel) {
    val jobs by vm.jobs.collectAsState()
    val busy by vm.officeBusy.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.loadJobs() }
    val archived = jobs.data?.objects().orEmpty().filter { it.boolOf("done") == true }.sortedByDescending { it.str("updated_at") ?: "" }
    HoccCard("Archived jobs (${archived.size})") {
        Text("Kept on the record for good, off the active lists. Reopen puts a job back at the standing it had.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (archived.isEmpty()) EmptyLine("No archived jobs.")
        (if (expanded) archived else archived.take(5)).forEach { p ->
            val pid = p.num("id")?.toInt() ?: return@forEach
            Divider()
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(p.str("client_name", "name") ?: "Job #$pid", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(listOfNotNull(p.str("name")?.takeIf { it != p.str("client_name") }, p.str("address")).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Tool("Reopen", !busy) { vm.reopenJob(pid) }
            }
        }
        if (archived.size > 5) TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Show fewer" else "Show all ${archived.size}") }
        OfficeNoteLine(vm)
    }
}

// ------------------------------------------------------------------ the trash: what was deleted, newest first, each with Restore

@Composable
fun TrashCard(vm: HoccViewModel) {
    val state by vm.trash.collectAsState()
    val busy by vm.officeBusy.collectAsState()
    LaunchedEffect(Unit) { vm.loadTrash() }
    val items = state.data.orEmpty()
    HoccCard("Trash (${items.size})") {
        Text("Everything deleted - from here, the Office or Caroline - waits here. Restore puts it back exactly as it was.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (state.loading && state.data == null) LoadingLine()
        state.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        if (!state.loading && items.isEmpty() && state.error == null) EmptyLine("The trash is empty.")
        items.forEach { t ->
            val tid = t.num("id")?.toLong() ?: return@forEach
            Divider()
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t.str("label") ?: t.str("ref") ?: "", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2)
                    Text(listOfNotNull(t.str("kind_words", "kind"), t.str("deleted_at")?.let { serverTimeToLocal(it) ?: it }, t.str("via")?.let { "from $it" }).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Tool("Restore", !busy) { vm.restoreFromTrash(tid) }
            }
        }
        TextButton(onClick = { vm.loadTrash() }) { Text("Read the trash again") }
        OfficeNoteLine(vm)
    }
}

// ------------------------------------------------------------------ booked visits: edit any field, cancel (calendar first), book again

@Composable
fun VisitsCard(vm: HoccViewModel) {
    val state by vm.visits.collectAsState()
    val busy by vm.officeBusy.collectAsState()
    var open by remember { mutableStateOf<Long?>(null) }
    var confirm by remember { mutableStateOf<JsonObject?>(null) }
    LaunchedEffect(Unit) { vm.loadVisits() }
    val rows = state.data.orEmpty()
    HoccCard("Booked visits (${rows.count { it.str("status") == "booked" }})") {
        Text("Tap a visit to fix anything on it - nothing is required. A new time moves the calendar too, or nothing changes.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (state.loading && state.data == null) LoadingLine()
        state.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        if (!state.loading && rows.isEmpty() && state.error == null) EmptyLine("No visits booked in the next two months.")
        rows.forEach { v ->
            val vid = v.num("id")?.toLong() ?: return@forEach
            val status = v.str("status") ?: ""
            Divider()
            Row(Modifier.fillMaxWidth().clickable { open = if (open == vid) null else vid }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(listOfNotNull(v.str("name") ?: prettyPhone(v.str("phone")), v.str("project")?.takeIf { it.isNotBlank() }).joinToString(" - "),
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2)
                    Text(listOfNotNull(v.str("start_at_local"), v.str("address")?.takeIf { it.isNotBlank() } ?: "no address yet").joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (status != "booked") humanStatus(status)?.let { StatusChip(it) }
            }
            if (open == vid) {
                var name by remember(v) { mutableStateOf(v.str("name") ?: "") }
                var phone by remember(v) { mutableStateOf(v.str("phone") ?: "") }
                var address by remember(v) { mutableStateOf(v.str("address") ?: "") }
                var forWhat by remember(v) { mutableStateOf(v.str("project") ?: "") }
                var start by remember(v) { mutableStateOf(v.str("start_at_local") ?: "") }
                OutlinedTextField(name, { name = it }, label = { Text("Who") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(address, { address = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(forWhat, { forWhat = it }, label = { Text("What it's for") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                OutlinedTextField(start, { start = it }, label = { Text("When (2026-11-09 10:00)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tool("Save", !busy) {
                        vm.editVisit(vid,
                            name.takeIf { it != (v.str("name") ?: "") }, phone.takeIf { it != (v.str("phone") ?: "") },
                            address.takeIf { it != (v.str("address") ?: "") }, forWhat.takeIf { it != (v.str("project") ?: "") },
                            start.takeIf { it != (v.str("start_at_local") ?: "") })
                    }
                    if (status == "cancelled") Tool("Book it again", !busy) { vm.restoreVisit(vid) }
                    else Tool("Cancel visit", !busy) { confirm = v }
                }
            }
        }
        OfficeNoteLine(vm)
    }
    confirm?.let { v ->
        val vid = v.num("id")?.toLong() ?: 0L
        ConfirmDelete("the visit with ${v.str("name") ?: prettyPhone(v.str("phone")) ?: "this client"}", verb = "Cancel",
            body = "It comes off the calendar and reads cancelled - it stays on the record, and \"Book it again\" puts it back.",
            onYes = { vm.cancelVisit(vid); confirm = null }, onNo = { confirm = null })
    }
}
