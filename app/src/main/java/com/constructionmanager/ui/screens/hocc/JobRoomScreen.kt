package com.constructionmanager.ui.screens.hocc

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.constructionmanager.data.caroline.serverTimeToLocal
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

/*
 * THE ROOM - one job as one screen, drawn to Tyler's "Wade OS Job Board" design file (Oct 9-11 2026, "this is what I
 * expected to see ... near identical", Nov 2). Opened Fold / wide: the header bar (Clients drawer, today, owner,
 * assistant) and the three-column board - Estimates / Job schedule / Daily logs | the client card and Caroline |
 * Scope / Selections / Photos - with the punch list under. Folded: the same cards stacked in his order, reorderable.
 * Every card writes; after every write the record is read again.
 */

data class RoomKey(val kind: String, val value: String, val projectIds: List<Int>, val startId: Int?) {
    companion object {
        const val ADDRESS = "address"
        const val CLIENT = "client"
        const val JOB = "job"
    }
}

private val DATE = DateTimeFormatter.ISO_LOCAL_DATE
private val MDAY = DateTimeFormatter.ofPattern("MMM d", Locale.US)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun JobRoomScreen(room: RoomViewModel, key: RoomKey, projects: List<JsonObject>, onBack: (() -> Unit)?, onOpenRoom: (RoomKey) -> Unit,
                  client: (@Composable (phone: String?, label: String?, onBack: () -> Unit, onOpenProject: (Int) -> Unit) -> Unit)? = null,
                  readJson: (suspend (String) -> JsonObject?)? = null) {
    val context = LocalContext.current
    var selectedId by rememberSaveable(key.value) { mutableStateOf(key.startId ?: key.projectIds.firstOrNull()) }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    // a client opened from anywhere on the wheel (the job card, a lead, a new caller): the client wheel over this one; back comes here
    var clientOpen by rememberSaveable { mutableStateOf<String?>(null) }      // "+1614..." or "label:Spam Risk"
    // THE WHEEL (Tyler, Nov 4 2026: "hub and spoke, left, center, right... each main page of any spoke off the main wheel has a left,
    // center and right: its own hub and spoke"). Home is the main wheel; a spoke opened is a wheel of its own; back goes up one wheel.
    var wheel by rememberSaveable { mutableStateOf(Wheel.HOME.name) }
    val state by room.room.collectAsState()
    val note by room.note.collectAsState()
    val busy by room.busy.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { room.remember(context); room.loadOrder(context) }
    LaunchedEffect(selectedId) { selectedId?.let { room.open(it) } }
    BackHandler(enabled = clientOpen != null || expanded != null || wheel != Wheel.HOME.name) {
        if (clientOpen != null) clientOpen = null else if (expanded != null) expanded = null else wheel = Wheel.HOME.name
    }
    val openClient: (String?, String?) -> Unit = { phone, label -> clientOpen = phone ?: ("label:" + (label ?: "")) }
    val co = clientOpen
    if (co != null && client != null) {
        val isLabel = co.startsWith("label:")
        client(if (isLabel) null else co, if (isLabel) co.removePrefix("label:") else null, { clientOpen = null },
               { pid -> clientOpen = null; onOpenRoom(RoomKey(RoomKey.JOB, "Job #$pid", listOf(pid), pid)); wheel = Wheel.HOME.name })
        return
    }

    val data = state?.data?.takeIf { it.id == selectedId }
    val id = selectedId

    Column(Modifier.fillMaxSize().background(Board.ground).imePadding()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // the Fold's open screen (Tyler, Nov 6 2026: "this is what i wanna see in hocc when i open the fold to is its whole screen"). The
            // Fold7's inner screen reads ~650-750 dp wide depending on its screen zoom, so 700 dp missed it; the cover screen is ~410 dp.
            val board = maxWidth >= 600.dp && maxHeight >= 480.dp
            Column(Modifier.fillMaxSize()) {
                val w = runCatching { Wheel.valueOf(wheel) }.getOrDefault(Wheel.HOME)
                HeaderBar(projects.count { it.boolOf("open") == true }, board,
                    onBack = if (expanded != null) ({ expanded = null }) else if (w != Wheel.HOME) ({ wheel = Wheel.HOME.name }) else onBack,
                    onLeads = { expanded = null; wheel = Wheel.LEADS.name }, onRefresh = { room.refresh() }, writing = busy != null)
                if (key.projectIds.size > 1) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(key.projectIds) { pid ->
                            val p = projects.firstOrNull { it.num("id")?.toInt() == pid }
                            FilterChip(selected = pid == selectedId, onClick = { selectedId = pid; expanded = null },
                                label = { Text(p?.str("name") ?: "Job #$pid", maxLines = 1) })
                        }
                    }
                }
                note?.let { n ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(n.text, style = MaterialTheme.typography.bodySmall, color = if (n.isError) Board.red else Board.blue, modifier = Modifier.weight(1f))
                        TextButton(onClick = { room.clearNote() }) { Text("ok") }
                    }
                }
                if (state?.loading == true && data == null) LoadingLine("Opening the room…")
                state?.error?.let { ErrorLine(it) { room.refresh() } }
                val ex = expanded?.let { RoomCard.byKey(it) }
                val pick: (RoomKey) -> Unit = { k -> expanded = null; onOpenRoom(k); wheel = Wheel.HOME.name }
                when {
                    ex != null && data != null && id != null -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp)) {
                        CardFor(ex, room, data, busy, full = true, onOpen = {})
                    }
                    board -> Box(Modifier.fillMaxSize()) {
                        androidx.compose.runtime.CompositionLocalProvider(LocalOpenClient provides openClient) {
                            BoardLayout(room, data, busy, projects, id, pick, onOpen = { expanded = it.key })
                        }
                        // the Clients drawer of his design file: the leads and clients by where each stands, over the board's left edge
                        if (w != Wheel.HOME) {
                            Box(Modifier.fillMaxSize().background(Color(0x731B2229)).clickable { wheel = Wheel.HOME.name })
                            Column(Modifier.fillMaxHeight().width(360.dp).background(Color.White).verticalScroll(rememberScrollState()).padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Disp("Clients", 26, modifier = Modifier.weight(1f))
                                    Tool("Close") { wheel = Wheel.HOME.name }
                                }
                                LeadsPage(projects, id, pick, room, busy != null)
                            }
                        }
                    }
                    else -> androidx.compose.runtime.CompositionLocalProvider(LocalOpenClient provides openClient) { androidx.compose.runtime.key(wheel) {
                        // the phone: a wheel is three pages - left spoke, hub, right spoke - opened on the hub. Swipe or tap a name.
                        val pager = androidx.compose.foundation.pager.rememberPagerState(initialPage = 1) { 3 }
                        PageStrip(w, pager.currentPage, onGo = { scope.launch { pager.animateScrollToPage(it) } }, onUp = { wheel = Wheel.HOME.name })
                        androidx.compose.foundation.pager.HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), beyondBoundsPageCount = 1) { page ->
                            val open: (Wheel) -> Unit = { wheel = it.name }
                            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                when (w) {
                                    // PARENT AND CHILD (Tyler, Nov 4 2026): a hub's left and right are its two closest children; the bottom wheel is
                                    // the rest; any child opened is a hub of its own. Everything within three taps of home.
                                    Wheel.HOME -> when (page) {
                                        0 -> { OpenSpoke("Leads", "Open the leads wheel: new callers on its left, the clients on its right, site visits below.") { open(Wheel.LEADS) }
                                               LeadsPage(projects, id, pick, room, busy != null) }
                                        1 -> if (data != null) {
                                            CardFor(RoomCard.Vitals, room, data, busy, false) { expanded = RoomCard.Vitals.key }
                                            CardFor(RoomCard.Caroline, room, data, busy, false) { expanded = RoomCard.Caroline.key }
                                            OpenSpoke("Schedule", "Open the schedule wheel: daily logs on its left, the punch list on its right, the week below.") { open(Wheel.SCHEDULE) }
                                            CardFor(RoomCard.Schedule, room, data, busy, false) { expanded = RoomCard.Schedule.key }
                                            CardFor(RoomCard.Logs, room, data, busy, false) { expanded = RoomCard.Logs.key }
                                            CardFor(RoomCard.Punch, room, data, busy, false) { expanded = RoomCard.Punch.key }
                                            CardFor(RoomCard.SiteHours, room, data, busy, false) { expanded = RoomCard.SiteHours.key }
                                        }
                                        else -> if (data != null) {
                                            OpenSpoke("Estimate", "Open the estimate wheel: scope and takeoff on its left, the money on its right, selections and photos below.") { open(Wheel.ESTIMATES) }
                                            CardFor(RoomCard.Estimate, room, data, busy, false) { expanded = RoomCard.Estimate.key }
                                            CardFor(RoomCard.Scope, room, data, busy, false) { expanded = RoomCard.Scope.key }
                                            CardFor(RoomCard.Selections, room, data, busy, false) { expanded = RoomCard.Selections.key }
                                            CardFor(RoomCard.Photos, room, data, busy, false) { expanded = RoomCard.Photos.key }
                                            CardFor(RoomCard.Texts, room, data, busy, false) { expanded = RoomCard.Texts.key }
                                            CardFor(RoomCard.Calls, room, data, busy, false) { expanded = RoomCard.Calls.key }
                                            CardFor(RoomCard.Recordings, room, data, busy, false) { expanded = RoomCard.Recordings.key }
                                        }
                                    }
                                    Wheel.LEADS -> when (page) {
                                        0 -> NewCallersPage(readJson)
                                        1 -> { LeadsPage(projects, id, pick, room, busy != null); Lbl("Site visits", modifier = Modifier.padding(top = 4.dp)); VisitsStrip(readJson, 14) }
                                        else -> ClientsPage(projects, id, pick)
                                    }
                                    Wheel.ESTIMATES -> if (data != null) when (page) {
                                        0 -> { CardFor(RoomCard.Scope, room, data, busy, true) {} }
                                        1 -> { CardFor(RoomCard.Estimate, room, data, busy, true) {}
                                               Lbl("Under the estimate", modifier = Modifier.padding(top = 4.dp))
                                               CardFor(RoomCard.Selections, room, data, busy, false) { expanded = RoomCard.Selections.key }
                                               CardFor(RoomCard.Photos, room, data, busy, false) { expanded = RoomCard.Photos.key } }
                                        else -> MoneyPage(room, data, busy != null)
                                    }
                                    Wheel.SCHEDULE -> if (data != null) when (page) {
                                        0 -> CardFor(RoomCard.Logs, room, data, busy, true) {}
                                        1 -> { CardFor(RoomCard.Schedule, room, data, busy, true) {}; Lbl("This week", modifier = Modifier.padding(top = 4.dp)); VisitsStrip(readJson, 7) }
                                        else -> CardFor(RoomCard.Punch, room, data, busy, true) {}
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    } }
                }
            }
        }
    }
}

