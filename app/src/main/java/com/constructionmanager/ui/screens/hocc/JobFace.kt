package com.constructionmanager.ui.screens.hocc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * THE JOB ROOM, DRAWN THE PROTOTYPE'S WAY (Nov 7 2026). Mirrors the approved prototype's markup and CSS one for one:
 *  - header: dark "Leads and jobs" button, "Today is" with the day's planned work, the blue "Tonight's office work" button;
 *  - Fold open (>= 600 dp): hub and spoke - Estimates / Schedule / Daily logs left, the client card in the centre, Scope / Selections /
 *    Photos right (columns 1 : 1.3 : 1, three equal rows), Caroline across the bottom;
 *  - folded: one stack in his order, each card with move up / down;
 *  - every dashboard card only reads; a tap opens its page, where the editing lives (Tyler, Nov 7).
 * Colors and type are the prototype's tokens (the same as Board.kt): ground #E9EDEA, card white, ink #1B2229, mute #5B6670, line
 * #D5DAD4, blue #1F4E9A, rust #9A3B12, wood #B5651D for this job's days; Barlow Condensed, IBM Plex Sans, IBM Plex Mono.
 */

private object Face {
    val ground = Color(0xFFE9EDEA)
    val card = Color.White
    val card2 = Color(0xFFF4F6F3)
    val ink = Color(0xFF1B2229)
    val ink2 = Color(0xFF36414B)
    val mute = Color(0xFF5B6670)
    val line = Color(0xFFD5DAD4)
    val line2 = Color(0xFFC9CFC8)
    val blue = Color(0xFF1F4E9A)
    val blueSoft = Color(0xFFEAF0F9)
    val rust = Color(0xFF9A3B12)
    val rustSoft = Color(0xFFFBE9E7)
    val green = Color(0xFF2E6B45)
    val greenSoft = Color(0xFFE5F2E9)
    val amberSoft = Color(0xFFFFF6E0)
    val amberLine = Color(0xFFE8D9A8)
    val amberInk = Color(0xFF6B4E00)
    val today = Color(0xFFB3261E)
    val wood = Color(0xFFB5651D)
}

/** The prototype's card keys and who keeps each (its OWNERS table). */
enum class FaceCard(val key: String, val title: String, val owner: String, val room: RoomCard) {
    Client("client", "Client and money", "Ledger", RoomCard.Vitals),
    Caro("caro", "Caroline", "Caroline", RoomCard.Caroline),
    Sched("sched", "Schedule", "Mason", RoomCard.Schedule),
    Logs("logs", "Daily logs", "Field Ops", RoomCard.Logs),
    Est("est", "Estimates", "Sawyer", RoomCard.Estimate),
    Scope("scope", "Scope", "Architect", RoomCard.Scope),
    Sel("sel", "Selections", "Ivy", RoomCard.Selections),
    Photos("photos", "Photos", "Field Ops", RoomCard.Photos);

    companion object {
        /** The prototype's folded order (ORDER_DEFAULT). */
        val ORDER = listOf(Client, Caro, Sched, Logs, Est, Scope, Sel, Photos)
        fun byKey(k: String) = values().firstOrNull { it.key == k }
    }
}

/** What the header shows: the day, the planned work today across the jobs, the next appointment, and tonight's count. */
data class FaceHeader(val today: LocalDate, val listCount: Int, val dayLine: String, val nextAppt: String?, val tonightCount: Int)

private val LONGDAY: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)

@Composable
private fun lbl(text: String, color: Color = Face.mute, modifier: Modifier = Modifier) =
    Text(text.uppercase(), fontFamily = Board.sans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.08.sp * 11, color = color, modifier = modifier)

@Composable
private fun meta(text: String, color: Color = Face.mute) = Text(text, fontFamily = Board.sans, fontSize = 12.sp, color = color, lineHeight = 16.sp)

