package com.constructionmanager.ui.screens.hocc

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/*
 * THE PLAN ENGINE (HOCC job room, Nov 7 2026) - the task-level Gantt that reflows as work finishes ahead or behind.
 *
 * Tyler, Nov 7 2026 (his words, on the job room prototype): "a gantt chart overview of the project with task level detail that
 * auto adjusts as tasks are completed either ahead or behind of estimated production time"; "I should have to be able to select
 * day on the schedule and it show me what the projected tasks are for that day"; "if that days milestone is missed and how it
 * affects the schedule moving forward across all project not just the one".
 *
 * What it reads is the office's own schedule (GET hub/project/{id}/schedule - schedule.read on the box): the job's phases and
 * items with their estimated minutes, the minutes logged against each item (sched_time_links), the job's start date and the
 * business's day model (tool minutes per day, the work weekdays). Nothing here is a constant about a job: every number comes from
 * the record, and a missing number shows as missing (an item with no estimate is "unplanned"), never as a guess.
 *
 * The box lays the BASELINE (generated blocks from the start date at full estimate). This engine is the PROJECTION on top of it:
 *  - finished items count what they really took; an item with time on it is "in progress" and keeps (estimate x pace - actual);
 *  - pace is learned from finished items, blended with 8 hours of the estimate so one task can't swing the whole job;
 *  - the projection starts at the next work day from today (or the start date, if that is later); work already done is laid
 *    BEFORE that point, so a job running long shows its finish moving later and a job running ahead shows it moving earlier.
 * One man crew: a missed day pushes that job's remaining work back a work day, and making it up takes the next work day from
 * whatever other job had it - the knock-on is computed across every job, never just the one open on the screen.
 *
 * Pure Kotlin (java.time + Gson only) so the JVM unit tests run it exactly as the phone does.
 */

/** The business's day model, as the office holds it: tool hours per day and the work weekdays (0 = Monday ... 6 = Sunday). */
data class DayRule(val hoursPerDay: Double, val workWeekdays: Set<Int>) {
    fun isWork(d: LocalDate): Boolean = (d.dayOfWeek.value - 1) in workWeekdays
    fun nextWork(d: LocalDate): LocalDate {
        var x = d
        repeat(8) { if (isWork(x)) return x; x = x.plusDays(1) }
        return d   // no work day in the week: the caller already refused this rule (valid() is false)
    }
    fun valid(): Boolean = hoursPerDay > 0 && workWeekdays.isNotEmpty()
}

enum class TaskState { OPEN, IN_PROGRESS, DONE }

/** One schedule item of a job. estHours null = no estimate on the record yet. */
data class PlanTask(
    val id: Long,
    val phaseId: Long?,
    val phaseName: String,
    val title: String,
    val estHours: Double?,
    val actualHours: Double,
    val finished: Boolean
) {
    val state: TaskState get() = when { finished -> TaskState.DONE; actualHours > 0.0 -> TaskState.IN_PROGRESS; else -> TaskState.OPEN }
}

data class PlanInput(
    val projectId: Int,
    val name: String,
    val startDate: LocalDate?,
    val rule: DayRule,
    val tasks: List<PlanTask>,
    /** true when the office can record "finished" on an item (its read carries the field); false = the door isn't built yet. */
    val canFinish: Boolean = false,
    val usePace: Boolean = true
)

/** A task laid on the timeline. Positions are in WORK DAYS from the anchor (0 = the anchor day's start; negative = before it).
 *  b0..b1 = the estimate laid end to end (baseline); s0..s1 = the projection; remHours = what is left of it. */
data class Bar(val task: PlanTask, val b0: Double, val b1: Double, val s0: Double, val s1: Double, val remHours: Double)

