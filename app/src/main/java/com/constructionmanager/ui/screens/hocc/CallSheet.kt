package com.constructionmanager.ui.screens.hocc

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject

/*
 * The call sheet and the Follow-up page (Tyler, Nov 6 2026).
 *
 * The call sheet is the form a secretary fills in while the phone rings: "Phone rings. She answers it. She takes notes.
 * Those notes have to go look like something and be something, right? It's a form. Fill in the blanks. Goes on file."
 * Every blank shows its answer and who wrote it (Caroline on the call, read from the recording, the phone's own record, or
 * him); a blank still empty shows as a blank. Tap any line to fill it, change it or clear it. Nothing is required. Once the
 * call is a lead the sheet has moved on (its answers are on the lead) and it leaves the Calls list; it stays on file.
 *
 * The Follow-up page is everyone who fell through the cracks, ranked by Caroline (NeuroRank: her score and one line on why),
 * his pins first and last. One tap calls or texts them from his phone; Called saves in the office. Only he sends Caroline
 * to call anyone - nothing on this page calls or texts on its own.
 */

/** Opens a full page over the wheel from any page: "follow" or "sheet:<call id>". */
val LocalOpenOverlay = androidx.compose.runtime.compositionLocalOf<(String) -> Unit> { {} }

/** The list of call sheets, newest first: who, when, which line, what they want, how much of the form is filled. */
@Composable
fun CallSheetsPage(readJson: (suspend (String) -> JsonObject?)?, room: RoomViewModel?, onOpen: (String) -> Unit) {
    var data by remember { mutableStateOf<JsonObject?>(null) }
    var unreadable by remember { mutableStateOf(false) }
    val tick = room?.leadsTick?.collectAsState()?.value ?: 0
    LaunchedEffect(tick) {
        val r = readJson?.invoke("calls/sheets?days=30")
        if (r != null) data = r
        unreadable = r == null
    }
    BoardCard {
        Disp("Calls", 24)
        val moved = data?.num("moved_to_leads")?.toInt() ?: 0
        val spam = data?.num("spam_kept_out")?.toInt() ?: 0
        Text("Every call as a filled-in sheet. Tap one to see it or fill a blank." + (if (moved > 0) " $moved moved on to Leads." else "") +
            (if (spam > 0) " $spam spam calls kept out of view." else ""),
            style = MaterialTheme.typography.bodySmall, color = Board.grey)
        if (room != null) Solid("+ New call sheet", Board.blue, modifier = Modifier.heightIn(min = 36.dp)) { onOpen("new") }
        val rows = data?.arrOf("sheets")?.objects().orEmpty()
        when {
            readJson == null -> EmptyLine("Not available here.")
            data == null && unreadable -> EmptyLine("The office didn't answer. Read again with ↻.")
            data == null -> LoadingLine("Reading the calls…")
            rows.isEmpty() -> EmptyLine("No calls in the last 30 days.")
            else -> rows.forEach { s -> SheetRow(s) { s.str("call_id")?.let(onOpen) } }
        }
    }
}

@Composable
private fun SheetRow(s: JsonObject, onClick: () -> Unit) {
    val who = s.str("name") ?: prettyPhone(s.str("phone")) ?: "No name or number yet"
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Board.tile).clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(who, style = MaterialTheme.typography.titleSmall, color = Board.ink, modifier = Modifier.weight(1f), maxLines = 1)
            Text(whenShort(s.str("when")), style = MaterialTheme.typography.labelSmall, color = Board.grey)
        }
        val want = s.str("wants") ?: s.str("summary")
        Text(want ?: "What they want: not filled in yet", style = MaterialTheme.typography.bodyMedium,
            color = if (want != null) Board.ink else Board.grey, maxLines = 2)
        val filled = s.num("filled")?.toInt() ?: 0
        val of = s.num("of")?.toInt() ?: 0
        val line = listOfNotNull(kindWords(s.str("kind")), s.str("line"), "$filled of $of filled in").joinToString(" · ")
        Text(line, style = MaterialTheme.typography.labelSmall, color = Board.grey)
    }
}

private fun kindWords(kind: String?): String? = when (kind?.lowercase()) {
    null, "" -> null
    "lead" -> "New lead"
    "client" -> "A client"
    "supplier" -> "Supplier"
    "trade" -> "Another contractor"
    "personal" -> "Personal"
    "spam" -> "Spam"
    "voicemail" -> "Voicemail"
    else -> "Other"
}