@Composable
private fun body(text: String, size: Int = 14, weight: FontWeight = FontWeight.Normal, color: Color = Face.ink, modifier: Modifier = Modifier, maxLines: Int = Int.MAX_VALUE) =
    Text(text, fontFamily = Board.sans, fontSize = size.sp, fontWeight = weight, color = color, lineHeight = (size * 1.4).sp, modifier = modifier, maxLines = maxLines, overflow = TextOverflow.Ellipsis)

@Composable
private fun num(text: String, size: Int = 13, weight: FontWeight = FontWeight.SemiBold, color: Color = Face.ink) =
    Text(text, fontFamily = Board.mono, fontSize = size.sp, fontWeight = weight, color = color)

@Composable
private fun chip(c: FaceChip) {
    val (bg, fg) = when (c) {
        FaceChip.Done -> Face.greenSoft to Face.green
        FaceChip.Prog -> Face.blueSoft to Face.blue
        FaceChip.Open, FaceChip.Ask -> Face.rustSoft to Face.rust
    }
    Text(c.word, fontFamily = Board.sans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = fg,
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(bg).padding(horizontal = 7.dp, vertical = 3.dp), maxLines = 1)
}

@Composable
private fun plainChip(text: String) = Text(text, fontFamily = Board.sans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Face.ink2,
    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Face.card2).border(1.dp, Face.line, RoundedCornerShape(4.dp)).padding(horizontal = 7.dp, vertical = 3.dp), maxLines = 1)

/** The prototype's "To find" tag: a gap in the record, said, never filled in. */
@Composable
private fun find(what: String? = null) = Text("To find" + (what?.let { ": $it" } ?: ""), fontFamily = Board.sans, fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp, color = Face.amberInk, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Face.amberSoft)
        .border(1.dp, Face.amberLine, RoundedCornerShape(4.dp)).padding(horizontal = 7.dp, vertical = 2.dp))

@Composable
private fun empty(text: String) = Text(text, fontFamily = Board.sans, fontSize = 13.sp, color = Face.mute, lineHeight = 18.sp,
    modifier = Modifier.fillMaxWidth().border(1.dp, Face.line2, RoundedCornerShape(8.dp)).padding(10.dp))

@Composable
private fun hint(text: String) = Text(text, fontFamily = Board.sans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Face.blue)

@Composable
private fun row(a: String, b: String, chip: FaceChip? = null, aWeight: FontWeight = FontWeight.SemiBold) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            body(a, 14, aWeight, modifier = Modifier.weight(1f))
            if (b.isNotBlank()) body(b, 13, color = Face.ink2, modifier = Modifier.widthIn(max = 200.dp))
            chip?.let { chip(it) }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Face.line))
    }
}

// ------------------------------------------------------------------ the header

@Composable
fun FaceHeaderBar(h: FaceHeader, onJobs: () -> Unit, onTonight: () -> Unit) {
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Face.card).border(1.dp, Face.line, RoundedCornerShape(10.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DarkButton("Leads and jobs", h.listCount, Face.ink, menu = true, onClick = onJobs)
        Column(Modifier.widthIn(min = 200.dp, max = 520.dp)) {
            lbl("Today is · your day kept by Hermes")
            Text(h.today.format(LONGDAY), fontFamily = Board.disp, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Face.ink, lineHeight = 25.sp)
            body(h.dayLine, 13, color = Face.ink2)
            h.nextAppt?.let { body("Next appointment: $it", 13, color = Face.ink2) }
        }
        DarkButton("Tonight's office work", h.tonightCount, Face.blue, onClick = onTonight)
    }
}