data class Plan(
    val input: PlanInput,
    val bars: List<Bar>,
    val anchor: LocalDate,
    val startIdx: Double,
    val endIdx: Double,
    val baseEndIdx: Double,
    val pace: Double,
    val measured: Int,
    val unplanned: Int,
    val leftHours: Double,
    val started: Boolean,
    /** false = a lead not started and with no start date: nothing of it belongs on a calendar yet. */
    val scheduled: Boolean
) {
    val rule get() = input.rule
    val hpd get() = input.rule.hoursPerDay
    /** Work days later (+) or earlier (-) than the estimate laid end to end, to a tenth. */
    val varianceDays: Double get() = round((endIdx - baseEndIdx) * 10) / 10.0

    fun idxDate(idx: Double): LocalDate {
        var d = anchor
        val n = floor(idx + 1e-6).toInt()
        if (n >= 0) repeat(n) { d = rule.nextWork(d.plusDays(1)) }
        else repeat(-n) { d = d.minusDays(1); var g = 0; while (!rule.isWork(d) && g++ < 8) d = d.minusDays(1) }
        return d
    }

    /** The work-day index of a date, or null when the date is not a work day in this plan. */
    fun dateIdx(date: LocalDate): Int? {
        if (!rule.isWork(date)) return null
        if (date == anchor) return 0
        var i = 0
        var d = anchor
        if (date.isAfter(anchor)) { while (d.isBefore(date) && i < 4000) { d = rule.nextWork(d.plusDays(1)); i++ } }
        else { while (d.isAfter(date) && i > -4000) { d = d.minusDays(1); var g = 0; while (!rule.isWork(d) && g++ < 8) d = d.minusDays(1); i-- } }
        return if (d == date) i else null
    }

    /** The last work day a span ending at idx touches. */
    fun endDate(idx: Double): LocalDate = idxDate(max(ceil(idx - 1e-6) - 1, floor(startIdx)))

    val finish: LocalDate get() = endDate(endIdx)
    val baselineFinish: LocalDate get() = endDate(baseEndIdx)

    /** The finish of each phase that still has work left, in phase order: (phase name, projected last day, end index). */
    fun openPhaseEnds(): List<Triple<String, LocalDate, Double>> =
        bars.groupBy { it.task.phaseName }.mapNotNull { (name, bs) ->
            val open = bs.filter { it.task.state != TaskState.DONE }
            if (open.isEmpty()) null else { val e = open.maxOf { it.s1 }; Triple(name, endDate(e), e) }
        }
}

object JobPlan {

    /** Lay out a job. Same input, same output. */
    fun compute(input: PlanInput, today: LocalDate): Plan {
        val rule = input.rule
        val hpd = rule.hoursPerDay
        val tasks = input.tasks
        val started = tasks.any { it.state != TaskState.OPEN }
        val base0 = if (input.startDate != null && input.startDate.isAfter(today)) input.startDate else today
        val anchor = rule.nextWork(base0)
        val measured = tasks.filter { it.state == TaskState.DONE && it.actualHours > 0 && (it.estHours ?: 0.0) > 0 }
        val sumA = measured.sumOf { it.actualHours }
        val sumE = measured.sumOf { it.estHours ?: 0.0 }
        val pace = if (input.usePace && measured.isNotEmpty()) (sumA + 8.0) / (sumE + 8.0) else 1.0
        var spent = 0.0
        tasks.forEach { t ->
            when (t.state) {
                TaskState.DONE -> spent += if (t.actualHours > 0) t.actualHours else (t.estHours ?: 0.0)
                TaskState.IN_PROGRESS -> spent += t.actualHours
                TaskState.OPEN -> {}
            }
        }
        val startIdx = if (started) -spent / hpd else 0.0
        var cur = startIdx
        var base = startIdx
        var now = 0.0
        var unplanned = 0
        var left = 0.0
        val bars = tasks.map { t ->
            val e = t.estHours ?: 0.0
            if (t.estHours == null && t.state != TaskState.DONE) unplanned++
            val b0 = base; base += e / hpd; val b1 = base
            if (t.state == TaskState.DONE) {
                val s0 = cur; cur += (if (t.actualHours > 0) t.actualHours else e) / hpd
                Bar(t, b0, b1, s0, cur, 0.0)
            } else {
                // in progress: what's left of the paced estimate. Done counting it on a task already past its estimate keeps
                // the task open but adds nothing more (it shows red: over). Open: the paced estimate.
                val r = if (t.state == TaskState.IN_PROGRESS) max(e * pace - t.actualHours, 0.0) else e * pace
                left += r
                val s0 = now; now += r / hpd
                Bar(t, b0, b1, s0, now, r)
            }
        }
        val endIdx = max(now, cur)
        return Plan(input, bars, anchor, startIdx, endIdx, base, pace, measured.size, unplanned, left, started,
                    scheduled = started || input.startDate != null)
    }