/** One call as a filled-in form. [callId] "new" opens a blank sheet that saves the moment a blank is filled. */
@Composable
fun CallSheetForm(callId: String, readJson: (suspend (String) -> JsonObject?)?, room: RoomViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    var id by remember { mutableStateOf(callId) }
    var sheet by remember { mutableStateOf<JsonObject?>(null) }
    var unreadable by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<String?>(null) }
    var showWords by remember { mutableStateOf(false) }
    val busy by room.busy.collectAsState()
    val tick by room.leadsTick.collectAsState()
    LaunchedEffect(id, tick) {
        if (id == "new") { sheet = null; unreadable = false; return@LaunchedEffect }
        val r = readJson?.invoke("calls/$id/sheet")
        if (r != null) sheet = r
        unreadable = r == null
    }
    val save: (String, String) -> Unit = { key, value ->
        editing = null
        if (id == "new") room.sheetNew(mapOf(key to value)) { newId -> id = newId }
        else room.sheetSave(id, mapOf(key to value))
    }
    Column(Modifier.fillMaxSize().background(Board.ground).imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Board.ink) }
            Disp("Call sheet", 20, modifier = Modifier.weight(1f))
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val s = sheet
            val fields = s?.arrOf("fields")?.objects().orEmpty()
            val byKey = fields.associateBy { it.str("key") ?: "" }
            if (id != "new" && s == null) {
                if (unreadable) EmptyLine("The office didn't answer. Go back and open it again.") else LoadingLine("Reading the call…")
            } else {
                BoardCard {
                    val name = byKey["caller_name"]?.str("value")
                    val phone = byKey["phone"]?.str("value")
                    Disp(name ?: prettyPhone(phone) ?: (if (id == "new") "A new call" else "No name yet"), 22)
                    if (s != null) {
                        val dur = s.num("duration_s")?.toInt()
                        val head = listOfNotNull(whenShort(s.str("when")).takeIf { it.isNotBlank() },
                            if (s.str("direction") == "outbound") "you called" else "they called",
                            dur?.takeIf { it > 0 }?.let { "${it / 60} min ${it % 60} s" }, s.str("line")).joinToString(" · ")
                        Text(head, style = MaterialTheme.typography.bodySmall, color = Board.grey)
                        val filled = s.num("filled")?.toInt() ?: 0
                        val of = s.num("of")?.toInt() ?: 0
                        Text("$filled of $of filled in. Tap any line to fill it or change it; nothing is required.",
                            style = MaterialTheme.typography.bodySmall, color = Board.grey)
                    } else {
                        Text("Fill in what you know. It saves with the first line you fill; nothing is required.",
                            style = MaterialTheme.typography.bodySmall, color = Board.grey)
                    }
                    if (!phone.isNullOrBlank()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Solid("Call", Board.blue) { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }
                        Solid("Text", Board.blue) { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))) }
                    }
                    s?.objOf("lead")?.let { lead ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Board.tile).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(9.dp).clip(CircleShape).background(Board.green))
                            Spacer(Modifier.size(8.dp))
                            Text("Moved on to Leads (" + (lead.str("stage") ?: "New") + "). Changes here carry onto the lead.",
                                style = MaterialTheme.typography.bodySmall, color = Board.ink)
                        }
                    }
                }
                val groups = s?.arrOf("groups")?.mapNotNull { runCatching { it.asString }.getOrNull() }
                    ?: SHEET_GROUPS
                val shown = if (fields.isNotEmpty()) fields else SHEET_BLANKS.map { (k, label, group) ->
                    JsonObject().apply { addProperty("key", k); addProperty("label", label); addProperty("group", group) } }
                groups.forEach { g ->
                    BoardCard {
                        Lbl(g)
                        shown.filter { it.str("group") == g }.forEach { f ->
                            val key = f.str("key") ?: return@forEach
                            if (editing == key) {
                                var text by remember(key) { mutableStateOf(f.str("value") ?: "") }
                                OutlinedTextField(text, { text = it }, label = { Text(f.str("label") ?: key) },
                                    placeholder = { f.str("hint")?.takeIf { it.isNotBlank() }?.let { Text(it) } },
                                    modifier = Modifier.fillMaxWidth(), maxLines = 6)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Solid("Save", Board.blue, enabled = busy == null) { save(key, text.trim()) }
                                    if (!f.str("value").isNullOrBlank()) Tool("Clear it", busy == null) { save(key, "") }
                                    Tool("Cancel") { editing = null }
                                }
                            } else SheetLine(f) { editing = key }
                        }
                    }
                }
                val words = s?.str("transcript")
                if (!words.isNullOrBlank()) BoardCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Lbl("What was said, word for word", modifier = Modifier.weight(1f))
                        TextButton(onClick = { showWords = !showWords }) { Text(if (showWords) "Hide" else "Show") }
                    }
                    if (showWords) Text(words, style = MaterialTheme.typography.bodySmall, color = Board.ink)
                    if (s?.str("audio") != null) Text("The recording is on file with this call.", style = MaterialTheme.typography.labelSmall, color = Board.grey)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SheetLine(f: JsonObject, onEdit: () -> Unit) {
    val value = f.str("value")?.takeIf { it.isNotBlank() }
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).clickable(onClick = onEdit).padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top) {
        Text(f.str("label") ?: "", style = MaterialTheme.typography.bodySmall, color = Board.grey, modifier = Modifier.weight(0.38f))
        Column(Modifier.weight(0.62f)) {
            Text(value ?: "—", style = MaterialTheme.typography.bodyMedium, color = if (value != null) Board.ink else Board.grey,
                fontWeight = if (value != null) FontWeight.Medium else FontWeight.Normal)
            if (value != null) f.str("from")?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Board.grey) }
        }
    }
}

