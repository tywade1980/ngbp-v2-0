package com.constructionmanager.ui.screens.hocc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * THE SCHEDULE PAGE (HOCC job room, Nov 7 2026) - what the job room's Job schedule card opens to, every job alike.
 *
 * Four tabs, built from the office's schedule for the job and the plan engine (JobPlan):
 *  - Gantt: every task, its estimate laid end to end (thin) and the projection (thick) that reflows as real hours land; tap a task
 *    to set its hours, log time on it, or mark it finished (once the office records "finished"); tap a day to open it.
 *  - Day: the tasks projected for any day, the other jobs that day, and what missing it would move across every job.
 *  - Time on site: GPS time and logged time by day, each day linked to its daily log; hours tied to the task they were for.
 *  - Phases: the phase bars, the start date and laying the schedule out (what this card already did, kept as it was).
 * The dashboard face (the card not opened) only reads: the finish, how far off the estimate, what's planned today.
 */

/** The day the daily log should open on, when a day on the schedule asked for it (null = every day). */
val LocalLogFocus = compositionLocalOf<MutableState<String?>> { mutableStateOf(null) }
/** Open another card's page from inside one (the schedule's "Daily log" opens the logs on that day). */
val LocalOpenCard = compositionLocalOf<(RoomCard) -> Unit> { {} }
/** Every live job's plan, for the day view's other jobs and the missed-day knock-on. */
val LocalPlanBoard = compositionLocalOf<PlanBoardViewModel?> { null }

private val DAY = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
private val SHORT = DateTimeFormatter.ofPattern("MMM d", Locale.US)
private fun d(x: LocalDate) = x.format(DAY)

/** This job's plan, from the room's own read of its schedule. */
internal fun RoomData.plan(today: LocalDate = LocalDate.now()): Pair<Plan?, String?> {
    val r = JobPlan.fromSchedule(id, card?.str("name") ?: "Job #$id", schedule)
    return (r.input?.let { JobPlan.compute(it, today) }) to r.problem
}

// ------------------------------------------------------------------ the dashboard face: read only

@Composable
internal fun ScheduleDashboard(d: RoomData, onOpen: () -> Unit) {
    val today = LocalDate.now()
    val (plan, problem) = d.plan(today)
    BoardCard(modifier = Modifier.clickable { onOpen() }) {
        CardHead("Job schedule") { Tool("Open schedule") { onOpen() } }
        when {
            plan == null -> Dashed(problem ?: "No schedule on this job yet.")
            !plan.scheduled -> Dashed("${JobPlan.hrs(plan.bars.sumOf { it.task.estHours ?: 0.0 })} of tasks planned. Not on the calendar yet: set a start date on the schedule and its days fill in.")
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Tile("Projected finish", d(plan.finish), Modifier.weight(1f))
                    Tile("Left", JobPlan.hrs(plan.leftHours), Modifier.weight(1f))
                }
                Text(JobPlan.varianceWords(plan).replaceFirstChar { it.uppercase() } + (if (plan.measured > 0) " · pace from ${countLabel(plan.measured, "finished task")}" else ""),
                    style = MaterialTheme.typography.bodySmall, color = if (plan.varianceDays > 0.25) Board.red else Board.grey)
                val day = JobPlan.projectedOn(plan, today)
                Lbl(if (day.offDay) "Not a work day today" else "Planned today · ${JobPlan.hrs(day.hours)}")
                day.rows.take(4).forEach { r -> Text("· ${r.bar.task.title}  ${JobPlan.hrs(r.hours)}", style = MaterialTheme.typography.bodySmall, color = Board.body) }
                if (plan.unplanned > 0) Text("${countLabel(plan.unplanned, "task")} without hours yet", style = MaterialTheme.typography.bodySmall, color = Board.rust)
            }
        }
    }
}

@Composable
private fun Tile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(8.dp)).background(Board.tile).padding(10.dp)) {
        Lbl(label)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Board.ink)
    }
}

// ------------------------------------------------------------------ the page

