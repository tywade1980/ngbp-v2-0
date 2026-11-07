package com.constructionmanager.ui.screens.hocc

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** The job room's plan engine: the Gantt that reflows as tasks finish ahead or behind, the day view and the missed-day knock-on. */
class JobPlanTest {
    // Saturday Nov 7 2026; the next work day is Monday Nov 9.
    private val today = LocalDate.of(2026, 11, 7)
    private val weekdays = DayRule(6.5, setOf(0, 1, 2, 3, 4))

    private fun t(id: Long, est: Double?, act: Double = 0.0, done: Boolean = false, phase: String = "Framing") =
        PlanTask(id, 1L, phase, "Task $id", est, act, done)

    private fun plan(tasks: List<PlanTask>, start: LocalDate? = null, id: Int = 1, name: String = "Job $id") =
        JobPlan.compute(PlanInput(id, name, start, weekdays, tasks), today)

    @Test
    fun `an untouched job finishes on its estimate`() {
        val p = plan(listOf(t(1, 6.5), t(2, 6.5), t(3, 6.5)), start = LocalDate.of(2026, 11, 9))
        assertEquals(LocalDate.of(2026, 11, 9), p.anchor)
        assertEquals(LocalDate.of(2026, 11, 11), p.finish)
        assertEquals(p.baselineFinish, p.finish)
        assertEquals(0.0, p.varianceDays, 1e-9)
        assertTrue(p.scheduled)
        assertEquals("on the estimate", JobPlan.varianceWords(p))
    }

    @Test
    fun `a lead with no start and no time on it is not on the calendar`() {
        val p = plan(listOf(t(1, 4.0)))
        assertFalse(p.scheduled)
        val day = JobPlan.projectedOn(p, LocalDate.of(2026, 11, 9))
        assertTrue(day.unscheduled)
        assertTrue(day.rows.isEmpty())
    }

    @Test
    fun `finishing a task ahead pulls the finish in`() {
        // 8 hr task finished in 4: pace (4+8)/(8+8) = 0.75 on what is left
        val p = plan(listOf(t(1, 8.0, act = 4.0, done = true), t(2, 13.0), t(3, 13.0)))
        assertEquals(0.75, p.pace, 1e-9)
        assertEquals(26.0 * 0.75, p.leftHours, 1e-9)
        assertTrue("finish moves earlier than the estimate", p.varianceDays < 0)
        assertTrue(JobPlan.varianceWords(p).contains("ahead"))
    }

    @Test
    fun `running long pushes the finish out`() {
        // 8 hr task took 12: pace (12+8)/(8+8) = 1.25
        val p = plan(listOf(t(1, 8.0, act = 12.0, done = true), t(2, 13.0)))
        assertEquals(1.25, p.pace, 1e-9)
        assertTrue(p.varianceDays > 0)
        assertTrue(JobPlan.varianceWords(p).contains("behind"))
    }

    @Test
    fun `one finished task cannot swing the whole job`() {
        // a 1 hr task that took 3 would be pace 3.0 raw; blended with 8 hr of estimate it is (3+8)/(1+8)
        val p = plan(listOf(t(1, 1.0, act = 3.0, done = true), t(2, 40.0)))
        assertEquals(11.0 / 9.0, p.pace, 1e-9)
    }

    @Test
    fun `an item over its estimate stays open and adds nothing more`() {
        val p = plan(listOf(t(1, 4.0, act = 5.0), t(2, 6.5)))
        val first = p.bars[0]
        assertEquals(TaskState.IN_PROGRESS, first.task.state)
        assertEquals(0.0, first.remHours, 1e-9)
        assertEquals(6.5, p.leftHours, 1e-9)
        assertTrue(p.started)
        assertTrue(p.scheduled)
    }

    @Test
    fun `a task with no estimate counts as unplanned`() {
        val p = plan(listOf(t(1, null), t(2, 3.0), t(3, null, done = true)))
        assertEquals(1, p.unplanned)
    }

    @Test
    fun `work days skip the weekend both ways`() {
        val p = plan(listOf(t(1, 6.5), t(2, 6.5)), start = LocalDate.of(2026, 11, 13)) // a Friday
        assertEquals(LocalDate.of(2026, 11, 13), p.anchor)
        assertEquals(LocalDate.of(2026, 11, 16), p.finish) // Monday
        assertEquals(1, p.dateIdx(LocalDate.of(2026, 11, 16)))
        assertNull(p.dateIdx(LocalDate.of(2026, 11, 14)))
        assertEquals(LocalDate.of(2026, 11, 12), p.idxDate(-1.0))
    }

    @Test
    fun `the day view splits each day by task to the quarter hour`() {
        val p = plan(listOf(t(1, 4.0), t(2, 5.0), t(3, 6.5)), start = LocalDate.of(2026, 11, 9))
        val mon = JobPlan.projectedOn(p, LocalDate.of(2026, 11, 9))
        assertEquals(listOf(1L, 2L), mon.rows.map { it.bar.task.id })
        assertEquals(4.0, mon.rows[0].hours, 1e-9)
        assertEquals(2.5, mon.rows[1].hours, 1e-9)
        assertEquals(6.5, mon.hours, 1e-9)
        val sat = JobPlan.projectedOn(p, LocalDate.of(2026, 11, 14))
        assertTrue(sat.offDay)
    }