@Composable
private fun DarkButton(text: String, count: Int, color: Color, menu: Boolean = false, onClick: () -> Unit) {
    Row(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(8.dp)).background(color).clickable { onClick() }.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (menu) Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Text(text, fontFamily = Board.sans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
        Text(count.toString(), fontFamily = Board.mono, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

// ------------------------------------------------------------------ the room

/**
 * The job room's dashboard for one job, the prototype's layout. [wide] = the Fold open. [onOpen] opens a card's page (the editing
 * pages already in the app); [onMove] reorders the folded stack; [photo] draws one of the job's photos by its record id.
 */
@Composable
fun JobRoomFace(
    face: RoomFace, header: FaceHeader, wide: Boolean, order: List<FaceCard> = FaceCard.ORDER,
    onOpen: (FaceCard) -> Unit, onMove: (FaceCard, Int) -> Unit = { _, _ -> }, onJobs: () -> Unit, onTonight: () -> Unit,
    photo: @Composable (String) -> Unit = {}
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Face.ground)) {
        val gridH = maxOf(maxHeight * 0.72f, 560.dp)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FaceHeaderBar(header, onJobs, onTonight)
            if (wide) {
                Row(Modifier.fillMaxWidth().height(gridH), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(FaceCard.Est, FaceCard.Sched, FaceCard.Logs).forEach { c -> CardShell(c, true, Modifier.weight(1f), onOpen, onMove) { CardContent(c, face, photo) } }
                    }
                    ClientFace(face, Modifier.weight(1.3f).fillMaxHeight(), scroll = true) { onOpen(FaceCard.Client) }
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(FaceCard.Scope, FaceCard.Sel, FaceCard.Photos).forEach { c -> CardShell(c, true, Modifier.weight(1f), onOpen, onMove) { CardContent(c, face, photo) } }
                    }
                }
                CardShell(FaceCard.Caro, true, Modifier.heightIn(max = maxOf(maxHeight * 0.31f, 220.dp)), onOpen, onMove) { CaroContent(face, wide = true) }
            } else {
                order.forEach { c ->
                    when (c) {
                        FaceCard.Client -> ClientFace(face, Modifier.fillMaxWidth(), scroll = false) { onOpen(FaceCard.Client) }
                        FaceCard.Caro -> CardShell(c, false, Modifier, onOpen, onMove) { CaroContent(face, wide = false) }
                        else -> CardShell(c, false, Modifier, onOpen, onMove) { CardContent(c, face, photo) }
                    }
                }
            }
            meta("Read from the office record. Tap any card to open its page.")
            Spacer(Modifier.height(14.dp))
        }
    }
}

/** A dashboard card: the prototype's card-h (title, kept by, move arrows when folded, chevron) over its body; tap opens the page. */
@Composable
private fun CardShell(c: FaceCard, wide: Boolean, modifier: Modifier, onOpen: (FaceCard) -> Unit, onMove: (FaceCard, Int) -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Face.card).border(1.dp, Face.line, RoundedCornerShape(10.dp)).clickable { onOpen(c) }) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(c.title, fontFamily = Board.disp, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (c == FaceCard.Caro) Face.blue else Face.ink,
                modifier = Modifier.weight(1f), maxLines = 1)
            if (wide) Text(if (c == FaceCard.Caro) "Front desk · voice first" else "Kept by ${c.owner}", fontFamily = Board.sans, fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp, color = Face.mute, maxLines = 1)
            else {
                IconBox(Icons.Default.KeyboardArrowUp, "Move ${c.title} up") { onMove(c, -1) }
                IconBox(Icons.Default.KeyboardArrowDown, "Move ${c.title} down") { onMove(c, 1) }
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Face.mute, modifier = Modifier.size(22.dp))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Face.line))
        val inner = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
        Column(if (wide) inner.verticalScroll(rememberScrollState()) else inner, verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}