/** A wheel: a hub, its two closest children as the left and right spoke, the rest as the bottom wheel. Home is the project; each
 *  spoke opened is a wheel of its own (a child become a parent). The client wheel lives in ClientWheel.kt. */
enum class Wheel(val left: String, val hub: String, val right: String) {
    HOME("Leads", "Project", "Estimate"),
    LEADS("New callers", "Leads", "Clients"),
    ESTIMATES("Scope & takeoff", "Estimate", "Money"),
    SCHEDULE("Daily logs", "Job schedule", "Punch list"),
}

/** Opens the client wheel for a number (or a caller-ID label with no number) from anywhere on a wheel. */
val LocalOpenClient = androidx.compose.runtime.compositionLocalOf<(String?, String?) -> Unit> { { _, _ -> } }

/** New callers: people who called or texted and are not on file yet - where leads come from. The log's data, not the log. */
@Composable
private fun NewCallersPage(readJson: (suspend (String) -> JsonObject?)?) {
    val openClient = LocalOpenClient.current
    var people by remember { mutableStateOf<List<JsonObject>?>(null) }
    LaunchedEffect(Unit) { people = readJson?.invoke("log/people?limit=300")?.arrOf("people")?.objects()?.filter { it.get("on_record")?.asBoolean != true }.orEmpty() }
    BoardCard {
        Disp("New callers", 24)
        Text("Called or texted, not on file yet. Tap one to open them; put them on file as a lead from there or from the job card.", style = MaterialTheme.typography.bodySmall, color = Board.grey)
        when {
            readJson == null -> EmptyLine("Not available here.")
            people == null -> LoadingLine("Reading…")
            people!!.isEmpty() -> EmptyLine("Nobody new.")
            else -> people!!.take(60).forEach { p ->
                val phone = p.str("phone"); val label = p.str("label")
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).clickable { openClient(phone, label) }.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(p.str("name") ?: prettyPhone(phone) ?: "No number", style = MaterialTheme.typography.titleSmall, color = Board.ink, modifier = Modifier.weight(1f))
                        Text(whenShort(p.str("last_at")), style = MaterialTheme.typography.labelSmall, color = Board.grey)
                    }
                    val line = listOfNotNull(p.str("snippet")?.takeIf { it.isNotBlank() }, countLabel(p.num("calls")?.toInt() ?: 0, "call").takeIf { (p.num("calls")?.toInt() ?: 0) > 0 },
                        countLabel(p.num("texts")?.toInt() ?: 0, "text").takeIf { (p.num("texts")?.toInt() ?: 0) > 0 }).joinToString(" · ")
                    if (line.isNotBlank()) Text(line, style = MaterialTheme.typography.bodySmall, color = Board.grey, maxLines = 2)
                }
            }
        }
    }
}

/** The bottom wheel under Leads and under the Schedule: what is on the calendar for the next [days] days. */
@Composable
private fun VisitsStrip(readJson: (suspend (String) -> JsonObject?)?, days: Int) {
    var items by remember { mutableStateOf<List<JsonObject>?>(null) }
    LaunchedEffect(days) {
        val r = readJson?.invoke("appointments?days=$days")
        items = (r?.arrOf("appointments", "events", "items") ?: r?.rowsOf("appointments", "events", "items"))?.objects().orEmpty()
    }
    BoardCard {
        when {
            readJson == null -> EmptyLine("Not available here.")
            items == null -> LoadingLine("Reading the calendar…")
            items!!.isEmpty() -> EmptyLine("Nothing on the calendar for the next $days days.")
            else -> items!!.take(20).forEach { ev ->
                val title = ev.str("summary", "title", "name") ?: return@forEach
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                val sub = listOfNotNull(ev.str("start", "start_at", "when")?.let { serverTimeToLocal(it) ?: it }, ev.str("location", "address")).joinToString(" · ")
                if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = Board.grey)
            }
        }
    }
}

/** The estimate's right spoke: the money - what the bill says, what is paid, what is owed. */
@Composable
private fun MoneyPage(room: RoomViewModel, d: RoomData, writing: Boolean) {
    val bill = d.billing?.objOf("bill")
    val est = d.estimate?.asObjOrNull()
    val contract = bill?.str("total")?.toDoubleOrNull() ?: est?.objOf("totals")?.num("total", "grand_total") ?: d.card?.num("budget")
    val paid = bill?.str("paid")?.toDoubleOrNull()
    val balance = bill?.str("balance")?.toDoubleOrNull() ?: (if (contract != null && paid != null) contract - paid else null)
    BoardCard {
        Disp("Money", 24)
        Text(d.billing?.str("stage")?.let { "Billing: $it" } ?: (if (est != null) "No bill yet - the estimate's total stands in for it." else "No estimate and no bill on file for this project."), style = MaterialTheme.typography.bodySmall, color = Board.grey)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile("Contract", money(contract), modifier = Modifier.weight(1f), approx = bill == null && contract != null)
            Tile("Paid", money(paid ?: 0.0), Board.green, Modifier.weight(1f), approx = bill == null)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile("Balance", money(balance), Board.rust, Modifier.weight(1f), approx = bill == null && balance != null)
            Tile("Hours worked", fmtH(d.hours?.rowsOf("entries")?.objects().orEmpty().sumOf { it.num("hours") ?: 0.0 }).replace(" h", " hr"), modifier = Modifier.weight(1f))
        }
        val payments = d.billing?.rowsOf("payments")?.objects().orEmpty()
        if (payments.isNotEmpty()) {
            Lbl("Payments")
            payments.forEach { p -> KvRow(p.str("received_at", "at", "date")?.let { serverTimeToLocal(it) ?: it } ?: "Payment", money(p.num("amount", "total"))) }
        }
        val invoices = d.billing?.rowsOf("invoices")?.objects().orEmpty()
        if (invoices.isNotEmpty()) {
            Lbl("Invoices")
            invoices.forEach { i -> KvRow((i.str("number", "id") ?: "Invoice") + (i.str("status")?.let { " · " + (humanStatus(it) ?: it) } ?: ""), money(i.num("total", "amount"))) }
        }
        if (bill == null && est != null) Row { Tool("To invoice", !writing) { room.convertBilling(d.id) } }
    }
}

/** The line that opens a spoke as its own wheel. */
@Composable
private fun OpenSpoke(name: String, words: String, onOpen: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Board.tile).clickable(onClick = onOpen).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Open $name", style = MaterialTheme.typography.labelLarge, color = Board.blue, fontWeight = FontWeight.SemiBold)
            Text(words, style = MaterialTheme.typography.bodySmall, color = Board.grey)
        }
        Text("›", style = MaterialTheme.typography.headlineSmall, color = Board.blue)
    }
}

/** The clients: the leads that signed, by stage, newest first. */
@Composable
private fun ClientsPage(projects: List<JsonObject>, currentId: Int?, onPick: (RoomKey) -> Unit) {
    // archived jobs (status complete) leave the default view; they stay on the record under Office › Archived jobs (0.7.4)
    val signed = projects.filter { (it.str("status") ?: "").lowercase() in Board.SIGNED && it.boolOf("done") != true }
    BoardCard {
        Disp("Clients", 24)
        Text("The leads that signed: on site, on hold, punch list. Archived jobs are under Office › Archived jobs.", style = MaterialTheme.typography.bodySmall, color = Board.grey)
        if (signed.isEmpty()) EmptyLine("Nobody has signed yet.")
        Board.STAGE_ORDER.forEach { stage ->
            val rows = signed.filter { Board.stage(it.str("status"), it.boolOf("open")).first == stage }.sortedByDescending { it.num("id") ?: 0.0 }
            if (rows.isNotEmpty()) {
                Lbl(stage, modifier = Modifier.padding(top = 6.dp))
                rows.forEach { p -> LeadRow(p, p.num("id")?.toInt() == currentId, onPick) }
            }
        }
    }
}

/** The three pages of the phone, named: Leads · Job · Estimates. Tap one or swipe. */
@Composable
private fun PageStrip(w: Wheel, current: Int, onGo: (Int) -> Unit, onUp: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        if (w != Wheel.HOME) Text("▲ Home", style = MaterialTheme.typography.labelLarge, color = Board.rust, modifier = Modifier.clickable(onClick = onUp).padding(vertical = 4.dp))
        listOf("‹ " + w.left, w.hub, w.right + " ›").forEachIndexed { i, label ->
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (i == current) Board.blue else Board.grey, maxLines = 1,
                modifier = Modifier.clickable { onGo(i) }.padding(vertical = 4.dp))
        }
    }
}

// ------------------------------------------------------------------ the header bar

