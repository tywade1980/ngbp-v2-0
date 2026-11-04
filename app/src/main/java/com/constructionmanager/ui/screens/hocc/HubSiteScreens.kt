package com.constructionmanager.ui.screens.hocc

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.constructionmanager.data.caroline.serverTimeToLocal
import com.constructionmanager.data.location.LocationReader

// ------------------------------------------------------------------ Hub

@Composable
fun HubScreen(vm: HoccViewModel, onGoJobs: () -> Unit, onGoSite: () -> Unit) {
    val state by vm.hub.collectAsState()
    LaunchedEffect(Unit) { vm.loadHub() }
    ScreenColumn(title = "Hub", onRefresh = { vm.loadHub() }) {
        if (state.loading && state.data == null) LoadingLine()
        state.error?.let { ErrorLine(it) { vm.loadHub() } }
        val d = state.data
        if (d != null) {
            d.home?.let { home ->
                val weather = home.objOf("weather")
                val wText = weather?.str("summary", "conditions", "text") ?: home.str("weather", "conditions")
                val wTemp = weather?.num("temp", "temperature") ?: home.num("temperature")
                if (wText != null || wTemp != null) {
                    HoccCard("Outside") {
                        Text(listOfNotNull(wTemp?.let { "${it.toInt()}°" }, wText).joinToString(" · "), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            val projects = d.projects.objects()
            val openCount = projects.count { it.boolOf("open") == true }
            val siteCount = projects.count { it.boolOf("site") == true }
            HoccCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("$openCount", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("open jobs", style = MaterialTheme.typography.bodyMedium)
                    }
                    Column {
                        Text("$siteCount", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("sites", style = MaterialTheme.typography.bodyMedium)
                    }
                    Column {
                        Text("${projects.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("projects", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onGoJobs) { Text("Jobs") }
                    Button(onClick = onGoSite) { Text("Sites") }
                }
            }
            if (!d.briefing.isNullOrBlank()) {
                HoccCard("Briefing") {
                    Text(d.briefing, style = MaterialTheme.typography.bodyMedium)
                }
            }
            d.day?.let { day ->
                val sections = day.arraysOfObjects()
                if (sections.isNotEmpty()) {
                    HoccCard("Today") {
                        sections.forEach { (key, arr) ->
                            Text(humanStatus(key) ?: key, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            arr.objects().take(8).forEach { item ->
                                val title = item.str("title", "what", "summary", "project", "who", "name") ?: "(untitled)"
                                val whenText = item.str("start", "time", "at", "when")?.let { serverTimeToLocal(it) ?: it }
                                val place = item.str("address", "place", "location")
                                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                listOfNotNull(whenText, place).takeIf { it.isNotEmpty() }?.let {
                                    Text(it.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            if (d.calls.isNotEmpty()) {
                HoccCard("Recent calls") {
                    d.calls.forEach { c ->
                        val who = c.callerName ?: c.partyNumber ?: "Unknown caller"
                        val whenText = serverTimeToLocal(c.startedAt)
                        Text(who, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        val sub = listOfNotNull(whenText, c.outcome ?: c.leadStatus, c.summary).joinToString(" · ")
                        if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (d.briefing.isNullOrBlank() && d.calls.isEmpty()) {
                EmptyLine("Nothing here yet - the briefing and today's calls land here.")
            }
        }
    }
}

// ------------------------------------------------------------------ Site

@Composable
fun SiteScreen(vm: HoccViewModel, onOpenJob: (Int) -> Unit) {
    val state by vm.site.collectAsState()
    val loc by vm.locationUi.collectAsState()
    val context = LocalContext.current
    var asked by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        vm.readLocation(context)
    }
    LaunchedEffect(Unit) {
        vm.loadSite()
        if (!asked) {
            asked = true
            if (LocationReader.hasPermission(context)) vm.readLocation(context)
            else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    ScreenColumn(title = "Site", onRefresh = { vm.loadSite(); vm.readLocation(context) }) {
        if (state.loading && state.data == null) LoadingLine()
        state.error?.let { ErrorLine(it) { vm.loadSite() } }

        HoccCard("You are here") {
            val summary = state.data?.where?.str("summary")
            if (!summary.isNullOrBlank()) {
                Text(summary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            state.data?.where?.let { w ->
                KvRow("Place", w.str("place", "place_name", "label"))
                KvRow("Address", w.str("address"))
                KvRow("Since", w.str("since", "arrived_at", "at")?.let { serverTimeToLocal(it) ?: it })
            }
            if (loc.reading) LoadingLine("Reading this phone's location…")
            loc.fix?.let { f ->
                val acc = f.accuracyM?.let { " ±${it.toInt()} m" } ?: ""
                Text(
                    "Phone fix: ${String.format("%.5f", f.lat)}, ${String.format("%.5f", f.lng)}$acc",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            loc.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Button(onClick = { vm.readLocation(context) }, enabled = !loc.reading) { Text("Read my location") }
        }

        val d = state.data
        if (d != null) {
            val all = d.projects.objects()
            val sites = all.filter { it.boolOf("site") == true }
                .sortedBy { (it.str("name") ?: "").lowercase() }
            val leads = all.count { it.boolOf("site") != true && it.str("status") == "lead" }
            HoccCard("Sites (${sites.size})") {
                if (sites.isEmpty()) EmptyLine("No sites on file yet - a project becomes a site once it is estimating, quoted, approved or underway.")
                sites.forEach { p ->
                    SiteRow(p) { id -> onOpenJob(id) }
                }
            }
            if (leads > 0) {
                Text(
                    "$leads ${if (leads == 1) "lead is" else "leads are"} not sites - leads live under Jobs until they become projects.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SiteRow(p: com.google.gson.JsonObject, onOpen: (Int) -> Unit) {
    val id = p.num("id")?.toInt()
    Column(
        Modifier.fillMaxWidth().clickable(enabled = id != null) { id?.let(onOpen) }.padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(p.str("address") ?: p.str("name") ?: "Site", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            p.str("status")?.let { humanStatus(it)?.let { s -> StatusChip(s) } }
        }
        val nameLine = p.str("name")
        val addrLine = p.str("address")
        if (!nameLine.isNullOrBlank() && !addrLine.isNullOrBlank() && nameLine != addrLine) {
            Text(nameLine, style = MaterialTheme.typography.bodyMedium)
        }
        p.str("client_name", "client")?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