// the blanks, in the office's order, for a sheet that has no row yet (call_sheet.FIELDS on the office)
private val SHEET_GROUPS = listOf("Who called", "Where", "The work", "Timing and money", "How they found us", "Next step", "The call")
private val SHEET_BLANKS = listOf(
    Triple("caller_name", "Name", "Who called"), Triple("phone", "Phone", "Who called"), Triple("alt_phone", "Other number", "Who called"),
    Triple("email", "Email", "Who called"), Triple("role", "Who they are", "Who called"),
    Triple("address", "Job address", "Where"), Triple("property_type", "Kind of property", "Where"), Triple("owns", "Owns or rents", "Where"),
    Triple("wants", "What they want", "The work"), Triple("project_type", "Kind of job", "The work"), Triple("areas", "Rooms or areas", "The work"),
    Triple("scope_details", "Details they gave", "The work"), Triple("photos", "Photos or plans", "The work"),
    Triple("timing", "When they want it", "Timing and money"), Triple("budget", "Budget they named", "Timing and money"),
    Triple("decision_maker", "Who decides", "Timing and money"), Triple("how_heard", "How they heard of us", "How they found us"),
    Triple("best_callback", "Best time to call back", "Next step"), Triple("meeting", "Site visit", "Next step"),
    Triple("next_step", "Next step", "Next step"), Triple("follow_up", "Follow up", "Next step"), Triple("decisions", "What was promised", "Next step"),
    Triple("outcome", "How the call went", "The call"), Triple("urgency", "How urgent", "The call"), Triple("kind", "What kind of call", "The call"),
    Triple("summary", "One-line summary", "The call"), Triple("notes", "Notes", "The call"),
)

// ------------------------------------------------------------------ Follow-up: who fell through the cracks, ranked by Caroline

/** A short card for the Leads spoke: how many are waiting and who's first; tap to open the whole page. */
@Composable
fun FollowUpPeek(readJson: (suspend (String) -> JsonObject?)?, room: RoomViewModel?, onOpen: () -> Unit) {
    var data by remember { mutableStateOf<JsonObject?>(null) }
    val tick = room?.leadsTick?.collectAsState()?.value ?: 0
    LaunchedEffect(tick) { data = readJson?.invoke("followups") }
    val people = data?.arrOf("people")?.objects().orEmpty()
    val waiting = people.count { it.str("group") != "booked" }
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White).border(2.dp, Board.rust, RoundedCornerShape(10.dp))
        .clickable(onClick = onOpen).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Disp("Follow up" + (if (data != null) " · $waiting" else ""), 20, color = Board.rust)
            val first = people.firstOrNull()
            Text(when {
                data == null -> "Reading who's waiting…"
                first == null -> "Nobody is waiting on you."
                else -> "First: " + (first.str("name") ?: prettyPhone(first.str("phone")) ?: "someone") + " · " + (first.str("todo") ?: "")
            }, style = MaterialTheme.typography.bodySmall, color = Board.grey, maxLines = 2)
        }
        Text("›", style = MaterialTheme.typography.headlineSmall, color = Board.rust)
    }
}