@Composable
private fun HeaderBar(openCount: Int, board: Boolean, onBack: (() -> Unit)?, onLeads: () -> Unit, onRefresh: () -> Unit, writing: Boolean) {
    val today = LocalDate.now()
    // the board spells the day out; the phone has no room for "Wednesday, November 4" beside the leads button
    val dayLine = if (board) today.dayOfWeek.getDisplayName(JTextStyle.FULL, Locale.US) + ", " + today.month.getDisplayName(JTextStyle.FULL, Locale.US) + " " + today.dayOfMonth
                  else today.dayOfWeek.getDisplayName(JTextStyle.SHORT, Locale.US) + ", " + today.month.getDisplayName(JTextStyle.SHORT, Locale.US) + " " + today.dayOfMonth
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp).clip(RoundedCornerShape(10.dp)).background(Color.White)
            .border(1.dp, Board.border, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Board.ink) }
        Solid(if (board) "☰  Clients  $openCount" else "☰ Leads $openCount", Board.ink, onClick = onLeads)
        Column(Modifier.weight(1f)) {
            if (board) Lbl("Today is")
            Disp(dayLine, if (board) 24 else 17, maxLines = 1)
        }
        if (board) {
            Column(horizontalAlignment = Alignment.End) { Lbl("Owner"); Text("Tyler Wade", style = MaterialTheme.typography.titleSmall) }
            Column(horizontalAlignment = Alignment.End) { Lbl("Assistant"); Text("Caroline", style = MaterialTheme.typography.titleSmall, color = Board.blue) }
        }
        if (writing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Board.blue)
        IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Refresh, contentDescription = "Read it again", tint = Board.grey) }
    }
}

// ------------------------------------------------------------------ the leads page: every lead by where it stands in its life; clients are the ones that signed

@Composable
private fun LeadsPage(projects: List<JsonObject>, currentId: Int?, onPick: (RoomKey) -> Unit, room: RoomViewModel? = null, writing: Boolean = false) {
    var q by rememberSaveable { mutableStateOf("") }
    var adding by rememberSaveable { mutableStateOf(false) }
    val digits = q.filter { it.isDigit() }
    val query = q.trim().lowercase()
    // a lead that signed is a client's project and leaves this page for the Clients spoke (Tyler, Nov 4 2026: "when a lead
    // converts to a project the lead card converts as well and should move from the leads list")
    val shown = projects.filter { p ->
        val st = (p.str("status") ?: "").lowercase()
        st !in Board.SIGNED && st != "cancelled" && st != "complete" && st != "declined" && st !in setOf("done", "completed", "closed", "archived", "paid", "canceled")
    }.filter { p ->
        query.isEmpty() || listOfNotNull(p.str("client_name", "client"), p.str("name"), p.str("address")).any { it.lowercase().contains(query) } ||
            (digits.length >= 3 && (p.str("client_phone", "phone") ?: "").filter { it.isDigit() }.contains(digits))
    }
    BoardCard {
        // the title on its own line, the one button under it (Tyler, Nov 4 2026: "the only thing needed there is the add lead button")
        Disp("Leads", 24)
        if (room != null) Solid("+ New lead", Board.blue, modifier = Modifier.heightIn(min = 36.dp)) { adding = !adding }
        if (adding && room != null) {
            var name by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }
            var address by remember { mutableStateOf("") }
            var need by remember { mutableStateOf("") }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Board.tile).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Who") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(address, { address = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(need, { need = it }, label = { Text("What they need") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Solid("Put it on file", Board.blue, enabled = !writing && (name.isNotBlank() || address.isNotBlank())) {
                        room.newLead(name.trim(), phone.trim(), address.trim(), need.trim()) { nid -> onPick(RoomKey(RoomKey.JOB, address.ifBlank { name }, listOf(nid), nid)) }
                        adding = false
                    }
                    Tool("Cancel") { adding = false }
                }
            }
        }
        OutlinedTextField(q, { q = it }, placeholder = { Text("Find a name, an address or a phone number") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Board.blue, unfocusedBorderColor = Board.toolBorder))
        val groups = shown.groupBy { Board.stage(it.str("status"), it.boolOf("open")) }
        Board.STAGE_ORDER.forEach { stageName ->
            val entry = groups.entries.firstOrNull { it.key.first == stageName } ?: return@forEach
            val (stage, rows) = entry
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(stage.second))
                Lbl(stage.first + " · ${rows.size}")
            }
            rows.sortedByDescending { it.str("updated_at") ?: it.str("created_at") ?: "" }.forEach { p -> LeadRow(p, p.num("id")?.toInt() == currentId, onPick) }
        }
        if (shown.isEmpty()) EmptyLine(if (query.isEmpty()) "No leads on file." else "Nobody on file matches that.")
    }
}

/** One lead or client in a list: who, the job, the address. Tap to open their room. */
@Composable
private fun LeadRow(p: JsonObject, cur: Boolean, onPick: (RoomKey) -> Unit) {
    val pid = p.num("id")?.toInt()
    val client = p.str("client_name", "client")?.takeIf { it.isNotBlank() }
    val job = p.str("name")?.takeIf { it.isNotBlank() }
    val addr = p.str("address")?.takeIf { it.isNotBlank() }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(if (cur) Board.hover else Color.Transparent)
            .clickable(enabled = pid != null) { if (pid != null) onPick(RoomKey(RoomKey.JOB, addr ?: client ?: job ?: "Job", listOf(pid), pid)) }
            .padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(client ?: job ?: "Job #$pid", style = MaterialTheme.typography.titleSmall, color = if (cur) Board.blue else Board.ink)
        val second = listOfNotNull(job?.takeIf { it != client }, addr ?: "no address on this job yet").joinToString(" · ")
        Text(second, style = MaterialTheme.typography.bodySmall, color = Board.grey, maxLines = 2)
    }
}

// ------------------------------------------------------------------ opened: the whole board on one screen - leads | the job and Caroline | the estimate and what hangs off it; scheduling across the bottom