    // ------------------------------------------------------------------ one day

    data class DayRow(val bar: Bar, val hours: Double)
    data class Day(val plan: Plan, val date: LocalDate, val rows: List<DayRow>, val offDay: Boolean, val unscheduled: Boolean, val past: Boolean) {
        val hours: Double get() = rows.sumOf { it.hours }
    }

    /** What the plan puts on one date: each task's share of that work day, to the quarter hour. */
    fun projectedOn(plan: Plan, date: LocalDate): Day {
        if (!plan.scheduled) return Day(plan, date, emptyList(), offDay = true, unscheduled = true, past = false)
        val i = plan.dateIdx(date) ?: return Day(plan, date, emptyList(), offDay = true, unscheduled = false, past = false)
        val rows = plan.bars.mapNotNull { b ->
            val ov = min(b.s1, i + 1.0) - max(b.s0, i.toDouble())
            if (ov > 0.001) DayRow(b, round(ov * plan.hpd * 4) / 4.0) else null
        }
        return Day(plan, date, rows, offDay = false, unscheduled = false, past = i < 0)
    }

    // ------------------------------------------------------------------ a missed day, across every job

    data class Shift(val label: String, val from: LocalDate, val to: LocalDate)
    data class OtherJob(val projectId: Int, val name: String, val loses: LocalDate, val finish: Shift, val phases: List<Shift>)
    data class Miss(val projectId: Int, val name: String, val date: LocalDate, val hours: Double, val finish: Shift,
                    val phases: List<Shift>, val others: List<OtherJob>, val leftHours: Double)

    /**
     * If the work planned on [date] for [plan] doesn't happen: that job's remaining work moves back one work day; its open phases
     * that end after the day move with it; and since he is one man, making up the day takes the next work day from every OTHER
     * scheduled job that had work planned on it - each of those finishes a work day later too. Null when nothing of this job
     * falls on that day (nothing to miss) or the day is already past.
     */
    fun missedDay(plan: Plan, date: LocalDate, all: List<Plan>): Miss? {
        val d = projectedOn(plan, date)
        if (d.offDay || d.rows.isEmpty() || d.past) return null
        val i = plan.dateIdx(date) ?: return null
        val finish = Shift("Finish", plan.finish, plan.endDate(plan.endIdx + 1))
        val phases = plan.openPhaseEnds().filter { it.third > i }.map { (n, at, e) -> Shift(n, at, plan.endDate(e + 1)) }
        val others = all.filter { it.input.projectId != plan.input.projectId && it.scheduled && it.leftHours > 0 }.mapNotNull { o ->
            var nd = date.plusDays(1)
            var k = 0
            while (!o.rule.isWork(nd) && k++ < 7) nd = nd.plusDays(1)
            val od = projectedOn(o, nd)
            if (od.offDay || od.rows.isEmpty()) null
            else {
                val oi = o.dateIdx(nd)
                OtherJob(o.input.projectId, o.input.name, nd, Shift("Finish", o.finish, o.endDate(o.endIdx + 1)),
                         o.openPhaseEnds().filter { oi != null && it.third > oi }.map { (n, at, e) -> Shift(n, at, o.endDate(e + 1)) })
            }
        }
        return Miss(plan.input.projectId, plan.input.name, date, d.hours, finish, phases, others, plan.leftHours)
    }

    // ------------------------------------------------------------------ reading the office's schedule

    /** Why a job has no plan, in words the screen can show. */
    data class Read(val input: PlanInput?, val problem: String?)

