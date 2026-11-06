package com.constructionmanager.ui.screens.hocc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.constructionmanager.data.caroline.serverTimeToLocal
import com.google.gson.JsonArray
import com.google.gson.JsonObject

// ------------------------------------------------------------------ Jobs

@Composable
fun JobsScreen(vm: HoccViewModel, openJobId: Int?, onOpenJob: (Int) -> Unit, onCloseJob: () -> Unit) {
    if (openJobId != null) {
        JobDetailView(vm, openJobId, onCloseJob)
        return
    }
    val state by vm.jobs.collectAsState()
    var query by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.loadJobs() }
    ScreenColumn(title = "Jobs", onRefresh = { vm.loadJobs() }) {
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            label = { Text("Search jobs, clients, addresses") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        if (state.loading && state.data == null) LoadingLine()
        state.error?.let { ErrorLine(it) { vm.loadJobs() } }
        val all = state.data?.objects().orEmpty()
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) all else all.filter { p ->
            listOfNotNull(p.str("name"), p.str("client_name"), p.str("address"), p.str("status"))
                .any { it.lowercase().contains(q) }
        }
        val open = filtered.filter { it.boolOf("open") == true }.sortedBy { (it.str("name") ?: "").lowercase() }
        val done = filtered.filter { it.boolOf("done") == true }.sortedBy { (it.str("name") ?: "").lowercase() }
        val rest = filtered.filter { it.boolOf("open") != true && it.boolOf("done") != true }
            .sortedBy { (it.str("name") ?: "").lowercase() }
        if (all.isNotEmpty() && filtered.isEmpty()) EmptyLine("No job matches that search.")
        if (open.isNotEmpty()) {
            HoccCard("Open jobs (${open.size})") { open.forEach { p -> JobRow(p, onOpenJob) } }
        }
        if (rest.isNotEmpty()) {
            HoccCard("Leads & other (${rest.size})") { rest.forEach { p -> JobRow(p, onOpenJob) } }
        }
        if (done.isNotEmpty()) {
            HoccCard("Archived - completed (${done.size})") { done.forEach { p -> JobRow(p, onOpenJob) } }
        }
        if (all.isEmpty() && !state.loading && state.error == null) {
            EmptyLine("No jobs on file yet.")
        }
    }
}