@Composable
private fun BoardLayout(room: RoomViewModel, d: RoomData?, busy: String?, projects: List<JsonObject>, currentId: Int?, onPick: (RoomKey) -> Unit, onOpen: (RoomCard) -> Unit) {
    // HIS DESIGN FILE'S GRID (Oct 9 2026, "Wade OS Job Board"; Nov 6: "this is what i wanna see in hocc when i open the fold"): estimates,
    // job schedule and daily logs down the left; the job card spanning two rows in the centre with Caroline under it; scope, selections and
    // photos down the right. Under them, the job's file around the same centre (Nov 6: "each will be a peripheral card around that center
    // card"): job site hours | call log | texts, then the punch list | recordings.
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 4.dp)) {
        // no early return out of this inline Column (it unbalances the composer's groups on this Compose version)
        if (d != null) {
        val cards = listOf(
            RoomCard.Estimate to Area(0, 0), RoomCard.Vitals to Area(1, 0, 2), RoomCard.Scope to Area(2, 0),
            RoomCard.Schedule to Area(0, 1), RoomCard.Selections to Area(2, 1),
            RoomCard.Logs to Area(0, 2), RoomCard.Caroline to Area(1, 2), RoomCard.Photos to Area(2, 2),
            RoomCard.SiteHours to Area(0, 3), RoomCard.Calls to Area(1, 3), RoomCard.Texts to Area(2, 3),
            RoomCard.Punch to Area(0, 4), RoomCard.Recordings to Area(1, 4),
        )
        // the design's 1 : 2.1 : 1 is drawn at 1480 px; on the Fold's ~750 dp the side cards would get ~165 dp, too narrow for their
        // rows, so the side columns take a little more under 1100 dp (the centre card still the widest, as drawn)
        val w = LocalConfiguration.current.screenWidthDp
        HubGrid(cards.map { it.second }, gap = if (w < 1100) 10.dp else 14.dp,
            weights = if (w < 1100) listOf(1.2f, 2f, 1.2f) else listOf(1f, 2.1f, 1f), modifier = Modifier.fillMaxWidth()) {
            cards.forEach { (c, _) -> Box { CardFor(c, room, d, busy, false) { onOpen(c) } } }
        }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CardFor(c: RoomCard, room: RoomViewModel, d: RoomData, busy: String?, full: Boolean, onOpen: () -> Unit) {
    val writing = busy != null
    when (c) {
        RoomCard.Vitals -> ClientCard(room, d, writing, full, onOpen)
        RoomCard.Caroline -> CarolineCard(room, d, writing, full, onOpen)
        RoomCard.Estimate -> EstimatesCard(room, d, writing, full, onOpen)
        RoomCard.Schedule -> ScheduleCard(room, d, writing, full, onOpen)
        RoomCard.Scope -> ScopeCard(room, d, writing, full, onOpen)
        RoomCard.Selections -> SelectionsCard(room, d, writing, full, onOpen)
        RoomCard.Photos -> PhotosCard(room, d, writing, full, onOpen)
        RoomCard.Logs -> LogsCard(room, d, writing, full, onOpen)
        RoomCard.Punch -> PunchCard(room, d, writing, full, onOpen)
        RoomCard.SiteHours -> SiteHoursCard(d, full, onOpen)
        RoomCard.Texts -> TextsCard(d, full, onOpen)
        RoomCard.Calls -> CallsCard(d, full, onOpen)
        RoomCard.Recordings -> RecordingsCard(d, full, onOpen)
    }
}

private fun firstName(c: JsonObject?): String = (c?.str("client_name", "client") ?: "").trim().substringBefore(' ').ifBlank { "the client" }

/** The local day (yyyy-mm-dd) of a stored time, in this phone's zone; "" when it can't be read. */
private fun localDayOf(raw: String?): String = try {
    raw?.let { java.time.OffsetDateTime.parse(it.replace(' ', 'T')).atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDate().toString() } ?: ""
} catch (e: Exception) { raw?.take(10) ?: "" }

/** An appointment's day and time as he reads them ("Nov 9 10:00 AM"). */
private fun apptWhen(a: JsonObject): String? {
    val raw = a.str("start_at", "start", "at", "when") ?: return null
    return try {
        val t = java.time.OffsetDateTime.parse(raw.replace(' ', 'T')).atZoneSameInstant(java.time.ZoneId.systemDefault())
        t.format(MDAY) + " " + t.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
    } catch (e: Exception) { d8(raw) }
}

@Composable
private fun CalRow(whenText: String, what: String, color: Color, dot: Color) {
    Row(Modifier.padding(start = 2.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.padding(top = 6.dp).size(8.dp).clip(RoundedCornerShape(2.dp)).background(dot))
        Num(whenText, 13, color = color, modifier = Modifier.width(112.dp))
        if (what.isNotBlank()) Text(what, style = MaterialTheme.typography.bodyMedium, color = if (color == Board.grey) Board.grey else Board.body)
    }
}

private fun apptWhat(a: JsonObject): String? = a.str("name", "title", "what", "summary")

private fun d8(iso: String?): String? = iso?.take(10)?.let { runCatching { LocalDate.parse(it).format(MDAY) }.getOrNull() }

// ------------------------------------------------------------------ the client card (the centre of the board)

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ClientCard(room: RoomViewModel, d: RoomData, writing: Boolean, full: Boolean, onOpen: () -> Unit) {
    val c = d.card
    val context = LocalContext.current
    val (stageName, stageColor) = Board.stage(c?.str("status"), c?.boolOf("open"))
    var editing by remember { mutableStateOf(false) }
    var staging by remember { mutableStateOf(false) }
    val phone = c?.str("client_phone", "phone")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White).border(2.dp, Board.ink, RoundedCornerShape(10.dp))) {
        // the stage band - tap the stage word to move this lead along its life
        Column(Modifier.fillMaxWidth().background(stageColor).padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.clickable { staging = !staging }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Lbl(stageName, Color.White.copy(alpha = 0.92f))
                    Text(if (staging) "▴" else "▾  change", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                }
                Text(listOfNotNull(d8(c?.str("created_at"))?.let { "Lead $it" }, c?.str("current_phase")?.takeIf { it.isNotBlank() }).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.92f))
            }
            if (staging) {
                Text("Where does this one stand now?", style = MaterialTheme.typography.bodySmall, color = Color.White)
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Board.LIFE.forEach { (word, label) ->
                        val on = (c?.str("status") ?: "").lowercase() == word
                        Box(Modifier.clip(RoundedCornerShape(6.dp)).background(if (on) Color.White else Color.White.copy(alpha = 0.18f))
                            .clickable(enabled = !writing && !on) { room.setStatus(d.id, word); staging = false }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(label, style = MaterialTheme.typography.labelMedium, color = if (on) stageColor else Color.White)
                        }
                    }
                }
            }
            Disp(c?.str("client_name", "client") ?: c?.str("name") ?: "Job", if (full) 44 else 36, Color.White, maxLines = 2)
            Text(c?.str("address")?.takeIf { it.isNotBlank() } ?: "No address on this job yet", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color.White)
            Num(listOfNotNull(phone, c?.str("client_email", "email") ?: "no email on file").joinToString(" · "), 13, Color.White.copy(alpha = 0.95f), FontWeight.Normal)
        }
        // NEXT
        // the company calendar opens on today (Tyler, Nov 6 2026: "why the company calendar sits in sept 14th is wild"): the record
        // gives start_at and name, newest first; read those, put them in time order, and start two before today
        val apptsAll = c?.arrOf("appointments")?.objects().orEmpty()
            .filter { (it.str("status") ?: "") != "cancelled" }
            .sortedBy { it.str("start_at", "start", "at", "when") ?: "" }
        val todayIso = LocalDate.now().toString()
        val firstUpcoming = apptsAll.indexOfFirst { (it.str("start_at", "start", "at", "when") ?: "").let { s -> localDayOf(s) >= todayIso } }
        val appts = if (firstUpcoming < 0) apptsAll.takeLast(2) else apptsAll.drop(maxOf(0, firstUpcoming - 2))
        val upcoming = if (firstUpcoming < 0) emptyList() else apptsAll.drop(firstUpcoming)
        val tasks = d.tasks?.rowsOf("tasks")?.objects().orEmpty().filter { it.str("status") != "Completed" }
        val next = upcoming.firstOrNull()?.let { a -> listOfNotNull(apptWhen(a), apptWhat(a)).joinToString(" ") }
            ?: tasks.firstOrNull()?.str("title")
        Row(Modifier.fillMaxWidth().background(Board.ink).padding(horizontal = 20.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Lbl("Next", Color(0xFFAEB8C2))
            Text(next ?: "Nothing on the calendar for this job yet.", style = MaterialTheme.typography.titleSmall, color = Color.White, modifier = Modifier.weight(1f))
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // the need: the client's own words, else what Caroline read off the record (the estimate, the texts, a call) - said where it
                // came from; never a rule picking a line of the scope (Tyler, Nov 4 2026: "allow the model behind Caroline to decide")
                val needSource = d.client?.str("need_source")
                Lbl("What they need" + (if (needSource != null && needSource.startsWith("read from")) " · $needSource" else ""))
                val need = d.client?.str("need")?.takeIf { it.isNotBlank() }
                Text(need ?: (if (needSource != null && !needSource.startsWith("read from") && !needSource.startsWith("in their")) needSource else "Caroline hasn't read this one yet."),
                    fontFamily = Board.sans, fontWeight = FontWeight.SemiBold, fontSize = if (need != null) 20.sp else 15.sp, lineHeight = if (need != null) 26.sp else 20.sp, color = if (need != null) Board.ink else Board.grey)
            }
            // the money and the hours, from the billing card and the hours record
            val bill = d.billing?.objOf("bill")
            val est = d.estimate?.asObjOrNull()
            val contract = bill?.str("total")?.toDoubleOrNull() ?: est?.objOf("totals")?.num("total", "grand_total") ?: c?.num("budget")
            val paid = bill?.str("paid")?.toDoubleOrNull()
            val balance = bill?.str("balance")?.toDoubleOrNull() ?: (if (contract != null && paid != null) contract - paid else null)
            val hourRows = d.hours?.rowsOf("entries")?.objects().orEmpty()
            val hours = hourRows.sumOf { it.num("hours") ?: 0.0 }
            val approx = bill == null
            // four tiles: one row when the card is wide, two by two on the phone (a four-across row wraps the dollars letter by letter)
            BoxWithConstraints { if (maxWidth >= 520.dp) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Tile("Contract", money(contract), modifier = Modifier.weight(1f), approx = approx && contract != null)
                    Tile("Paid", money(paid ?: 0.0), Board.green, Modifier.weight(1f), approx = approx)
                    Tile("Balance", money(balance), Board.rust, Modifier.weight(1f), approx = approx && balance != null)
                    Tile("Hours worked", fmtH(hours).replace(" h", " hr"), modifier = Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Tile("Contract", money(contract), modifier = Modifier.weight(1f), approx = approx && contract != null)
                        Tile("Paid", money(paid ?: 0.0), Board.green, Modifier.weight(1f), approx = approx)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Tile("Balance", money(balance), Board.rust, Modifier.weight(1f), approx = approx && balance != null)
                        Tile("Hours worked", fmtH(hours).replace(" h", " hr"), modifier = Modifier.weight(1f))
                    }
                }
            } }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(when {
                    bill != null -> "Money is from the books: " + (d.billing?.str("stage") ?: "") + "."
                    est != null -> "Money is the estimate's total; nothing billed yet. " + (d.billing?.str("stage") ?: "")
                    contract != null -> "Money is the job's budget; no estimate on file."
                    else -> "No money on file for this job yet."
                }, style = MaterialTheme.typography.bodySmall, color = Board.grey)
                Text("Hours: " + (hourRows.firstOrNull()?.str("work_date")?.let { "logged through " + (d8(it) ?: it) } ?: "none logged yet") + " · ${hourRows.size} entries",
                    style = MaterialTheme.typography.bodySmall, color = Board.grey)
            }
            // the company calendar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Lbl("On the company calendar")
                // past (greyed, the last two), then TODAY, then what is ahead - so the list always opens on today's date
                val past = appts.filter { localDayOf(it.str("start_at", "start", "at", "when")) < todayIso }
                val ahead = appts.filter { localDayOf(it.str("start_at", "start", "at", "when")) >= todayIso }
                Column {
                    past.forEach { a -> CalRow(apptWhen(a) ?: "", apptWhat(a) ?: "Appointment", Board.grey, Board.border) }
                    CalRow("Today " + LocalDate.now().format(MDAY), if (ahead.isEmpty()) "Nothing booked ahead for this job." else "", Board.red, Board.red)
                    ahead.take(if (full) 20 else 4).forEach { a -> CalRow(apptWhen(a) ?: "", apptWhat(a) ?: "Appointment", Board.ink, Board.blue) }
                }
            }
            // the tools: real actions, one tap each
            val open = d.clock?.arrOf("open")?.objects()?.firstOrNull { it.num("project_id")?.toInt() == d.id }
            val anyOpen = d.clock?.arrOf("open")?.size() ?: 0
            Divider(color = Board.borderSoft)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (open != null) Tool("Clock out", !writing) { room.clockOut(d.id) }
                else Tool("Clock in", !writing && anyOpen == 0) { room.clockIn(d.id) }
                if (phone != null) Tool("Call") { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }
                if (phone != null) Tool("Text") { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))) }
                if (phone != null) { val openClient = LocalOpenClient.current; Tool("Client") { openClient(phone, null) } }
                Tool(if (editing) "Close" else "Edit job") { if (full) editing = !editing else onOpen() }
            }
            var removing by remember { mutableStateOf(false) }
            val st = (c?.str("status") ?: "").lowercase()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (st in setOf("lead", "estimating", "quoted")) Solid("Award - they signed", Board.green, enabled = !writing, modifier = Modifier.heightIn(min = 36.dp)) { room.award(d.id) }
                if (st == "approved") Solid("Start on site", Board.rust, enabled = !writing, modifier = Modifier.heightIn(min = 36.dp)) { room.setStatus(d.id, "active") }
                if (st == "active") Solid("To punch list", Board.rust, enabled = !writing, modifier = Modifier.heightIn(min = 36.dp)) { room.setStatus(d.id, "punch") }
                if (st == "punch") Solid("Done", Board.ink, enabled = !writing, modifier = Modifier.heightIn(min = 36.dp)) { room.setStatus(d.id, "complete") }
                Tool("Remove", !writing) { removing = !removing }
            }
            // archive keeps the job for good, off the active lists; delete sends a draft to the trash (0.7.4, Tyler Nov 6 2026)
            var confirmDelete by remember { mutableStateOf(false) }
            val leave = LocalLeaveRoom.current
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (st == "complete") Tool("Reopen", !writing) { room.unarchiveJob(d.id) }
                else Tool("Archive", !writing) { room.archiveJob(d.id) }
                Tool("Delete job", !writing) { confirmDelete = true }
            }
            if (confirmDelete) ConfirmDelete("this job", body = "It goes to the trash with its draft estimates and scope - restore it from Office › Trash. " +
                "Signed work or a job with hours, photos or money on it can't be deleted; archive it instead.",
                onYes = { confirmDelete = false; room.deleteJob(d.id) { leave() } }, onNo = { confirmDelete = false })
            if (removing) {
                var why by remember { mutableStateOf("") }
                OutlinedTextField(why, { why = it }, label = { Text("Why it comes off the board (kept on file)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Solid("Remove it", Board.red, enabled = !writing) { room.remove(d.id, why); removing = false }
                    Tool("Keep it") { removing = false }
                }
            }
            if (open != null) Text("On the clock here since " + (open.str("started_at")?.let { serverTimeToLocal(it) ?: it } ?: "?"), style = MaterialTheme.typography.bodySmall, color = Board.grey)
            else if (anyOpen > 0) Text("A shift is open on another job - clock out there first.", style = MaterialTheme.typography.bodySmall, color = Board.red)
            if (full) {
                var need by remember(d.client) { mutableStateOf(d.client?.str("need_own") ?: "") }
                if (phone != null) {
                    OutlinedTextField(need, { need = it }, label = { Text("What they need, in their own words (stands over Caroline's reading)") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                    Row { Tool("Save need", !writing) { room.saveNeed(phone, need) } }
                }
                if (editing && c != null) {
                    var name by remember(c) { mutableStateOf(c.str("name") ?: "") }
                    var status by remember(c) { mutableStateOf(c.str("status") ?: "") }
                    var phase by remember(c) { mutableStateOf(c.str("current_phase", "phase") ?: "") }
                    var budget by remember(c) { mutableStateOf(c.num("budget")?.let { fmtQ(it) } ?: "") }
                    var address by remember(c) { mutableStateOf(c.str("address") ?: "") }
                    OutlinedTextField(name, { name = it }, label = { Text("Job name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(address, { address = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(status, { status = it }, label = { Text("Status") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(phase, { phase = it }, label = { Text("Phase") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(budget, { budget = it }, label = { Text("Budget") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    Row { Solid("Save job", enabled = !writing) { room.editJob(d.id, name, status, phase, budget, address); editing = false } }
                }
                val instr = c?.arrOf("instructions")?.objects().orEmpty()
                if (instr.isNotEmpty()) {
                    Lbl("Standing instructions")
                    instr.forEach { Text("· " + (it.str("instruction", "text", "body") ?: ""), style = MaterialTheme.typography.bodySmall, color = Board.body) }
                }
                val issues = c?.arrOf("issues")?.objects().orEmpty()
                if (issues.isNotEmpty()) {
                    Lbl("Open issues on this job")
                    issues.forEach { Text("· " + (it.str("title", "text", "summary") ?: ""), style = MaterialTheme.typography.bodySmall, color = Board.body) }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Caroline: all communications with the client; Enter is a new line

@Composable
private fun CarolineCard(room: RoomViewModel, d: RoomData, writing: Boolean, full: Boolean, onOpen: () -> Unit) {
    val first = firstName(d.card)
    var filter by rememberSaveable { mutableStateOf("all") }
    val calls = d.client?.arrOf("calls")?.objects().orEmpty().map { Triple(it.str("started_at", "at", "created_at") ?: "", "Call", it.str("summary", "outcome", "transcript_summary") ?: "(call on file)") }
    val texts = d.client?.arrOf("messages", "texts")?.objects().orEmpty().map { Triple(it.str("created_at", "at") ?: "", if (it.str("direction") == "inbound") "Text from $first" else "Text to $first", it.str("body", "text") ?: "") }
    val orders = d.thread?.rowsOf("messages")?.objects().orEmpty().map { Triple(it.str("at") ?: "", if (it.str("from") == "tyler") "You" else "Caroline", it.str("text") ?: "") }
    val rows = when (filter) { "calls" -> calls; "texts" -> texts; "emails" -> emptyList(); "orders" -> orders; else -> calls + texts + orders }
        .sortedByDescending { it.first }
    BoardCard(strong = Board.blue, padding = 18) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Disp("Caroline", 24, Board.blue)
                Text("All communications with $first", style = MaterialTheme.typography.bodySmall, color = Board.grey)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("all" to "All", "calls" to "Calls", "texts" to "Texts", "emails" to "Emails", "orders" to "Your orders").forEach { (k, l) ->
                if (filter == k) Solid(l, Board.blue, modifier = Modifier.heightIn(min = 34.dp)) { } else Tool(l) { filter = k }
            }
        }
        if (rows.isEmpty()) Dashed(if (filter == "emails") "No email on file for $first - email lands here once the business mailbox is connected."
            else "Nothing from Caroline on $first yet. Every call, text, email and order you give her about this client lands here, newest first.")
        rows.take(if (full) 60 else 6).forEach { (at, kind, text) ->
            Column(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Lbl(kind, if (kind == "Caroline") Board.blue else Board.grey)
                    Num(at.let { serverTimeToLocal(it) ?: it }, 11, Board.grey, FontWeight.Normal)
                }
                Text(text, style = MaterialTheme.typography.bodyMedium, color = Board.ink, maxLines = if (full) Int.MAX_VALUE else 3)
            }
        }
        if (!full && rows.size > 6) TextButton(onClick = onOpen) { Text("${rows.size - 6} more", color = Board.blue) }
        Lbl("Tell Caroline about $first")
        // TextFieldValue, not a String: with a String the keyboard's half-typed word came back after Send (Tyler, Nov 4 2026)
        var text by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue("")) }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                text, { text = it }, placeholder = { Text("Type or speak an order for Caroline") },
                modifier = Modifier.weight(1f), maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Board.blue, unfocusedBorderColor = Board.toolBorder)
            )
            Solid("Send to Caroline", Board.blue, enabled = !writing && text.text.isNotBlank()) {
                val t = text.text.trim(); if (t.isNotEmpty()) { room.tellCaroline(d.id, d.card?.str("name") ?: "job #${d.id}", t); text = androidx.compose.ui.text.input.TextFieldValue("") }
            }
        }
        Text("Enter makes a new line. Only the button sends.", style = MaterialTheme.typography.bodySmall, color = Board.grey)
    }
}

// ------------------------------------------------------------------ Estimates

@Composable
private fun EstimatesCard(room: RoomViewModel, d: RoomData, writing: Boolean, full: Boolean, onOpen: () -> Unit) {
    val cur = d.estimate?.asObjOrNull()
    val mine = d.myEstimates()
    val b = d.billing
    var title by remember { mutableStateOf("") }
    var naming by remember { mutableStateOf(false) }
    BoardCard {
        CardHead("Estimates") {
            Tool("New", !writing) { if (full) naming = !naming else onOpen() }
            Tool("To invoice", !writing && b?.boolOf("needs_convert") == true) { room.convertBilling(d.id) }
        }
        if (cur == null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("No estimate on this job", style = MaterialTheme.typography.titleSmall); Text("None on file", style = MaterialTheme.typography.bodySmall, color = Board.grey) }
            }
        } else {
            val tot = cur.objOf("totals")
            EstRow(listOfNotNull(cur.str("title"), cur.num("version")?.let { "v${it.toInt()}" }).joinToString(" · ").ifBlank { "Estimate" },
                listOfNotNull(humanStatus(cur.str("status")), money(tot?.num("total", "grand_total") ?: cur.num("total"))).joinToString(" · "))
            val ps = b?.objOf("estimate")?.objOf("payment_schedule") ?: cur.objOf("payment_schedule")
            if (ps != null) {
                val draws = ps.arrOf("draws", "milestones", "schedule")?.objects().orEmpty()
                EstRow(if (draws.size == 3) "Billed in thirds" else "Draws", draws.joinToString(" · ") { listOfNotNull(it.str("label", "name"), it.num("pct")?.let { p -> "${p.toInt()}%" }, money(it.num("amount"))).joinToString(" ") }.ifBlank { ps.toString().take(100) })
            }
            val pays = b?.arrOf("payments")?.objects().orEmpty()
            if (pays.isNotEmpty()) EstRow("Payments on file", pays.joinToString(" · ") { listOfNotNull(money(it.num("amount")), it.str("received_on")).joinToString(" ") })
            else if (b?.objOf("bill") != null) EstRow("Payments", "none recorded yet")
        }
        if (mine.size > 1 && full) {
            Lbl("All versions")
            mine.sortedByDescending { it.num("version") ?: 0.0 }.forEach { e -> EstRow(listOfNotNull(e.str("title"), e.num("version")?.let { "v${it.toInt()}" }).joinToString(" · "), listOfNotNull(humanStatus(e.str("status")), money(e.num("col_total") ?: e.num("subtotal")), e.num("line_count")?.let { "${it.toInt()} lines" }).joinToString(" · ")) }
        }
        // a draft that never went out can be deleted (to the trash); a sent or approved one stays - the office says why (0.7.4)
        if (full) {
            val drafts = (if (mine.isNotEmpty()) mine else listOfNotNull(cur)).filter { (it.str("status") ?: "").lowercase() == "draft" && it.num("id") != null }
            var confirm by remember { mutableStateOf<JsonObject?>(null) }
            drafts.forEach { e ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Draft " + listOfNotNull(e.str("title"), e.num("version")?.let { "v${it.toInt()}" }).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Board.grey, modifier = Modifier.weight(1f))
                    Tool("Delete draft", !writing) { confirm = e }
                }
            }
            confirm?.let { e -> ConfirmDelete("this draft estimate", onYes = { e.num("id")?.toLong()?.let { room.deleteEstimate(it) }; confirm = null }, onNo = { confirm = null }) }
        }
        if (full && cur != null) {
            val lines = cur.rowsOf("lines").objects()
            if (lines.isNotEmpty()) {
                Lbl("${lines.size} lines")
                lines.forEach { l ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(l.str("description", "item", "name") ?: "", style = MaterialTheme.typography.bodySmall)
                            l.str("note")?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Board.grey) }
                        }
                        Num(money(l.num("cost", "total", "amount")) ?: "", 12)
                    }
                }
            }
        }
        if (naming) {
            OutlinedTextField(title, { title = it }, label = { Text("Title for the new estimate") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row { Solid(if (cur == null) "Start the estimate" else "New version", Board.blue, enabled = !writing) { room.newEstimate(d.id, title.ifBlank { d.card?.str("name") ?: "Estimate" }, d.card?.str("scope")); title = ""; naming = false } }
            Text("Lines are priced from the catalog with their source; quantities come from the takeoff. The line builder is on the Office until the estimator screen lands here.", style = MaterialTheme.typography.bodySmall, color = Board.grey)
        }
    }
}