    @Test
    fun `a missed day moves this job and takes the next day from every other job`() {
        val a = plan(listOf(t(1, 13.0), t(2, 13.0, phase = "Drywall")), start = LocalDate.of(2026, 11, 9), id = 1, name = "Ricordati")
        val b = plan(listOf(t(10, 19.5)), start = LocalDate.of(2026, 11, 9), id = 2, name = "Humphrey")
        val c = plan(listOf(t(20, 3.0)), start = LocalDate.of(2026, 12, 1), id = 3, name = "Later job")
        val miss = JobPlan.missedDay(a, LocalDate.of(2026, 11, 9), listOf(a, b, c))
        assertNotNull(miss)
        miss!!
        assertEquals(6.5, miss.hours, 1e-9)
        assertEquals(LocalDate.of(2026, 11, 12), miss.finish.from)
        assertEquals(LocalDate.of(2026, 11, 13), miss.finish.to)
        assertEquals(listOf("Framing", "Drywall"), miss.phases.map { it.label })
        // Humphrey had Tuesday planned: it loses Tuesday and finishes a work day later. The later job had nothing that day.
        assertEquals(listOf(2), miss.others.map { it.projectId })
        val h = miss.others.single()
        assertEquals(LocalDate.of(2026, 11, 10), h.loses)
        assertEquals(LocalDate.of(2026, 11, 11), h.finish.from)
        assertEquals(LocalDate.of(2026, 11, 12), h.finish.to)
    }

    @Test
    fun `nothing to miss on a day with no work for the job`() {
        val a = plan(listOf(t(1, 6.5)), start = LocalDate.of(2026, 11, 9))
        assertNull(JobPlan.missedDay(a, LocalDate.of(2026, 11, 20), listOf(a)))
    }

    @Test
    fun `the office schedule read becomes the plan input`() {
        val s = JsonParser.parseString("""
            {"project": {"id": 7, "name": "Ricordati basement", "start_date": "2026-10-12"},
             "day_model": {"value": {"tool_min": 390, "work_weekdays": [0,1,2,3,4]}},
             "phases": [{"id": 2, "name": "Drywall", "seq": 2, "state": "live"},
                        {"id": 1, "name": "Framing", "seq": 1, "state": "live"},
                        {"id": 3, "name": "Old", "seq": 3, "state": "removed", "removed_at": "2026-10-01"}],
             "items": [{"id": 21, "phase_id": 2, "seq": 0, "title": "Hang", "est_minutes": 780, "actual_min": 0, "state": "live"},
                       {"id": 11, "phase_id": 1, "seq": 1, "title": "Walls", "est_minutes": 600, "actual_min": 660, "state": "live"},
                       {"id": 10, "phase_id": 1, "seq": 0, "title": "Plates", "est_minutes": null, "actual_min": 0, "state": "live"},
                       {"id": 12, "phase_id": 1, "seq": 2, "title": "Gone", "est_minutes": 60, "actual_min": 0, "state": "removed"},
                       {"id": 31, "phase_id": 3, "seq": 0, "title": "In a removed phase", "est_minutes": 60, "actual_min": 0, "state": "live"}]}
        """).asJsonObject
        val r = JobPlan.fromSchedule(7, "Job 7", s)
        assertNull(r.problem)
        val input = r.input!!
        assertEquals("Ricordati basement", input.name)
        assertEquals(LocalDate.of(2026, 10, 12), input.startDate)
        assertEquals(6.5, input.rule.hoursPerDay, 1e-9)
        assertEquals(listOf(10L, 11L, 21L), input.tasks.map { it.id })
        assertNull(input.tasks[0].estHours)
        assertEquals(11.0, input.tasks[1].actualHours, 1e-9)
        assertEquals("Drywall", input.tasks[2].phaseName)
        assertFalse("no finished_at field on the record yet", input.canFinish)
    }

    @Test
    fun `finished on the record counts as done once the office carries it`() {
        val s = JsonParser.parseString("""
            {"project": {"id": 7, "name": "J"}, "day_model": {"value": {"tool_min": 390, "work_weekdays": [0,1,2,3,4]}},
             "phases": [{"id": 1, "name": "Framing", "seq": 1}],
             "items": [{"id": 1, "phase_id": 1, "title": "A", "est_minutes": 480, "actual_min": 240, "finished_at": "2026-11-06T20:00:00Z"},
                       {"id": 2, "phase_id": 1, "title": "B", "est_minutes": 480, "actual_min": 0, "finished_at": null}]}
        """).asJsonObject
        val input = JobPlan.fromSchedule(7, "J", s).input!!
        assertTrue(input.canFinish)
        assertEquals(TaskState.DONE, input.tasks[0].state)
        assertEquals(TaskState.OPEN, input.tasks[1].state)
    }

    @Test
    fun `no day model is said in words, not guessed`() {
        val s = JsonParser.parseString("""{"day_model": {"value": null, "problem": "no day model for this business"}, "items": []}""").asJsonObject
        val r = JobPlan.fromSchedule(1, "J", s)
        assertNull(r.input)
        assertEquals("no day model for this business", r.problem)
    }
}