@Composable
internal fun SchedulePage(room: RoomViewModel, d: RoomData, writing: Boolean, phases: @Composable () -> Unit) {
    var tab by rememberSaveable(d.id) { mutableStateOf("gantt") }
    var day by rememberSaveable(d.id) { mutableStateOf<String?>(null) }
    val today = LocalDate.now()
    val (plan, problem) = d.plan(today)
    BoardCard {
        CardHead("Job schedule")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("gantt" to "Gantt", "day" to "Day", "time" to "Time on site", "phases" to "Phases").forEach { (k, l) ->
                if (tab == k) Solid(l, Board.ink, modifier = Modifier.heightIn(min = 34.dp)) {} else Tool(l) { tab = k }
            }
        }
        val openDay: (LocalDate) -> Unit = { day = it.toString(); tab = "day" }
        when (tab) {
            "gantt" -> if (plan == null) Dashed(problem ?: "No schedule on this job yet.") else Gantt(room, d, plan, writing, openDay)
            "day" -> if (plan == null) Dashed(problem ?: "No schedule on this job yet.")
                     else DayPanel(plan, day?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: plan.rule.nextWork(today)) { day = it.toString() }
            "time" -> TimeOnSite(room, d, plan, writing)
            else -> phases()
        }
    }
}

// ------------------------------------------------------------------ Gantt