@Composable
private fun EstRow(a: String, b: String) {
    Column(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Divider(color = Board.borderSoft)
        Text(a, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))
        if (b.isNotBlank()) Text(b, style = MaterialTheme.typography.bodySmall, color = Board.grey)
    }
}

// ------------------------------------------------------------------ Job schedule: the phases as bars, today as the red line

@Composable
private fun ScheduleCard(room: RoomViewModel, d: RoomData, writing: Boolean, full: Boolean, onOpen: () -> Unit) {
    val s = d.schedule
    val phases = s?.arrOf("phases")?.objects().orEmpty().filter { it.str("state") != "removed" }
    val items = s?.arrOf("items")?.objects().orEmpty().filter { it.str("state") == "live" }
    val today = LocalDate.now()
    val days = phases.flatMap { listOfNotNull(it.str("first_day")?.take(10), it.str("last_day")?.take(10)) }.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
    val start = days.minOrNull(); val end = days.maxOrNull()
    val remainingMin = items.sumOf { maxOf(0.0, (it.num("est_minutes") ?: 0.0) - (it.num("actual_min") ?: 0.0)) }
    val futureDays = items.flatMap { it.arrOf("blocks")?.objects().orEmpty() }.mapNotNull { it.str("work_date")?.take(10) }.distinct().count { runCatching { !LocalDate.parse(it).isBefore(today) }.getOrDefault(false) }
    BoardCard {
        CardHead("Job schedule") { Tool("Open schedule") { onOpen() } }
        if (phases.isEmpty()) Dashed("No schedule laid out on this job yet. Lay it out from the scope below.")
        else {
            Text("Remaining work: ${fmtH(remainingMin / 60.0)} over $futureDays work days" + (end?.let { " · projected finish ${it.format(MDAY)}" } ?: ""), style = MaterialTheme.typography.titleSmall)
            val span = if (start != null && end != null && end.isAfter(start)) (end.toEpochDay() - start.toEpochDay()).toFloat() else 1f
            phases.forEach { p ->
                val f = p.str("first_day")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                val l = p.str("last_day")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                val over = p.boolOf("over") == true
                Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text(p.str("name", "title") ?: "Phase", style = MaterialTheme.typography.bodySmall, color = Board.body)
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Board.track)) {
                        if (f != null && l != null && start != null) {
                            val x0 = ((f.toEpochDay() - start.toEpochDay()) / span).coerceIn(0f, 1f)
                            val x1 = ((l.toEpochDay() - start.toEpochDay() + 1) / span).coerceIn(x0 + 0.02f, 1f)
                            val done = !l.isAfter(today)
                            Row(Modifier.fillMaxSize()) {
                                Spacer(Modifier.weight(maxOf(x0, 0.0001f)))
                                Box(Modifier.weight(maxOf(x1 - x0, 0.0001f)).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(if (over) Board.red else if (done) Board.barDone else if (p.boolOf("on_calendar") == true) Board.barCalendar else Board.barProjected))
                                Spacer(Modifier.weight(maxOf(1f - x1, 0.0001f)))
                            }
                        }
                        if (start != null && end != null && !today.isBefore(start) && !today.isAfter(end)) {
                            val tx = ((today.toEpochDay() - start.toEpochDay()) / span).coerceIn(0f, 1f)
                            Row(Modifier.fillMaxSize()) { Spacer(Modifier.weight(maxOf(tx, 0.0001f))); Box(Modifier.width(2.dp).fillMaxHeight().background(Board.red)); Spacer(Modifier.weight(maxOf(1f - tx, 0.0001f))) }
                        }
                    }
                    if (full) {
                        val pid = p.num("id")
                        Text("planned ${fmtH((p.num("est_min") ?: 0.0) / 60.0)} · actual ${fmtH((p.num("actual_min") ?: 0.0) / 60.0)}" + (if (over) " · OVER" else ""), style = MaterialTheme.typography.bodySmall, color = if (over) Board.red else Board.grey)
                        items.filter { it.num("phase_id") == pid }.forEach { i ->
                            val blocks = i.arrOf("blocks")?.objects().orEmpty()
                            Text("   · " + (i.str("name", "title", "line_text") ?: "") + "  " + fmtH((i.num("est_minutes") ?: 0.0) / 60.0) +
                                (blocks.firstOrNull()?.let { b -> "  ${d8(b.str("work_date")) ?: ""} ${minToClock(b.num("start_min"))}–${minToClock(b.num("end_min"))}" } ?: ""),
                                style = MaterialTheme.typography.bodySmall, color = Board.body)
                        }
                    }
                }
            }
            if (full) Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Legend(Board.barDone, "Done"); Legend(Board.barCalendar, "On your calendar"); Legend(Board.barProjected, "Projected"); Legend(Board.red, "Today / over")
            }
        }
        if (full) {
            var startDate by remember { mutableStateOf(today.format(DATE)) }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(startDate, { startDate = it }, label = { Text("Start date") }, singleLine = true, modifier = Modifier.weight(1f))
                Tool("Set start", !writing) { room.setStart(d.id, startDate) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Solid(if (phases.isEmpty()) "Lay out from the scope" else "Lay out again", Board.blue, enabled = !writing) { room.generateSchedule(d.id) }
                if (phases.isNotEmpty()) Tool("Replan", !writing) { room.replanSchedule(d.id) }
            }
            Text("The Gantt - a phase opened to its tasks, a day opened to the half hour - is the next build on this card.", style = MaterialTheme.typography.bodySmall, color = Board.grey)
        }
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.width(18.dp).height(10.dp).clip(RoundedCornerShape(3.dp)).background(color)); Text(text, style = MaterialTheme.typography.bodySmall, color = Board.body)
    }
}