/** The whole Follow-up page: one ranked list, Caroline's order with her one-line reason, his pins first and last. */
@Composable
fun FollowUpPage(readJson: (suspend (String) -> JsonObject?)?, room: RoomViewModel, onClose: () -> Unit, onOpenSheet: (String) -> Unit = {}) {
    var data by remember { mutableStateOf<JsonObject?>(null) }
    var items by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var unreadable by remember { mutableStateOf(false) }
    val tick by room.leadsTick.collectAsState()
    val busy by room.busy.collectAsState()
    var askedRank by remember { mutableStateOf(false) }
    LaunchedEffect(tick) {
        val r = readJson?.invoke("followups")
        if (r != null) data = r
        unreadable = r == null
        // his other open Wade Custom Carpentry items (client concerns, material delays, approvals, held messages, errands),
        // ranked the same way; folded into one list with the people
        items = readJson?.invoke("followups/items?entity=wcc")?.arrOf("items")?.objects().orEmpty()
        // the ones that moved since Caroline last weighed them get weighed again, once per open of the page
        if (r != null && !askedRank && (r.num("unranked")?.toInt() ?: 0) > 0) { askedRank = true; room.followRank() }
    }
    Column(Modifier.fillMaxSize().background(Board.ground)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Board.ink) }
            Disp("Follow up", 20, modifier = Modifier.weight(1f))
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val people = rankedTogether(data?.arrOf("people")?.objects().orEmpty(), items)
            Text("Ranked by Caroline: who needs you most, and why. Pin anyone up or down; your pin wins. " +
                "Call and Text use your phone; nobody is called unless you send her.", style = MaterialTheme.typography.bodySmall, color = Board.grey)
            when {
                readJson == null -> EmptyLine("Not available here.")
                data == null && unreadable -> EmptyLine("The office didn't answer. Go back and open it again.")
                data == null -> LoadingLine("Reading who's waiting…")
                people.isEmpty() -> EmptyLine("Nobody is waiting on you.")
                else -> people.forEachIndexed { i, p -> if (p.has("source_id")) ItemCard(i + 1, p, room, busy != null) else FollowCard(i + 1, p, room, busy != null) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FollowCard(n: Int, p: JsonObject, room: RoomViewModel, writing: Boolean) {
    val context = LocalContext.current
    val id = p.num("id")?.toInt()
    val phone = p.str("phone")
    val rank = p.objOf("rank")
    val pin = rank?.str("pin")
    var noting by remember { mutableStateOf(false) }
    BoardCard(strong = if (pin == "up") Board.rust else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$n", style = MaterialTheme.typography.titleMedium, color = Board.rust, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp))
            Column(Modifier.weight(1f)) {
                Text(p.str("name") ?: prettyPhone(phone) ?: "No name yet", style = MaterialTheme.typography.titleSmall, color = Board.ink)
                Text(p.str("todo") ?: "", style = MaterialTheme.typography.labelLarge, color = Board.blue)
            }
            rank?.num("score")?.toInt()?.let { Text("$it", style = MaterialTheme.typography.labelLarge, color = Board.grey) }
        }
        Text(p.str("what_they_want") ?: "What they want: not on file yet", style = MaterialTheme.typography.bodyMedium,
            color = if (p.str("what_they_want") != null) Board.ink else Board.grey, maxLines = 3)
        rank?.str("why")?.takeIf { it.isNotBlank() }?.let {
            Text((if (rank.str("by") == "Tyler") "You: " else "Caroline: ") + it, style = MaterialTheme.typography.bodySmall, color = Board.ink)
        }
        val last = listOfNotNull(
            p.str("last_contact_at")?.let { "Last contact " + whenShort(it) },
            when (p.str("who_spoke_last")) { "them" -> "they spoke last"; "you" -> "you spoke last"; else -> null },
            p.num("days_waiting")?.toInt()?.takeIf { it > 0 }?.let { countLabel(it, "day") + " waiting" }).joinToString(" · ")
        if (last.isNotBlank()) Text(last, style = MaterialTheme.typography.labelSmall, color = Board.grey)
        p.str("last_text")?.takeIf { it.isNotBlank() }?.let { Text("Last text: “$it”", style = MaterialTheme.typography.bodySmall, color = Board.grey, maxLines = 2) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!phone.isNullOrBlank()) {
                Solid("Call", Board.blue) { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }
                Solid("Text", Board.blue) { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))) }
            }
            if (id != null) Tool("Called", !writing) { noting = !noting }
        }
        if (noting && id != null) {
            var note by remember { mutableStateOf("") }
            OutlinedTextField(note, { note = it }, label = { Text("How it went (optional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Solid("Save", Board.blue, enabled = !writing) { room.followCalled(id, note.trim()); noting = false }
                Tool("Cancel") { noting = false }
            }
        }
        if (id != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (pin != "up") Tool("Pin to top", !writing) { room.followPin(id, "up") }
            if (pin != "down") Tool("Move to bottom", !writing) { room.followPin(id, "down") }
            if (pin != null) Tool("Unpin", !writing) { room.followPin(id, "none") }
        }
    }
}