@Composable
private fun IconBox(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Face.line2, RoundedCornerShape(8.dp)).clickable { onClick() },
        contentAlignment = Alignment.Center) { Icon(icon, contentDescription = label, tint = Face.ink, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun CardContent(c: FaceCard, f: RoomFace, photo: @Composable (String) -> Unit) {
    when (c) {
        FaceCard.Est -> {
            if (f.estimates.isEmpty()) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                body("No estimate on record here", 14, FontWeight.SemiBold); find()
            }
            f.estimates.forEach { row(it.a, it.b) }
        }
        FaceCard.Sched -> SchedContent(f)
        FaceCard.Logs -> {
            if (f.logs.isEmpty()) empty("No daily logs on this job yet.")
            f.logs.take(4).forEach { l ->
                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    meta(RoomFaces.fmt(l.date) + (l.hours?.takeIf { it > 0 }?.let { " · " + JobPlan.hrs(it) } ?: ""))
                    body(l.words.ifBlank { "Hours logged, no words with them." }, 14, color = if (l.words.isBlank()) Face.mute else Face.ink, maxLines = 4)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Face.line))
            }
            hint(if (f.logs.size > 4) "${f.logs.size - 4} more · tap to open" else "Tap to add a log")
        }
        FaceCard.Scope -> {
            val done = f.scope.count { it.chip == FaceChip.Done }
            if (f.scope.isEmpty()) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                body("No scope written for this job yet", 14, FontWeight.SemiBold); find()
            } else meta(if (f.scope.any { it.chip != null }) "$done of ${f.scope.size} done" else "${f.scope.size} lines" + (f.scopeNote?.let { " · $it" } ?: ""))
            f.scope.take(6).forEach { row(it.a, "", it.chip, FontWeight.Medium) }
            if (f.scope.size > 6) hint("${f.scope.size - 6} more · tap to open")
        }
        FaceCard.Sel -> {
            if (f.selections.isEmpty()) empty("No selections recorded yet.")
            f.selections.take(7).forEach { row(it.a, it.b, it.chip) }
            if (f.selections.size > 7) hint("${f.selections.size - 7} more · tap to open")
        }
        FaceCard.Photos -> {
            if (f.photoIds.isEmpty()) empty("No photos filed for this job yet.")
            else Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { f.photoIds.take(3).forEach { id -> Box(Modifier.weight(1f).height(84.dp).clip(RoundedCornerShape(6.dp))) { photo(id) } } }
            if (f.photoIds.size > 3) hint("${f.photoIds.size - 3} more · tap to open")
        }
        else -> {}
    }
}

@Composable
private fun SchedContent(f: RoomFace) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        f.days.forEach { d ->
            val isToday = d.date == LocalDate.now() || (f.days.size == 14 && d == f.days[1])
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val bg = when (d.pip) { FacePip.Mine -> Face.wood; FacePip.Other -> Face.line2.copy(alpha = 0.6f); FacePip.Appt -> Face.green; FacePip.None -> Face.card2 }
                Box(Modifier.fillMaxWidth().height(22.dp).let { if (isToday) it.border(2.dp, Face.today, RoundedCornerShape(5.dp)).padding(3.dp) else it }
                    .clip(RoundedCornerShape(4.dp)).background(bg).let { if (d.pip == FacePip.None) it.border(1.dp, Face.line, RoundedCornerShape(4.dp)) else it })
                val c = if (isToday) Face.today else Face.mute
                Text(d.date.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, Locale.US), fontSize = 10.sp, color = c, fontFamily = Board.sans,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                Text(d.date.dayOfMonth.toString(), fontSize = 10.sp, color = c, fontFamily = Board.mono, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        legend(Face.wood, "This job"); legend(Face.line2, "Other jobs"); legend(Face.green, "Appointment")
        Text("Red outline: today", fontSize = 11.sp, color = Face.today, fontFamily = Board.sans)
        Text("Tap a day for its tasks", fontSize = 11.sp, color = Face.mute, fontFamily = Board.sans)
    }
    if (f.ganttLine != null || f.ganttNote != null) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Face.blueSoft).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            lbl(f.ganttLine ?: "Gantt · not on the calendar yet")
            f.ganttNote?.let { body(it, 13) }
        }
    }
    f.blocks.forEach { b ->
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Face.amberSoft).padding(horizontal = 12.dp, vertical = 10.dp)) {
            lbl("Waiting on", Face.amberInk); body(b, 13)
        }
    }
    f.upcoming.forEach { (w, what) ->
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { meta(w); body(what, 14, maxLines = 3) }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Face.line))
    }
    if (f.upcoming.isEmpty()) empty("Nothing planned for this job in the next weeks." + (f.nextWhat?.let { " Next: $it" } ?: " The office schedule has no tasks on it yet."))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { meta("Hours: ${f.hoursLine}") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        meta("Left: ${f.leftLine}"); if (f.ganttNote == null) find("task hours")
    }
}

