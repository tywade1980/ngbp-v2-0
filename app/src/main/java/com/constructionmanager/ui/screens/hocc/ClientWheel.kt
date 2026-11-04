package com.constructionmanager.ui.screens.hocc

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


/*
 * THE CLIENT WHEEL (Tyler, Nov 4 2026: "a parent-child hierarchy with a right spoke and left spoke and a bottom wheel... everything,
 * three clicks... use client"). A client is a hub: texts on the left as the Messages thread, the client in the center, calls on the
 * right as the Phone history, and the bottom wheel is their projects - tap one and that project is the hub (a child become a parent).
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ClientWheel(vm: HoccViewModel, phone: String?, label: String?, onBack: () -> Unit, onOpenProject: (Int) -> Unit) {
    val context = LocalContext.current
    var th by remember { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    LaunchedEffect(phone, label) {
        val path = if (phone != null) "log/people/" + Uri.encode(phone) + "?limit=800" else "log/people/none?limit=800&label=" + Uri.encode(label ?: "")
        val r = vm.readJson(path)
        if (r == null) error = "The office did not answer." else th = r
    }
    val items = th?.arrOf("items")?.objects().orEmpty()
    // THE PERSON IS A WHEEL (Tyler, Nov 4 2026: "hub and spoke, left, center, right... each main page of any spoke has its own hub and
    // spoke"): texts on the left as the Messages thread, the person's card in the center, the calls on the right as the Phone history.
    val texts = items.filter { it.str("kind") == "text" }
    val calls = items.filter { it.str("kind") == "call" }.reversed()      // the phone lists calls newest first
    LaunchedEffect(texts.size) { if (texts.isNotEmpty()) listState.scrollToItem(texts.size - 1) }
    val name = th?.str("name") ?: label ?: prettyPhone(phone) ?: "No number"
    val pager = androidx.compose.foundation.pager.rememberPagerState(initialPage = 1) { 3 }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(Board.ground)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Board.ink) }
            Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (phone != null) {
                IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }) { Icon(Icons.Default.Call, contentDescription = "Call", tint = Board.blue) }
                Solid("Text", Board.blue, onClick = { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))) })
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf("‹ Texts" + (if (texts.isNotEmpty()) " ${texts.size}" else ""), "Client", "Calls" + (if (calls.isNotEmpty()) " ${calls.size}" else "") + " ›").forEachIndexed { i, l ->
                Text(l, style = MaterialTheme.typography.labelLarge, color = if (i == pager.currentPage) Board.blue else Board.grey,
                    modifier = Modifier.clickable { scope.launch { pager.animateScrollToPage(i) } }.padding(vertical = 4.dp))
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(14.dp)) }
        if (th == null && error == null) Box(Modifier.padding(14.dp)) { LoadingLine("Reading the conversation…") }
        if (th != null) androidx.compose.foundation.pager.HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), beyondBoundsPageCount = 1) { page ->
            when (page) {
                0 -> Column(Modifier.fillMaxSize()) {
                    if (texts.isEmpty()) Text("No texts with them.", style = MaterialTheme.typography.bodyMedium, color = Board.grey, modifier = Modifier.padding(14.dp))
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        var lastDay: LocalDate? = null
                        texts.forEach { m ->
                            val day = dayOf(m.str("at"))
                            if (day != null && day != lastDay) {
                                lastDay = day
                                item(key = "day-" + day) { DayLine(day) }
                            }
                            item(key = "text-" + (m.str("id") ?: m.hashCode().toString())) { TextBubble(m) }
                        }
                        item { Spacer(Modifier.height(10.dp)) }
                    }
                }
                1 -> ClientHub(th!!, phone, label, texts.size, calls.size, items.lastOrNull()?.str("at"), onOpenProject)
                else -> Column(Modifier.fillMaxSize()) {
                    if (calls.isEmpty()) Text("No calls with them.", style = MaterialTheme.typography.bodyMedium, color = Board.grey, modifier = Modifier.padding(14.dp))
                    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items(calls, key = { "call-" + (it.str("id") ?: it.hashCode().toString()) }) { c -> CallHistoryRow(c) }
                        item { Spacer(Modifier.height(10.dp)) }
                    }
                }
            }
        }
    }
}

/** The hub of the client wheel: who they are, their number, what passed between you; the bottom wheel is their projects. */
@Composable
private fun ClientHub(th: JsonObject, phone: String?, label: String?, texts: Int, calls: Int, lastAt: String?, onOpenProject: (Int) -> Unit) {
    val jobs = th.arrOf("jobs")?.objects().orEmpty()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BoardCard {
            Lbl(if (th.str("name") != null && label == null) "Client" else if (label != null) "The phone labeled them" else "Caller, not on file")
            Disp(th.str("name") ?: label ?: prettyPhone(phone) ?: "No number", 26)
            if (phone != null) Text(prettyPhone(phone) ?: phone, style = MaterialTheme.typography.titleMedium, color = Board.ink)
            if (label != null) Text("No number on the recording - the phone's caller ID said \"$label\".", style = MaterialTheme.typography.bodySmall, color = Board.grey)
            Text(listOfNotNull(countLabel(texts, "text"), countLabel(calls, "call"), lastAt?.let { "last " + whenShort(it) }).joinToString(" · "), style = MaterialTheme.typography.bodyMedium, color = Board.grey)
        }
        BoardCard {
            Lbl("Their projects")
            if (jobs.isEmpty()) EmptyLine(if (phone != null) "No project on file for this number yet." else "A project needs a number.")
            jobs.forEach { j ->
                val pid = j.num("id")?.toInt()
                Row(Modifier.fillMaxWidth().clickable(enabled = pid != null) { if (pid != null) onOpenProject(pid) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(j.str("name") ?: "Job", style = MaterialTheme.typography.titleSmall, color = Board.ink)
                        Text(humanStatus(j.str("status")) ?: "", style = MaterialTheme.typography.bodySmall, color = Board.grey)
                    }
                    val (stageName, stageColor) = Board.stage(j.str("status"), null)
                    Surface(color = stageColor.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) { Text(stageName, color = stageColor, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) }
                    Text("  ›", style = MaterialTheme.typography.titleMedium, color = Board.blue)
                }
            }
        }
    }
}