@Composable
private fun JobRow(p: JsonObject, onOpen: (Int) -> Unit) {
    val id = p.num("id")?.toInt()
    Column(
        Modifier.fillMaxWidth().clickable(enabled = id != null) { id?.let(onOpen) }.padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(p.str("name") ?: "Job", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            p.str("status")?.let { humanStatus(it)?.let { s -> StatusChip(s) } }
        }
        p.str("address")?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        val sub = listOfNotNull(p.str("client_name", "client"), money(p.num("budget"))).joinToString(" · ")
        if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ------------------------------------------------------------------ one job, in full

@Composable
private fun JobDetailView(vm: HoccViewModel, id: Int, onBack: () -> Unit) {
    val state by vm.jobDetail.collectAsState()
    LaunchedEffect(id) { vm.openJob(id) }
    ScreenColumn(title = "Job", onRefresh = { vm.openJob(id) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back to jobs") }
            Text("Back to jobs", style = MaterialTheme.typography.bodyMedium)
        }
        if (state?.loading == true && state?.data == null) LoadingLine()
        state?.error?.let { ErrorLine(it) { vm.openJob(id) } }
        val d = state?.data
        if (d != null) {
            val card = d.card
            HoccCard(card?.str("name") ?: "Job") {
                card?.str("say")?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                card?.let {
                    KvRow("Client", it.str("client_name", "client"))
                    KvRow("Phone", it.str("client_phone", "phone"))
                    KvRow("Address", it.str("address"))
                    KvRow("Status", humanStatus(it.str("status")))
                    KvRow("Phase", humanStatus(it.str("current_phase", "phase")))
                    KvRow("Budget", money(it.num("budget")))
                    it.str("scope")?.let { s ->
                        Text("Scope", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(s, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Estimate
            val estObj = d.estimate?.asObjOrNull()
            val estLines = d.estimate?.rowsOf("lines", "items", "sections") ?: JsonArray()
            HoccCard("Estimate") {
                if (estObj == null && estLines.isEmpty()) {
                    EmptyLine("No estimate on this job yet.")
                } else {
                    estObj?.let {
                        KvRow("Estimate", it.str("number", "label", "title"))
                        KvRow("Status", humanStatus(it.str("status")))
                        KvRow("Total", money(it.num("total", "grand_total", "amount")))
                    }
                    estLines.objects().take(25).forEach { line ->
                        val desc = line.str("description", "name", "title", "item") ?: return@forEach
                        val amt = money(line.num("amount", "line_total", "total", "price"))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(desc, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            if (amt != null) Text(amt, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Takeoff
            val tkLines = d.takeoffs?.rowsOf("lines", "items", "takeoffs") ?: JsonArray()
            HoccCard("Takeoff (${tkLines.size()} lines)") {
                if (tkLines.isEmpty()) EmptyLine("No takeoff lines on this job yet.")
                tkLines.objects().take(30).forEach { line ->
                    val desc = line.str("description", "name", "title", "item") ?: return@forEach
                    val qty = line.num("qty", "quantity")
                    val unit = line.str("unit", "uom")
                    val right = listOfNotNull(qty?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }, unit).joinToString(" ")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(desc, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (right.isNotBlank()) Text(right, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Tasks
            val taskRows = d.tasks?.rowsOf("tasks", "items") ?: JsonArray()
            HoccCard("Tasks (${taskRows.size()})") {
                if (taskRows.isEmpty()) EmptyLine("No tasks on this job.")
                taskRows.objects().take(20).forEach { t ->
                    val title = t.str("title", "name", "what", "description") ?: return@forEach
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        t.str("status", "state")?.let { s -> humanStatus(s)?.let { StatusChip(it) } }
                    }
                    t.str("due", "due_at", "when")?.let { due ->
                        Text("Due ${serverTimeToLocal(due) ?: due}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Hours
            val hourRows = d.hours?.rowsOf("entries", "rows", "hours") ?: JsonArray()
            val totalHours = hourRows.objects().sumOf { it.num("hours") ?: 0.0 }
            HoccCard("Hours (${String.format("%.1f", totalHours)} logged)") {
                if (hourRows.isEmpty()) EmptyLine("No hours logged on this job in the last year.")
                hourRows.objects().take(15).forEach { h ->
                    val day = h.str("work_date", "date")
                    val hrs = h.num("hours")
                    val note = h.str("note", "category")
                    Text(
                        listOfNotNull(day, hrs?.let { "${it}h" }, note).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Schedule

@Composable
fun ScheduleScreen(vm: HoccViewModel) {
    val state by vm.schedule.collectAsState()
    LaunchedEffect(Unit) { vm.loadSchedule() }
    ScreenColumn(title = "Schedule", onRefresh = { vm.loadSchedule(); vm.loadVisits() }) {
        if (state.loading && state.data == null) LoadingLine()
        state.error?.let { ErrorLine(it) { vm.loadSchedule() } }
        // the booked visits as the record holds them - edit, cancel, book again (0.7.4, Tyler Nov 6 2026)
        VisitsCard(vm)
        val d = state.data
        if (d != null) {
            val sheet = d.sheet
            val dateLine = sheet?.str("date")?.let { "Day sheet - $it" } ?: "Day sheet"
            var rendered = false
            sheet?.let { s ->
                val sections = s.arraysOfObjects()
                if (sections.isNotEmpty()) {
                    rendered = true
                    HoccCard(dateLine) {
                        sections.forEach { (key, arr) ->
                            Text("${humanStatus(key) ?: key} (${arr.size()})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            arr.objects().take(12).forEach { item -> ScheduleRow(item) }
                        }
                    }
                }
                s.str("summary", "note", "text")?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
            d.day?.let { day ->
                val sections = day.arraysOfObjects()
                if (sections.isNotEmpty()) {
                    HoccCard("Commitments") {
                        sections.forEach { (key, arr) ->
                            Text("${humanStatus(key) ?: key} (${arr.size()})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            arr.objects().take(12).forEach { item -> ScheduleRow(item) }
                        }
                    }
                    rendered = true
                }
            }
            if (d.appointments.size() > 0) {
                HoccCard("Calendar - next two weeks (${d.appointments.size()})") {
                    d.appointments.objects().take(20).forEach { ev ->
                        val title = ev.str("summary", "title", "name") ?: return@forEach
                        val start = ev.str("start", "start_at", "when")?.let { serverTimeToLocal(it) ?: it }
                        val place = ev.str("location", "address")
                        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        val sub = listOfNotNull(start, place).joinToString(" · ")
                        if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                rendered = true
            }
            if (!rendered) EmptyLine("Nothing scheduled right now.")
        }
    }
}

@Composable
private fun ScheduleRow(item: JsonObject) {
    val title = item.str("title", "what", "summary", "project", "who", "name") ?: "(untitled)"
    val whenText = item.str("start", "time", "at", "when", "start_at")?.let { serverTimeToLocal(it) ?: it }
    val place = item.str("address", "place", "location")
    Column(Modifier.padding(vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            item.str("status", "state")?.let { s -> humanStatus(s)?.let { StatusChip(it) } }
        }
        val sub = listOfNotNull(whenText, place).joinToString(" · ")
        if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