@Composable
private fun legend(color: Color, text: String) = Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    Box(Modifier.width(12.dp).height(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
    Text(text, fontSize = 11.sp, color = Face.mute, fontFamily = Board.sans)
}

@Composable
private fun CaroContent(f: RoomFace, wide: Boolean) {
    val calls: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            lbl("Calls and texts with this client")
            if (f.comms.isEmpty()) empty("No client calls or texts are linked to this job yet.")
            f.comms.forEach { m ->
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Face.card2).border(1.dp, Face.line, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 7.dp)) {
                    meta(listOfNotNull(m.date?.let { RoomFaces.fmt(it) }, m.who).joinToString(" · "))
                    body(m.words, 13, maxLines = 4)
                }
            }
        }
    }
    val you: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            lbl("With you")
            empty("Tap to ask Caroline about ${f.shortName}.")
            if (f.tonightCount > 0) hint("${f.tonightCount} item${if (f.tonightCount > 1) "s" else ""} on tonight's office list")
        }
    }
    if (wide) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Box(Modifier.weight(1.2f)) { calls() }; Box(Modifier.weight(1f)) { you() } }
    else { calls(); you() }
}

// ------------------------------------------------------------------ the client card

@Composable
private fun ClientFace(f: RoomFace, modifier: Modifier, scroll: Boolean, onOpen: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(10.dp)).background(Face.card).border(1.dp, Face.line, RoundedCornerShape(10.dp)).clickable { onOpen() }) {
        Column(Modifier.fillMaxWidth().background(f.stage.band).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            lbl(listOfNotNull(f.stage.label, f.since).joinToString(" · "), Color.White.copy(alpha = 0.92f))
            Text(f.name, fontFamily = Board.disp, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 34.sp, color = Color.White, maxLines = 2)
            f.what?.let { Text(it, fontFamily = Board.sans, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color.White) }
        }
        val inner = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)
        Column(if (scroll) inner.verticalScroll(rememberScrollState()) else inner, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (f.addr != null) body(f.addr, 16, FontWeight.SemiBold) else find("address")
                f.city?.let { Text(it, fontFamily = Board.sans, fontSize = 12.sp, color = Face.mute, modifier = Modifier.align(Alignment.CenterVertically)) }
            }
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (f.phone != null) num(f.phone, 14, FontWeight.Medium) else find("phone, on Caroline's card")
                if (f.email != null) body(f.email, 14) else meta("No email on file")
            }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Face.blueSoft).padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                lbl("Next · " + (f.nextWhen ?: "not on the calendar"))
                Text(f.nextWhat ?: "Nothing planned for this job yet.", fontFamily = Board.sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Face.ink)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    lbl("Money"); Text("Kept by Ledger", fontFamily = Board.sans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Face.mute)
                }
                if (f.contract != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tile("Contract", money(f.contract), Modifier.weight(1f))
                        tile("Paid", f.paid?.let { money(it) }, Modifier.weight(1f), findIfNull = true)
                        tile("Balance", f.balance?.let { money(it) }, Modifier.weight(1f))
                    }
                    val pct = f.pctPaid
                    if (pct != null) {
                        Row(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Face.card2).border(1.dp, Face.line, RoundedCornerShape(6.dp))) {
                            if (pct > 0) Box(Modifier.fillMaxHeight().weight(pct.toFloat()).background(Face.green))
                            if (pct < 100) Spacer(Modifier.weight((100 - pct).toFloat()))
                        }
                        Row { num("$pct%", 12, FontWeight.Medium, Face.mute); meta(" paid") }
                    }
                } else tile("Amount", null, Modifier.fillMaxWidth(), findIfNull = true, findWhat = "contract amount")
                f.draws.forEach { d ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        body(d.a, 13, modifier = Modifier.weight(1f)); if (d.chip != null) chip(d.chip) else if (d.b.isNotBlank()) plainChip(d.b)
                    }
                }
                meta(f.moneyNote)
            }
            if (f.needs.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                lbl("Needs you · ${f.needs.size}")
                f.needs.forEach { n -> Row { body("•  ", 13, color = Face.ink2); body(n, 13, color = Face.ink2) } }
            }
        }
    }
}