private fun minToClock(m: Double?): String { val x = (m ?: 0.0).toInt(); return String.format("%d:%02d", x / 60, x % 60) }

// ------------------------------------------------------------------ Daily logs: his words by day, with the time in / time out (code_requests #7/#8)

@Composable
private fun LogsCard(room: RoomViewModel, d: RoomData, writing: Boolean, full: Boolean, onOpen: () -> Unit) {
    val rows = d.hours?.rowsOf("entries")?.objects().orEmpty()
    var logging by remember { mutableStateOf(false) }
    BoardCard {
        CardHead("Daily logs") { Tool("Log today", !writing) { if (full) logging = !logging else onOpen() } }
        if (logging && full) LogForm(room, d, writing) { logging = false }
        if (rows.isEmpty()) Dashed("No days logged yet.")
        rows.take(if (full) 200 else 3).forEach { h ->
            var editing by remember(h) { mutableStateOf(false) }
            Divider(color = Board.borderSoft)
            if (!full) Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Num(d8(h.str("work_date")) ?: (h.str("work_date") ?: ""), 12)
                Text(listOfNotNull(fmtH(h.num("hours") ?: 0.0), h.str("source_kind")?.let { "from $it" }).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Board.ink)
                h.str("note")?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Board.grey, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }
            } else
            Row(Modifier.fillMaxWidth().clickable(enabled = full) { editing = !editing }.padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Num(d8(h.str("work_date")) ?: (h.str("work_date") ?: ""), 12, modifier = Modifier.width(56.dp))
                Column(Modifier.weight(1f)) {
                    val inOut = listOfNotNull(h.str("started_at")?.let { serverTimeToLocal(it)?.substringAfter(' ') }, h.str("ended_at")?.let { serverTimeToLocal(it)?.substringAfter(' ') }).joinToString("–")
                    Text(h.str("note")?.takeIf { it.isNotBlank() } ?: (h.str("category")?.let { "$it hours" } ?: "hours"), style = MaterialTheme.typography.bodySmall, color = Board.ink)
                    Text(listOfNotNull(fmtH(h.num("hours") ?: 0.0), inOut.takeIf { it.isNotBlank() }, h.str("source_kind")?.let { "from $it" }).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Board.grey)
                }
            }
            if (editing && full) {
                val hid = h.num("id")?.toLong()
                var hrs by remember(h) { mutableStateOf(h.num("hours")?.let { fmtQ(it) } ?: "") }
                var nt by remember(h) { mutableStateOf(h.str("note") ?: "") }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(hrs, { hrs = it }, label = { Text("Hours") }, singleLine = true, modifier = Modifier.width(90.dp))
                    OutlinedTextField(nt, { nt = it }, label = { Text("What you did") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tool("Correct", !writing && hid != null) { if (hid != null) room.editHours(hid, hrs, nt, false); editing = false }
                    var confirm by remember(h) { mutableStateOf(false) }
                    Tool("Delete", !writing && hid != null) { confirm = true }
                    if (confirm && hid != null) ConfirmDelete("this hours entry", onYes = { room.deleteHours(hid); confirm = false; editing = false }, onNo = { confirm = false })
                }
            }
        }
        if (!full && rows.size > 3) TextButton(onClick = onOpen) { Text("${rows.size - 3} more days", color = Board.blue) }
    }
}