@Composable
private fun DayLine(day: LocalDate) {
    val today = LocalDate.now()
    val words = when {
        day == today -> "Today"
        day == today.minusDays(1) -> "Yesterday"
        day.year == today.year -> day.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US))
        else -> day.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))
    }
    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) { Text(words, style = MaterialTheme.typography.labelSmall, color = Board.grey) }
}

@Composable
private fun TextBubble(m: JsonObject) {
    val you = m.str("from") == "you"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (you) Arrangement.End else Arrangement.Start) {
        Surface(color = if (you) Board.blue else Color.White, shape = RoundedCornerShape(16.dp, 16.dp, if (you) 4.dp else 16.dp, if (you) 16.dp else 4.dp), modifier = Modifier.widthIn(max = 300.dp)) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(m.str("text")?.ifBlank { "(empty text)" } ?: "(empty text)", style = MaterialTheme.typography.bodyMedium, color = if (you) Color.White else Board.ink)
                Text(timeOf(m.str("at")), style = MaterialTheme.typography.labelSmall, color = if (you) Color.White.copy(alpha = 0.8f) else Board.grey, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

@Composable
private fun CallHistoryRow(c: JsonObject) {
    val dir = c.str("from")
    val outcome = c.str("outcome")
    val missed = outcome == "missed" || outcome == "rejected" || outcome == "blocked" || outcome == "voicemail"
    val secs = c.num("seconds")?.toInt()
    val fromPhone = c.str("channel") == "phone_recording" || c.str("channel") == "phone_log"
    val head = when {
        outcome == "missed" -> "Missed"
        outcome == "voicemail" -> "Voicemail"
        outcome == "rejected" -> "Declined"
        outcome == "blocked" -> "Blocked"
        dir == "you" -> "Outgoing"
        else -> "Incoming"
    }
    val glyph = when { missed -> "↙" ; dir == "you" -> "↗" ; else -> "↙" }
    val at = parseAt(c.str("at"))?.atZoneSameInstant(ZoneId.systemDefault())
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(glyph, color = if (missed) Board.rust else Board.green, fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f)) {
                Text(head + (secs?.takeIf { it > 0 }?.let { " · " + fmtSecs(it) } ?: ""), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = if (missed) Board.rust else Board.ink)
                Text(listOfNotNull(at?.let { whenShort(c.str("at")) + " " + it.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US)) },
                    if (fromPhone) (if (c.get("has_audio")?.asBoolean == true) "your phone, recorded" else "your phone") else "Caroline's line").joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall, color = Board.grey)
            }
        }
        val summary = c.str("summary")?.trim()
        if (!summary.isNullOrBlank()) {
            Text(summary, style = MaterialTheme.typography.bodySmall, color = Board.ink, modifier = Modifier.padding(start = 18.dp, top = 2.dp), maxLines = 4, overflow = TextOverflow.Ellipsis)
            val tags = listOfNotNull(c.str("lead_status")?.let { humanStatus(it) }, if (c.get("has_transcript")?.asBoolean == true) "transcript on file" else null)
            if (tags.isNotEmpty()) Text(tags.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = Board.grey, modifier = Modifier.padding(start = 18.dp))
        }
        Divider(color = Board.border, thickness = 0.5.dp, modifier = Modifier.padding(top = 5.dp))
    }
}

