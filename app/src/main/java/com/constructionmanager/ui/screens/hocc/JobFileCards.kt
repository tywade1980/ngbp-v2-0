package com.constructionmanager.ui.screens.hocc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * THE JOB'S FILE (Tyler, Nov 6 2026): "each jobs sheets with each clients projects... and the text records and the call logs and once we
 * get recordings figured out those also" - "each will be a peripheral card around that center card". Job site hours read like the Job Site
 * Hours page's timesheet (the day, every stretch on site, the day's total, how sure the match is); texts and calls are the client's own
 * record by their number; recordings are the calls that carry audio. Nothing here is made up: an empty card says what is missing.
 */

private val DAY = DateTimeFormatter.ofPattern("EEE MMM d, yyyy", Locale.US)
private val CLOCK = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

/** One day on site: the stretches between arriving and leaving (each "7:58 AM–9:44 AM · 1:46"), the day's total, its trips. */
data class SiteDay(val date: LocalDate, val stretches: List<String>, val hours: Double, val time: String?, val trips: Int, val note: String?)

/** A job's sheet: its days, where they came from, the hours on file from other sources (never added in), and the page's stats. */
data class SiteSheet(val days: List<SiteDay>, val fromHistory: Boolean, val otherHours: Double, val stats: JsonObject?, val address: String?)

private val STRETCH = Regex("""(\d{1,2}:\d{2} [AP]M)-(\d{1,2}:\d{2} [AP]M)""")

/** The job's sheet: the office's site-hours answer for this job (clients -> projects -> days) when it serves one; else the location rows in
 *  the job's hours (source location, the trips read from each row's note), with every other source counted beside them, never added in;
 *  else the hours clocked and logged on the job, one row per day. */
fun siteSheetOf(d: RoomData): SiteSheet {
    val proj = d.siteHours.asObjOrNull()?.arrOf("clients")?.objects().orEmpty()
        .flatMap { it.arrOf("projects")?.objects().orEmpty() }
        .firstOrNull { it.num("project_id")?.toInt() == d.id }
    if (proj != null) {
        val days = proj.arrOf("days")?.objects().orEmpty().mapNotNull { x ->
            val date = x.str("date")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@mapNotNull null
            val st = x.arrOf("stretches")?.objects().orEmpty().map { s ->
                listOfNotNull(s.str("clock_in"), s.str("clock_out")).joinToString("–") + (s.str("time")?.let { " · $it" } ?: "")
            }.ifEmpty { listOfNotNull(listOfNotNull(x.str("clock_in"), x.str("clock_out")).takeIf { it.size == 2 }?.joinToString("–")) }
            SiteDay(date, st, x.num("hours") ?: 0.0, x.str("time"), x.num("trips")?.toInt() ?: st.size, x.str("note"))
        }
        return SiteSheet(days.sortedByDescending { it.date }, true, proj.num("other_hours") ?: 0.0, proj.objOf("stats"), proj.str("address"))
    }
    val rows = d.hours?.rowsOf("entries")?.objects().orEmpty()
    val loc = rows.filter { it.str("source_kind") == "location" }
    if (loc.isNotEmpty()) {
        val days = loc.mapNotNull { e ->
            val date = e.str("work_date")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@mapNotNull null
            val st = STRETCH.findAll(e.str("note") ?: "").map { "${it.groupValues[1]}–${it.groupValues[2]}" }.toList()
            SiteDay(date, st, e.num("hours") ?: 0.0, null, st.size.coerceAtLeast(1), e.str("note"))
        }
        return SiteSheet(days.sortedByDescending { it.date }, true, rows.filter { it.str("source_kind") != "location" }.sumOf { it.num("hours") ?: 0.0 }, null, null)
    }
    // the hours on the record (clocked in and out, or logged), one row per day
    val byDay = rows.groupBy { it.str("work_date")?.take(10) }.filterKeys { it != null }
    val days = byDay.mapNotNull { (day, es) ->
        val date = runCatching { LocalDate.parse(day) }.getOrNull() ?: return@mapNotNull null
        val stretches = es.mapNotNull { e ->
            val a = localClock(e.str("started_at")); val b = localClock(e.str("ended_at"))
            if (a != null && b != null) "$a–$b" else null
        }
        SiteDay(date, stretches, es.sumOf { it.num("hours") ?: 0.0 }, null, stretches.size.coerceAtLeast(1), null)
    }
    return SiteSheet(days.sortedByDescending { it.date }, false, 0.0, null, null)
}