@Composable
private fun LogForm(room: RoomViewModel, d: RoomData, writing: Boolean, onDone: () -> Unit) {
    var date by remember { mutableStateOf(LocalDate.now().format(DATE)) }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("field") }
    var what by remember { mutableStateOf("") }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Board.tile).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(date, { date = it }, label = { Text("Day") }, singleLine = true, modifier = Modifier.weight(1.3f))
            OutlinedTextField(start, { start = it }, label = { Text("In") }, placeholder = { Text("07:00") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(end, { end = it }, label = { Text("Out") }, placeholder = { Text("15:30") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(what, { what = it }, label = { Text("What you did - your words, kept verbatim") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("field", "shop", "office", "travel").forEach { k -> if (category == k) Solid(k, Board.ink, modifier = Modifier.heightIn(min = 34.dp)) {} else Tool(k) { category = k } } }
        val inOk = start.matches(Regex("\\d{1,2}:\\d{2}")) && end.matches(Regex("\\d{1,2}:\\d{2}"))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Solid("Log in/out", Board.blue, enabled = !writing && inOk) { room.logHours(d.id, date, pad(start), pad(end), category, what); onDone() }
            OutlinedTextField(count, { count = it }, label = { Text("or hours") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.width(100.dp))
            Tool("Log", !writing && count.toDoubleOrNull() != null) { room.logHoursCount(d.id, date, count, category, what); onDone() }
            Tool("Note only", !writing && what.isNotBlank()) { room.addNote(d.id, what); onDone() }
        }
        Text("Time in and out are what counts; the hours come from them, not arithmetic in your head.", style = MaterialTheme.typography.bodySmall, color = Board.grey)
    }
}

private fun fmtH(h: Double): String = if (h == h.toLong().toDouble()) "${h.toLong()} h" else String.format("%.1f h", h)
private fun pad(t: String): String = t.split(':').let { if (it.size == 2 && it[0].length == 1) "0$t" else t }
private fun fmtQ(q: Double): String = if (q == q.toLong().toDouble()) q.toLong().toString() else q.toString()

// ------------------------------------------------------------------ Scope: the bullets; opened, the description and the detailed scope's lines

@Composable
private fun ScopeCard(room: RoomViewModel, d: RoomData, writing: Boolean, full: Boolean, onOpen: () -> Unit) {
    val c = d.card
    val tk = d.takeoff
    val tkId = tk?.num("id")?.toInt() ?: d.takeoffs?.rowsOf("takeoffs")?.objects()?.firstOrNull()?.num("id")?.toInt()
    val lines = tk?.rowsOf("lines")?.objects()?.ifEmpty { tk.arrOf("sections")?.objects()?.flatMap { s -> s.arrOf("lines")?.objects().orEmpty() }.orEmpty() }.orEmpty()
    // the scope as the job states it: the detailed scope's lines, else his written description, else the estimate's own lines
    // (Nov 5 2026: an estimate's lines are its scope - a Houzz job has no takeoff and no description, and its scope is on the estimate)
    val estLines = d.estimate?.asObjOrNull()?.rowsOf("lines")?.objects().orEmpty()
    val written = (c?.str("scope") ?: "").split('\n').map { it.trim().trimStart('-', '•', '·').trim() }.filter { it.isNotBlank() }
    val bullets = when {
        lines.isNotEmpty() -> lines.map { it.str("item", "description", "name") ?: "" }
        written.isNotEmpty() -> written
        else -> estLines.map { l -> listOfNotNull(l.str("description", "item", "name"), l.num("qty")?.takeIf { it != 0.0 && it != 1.0 }?.let { q -> "${fmtQ(q)} ${l.str("unit") ?: ""}".trim() }).joinToString(" — ") }.filter { it.isNotBlank() }
    }
    var editing by remember { mutableStateOf(false) }
    BoardCard {
        CardHead("Scope") { Tool("Edit") { if (full) editing = !editing else onOpen() } }
        if (bullets.isEmpty()) Dashed("No scope written on this job yet.")
        bullets.take(if (full) 200 else 8).forEach { b ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("·", color = Board.blue, fontWeight = FontWeight.Bold); Text(b, style = MaterialTheme.typography.bodySmall, color = Board.ink) }
        }
        if (!full && bullets.size > 8) TextButton(onClick = onOpen) { Text("${bullets.size - 8} more", color = Board.blue) }
        if (full) {
            var scope by remember(c) { mutableStateOf(c?.str("scope") ?: "") }
            if (editing) {
                OutlinedTextField(scope, { scope = it }, label = { Text("Description of the work") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 14)
                Row { Solid("Save description", Board.blue, enabled = !writing && scope != (c?.str("scope") ?: "")) { room.saveScopeText(d.id, scope) } }
            }
            Divider(color = Board.borderSoft)
            if (tkId == null) {
                Text("No detailed scope (takeoff) on this job yet.", style = MaterialTheme.typography.bodyMedium)
                var t by remember { mutableStateOf("") }
                OutlinedTextField(t, { t = it }, label = { Text("Name it (or leave blank)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row { Solid("Start the detailed scope", Board.blue, enabled = !writing) { room.createTakeoff(d.id, t) } }
            } else {
                val open = lines.filter { it.num("qty") == null }
                Lbl("Detailed scope · ${lines.size} lines · ${open.size} open (no quantity yet)")
                var section by remember { mutableStateOf("") }
                var item by remember { mutableStateOf("") }
                var qty by remember { mutableStateOf("") }
                var unit by remember { mutableStateOf("") }
                OutlinedTextField(item, { item = it }, label = { Text("New line: what the work is") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(section, { section = it }, label = { Text("Section") }, singleLine = true, modifier = Modifier.weight(1.4f))
                    OutlinedTextField(qty, { qty = it }, label = { Text("Qty") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(0.8f))
                    OutlinedTextField(unit, { unit = it }, label = { Text("Unit") }, singleLine = true, modifier = Modifier.weight(0.8f))
                }
                Row { Solid("Add line", Board.blue, enabled = !writing && item.isNotBlank()) { room.addTakeoffLine(tkId, section.ifBlank { "General" }, item, qty, unit, "measured", ""); item = ""; qty = "" } }
                Text("No quantity = an OPEN line; the number comes from the takeoff, never guessed.", style = MaterialTheme.typography.bodySmall, color = Board.grey)
                lines.groupBy { it.str("section") ?: "General" }.forEach { (sec, ls) ->
                    Lbl(sec, modifier = Modifier.padding(top = 6.dp))
                    ls.forEach { l -> TakeoffLineRow(room, l, writing) }
                }
            }
        }
    }
}

@Composable
private fun TakeoffLineRow(room: RoomViewModel, l: JsonObject, writing: Boolean) {
    var editing by remember(l) { mutableStateOf(false) }
    val lid = l.num("id")?.toLong()
    val q = l.num("qty")
    Divider(color = Board.borderSoft)
    Row(Modifier.fillMaxWidth().clickable { editing = !editing }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(l.str("item", "description", "name") ?: "", style = MaterialTheme.typography.bodySmall, color = Board.ink)
            Text(listOfNotNull(q?.let { fmtQ(it) + " " + (l.str("unit") ?: "") }, l.str("qty_basis", "source")).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Board.grey)
        }
        if (q == null) Lbl("Open", Board.rust)
    }
    if (editing && lid != null) {
        var item by remember(l) { mutableStateOf(l.str("item", "description", "name") ?: "") }
        var qty by remember(l) { mutableStateOf(q?.let { fmtQ(it) } ?: "") }
        var unit by remember(l) { mutableStateOf(l.str("unit") ?: "") }
        OutlinedTextField(item, { item = it }, label = { Text("Line") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(qty, { qty = it }, label = { Text("Qty") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
            OutlinedTextField(unit, { unit = it }, label = { Text("Unit") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Tool("Save", !writing) { room.editTakeoffLine(lid, item, qty, unit, null); editing = false }
            var confirm by remember(l) { mutableStateOf(false) }
            Tool("Delete", !writing) { confirm = true }
            if (confirm) ConfirmDelete("this scope line", onYes = { room.deleteTakeoffLine(lid); confirm = false; editing = false }, onNo = { confirm = false })
        }
    }
}

// ------------------------------------------------------------------ Selections: what was chosen, as logged (the materials record)

@Composable
private fun SelectionsCard(room: RoomViewModel, d: RoomData, writing: Boolean, full: Boolean, onOpen: () -> Unit) {
    val rows = d.materials?.rowsOf("materials")?.objects().orEmpty()
    val chain = d.materials?.asObjOrNull()?.arrOf("chain")?.map { it.asString }.orEmpty()
    var adding by remember { mutableStateOf(false) }
    BoardCard {
        CardHead("Selections") { Tool("Add", !writing) { if (full) adding = !adding else onOpen() } }
        if (adding && full) {
            var text by remember { mutableStateOf("") }
            OutlinedTextField(text, { text = it }, label = { Text("Say it: \"stone: MSI White Drystack, client supplied\"") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            Row { Solid("Log", Board.blue, enabled = !writing && text.isNotBlank()) { room.logMaterial(d.id, text); text = ""; adding = false } }
        }
        if (rows.isEmpty()) Dashed("No selections recorded yet.")
        rows.take(if (full) 200 else 6).forEach { m ->
            Divider(color = Board.borderSoft)
            // folded into a narrow board column the label sits over the line, so the words keep the card's whole width
            if (!full) Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Lbl((m.str("supplier")?.takeIf { it.isNotBlank() } ?: humanStatus(m.str("status")) ?: "item"))
                Text(m.str("line") ?: m.str("item") ?: "", style = MaterialTheme.typography.bodySmall, color = Board.ink)
            } else
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Lbl((m.str("supplier")?.takeIf { it.isNotBlank() } ?: humanStatus(m.str("status")) ?: "item"), modifier = Modifier.width(78.dp).padding(top = 2.dp))
                Column(Modifier.weight(1f)) {
                    Text(m.str("line") ?: m.str("item") ?: "", style = MaterialTheme.typography.bodySmall, color = Board.ink)
                    if (full) {
                        val mid = m.num("id")?.toLong()
                        val next = chain.getOrNull((m.num("chain")?.toInt() ?: -1) + 1)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(humanStatus(m.str("status")) ?: "", style = MaterialTheme.typography.bodySmall, color = Board.grey)
                            if (next != null && mid != null) Tool("→ ${humanStatus(next)}", !writing) { room.setMaterialStatus(mid, next) }
                        }
                        m.str("source_line")?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Board.grey) }
                        if (mid != null) MaterialEdit(room, m, mid, writing)
                    }
                }
            }
        }
        if (!full && rows.size > 6) TextButton(onClick = onOpen) { Text("${rows.size - 6} more", color = Board.blue) }
    }
}

// ------------------------------------------------------------------ Photos

@Composable
private fun PhotosCard(room: RoomViewModel, d: RoomData, writing: Boolean, full: Boolean, onOpen: () -> Unit) {
    val context = LocalContext.current
    val atts = d.attachments?.rowsOf("attachments")?.objects().orEmpty()
    var pending by remember { mutableStateOf<Uri?>(null) }
    val stamp = { "photo-" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".jpg" }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val u = pending
        if (ok && u != null) room.addPhoto(context, d.id, u, u.lastPathSegment ?: stamp())
        pending = null
    }
    val askCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val dir = File(context.cacheDir, "images").apply { mkdirs() }
            val u = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", File(dir, stamp()))
            pending = u
            takePicture.launch(u)
        }
    }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u -> if (u != null) room.addPhoto(context, d.id, u, stamp()) }
    val auth = room.authHeader()
    BoardCard {
        CardHead("Photos") {
            Tool("Take photo", !writing) { askCamera.launch(Manifest.permission.CAMERA) }
            Tool("Add photos", !writing) { pick.launch("image/*") }
        }
        val images = atts.filter { (it.str("media_type") ?: "").startsWith("image/") }
        val cells = if (full) images else images.take(3)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { i ->
                val a = cells.getOrNull(i)
                Box(Modifier.weight(1f).height(68.dp).clip(RoundedCornerShape(6.dp)).border(1.dp, Board.dashed, RoundedCornerShape(6.dp))) {
                    if (a != null) {
                        val itemId = a.str("item_id") ?: ""
                        val req = ImageRequest.Builder(context).data(room.photoUrl(itemId)).apply { if (auth != null) addHeader("Authorization", auth) }.crossfade(true).build()
                        AsyncImage(model = req, contentDescription = a.str("title", "first_filename"), modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
        if (atts.isEmpty()) Text("No photos filed for this job yet.", style = MaterialTheme.typography.bodySmall, color = Board.grey)
        else Text("${atts.size} on the job", style = MaterialTheme.typography.bodySmall, color = Board.grey)
        if (full) atts.forEach { a ->
            Divider(color = Board.borderSoft)
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                val itemId = a.str("item_id") ?: ""
                if ((a.str("media_type") ?: "").startsWith("image/")) {
                    val req = ImageRequest.Builder(context).data(room.photoUrl(itemId)).apply { if (auth != null) addHeader("Authorization", auth) }.build()
                    AsyncImage(model = req, contentDescription = null, modifier = Modifier.size(96.dp).clip(RoundedCornerShape(6.dp)))
                }
                Column(Modifier.weight(1f)) {
                    Text(a.str("title") ?: a.str("first_filename") ?: a.str("media_type") ?: "file", style = MaterialTheme.typography.bodySmall, color = Board.ink, maxLines = 2)
                    Text(listOfNotNull(a.str("last_at")?.let { serverTimeToLocal(it) ?: it }, a.str("last_source"), a.str("role")).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Board.grey)
                }
                // off this job (0.7.4): the file stays in the library; the trash puts it back on
                var confirm by remember(a) { mutableStateOf(false) }
                if (itemId.isNotBlank()) Tool("Remove", !writing) { confirm = true }
                if (confirm) ConfirmDelete("this photo from the job", verb = "Remove",
                    body = "It comes off this job; the file itself is kept. Restore it from Office › Trash.",
                    onYes = { room.removePhoto(d.id, itemId); confirm = false }, onNo = { confirm = false })
            }
        }
    }
}

// ------------------------------------------------------------------ Punch list

@Composable
private fun PunchCard(room: RoomViewModel, d: RoomData, writing: Boolean, full: Boolean, onOpen: () -> Unit) {
    val rows = d.tasks?.rowsOf("tasks")?.objects().orEmpty()
    val open = rows.filter { it.str("status") != "Completed" }
    val done = rows.filter { it.str("status") == "Completed" }
    var title by remember { mutableStateOf("") }
    BoardCard {
        CardHead("Punch list") { Tool("Open") { onOpen() } }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(title, { title = it }, placeholder = { Text("Punch item") }, singleLine = true, modifier = Modifier.weight(1f))
            Tool("Add", !writing && title.isNotBlank()) { room.addTask(d.id, title); title = "" }
        }
        Text("${open.size} open · ${done.size} done", style = MaterialTheme.typography.bodySmall, color = Board.grey)
        if (rows.isEmpty()) Dashed("Nothing on the punch list.")
        (if (full) open else open.take(6)).forEach { t -> TaskRow(room, t, writing) }
        if (full) done.forEach { t -> TaskRow(room, t, writing) }
        if (!full && open.size > 6) TextButton(onClick = onOpen) { Text("${open.size - 6} more", color = Board.blue) }
    }
}

@Composable
private fun TaskRow(room: RoomViewModel, t: JsonObject, writing: Boolean) {
    val tid = t.num("id")?.toLong()
    val done = t.str("status") == "Completed"
    var editing by remember(t) { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = done, onCheckedChange = { if (tid != null) room.flipTask(tid, it) }, enabled = !writing && tid != null)
        Column(Modifier.weight(1f).clickable(enabled = tid != null) { editing = !editing }) {
            Text(t.str("title") ?: "", style = MaterialTheme.typography.bodyMedium, color = if (done) Board.grey else Board.ink)
            val sub = listOfNotNull(t.str("due_date")?.let { "due $it" }, t.str("phase")?.takeIf { it.isNotBlank() }, t.str("assignee")?.takeIf { it.isNotBlank() }).joinToString(" · ")
            if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = Board.grey)
        }
    }
    if (editing && tid != null) {
        var title by remember(t) { mutableStateOf(t.str("title") ?: "") }
        var confirm by remember(t) { mutableStateOf(false) }
        OutlinedTextField(title, { title = it }, label = { Text("Punch item") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Tool("Save", !writing && title.isNotBlank() && title != (t.str("title") ?: "")) { room.editTask(tid, title.trim()); editing = false }
            Tool("Delete", !writing) { confirm = true }
            Tool("Close") { editing = false }
        }
        if (confirm) ConfirmDelete("this punch item", onYes = { room.deleteTask(tid); confirm = false; editing = false }, onNo = { confirm = false })
    }
}

/** A selection line, opened: fix its words, how many and the unit, or delete it (to the trash). */
@Composable
private fun MaterialEdit(room: RoomViewModel, m: JsonObject, mid: Long, writing: Boolean) {
    var editing by remember(m) { mutableStateOf(false) }
    var confirm by remember(m) { mutableStateOf(false) }
    if (!editing) { Row { Tool("Edit") { editing = true } }; return }
    var item by remember(m) { mutableStateOf(m.str("item") ?: "") }
    var qty by remember(m) { mutableStateOf(m.num("qty")?.let { fmtQ(it) } ?: "") }
    var unit by remember(m) { mutableStateOf(m.str("unit") ?: "") }
    OutlinedTextField(item, { item = it }, label = { Text("Item") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(qty, { qty = it }, label = { Text("How many") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
        OutlinedTextField(unit, { unit = it }, label = { Text("Unit") }, singleLine = true, modifier = Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Tool("Save", !writing) { room.editMaterial(mid, item, qty, unit); editing = false }
        Tool("Delete", !writing) { confirm = true }
        Tool("Close") { editing = false }
    }
    if (confirm) ConfirmDelete("this selection", onYes = { room.deleteMaterial(mid); confirm = false; editing = false }, onNo = { confirm = false })
}

/** Leaves the room after its job was deleted (the Home screen supplies it; elsewhere it does nothing). */
val LocalLeaveRoom = androidx.compose.runtime.compositionLocalOf<() -> Unit> { {} }