@Composable
private fun Gantt(room: RoomViewModel, d: RoomData, plan: Plan, writing: Boolean, onDay: (LocalDate) -> Unit) {
    var open by remember(d.id) { mutableStateOf<Long?>(null) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tile("Left", JobPlan.hrs(plan.leftHours), Modifier.weight(1f))
        Tile("Projected finish", if (plan.scheduled) plan.finish.format(SHORT) else "not set", Modifier.weight(1f))
        Tile("Vs estimate", if (!plan.scheduled) "—" else if (kotlin.math.abs(plan.varianceDays) < 0.25) "on it" else (if (plan.varianceDays > 0) "+" else "") + plan.varianceDays + " d", Modifier.weight(1f))
    }
    Text(buildString {
        append("Laid on ${JobPlan.hrs(plan.hpd)} of tool time a day (your day model). ")
        append(if (plan.measured > 0) "Pace ${String.format(Locale.US, "%.2f", plan.pace)} from ${countLabel(plan.measured, "finished task")}, blended with the estimate. "
               else "No finished tasks yet, so the plan runs at the estimate. ")
        append("It starts from ${d(plan.anchor)} and moves as real hours land.")
    }, style = MaterialTheme.typography.bodySmall, color = Board.grey)
    if (!plan.scheduled) Dashed("Not on the calendar yet. Set a start date under Phases and the days fill in; until then the bars show the order and the size of the work.")
    if (plan.unplanned > 0) Text("${countLabel(plan.unplanned, "task")} without hours: tap one to set them, or let Caroline research them in Tonight's office work.", style = MaterialTheme.typography.bodySmall, color = Board.rust)

    // the days: tap one to open it
    if (plan.scheduled) {
        val days = remember(plan.anchor, plan.endIdx) {
            val n = kotlin.math.min(40, kotlin.math.max(10, kotlin.math.ceil(plan.endIdx).toInt() + 1))
            (0 until n).map { plan.idxDate(it.toDouble()) }
        }
        LazyRow(contentPadding = PaddingValues(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(days) { x ->
                val dayHrs = JobPlan.projectedOn(plan, x).hours
                Column(Modifier.clip(RoundedCornerShape(6.dp)).background(if (x == plan.finish) Board.amberBg else Board.tile).clickable { onDay(x) }.padding(horizontal = 8.dp, vertical = 6.dp),
                       horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(x.format(DateTimeFormatter.ofPattern("EEE", Locale.US)), style = MaterialTheme.typography.labelSmall, color = Board.grey)
                    Text(x.dayOfMonth.toString(), style = MaterialTheme.typography.titleSmall, color = Board.ink)
                    Text(if (dayHrs > 0) JobPlan.hrs(dayHrs) else "–", style = MaterialTheme.typography.labelSmall, color = Board.body)
                }
            }
        }
    }

    val lo = minOf(plan.startIdx, 0.0, plan.bars.minOfOrNull { it.b0 } ?: 0.0)
    val hi = maxOf(plan.endIdx, plan.baseEndIdx, lo + 1.0)
    val span = (hi - lo).coerceAtLeast(0.5)
    fun fx(i: Double) = ((i - lo) / span).toFloat().coerceIn(0f, 1f)
    Row(Modifier.fillMaxWidth()) {
        Text(if (plan.started) "started" else d(plan.anchor), style = MaterialTheme.typography.labelSmall, color = Board.grey, modifier = Modifier.weight(1f))
        Text("finish ${plan.finish.format(SHORT)}", style = MaterialTheme.typography.labelSmall, color = Board.grey)
    }
    plan.bars.groupBy { it.task.phaseName }.forEach { (phase, bars) ->
        val left = bars.sumOf { it.remHours }
        Text(phase + (if (left > 0) " · ${JobPlan.hrs(left)} left" else " · done"), style = MaterialTheme.typography.titleSmall, color = Board.ink, modifier = Modifier.padding(top = 8.dp))
        bars.forEach { b ->
            val t = b.task
            val over = t.estHours != null && t.actualHours > t.estHours
            val color = when { t.state == TaskState.DONE -> Board.barDone; over -> Board.red; t.state == TaskState.IN_PROGRESS -> Board.barCalendar; else -> Board.barProjected }
            Column(Modifier.fillMaxWidth().clickable { open = if (open == t.id) null else t.id }.padding(vertical = 3.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(t.title, style = MaterialTheme.typography.bodySmall, color = if (t.state == TaskState.DONE) Board.grey else Board.ink, modifier = Modifier.weight(1f), maxLines = 1)
                    Text(listOfNotNull(JobPlan.hrs(t.estHours), if (t.actualHours > 0) "${JobPlan.hrs(t.actualHours)} real" else null).joinToString(" · "),
                         style = MaterialTheme.typography.labelSmall, color = if (over) Board.red else Board.grey)
                }
                Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(4.dp)).background(Board.track)) {
                    if (t.estHours != null) SpanBar(fx(b.b0), fx(b.b1), Board.toolBorder, Modifier.fillMaxWidth().height(4.dp).align(Alignment.TopStart))
                    SpanBar(fx(b.s0), fx(b.s1), color, Modifier.fillMaxWidth().height(9.dp).align(Alignment.BottomStart))
                    if (lo < 0 && plan.scheduled) SpanBar(fx(0.0), fx(0.0) + 0.004f, Board.red, Modifier.fillMaxSize())
                }
            }
            if (open == t.id) TaskEditor(room, d, plan, b, writing) { open = null }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp)) {
        Key(Board.toolBorder, "Estimate"); Key(Board.barDone, "Done"); Key(Board.barCalendar, "Under way"); Key(Board.barProjected, "Projected"); Key(Board.red, "Over / today")
    }
}

/** A bar from x0 to x1 (0..1 of the width). */
@Composable
private fun SpanBar(x0: Float, x1: Float, color: Color, modifier: Modifier) {
    val a = x0.coerceIn(0f, 1f)
    val b = x1.coerceIn(a + 0.006f, 1f)
    Row(modifier) {
        if (a > 0f) Spacer(Modifier.weight(a))
        Box(Modifier.weight((b - a).coerceAtLeast(0.006f)).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(color))
        if (b < 1f) Spacer(Modifier.weight(1f - b))
    }
}

@Composable
private fun Key(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.width(14.dp).height(8.dp).clip(RoundedCornerShape(2.dp)).background(color)); Text(text, style = MaterialTheme.typography.labelSmall, color = Board.body)
    }
}