private fun localClock(raw: String?): String? = try {
    raw?.let { OffsetDateTime.parse(it.replace(' ', 'T')).atZoneSameInstant(ZoneId.systemDefault()).format(CLOCK) }
} catch (e: Exception) { null }

private fun hrs(h: Double): String = String.format(Locale.US, "%.2f", h).trimEnd('0').trimEnd('.') + " h"

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(8.dp)).background(Board.tile).padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Lbl(label)
        Num(value, 15)
    }
}

/** Job site hours: the timesheet for this job, read left to right like the Job Site Hours page - the day, every stretch on site with its
 *  clock in, clock out and time, the day's total. Hours from other sources sit beside it, never added in. */
@Composable
fun SiteHoursCard(d: RoomData, full: Boolean, onOpen: () -> Unit) {
    val sheet = siteSheetOf(d)
    val days = sheet.days
    BoardCard {
        CardHead("Job site hours") { if (!full && days.isNotEmpty()) Tool("Open sheet") { onOpen() } }
        if (days.isEmpty()) Dashed("No days on site for this job yet. Days from your location history land here as soon as they are matched to this job's address; hours you clock or log here land here too.")
        else SiteHoursBody(sheet, full)
    }
}

@Composable
private fun SiteHoursBody(sheet: SiteSheet, full: Boolean) {
    val days = sheet.days
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val st = sheet.stats
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat("On site", st?.str("time") ?: hrs(days.sumOf { it.hours }), Modifier.weight(1f))
            Stat("Days", (st?.num("days_on_site")?.toInt() ?: days.size).toString(), Modifier.weight(1f))
        }
        if (sheet.otherHours > 0.0) Text("Other hours on file (Houzz, calls, the clock): " + hrs(sheet.otherHours) + ", counted beside these, never added in.",
            style = MaterialTheme.typography.bodySmall, color = Board.body)
        Text((if (sheet.fromHistory) "From your location history, matched to this job by address. " else "From the hours clocked and logged on this job. ") +
            days.last().date.format(DAY) + " to " + days.first().date.format(DAY) + ".", style = MaterialTheme.typography.bodySmall, color = Board.grey)
        sheet.address?.let { Text(it, style = MaterialTheme.typography.titleSmall) }
        if (full && st != null) {
            listOfNotNull(
                st.str("avg_in")?.let { "Average arrival $it" }, st.str("avg_out")?.let { "average departure $it" },
                st.num("longest_streak")?.let { "longest run ${it.toInt()} days in a row" + (st.str("streak_dates")?.let { d -> " ($d)" } ?: "") },
                st.num("early_starts")?.takeIf { it > 0 }?.let { "${it.toInt()} early starts" }, st.num("late_nights")?.takeIf { it > 0 }?.let { "${it.toInt()} late nights" },
                st.num("weekend_days")?.takeIf { it > 0 }?.let { "${it.toInt()} weekend days" }
            ).takeIf { it.isNotEmpty() }?.let { Text(it.joinToString(", ") + ".", style = MaterialTheme.typography.bodySmall, color = Board.body) }
        }
        days.take(if (full) days.size else 6).forEach { day ->
            Divider(color = Board.borderSoft)
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(day.date.format(DAY), style = MaterialTheme.typography.bodySmall, color = Board.ink)
                    day.stretches.forEach { Num(it, 12, Board.body, androidx.compose.ui.text.font.FontWeight.Normal) }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Num(day.time ?: hrs(day.hours), 13)
                    if (day.trips > 1) Text("${day.trips} trips", style = MaterialTheme.typography.labelSmall, color = Board.grey)
                }
            }
        }
        if (!full && days.size > 6) Text("${days.size - 6} more days on the sheet", style = MaterialTheme.typography.bodySmall, color = Board.blue)
    }
}

private fun commsOf(d: RoomData): List<JsonObject> = d.comms?.arrOf("items")?.objects().orEmpty()
private fun clientWord(d: RoomData): String = (d.card?.str("client_name", "client") ?: "").trim().substringBefore(' ').ifBlank { "the client" }
private fun noNumber(d: RoomData) = d.card?.str("client_phone", "phone") == null