/** One ranked list: his pins first, then Caroline's score high to low, then the unranked, then his pins down. */
private fun rankedTogether(people: List<JsonObject>, items: List<JsonObject>): List<JsonObject> {
    val all = people + items
    val pos = all.withIndex().associate { (i, o) -> System.identityHashCode(o) to i }
    fun pinOrder(o: JsonObject) = when (o.objOf("rank")?.str("pin")) { "up" -> 0; "down" -> 2; else -> 1 }
    return all.sortedWith(compareBy<JsonObject>({ pinOrder(it) }, { -(it.objOf("rank")?.num("score") ?: -1.0) }, { pos[System.identityHashCode(it)] ?: 0 }))
}

/** One of his other open items (not a lead): what it is, who, why Caroline put it here, and the same buttons. */
@Composable
private fun ItemCard(n: Int, p: JsonObject, room: RoomViewModel, writing: Boolean) {
    val context = LocalContext.current
    val id = p.str("id")
    val phone = p.str("phone")
    val rank = p.objOf("rank")
    val pin = rank?.str("pin")
    var noting by remember { mutableStateOf(false) }
    BoardCard(strong = if (pin == "up") Board.rust else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$n", style = MaterialTheme.typography.titleMedium, color = Board.rust, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp))
            Column(Modifier.weight(1f)) {
                Text(p.str("who") ?: p.str("job") ?: "No name on it", style = MaterialTheme.typography.titleSmall, color = Board.ink)
                Text(p.str("title") ?: "", style = MaterialTheme.typography.labelLarge, color = Board.blue, maxLines = 3)
            }
            rank?.num("score")?.toInt()?.let { Text("$it", style = MaterialTheme.typography.labelLarge, color = Board.grey) }
        }
        p.str("job")?.takeIf { it.isNotBlank() && it != p.str("who") }?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Board.grey) }
        (rank?.str("why") ?: p.str("reason"))?.takeIf { it.isNotBlank() }?.let {
            Text((if (rank?.str("by") == "Tyler") "You: " else "Caroline: ") + it, style = MaterialTheme.typography.bodySmall, color = Board.ink)
        }
        val last = listOfNotNull(p.str("since")?.let { "Since " + whenShort(it) }, p.str("last_touched")?.let { "last touched " + whenShort(it) }).joinToString(" · ")
        if (last.isNotBlank()) Text(last, style = MaterialTheme.typography.labelSmall, color = Board.grey)
        p.str("last_note")?.takeIf { it.isNotBlank() }?.let { Text("Last note: $it", style = MaterialTheme.typography.bodySmall, color = Board.grey, maxLines = 2) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!phone.isNullOrBlank()) {
                Solid("Call", Board.blue) { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }
                Solid("Text", Board.blue) { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))) }
            }
            if (id != null) Tool("Called", !writing) { noting = !noting }
            if (id != null) Tool("Done", !writing) { room.itemAct(id, "done") }
        }
        if (noting && id != null) {
            var note by remember { mutableStateOf("") }
            OutlinedTextField(note, { note = it }, label = { Text("How it went (optional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Solid("Save", Board.blue, enabled = !writing) { room.itemAct(id, "called", note = note.trim()); noting = false }
                Tool("Cancel") { noting = false }
            }
        }
        if (id != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (pin != "up") Tool("Pin to top", !writing) { room.itemAct(id, "pin", pin = "up") }
            if (pin != "down") Tool("Move to bottom", !writing) { room.itemAct(id, "pin", pin = "down") }
            if (pin != null) Tool("Unpin", !writing) { room.itemAct(id, "pin", pin = "none") }
        }
    }
}