@Composable
private fun tile(label: String, value: String?, modifier: Modifier, findIfNull: Boolean = false, findWhat: String? = null) {
    Column(modifier.clip(RoundedCornerShape(8.dp)).background(Face.card2).border(1.dp, Face.line, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)) {
        lbl(label)
        if (value != null) Text(value, fontFamily = Board.mono, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Face.ink, lineHeight = 22.sp)
        else if (findIfNull) find(findWhat) else num("—", 18)
    }
}

// ------------------------------------------------------------------ Leads and jobs (the prototype's drawer; also the Jobs tab)

/**
 * The prototype's Leads and jobs list: by stage (on site, wrapping up, signed, estimate out, site visit booked, then leads), each
 * line the street and the client, the job under it. [drawer] draws its header with a Close button.
 */
@Composable
fun FaceJobsList(groups: List<Pair<FaceStage, List<FaceListItem>>>, currentId: Int?, query: String, onQuery: (String) -> Unit,
                 onPick: (Int) -> Unit, onClose: (() -> Unit)?, leadsOpen: Boolean, onLeadsOpen: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.background(Face.card)) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Leads and jobs", fontFamily = Board.disp, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Face.ink, modifier = Modifier.weight(1f))
            if (onClose != null) IconBox(Icons.Default.Close, "Close", onClose)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Face.line))
        BasicTextField(value = query, onValueChange = onQuery, singleLine = true, textStyle = TextStyle(fontFamily = Board.sans, fontSize = 15.sp, color = Face.ink),
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 4.dp).fillMaxWidth().heightIn(min = 48.dp)
                .border(1.dp, Face.line2, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 14.dp),
            decorationBox = { inner -> if (query.isEmpty()) Text("Address, name or phone", fontFamily = Board.sans, fontSize = 15.sp, color = Face.mute); inner() })
        val q = query.trim().lowercase()
        val shown = groups.map { (st, items) -> st to items.filter { q.isEmpty() || (it.title + " " + it.sub).lowercase().contains(q) } }.filter { it.second.isNotEmpty() }
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (shown.isEmpty()) empty("No match. Try a street name.")
            shown.forEach { (st, items) ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val isLeads = st == FaceStage.Lead
                    Row(Modifier.fillMaxWidth().let { if (isLeads && q.isEmpty()) it.clickable { onLeadsOpen(!leadsOpen) } else it }.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(st.band))
                        lbl(if (isLeads) "Leads · ${items.size}" else st.label)
                        if (isLeads && q.isEmpty()) Text(if (leadsOpen) "hide" else "show", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Face.blue, fontFamily = Board.sans)
                    }
                    if (!isLeads || leadsOpen || q.isNotEmpty()) items.forEach { it ->
                        Column(Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(8.dp)).background(if (it.projectId == currentId) Face.blueSoft else Color.Transparent)
                            .clickable { onPick(it.projectId) }.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            body(it.title, 15, FontWeight.SemiBold, maxLines = 2)
                            if (it.sub.isNotBlank()) Text(it.sub, fontFamily = Board.sans, fontSize = 12.sp, color = Face.mute, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

/** The day line under "Today is": each job's work planned today, from the plans ("Humphrey: stone, living-room face"). */
fun dayLine(plans: List<Plan>, today: LocalDate): String {
    val parts = plans.mapNotNull { p ->
        val d = JobPlan.projectedOn(p, today)
        if (d.rows.isEmpty()) null else "${p.input.name}: " + d.rows.joinToString("; ") { it.bar.task.title }
    }
    return if (parts.isEmpty()) "Nothing planned today" else parts.joinToString(" · ")
}

@Suppress("unused") private val keepDp: Dp = 0.dp