/** Texts: the client's messages by their number, newest at the bottom like the Messages thread. */
@Composable
fun TextsCard(d: RoomData, full: Boolean, onOpen: () -> Unit) {
    val texts = commsOf(d).filter { it.str("kind") == "text" }
    BoardCard {
        CardHead("Texts" + if (texts.isNotEmpty()) " · ${texts.size}" else "") { if (!full && texts.size > 4) Tool("All texts") { onOpen() } }
        when {
            noNumber(d) -> Dashed("No phone number on this job, so no texts can be matched to it. Add the client's number on the job card.")
            texts.isEmpty() -> Dashed("No texts with ${clientWord(d)} on file.")
            else -> texts.takeLast(if (full) texts.size else 4).forEach { TextBubble(it) }
        }
    }
}

/** Call log: the client's calls by their number, newest first like the Phone app. */
@Composable
fun CallsCard(d: RoomData, full: Boolean, onOpen: () -> Unit) {
    val calls = commsOf(d).filter { it.str("kind") == "call" }.reversed()
    BoardCard {
        CardHead("Call log" + if (calls.isNotEmpty()) " · ${calls.size}" else "") { if (!full && calls.size > 4) Tool("All calls") { onOpen() } }
        when {
            noNumber(d) -> Dashed("No phone number on this job, so no calls can be matched to it.")
            calls.isEmpty() -> Dashed("No calls with ${clientWord(d)} on file.")
            else -> calls.take(if (full) calls.size else 4).forEach { CallHistoryRow(it) }
        }
    }
}

/** Recordings: the calls that carry audio. The place is here now; recordings fill it as recording comes on. */
@Composable
fun RecordingsCard(d: RoomData, full: Boolean, onOpen: () -> Unit) {
    val recs = commsOf(d).filter { it.str("kind") == "call" && (it.get("has_audio")?.let { e -> runCatching { e.asBoolean }.getOrNull() } == true || it.str("channel") == "phone_recording") }.reversed()
    BoardCard {
        CardHead("Recordings" + if (recs.isNotEmpty()) " · ${recs.size}" else "") { if (!full && recs.size > 3) Tool("All recordings") { onOpen() } }
        if (recs.isEmpty()) Dashed("No call recordings with ${clientWord(d)} on file yet. Recorded calls land here once call recording is set up.")
        else recs.take(if (full) recs.size else 3).forEach { CallHistoryRow(it) }
    }
}

/** Where a card sits on the board's grid: its column (0 left, 1 centre, 2 right), its row, and how many rows it spans. */
data class Area(val col: Int, val row: Int, val rowSpan: Int = 1)

/**
 * The board's grid, the design file's CSS grid in Compose (Oct 9 2026: columns 1fr 2.1fr 1fr, 14px gaps, cards top-aligned, a card may span
 * rows). Rows size to their tallest card; a spanning card that is taller than its rows grows its last row.
 */
@Composable
fun HubGrid(areas: List<Area>, gap: Dp = 14.dp, weights: List<Float> = listOf(1f, 2.1f, 1f), modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val g = gap.roundToPx()
        val total = constraints.maxWidth
        val sum = weights.sum()
        val usable = (total - g * (weights.size - 1)).coerceAtLeast(0)
        val colW = weights.map { (usable * it / sum).toInt() }
        val colX = colW.runningFold(0) { acc, w -> acc + w + g }
        val placeables = measurables.mapIndexed { i, m ->
            val a = areas.getOrElse(i) { Area(1, 999) }
            m.measure(Constraints(minWidth = colW[a.col], maxWidth = colW[a.col]))
        }
        val rows = (areas.take(measurables.size).maxOfOrNull { it.row + it.rowSpan } ?: 0)
        val rowH = IntArray(rows)
        areas.take(measurables.size).forEachIndexed { i, a -> if (a.rowSpan == 1) rowH[a.row] = maxOf(rowH[a.row], placeables[i].height) }
        areas.take(measurables.size).forEachIndexed { i, a ->
            if (a.rowSpan > 1) {
                val have = (a.row until a.row + a.rowSpan).sumOf { rowH[it] } + g * (a.rowSpan - 1)
                val need = placeables[i].height - have
                if (need > 0) rowH[a.row + a.rowSpan - 1] += need
            }
        }
        val used = (0 until rows).filter { r -> areas.take(measurables.size).any { r >= it.row && r < it.row + it.rowSpan } }
        val rowY = IntArray(rows)
        var y = 0
        for (r in 0 until rows) { rowY[r] = y; if (r in used) y += rowH[r] + g }
        val height = (y - g).coerceAtLeast(0)
        layout(total, height) {
            placeables.forEachIndexed { i, p ->
                val a = areas.getOrElse(i) { Area(1, 0) }
                p.place(colX[a.col], rowY[a.row])
            }
        }
    }
}