    /**
     * The office's schedule read (hub/project/{id}/schedule) -> the engine's input. Live phases and items only (removed and
     * superseded ones are history, not plan); items in phase order then their own order; minutes -> hours.
     */
    fun fromSchedule(projectId: Int, fallbackName: String, s: JsonObject?): Read {
        if (s == null) return Read(null, "The office's schedule for this job didn't come back.")
        val dm = obj(s, "day_model")
        val v = dm?.let { obj(it, "value") }
        val rule = v?.let {
            val tool = n(it, "tool_min")
            val days = arr(it, "work_weekdays")?.mapNotNull { e -> runCatching { e.asInt }.getOrNull() }?.toSet()
            if (tool != null && days != null) DayRule(tool / 60.0, days) else null
        }
        if (rule == null || !rule.valid()) return Read(null, dm?.let { st(it, "problem") } ?: "The business day model isn't set on the office yet, so the plan has no hours per day to lay work on.")
        val proj = obj(s, "project")
        val name = proj?.let { st(it, "name") } ?: fallbackName
        val start = proj?.let { st(it, "start_date") }?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val phases = (arr(s, "phases") ?: JsonArray()).mapNotNull { it.takeIf { e -> e.isJsonObject }?.asJsonObject }
            .filter { st(it, "state") != "removed" && isNull(it, "removed_at") }
            .sortedWith(compareBy({ n(it, "seq") ?: 0.0 }, { n(it, "id") ?: 0.0 }))
        val items = (arr(s, "items") ?: JsonArray()).mapNotNull { it.takeIf { e -> e.isJsonObject }?.asJsonObject }
            .filter { (st(it, "state") ?: "live") == "live" && isNull(it, "removed_at") && isNull(it, "superseded_at") }
        val canFinish = items.any { it.has("finished_at") }
        val order = phases.mapIndexed { i, p -> (n(p, "id")?.toLong() ?: -1L) to i }.toMap()
        val phaseName = phases.associate { (n(it, "id")?.toLong() ?: -1L) to (st(it, "name") ?: "Phase") }
        val tasks = items.sortedWith(compareBy({ order[n(it, "phase_id")?.toLong()] ?: Int.MAX_VALUE }, { n(it, "seq") ?: 0.0 }, { n(it, "id") ?: 0.0 }))
            .filter { val pid = n(it, "phase_id")?.toLong(); pid == null || pid in order }
            .map { i ->
                val pid = n(i, "phase_id")?.toLong()
                PlanTask(
                    id = n(i, "id")?.toLong() ?: 0L,
                    phaseId = pid,
                    phaseName = pid?.let { phaseName[it] } ?: "No phase",
                    title = st(i, "title", "line_text") ?: "Untitled item",
                    estHours = n(i, "est_minutes")?.let { it / 60.0 },
                    actualHours = (n(i, "actual_min") ?: 0.0) / 60.0,
                    finished = !isNull(i, "finished_at")
                )
            }
        if (tasks.isEmpty()) return Read(null, "No tasks laid out on this job yet. Lay the schedule out from the scope or the estimate.")
        return Read(PlanInput(projectId, name, start, rule, tasks, canFinish), null)
    }

    // tolerant readers, the same reading as HoccCommon's (kept here so the engine and its tests need no Compose)
    private fun obj(o: JsonObject, k: String): JsonObject? = o.get(k)?.takeIf { it.isJsonObject }?.asJsonObject
    private fun arr(o: JsonObject, k: String): JsonArray? = o.get(k)?.takeIf { it.isJsonArray }?.asJsonArray
    private fun n(o: JsonObject, k: String): Double? = o.get(k)?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asDouble }.getOrNull() }
    private fun st(o: JsonObject, vararg ks: String): String? {
        for (k in ks) { val e: JsonElement = o.get(k) ?: continue; if (e.isJsonPrimitive) { val s = runCatching { e.asString }.getOrNull(); if (!s.isNullOrBlank()) return s } }
        return null
    }
    private fun isNull(o: JsonObject, k: String): Boolean = o.get(k).let { it == null || it.isJsonNull || (it.isJsonPrimitive && runCatching { it.asString }.getOrNull().isNullOrBlank()) }

    // ------------------------------------------------------------------ words

    fun hrs(h: Double?): String = if (h == null) "no estimate" else { val r = round(h * 100) / 100.0; if (r == floor(r)) "${r.toLong()} hr" else "$r hr" }

    fun varianceWords(p: Plan): String {
        val v = p.varianceDays
        return when { abs(v) < 0.25 -> "on the estimate"; v > 0 -> "${fmt1(v)} work days behind the estimate"; else -> "${fmt1(-v)} work days ahead of the estimate" }
    }

    private fun fmt1(x: Double): String { val r = round(x * 10) / 10.0; return if (r == floor(r)) r.toLong().toString() else r.toString() }
}
