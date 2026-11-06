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

/** One day on site: the stretches between arriving and leaving, the day's total, and how the day was matched to this job. */
data class SiteDay(val date: LocalDate, val stretches: List<String>, val hours: Double, val conf: String?, val address: String?,
                   val calls: Int?, val texts: Int?)

/** Days on site for this job: from the location history when the office serves it (projects/{id}/site-hours), else the hours clocked
 *  and logged on the job, grouped by day. Second value: true when the days came from the location history. */
fun siteDaysOf(d: RoomData): Pair<List<SiteDay>, Boolean> {
    val sh = d.siteHours
    val fromHistory = mutableListOf<SiteDay>()
    if (sh != null) {
        val obj = sh.asObjOrNull()
        val files = obj?.arrOf("files")?.objects()
        val groups: List<Pair<String?, List<JsonObject>>> = when {
            files != null -> files.map { f -> f.str("label", "address") to (f.arrOf("days")?.objects().orEmpty()) }
            else -> listOf(null to sh.rowsOf("days", "rows", "items").objects())
        }
        groups.forEach { (label, days) ->
            days.forEach { x ->
                val date = x.str("date", "work_date")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@forEach
                val trips = x.str("trip_list", "stretches")?.split(';')?.map { it.trim().replace("-", "–") }?.filter { it.isNotBlank() }
                    ?: listOfNotNull(listOfNotNull(x.str("in", "arrived"), x.str("out", "left")).takeIf { it.size == 2 }?.joinToString("–"))
                fromHistory += SiteDay(date, trips, x.num("hours") ?: 0.0, x.str("conf", "confidence"), x.str("address", "label") ?: label,
                    x.num("calls")?.toInt(), x.num("texts")?.toInt())
            }
        }
    }
    if (fromHistory.isNotEmpty()) return fromHistory.sortedByDescending { it.date } to true
    // the hours on the record (clocked in and out, or logged), one row per day
    val rows = d.hours?.rowsOf("entries")?.objects().orEmpty()
    val byDay = rows.groupBy { it.str("work_date")?.take(10) }.filterKeys { it != null }
    val days = byDay.mapNotNull { (day, es) ->
        val date = runCatching { LocalDate.parse(day) }.getOrNull() ?: return@mapNotNull null
        val stretches = es.mapNotNull { e ->
            val a = localClock(e.str("started_at")); val b = localClock(e.str("ended_at"))
            if (a != null && b != null) "$a–$b" else null
        }
        SiteDay(date, stretches, es.sumOf { it.num("hours") ?: 0.0 }, "clocked", null, null, null)
    }
    return days.sortedByDescending { it.date } to false
}

private fun localClock(raw: String?): String? = try {
    raw?.let { OffsetDateTime.parse(it.replace(' ', 'T')).atZoneSameInstant(ZoneId.systemDefault()).format(CLOCK) }
} catch (e: Exception) { null }

private fun hrs(h: Double): String = String.format(Locale.US, "%.2f", h).trimEnd('0').trimEnd('.') + " h"

/** The match's sureness, coloured as the Job Site Hours page colours it: high green, medium amber, low red; his own map blue. */
@Composable
private fun ConfPill(conf: String?) {
    val c = (conf ?: "").lowercase()
    val (label, color) = when (c) {
        "high" -> "high" to Board.green
        "medium" -> "medium" to Board.barProjected
        "low" -> "low" to Board.red
        "your map" -> "your map" to Board.blue
        "clocked" -> "clocked" to Board.grey
        "" -> return
        else -> c to Board.grey
    }
    Text(label, style = MaterialTheme.typography.labelSmall, color = color,
        modifier = Modifier.clip(RoundedCornerShape(50)).border(1.dp, color, RoundedCornerShape(50)).padding(horizontal = 7.dp, vertical = 1.dp))
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(8.dp)).background(Board.tile).padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Lbl(label)
        Num(value, 15)
    }
}

/** Job site hours: the timesheet for this job - each day, each stretch on site, the day's total, the match. */
@Composable
fun SiteHoursCard(d: RoomData, full: Boolean, onOpen: () -> Unit) {
    val (days, fromHistory) = siteDaysOf(d)
    BoardCard {
        CardHead("Job site hours") { if (!full && days.isNotEmpty()) Tool("Open sheet") { onOpen() } }
        if (days.isEmpty()) {
            Dashed("No days on site for this job yet. Days from your location history land here as soon as the office has them matched to this address; hours you clock or log here land here too.")
            return@BoardCard
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat("Hours", hrs(days.sumOf { it.hours }), Modifier.weight(1f))
            Stat("Days on site", days.size.toString(), Modifier.weight(1f))
        }
        Text(if (fromHistory) "From your location history, matched to this job by address. " + days.first().date.format(DAY) + " back to " + days.last().date.format(DAY) + "."
             else "From the hours clocked and logged on this job. Days from your location history join these once the office has them matched.",
            style = MaterialTheme.typography.bodySmall, color = Board.grey)
        days.mapNotNull { it.address }.distinct().takeIf { it.isNotEmpty() }?.let { Text(it.joinToString(" · "), style = MaterialTheme.typography.titleSmall) }
        days.take(if (full) days.size else 6).forEach { day ->
            Divider(color = Board.borderSoft)
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(day.date.format(DAY), style = MaterialTheme.typography.bodySmall, color = Board.ink)
                    day.stretches.forEach { Num(it, 12, Board.body, androidx.compose.ui.text.font.FontWeight.Normal) }
                    if (full && ((day.calls ?: 0) > 0 || (day.texts ?: 0) > 0))
                        Text(listOfNotNull(day.calls?.takeIf { it > 0 }?.let { countLabel(it, "call") }, day.texts?.takeIf { it > 0 }?.let { countLabel(it, "text") }).joinToString(" · ") + " with the client that day",
                            style = MaterialTheme.typography.labelSmall, color = Board.grey)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Num(hrs(day.hours), 13)
                    ConfPill(day.conf)
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