@Composable
private fun TaskEditor(room: RoomViewModel, d: RoomData, plan: Plan, b: Bar, writing: Boolean, onClose: () -> Unit) {
    val t = b.task
    var est by remember(t.id, t.estHours) { mutableStateOf(t.estHours?.let { JobPlan.hrs(it).removeSuffix(" hr") } ?: "") }
    var date by remember(t.id) { mutableStateOf(LocalDate.now().toString()) }
    var hours by remember(t.id) { mutableStateOf("") }
    var note by remember(t.id) { mutableStateOf("") }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Board.tile).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${t.phaseName}: ${t.title}", style = MaterialTheme.typography.titleSmall, color = Board.ink)
        Text(buildString {
            append(if (t.estHours == null) "No estimate yet. " else "Estimate ${JobPlan.hrs(t.estHours)}. ")
            append(if (t.actualHours > 0) "${JobPlan.hrs(t.actualHours)} logged against it. " else "No hours tied to it yet. ")
            if (t.state != TaskState.DONE) append("Projected ${JobPlan.hrs(b.remHours)} left" + (if (plan.scheduled) ", done ${d(plan.endDate(b.s1))}." else "."))
            else append("Finished.")
        }, style = MaterialTheme.typography.bodySmall, color = Board.body)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(est, { est = it }, label = { Text("Estimated hours") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
            Tool("Set hours", !writing && est.toDoubleOrNull() != null && est.toDoubleOrNull() != t.estHours) { room.setItemEstimate(t.id, est) }
        }
        Lbl("Log time on this task")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(date, { date = it }, label = { Text("Day") }, singleLine = true, modifier = Modifier.weight(1.3f))
            OutlinedTextField(hours, { hours = it }, label = { Text("Hours") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
        }
        OutlinedTextField(note, { note = it }, label = { Text("What you did - your words, kept verbatim") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Solid("Log on this task", Board.blue, enabled = !writing && hours.toDoubleOrNull() != null) { room.logHoursOnItem(d.id, t.id, date, hours, note); hours = ""; note = "" }
            if (plan.input.canFinish) {
                if (t.state == TaskState.DONE) Tool("Reopen", !writing) { room.finishItem(t.id, false) }
                else Tool("Mark finished", !writing) { room.finishItem(t.id, true) }
            }
            Tool("Close") { onClose() }
        }
        if (!plan.input.canFinish) Text("Marking a task finished needs one small change on your office (a finished mark on schedule items). Until it lands, the plan counts real hours against each estimate.",
            style = MaterialTheme.typography.bodySmall, color = Board.grey)
    }
}

// ------------------------------------------------------------------ Day

@Composable
private fun DayPanel(plan: Plan, date: LocalDate, onDate: (LocalDate) -> Unit) {
    val board = LocalPlanBoard.current
    val today = LocalDate.now()
    val day = JobPlan.projectedOn(plan, date)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tool("‹ Day before") { var x = date.minusDays(1); var g = 0; while (!plan.rule.isWork(x) && g++ < 8) x = x.minusDays(1); onDate(x) }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Lbl(when { date == today -> "Today"; date.isBefore(today) -> "Past day"; else -> "Projected" })
            Text(d(date), style = MaterialTheme.typography.titleMedium, color = Board.ink)
        }
        Tool("Day after ›") { onDate(plan.rule.nextWork(date.plusDays(1))) }
    }
    var typed by remember(date) { mutableStateOf(date.toString()) }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(typed, { typed = it }, label = { Text("Go to a day (yyyy-mm-dd)") }, singleLine = true, modifier = Modifier.weight(1f))
        Tool("Go", runCatching { LocalDate.parse(typed.trim()) }.isSuccess) { runCatching { LocalDate.parse(typed.trim()) }.getOrNull()?.let(onDate) }
    }
    val name = plan.input.name
    when {
        day.unscheduled -> Dashed("$name isn't on the calendar yet. Set a start date under Phases and its days fill in.")
        day.offDay -> Dashed("Not a work day in your day model.")
        day.rows.isEmpty() -> Dashed("No $name tasks fall on this day in the plan.")
        else -> {
            Lbl("$name · ${JobPlan.hrs(day.hours)} of tool time" + if (day.past) " · laid out from finished work" else "")
            day.rows.forEach { r ->
                val t = r.bar.task
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(when (t.state) { TaskState.DONE -> "Done"; TaskState.IN_PROGRESS -> "Under way"; TaskState.OPEN -> "Projected" },
                         when (t.state) { TaskState.DONE -> Board.barDone; TaskState.IN_PROGRESS -> Board.barCalendar; TaskState.OPEN -> Board.barProjected })
                    Text("${t.phaseName}: ${t.title}", style = MaterialTheme.typography.bodyMedium, color = Board.ink, modifier = Modifier.weight(1f))
                    Text(JobPlan.hrs(r.hours), style = MaterialTheme.typography.labelMedium, color = Board.body)
                }
            }
        }
    }
    val all = board?.plansWith(plan) ?: listOf(plan)
    JobPlan.missedDay(plan, date, all)?.let { MissBox(it) }
    val others = all.filter { it.input.projectId != plan.input.projectId }.mapNotNull { o ->
        JobPlan.projectedOn(o, date).takeIf { !it.offDay && it.rows.isNotEmpty() }
    }
    if (others.isNotEmpty()) {
        Lbl("Also that day", modifier = Modifier.padding(top = 6.dp))
        others.forEach { o ->
            Text("${o.plan.input.name} · ${JobPlan.hrs(o.hours)} projected: " + o.rows.take(3).joinToString("; ") { it.bar.task.title } + (if (o.rows.size > 3) "; more" else ""),
                 style = MaterialTheme.typography.bodySmall, color = Board.body)
        }
        val total = others.sumOf { it.hours } + day.hours
        if (total > plan.hpd + 0.01) Text("That's ${JobPlan.hrs(total)} planned across jobs on one day of ${JobPlan.hrs(plan.hpd)}: the day is over-booked.", style = MaterialTheme.typography.bodySmall, color = Board.red)
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.14f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun MissBox(m: JobPlan.Miss) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(8.dp)).background(Board.amberBg).border(1.dp, Board.amberLine, RoundedCornerShape(8.dp)).padding(12.dp),
           verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Lbl("If this day is missed · ${JobPlan.hrs(m.hours)} of ${m.name} work", Board.rust)
        Text("${m.name} finishes ${d(m.finish.from)} → ${d(m.finish.to)}. All ${JobPlan.hrs(m.leftHours)} left moves back one work day.", style = MaterialTheme.typography.bodySmall, color = Board.ink)
        m.phases.forEach { s -> Text("· ${s.label} ends ${s.from.format(SHORT)} → ${s.to.format(SHORT)}", style = MaterialTheme.typography.bodySmall, color = Board.body) }
        if (m.others.isEmpty()) Text("No other job has work planned on the next work day, so the other jobs hold.", style = MaterialTheme.typography.bodySmall, color = Board.body)
        m.others.forEach { o ->
            Text("${o.name} loses ${d(o.loses)} to the catch-up: finish ${o.finish.from.format(SHORT)} → ${o.finish.to.format(SHORT)}" +
                 (if (o.phases.isNotEmpty()) "; " + o.phases.joinToString("; ") { "${it.label} ${it.from.format(SHORT)} → ${it.to.format(SHORT)}" } else "") + ".",
                 style = MaterialTheme.typography.bodySmall, color = Board.ink)
        }
        Text("You're one person, so making up the day takes the next work day from whatever was planned for it. These are the plan's dates; they firm up as you log real hours.",
             style = MaterialTheme.typography.labelSmall, color = Board.grey)
    }
}