// ------------------------------------------------------------------ words

private fun callWords(dir: String?, snippet: String?): String {
    val s = snippet?.trim().orEmpty()
    return when {
        s == "missed" -> "Missed call"
        s == "voicemail" -> "Voicemail"
        s == "rejected" -> "Declined call"
        s == "blocked" -> "Blocked call"
        s == "recorded" || s == "answered" || s.isBlank() -> if (dir == "outbound") "Outgoing call" else "Incoming call"
        else -> s
    }
}

fun prettyPhone(p: String?): String? {
    if (p.isNullOrBlank()) return null
    val d = p.filter { it.isDigit() }
    return when {
        d.length == 11 && d.startsWith("1") -> "(" + d.substring(1, 4) + ") " + d.substring(4, 7) + "-" + d.substring(7)
        d.length == 10 -> "(" + d.substring(0, 3) + ") " + d.substring(3, 6) + "-" + d.substring(6)
        else -> p
    }
}

private fun fmtSecs(s: Int): String = if (s < 60) "${s}s" else "${s / 60} min" + (if (s % 60 != 0) " ${s % 60}s" else "")

private fun parseAt(raw: String?): OffsetDateTime? = try {
    if (raw.isNullOrBlank()) null else OffsetDateTime.parse(raw.trim().replaceFirst(' ', 'T'))
} catch (e: Exception) { null }

private fun dayOf(raw: String?): LocalDate? = parseAt(raw)?.atZoneSameInstant(ZoneId.systemDefault())?.toLocalDate()

private fun timeOf(raw: String?): String = parseAt(raw)?.atZoneSameInstant(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US)) ?: ""

/** The way the phone writes it in a list: the time today, the weekday this week, else the date. */
fun whenShort(raw: String?): String {
    val t = parseAt(raw)?.atZoneSameInstant(ZoneId.systemDefault()) ?: return ""
    val today = LocalDate.now()
    val d = t.toLocalDate()
    return when {
        d == today -> t.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
        d == today.minusDays(1) -> "Yesterday"
        ChronoUnit.DAYS.between(d, today) < 7 -> t.format(DateTimeFormatter.ofPattern("EEE", Locale.US))
        d.year == today.year -> t.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))
        else -> t.format(DateTimeFormatter.ofPattern("M/d/yy", Locale.US))
    }
}