// ------------------------------------------------------------------ Time on site

@Composable
private fun TimeOnSite(room: RoomViewModel, d: RoomData, plan: Plan?, writing: Boolean) {
    val openCard = LocalOpenCard.current
    val focus = LocalLogFocus.current
    val days = d.schedule?.arrOf("days")?.objects().orEmpty()
        .filter { (it.objOf("actual_min")?.entrySet()?.sumOf { e -> runCatching { e.value.asDouble }.getOrDefault(0.0) } ?: 0.0) > 0 }
        .sortedByDescending { it.str("date") ?: "" }
    val totals = d.schedule?.objOf("totals")
    val open = d.clock?.arrOf("open")?.objects()?.firstOrNull { it.num("project_id")?.toInt() == d.id }
    val anyOpen = d.clock?.arrOf("open")?.size() ?: 0
    var adding by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tile("Logged", JobPlan.hrs((totals?.num("logged_min") ?: 0.0) / 60.0), Modifier.weight(1f))
        Tile("GPS on site", JobPlan.hrs((totals?.num("field_clock_min") ?: 0.0) / 60.0), Modifier.weight(1f))
        Tile("Not tied to a task", JobPlan.hrs((totals?.num("unlinked_logged_min") ?: 0.0) / 60.0), Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (open != null) Solid("Clock out", Board.rust, enabled = !writing) { room.clockOut(d.id) }
        else Solid("Clock in here", Board.blue, enabled = !writing && anyOpen == 0) { room.clockIn(d.id) }
        Tool(if (adding) "Close" else "Add time by hand") { adding = !adding }
    }
    if (anyOpen > 0 && open == null) Text("A shift is open on another job - clock out there first.", style = MaterialTheme.typography.bodySmall, color = Board.red)
    if (adding) LogForm(room, d, writing) { adding = false }
    Text("Your phone's GPS fills in time on site as site visits; manual entry is the backup. Each day opens its daily log.", style = MaterialTheme.typography.bodySmall, color = Board.grey)
    if (days.isEmpty()) Dashed("No time on site recorded on this job yet.")
    days.take(60).forEach { x ->
        val a = x.objOf("actual_min")
        val date = x.str("date") ?: ""
        val parts = listOfNotNull(
            a?.num("logged")?.takeIf { it > 0 }?.let { "logged ${JobPlan.hrs(it / 60.0)}" },
            a?.num("field_clock")?.takeIf { it > 0 }?.let { "GPS ${JobPlan.hrs(it / 60.0)}" },
            a?.num("calendar")?.takeIf { it > 0 }?.let { "calendar ${JobPlan.hrs(it / 60.0)}" },
            x.num("tool_min")?.takeIf { it > 0 }?.let { "planned ${JobPlan.hrs(it / 60.0)}" } ?: if (x.boolOf("unplanned") == true) "nothing planned" else null)
        Divider(color = Board.borderSoft)
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(runCatching { d(LocalDate.parse(date.take(10))) }.getOrDefault(date), style = MaterialTheme.typography.labelMedium, color = Board.ink, modifier = Modifier.width(92.dp))
            Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = if (x.boolOf("over") == true) Board.red else Board.body, modifier = Modifier.weight(1f))
            Tool("Daily log") { focus.value = date.take(10); openCard(RoomCard.Logs) }
        }
    }
    // hours rows: say which task each was for, so its real hours count on the plan
    val rows = d.hours?.rowsOf("entries")?.objects().orEmpty()
    val tasks = plan?.bars?.map { it.task }.orEmpty()
    if (rows.isNotEmpty() && tasks.isNotEmpty()) {
        Lbl("Tie hours to a task", modifier = Modifier.padding(top = 8.dp))
        rows.take(30).forEach { h ->
            val hid = h.num("id")?.toLong()
            var menu by remember(h) { mutableStateOf(false) }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${h.str("work_date")?.take(10) ?: ""} · ${h.num("hours")?.let { JobPlan.hrs(it) } ?: "hours?"} · ${h.str("note")?.take(60) ?: h.str("category") ?: ""}",
                     style = MaterialTheme.typography.bodySmall, color = Board.body, modifier = Modifier.weight(1f))
                Box {
                    Tool("Task…", !writing && hid != null) { menu = true }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        tasks.forEach { t -> DropdownMenuItem(text = { Text("${t.phaseName}: ${t.title}") }, onClick = { menu = false; if (hid != null) room.linkHours(hid, t.id) }) }
                        DropdownMenuItem(text = { Text("No task") }, onClick = { menu = false; if (hid != null) room.linkHours(hid, null) })
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Tonight's office work

@Composable
internal fun TonightPage(board: PlanBoardViewModel, onOpenJob: (Int) -> Unit) {
    val context = LocalContext.current
    val st by board.state.collectAsState()
    val handled by board.handled.collectAsState()
    val prep by board.prep.collectAsState()
    val auto by board.autoPrep.collectAsState()
    val note by board.note.collectAsState()
    val items = remember(st, handled) { board.items() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BoardCard(strong = Board.blue) {
            Disp("Tonight's office work", 24, Board.blue)
            Text("Your at-home pre-con across every job: estimates to write, scopes to plan, and re-planning live jobs from today's real hours. Caroline does the legwork first so you start ahead.",
                 style = MaterialTheme.typography.bodySmall, color = Board.body)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(checked = auto, onCheckedChange = { board.setAutoPrep(it) })
                Text(if (auto) "Caroline starts the prep on her own when the app opens" else "Caroline starts the prep when you tap", style = MaterialTheme.typography.bodySmall, color = Board.ink, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Solid("Start all of Caroline's prep", Board.blue, enabled = items.any { it.key !in prep }) { board.startAll(items) }
                Tool("Read again") { board.load(context, force = true) }
            }
            note?.let { n -> Row(verticalAlignment = Alignment.CenterVertically) { Text(n, style = MaterialTheme.typography.bodySmall, color = Board.blue, modifier = Modifier.weight(1f)); TextButton(onClick = { board.clearNote() }) { Text("ok") } } }
        }
        if (st?.loading == true && st?.data == null) LoadingLine("Reading every job…")
        st?.error?.let { ErrorLine(it) { board.load(context, force = true) } }
        if (st?.data != null && items.isEmpty()) Dashed("Nothing waiting tonight. Every estimate is out, every task has hours, and no live job needs a re-plan.")
        TonightGroup.values().forEach { g ->
            val gi = items.filter { it.group == g }
            if (gi.isNotEmpty()) {
                Lbl("${g.title} · ${gi.size}")
                gi.forEach { item -> TonightCard(item, prep[item.key], onPrep = { board.startPrep(item) }, onOpen = { onOpenJob(item.projectId) }, onHandled = { board.markHandled(item.key) }) }
            }
        }
        val problems = st?.data?.jobs?.filter { it.plan == null && it.problem != null }.orEmpty()
        if (problems.isNotEmpty()) {
            Lbl("Live jobs without a plan")
            problems.forEach { r -> Text("${r.project.str("name") ?: "Job"}: ${r.problem}", style = MaterialTheme.typography.bodySmall, color = Board.body) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TonightCard(item: TonightItem, sentAt: String?, onPrep: () -> Unit, onOpen: () -> Unit, onHandled: () -> Unit) {
    BoardCard {
        Text(item.title, style = MaterialTheme.typography.titleMedium, color = Board.ink)
        Lbl(item.jobName)
        Text(item.detail, style = MaterialTheme.typography.bodySmall, color = Board.body)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Board.tile).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Lbl(if (sentAt != null) "Caroline's prep · sent $sentAt" else "Caroline's prep", if (sentAt != null) Board.blue else Board.grey)
            item.prep.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(if (!s.caroline) "Needs you" else if (sentAt != null) "Caroline" else "Not started", if (!s.caroline) Board.rust else if (sentAt != null) Board.blue else Board.grey)
                    Text(s.words, style = MaterialTheme.typography.bodySmall, color = Board.ink, modifier = Modifier.weight(1f))
                }
            }
            if (sentAt != null) Text("Her work lands on the job and in her thread.", style = MaterialTheme.typography.labelSmall, color = Board.grey)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (sentAt == null) Solid("Start Caroline's prep", Board.blue, onClick = onPrep)
            Tool("Open the job") { onOpen() }
            Tool("Handled tonight") { onHandled() }
        }
    }
}
