package com.constructionmanager.ui.screens.hocc

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Tonight's office work: built from the record across every job, with Caroline's prep and his step kept apart. */
class TonightTest {
    private val today = LocalDate.of(2026, 11, 7)
    private fun o(json: String): JsonObject = JsonParser.parseString(json).asJsonObject
    private val rule = DayRule(6.5, setOf(0, 1, 2, 3, 4))

    private fun plan(id: Int, tasks: List<PlanTask>, start: LocalDate? = null) =
        JobPlan.compute(PlanInput(id, "Job $id", start, rule, tasks), today)

    @Test
    fun `leads with nothing sent become estimates to write, a sent one does not`() {
        val leads = listOf(o("""{"id": 5, "name": "Attic ladder", "client_name": "Pat Doe", "status": "lead"}"""),
                           o("""{"id": 6, "name": "Deck", "client_name": "Sam Roe", "status": "estimating"}"""),
                           o("""{"id": 7, "name": "Sent one", "client_name": "Lee", "status": "estimating"}"""))
        val estimates = listOf(o("""{"id": 1, "project_id": 7, "status": "sent"}"""))
        val appts = listOf(o("""{"project_id": 5, "start_at": "2026-11-09T10:00:00-05:00"}"""))
        val items = Tonight.build(leads, estimates, emptyList(), appts, today, emptySet())
        assertEquals(listOf("est-5", "est-6"), items.map { it.key })
        assertEquals("Write and submit Pat Doe's estimate", items[0].title)
        assertTrue(items[0].detail.contains("2026-11-09 10:00"))
    }

    @Test
    fun `unplanned tasks, a missing start and live jobs with work left each show once`() {
        val p = plan(9, listOf(PlanTask(1, 1, "Framing", "Walls", 6.5, 2.0, false), PlanTask(2, 1, "Framing", "Pocket doors", null, 0.0, false)))
        val job = Tonight.JobPlanRow(o("""{"id": 9, "name": "Basement", "status": "active"}"""), p, null, needsStart = false)
        val signed = Tonight.JobPlanRow(o("""{"id": 10, "name": "Stairs", "status": "approved"}"""), null, "no tasks", needsStart = true)
        val items = Tonight.build(emptyList(), emptyList(), listOf(job, signed), emptyList(), today, emptySet())
        assertEquals(listOf(TonightGroup.Scope, TonightGroup.Scope, TonightGroup.Replan), items.map { it.group })
        assertTrue(items.any { it.title == "Plan hours for 1 Basement task" && it.detail.startsWith("Pocket doors") })
        assertTrue(items.any { it.key == "start-10" })
        val rp = items.single { it.group == TonightGroup.Replan }
        assertEquals("rp-9-2026-11-07", rp.key)
        assertTrue(rp.detail.contains("In progress: Walls"))
    }

    @Test
    fun `an item he marked handled tonight stays off the list`() {
        val leads = listOf(o("""{"id": 5, "name": "A", "client_name": "Pat", "status": "lead"}"""))
        assertTrue(Tonight.build(leads, emptyList(), emptyList(), emptyList(), today, setOf("est-5")).isEmpty())
    }

    @Test
    fun `caroline's prep request is prep only and names his step apart`() {
        val it = TonightItem("est-5", TonightGroup.Estimate, 5, "Attic ladder", "Write and submit Pat's estimate", "Still a lead.")
        val ask = Tonight.prepRequest(it)
        assertTrue(ask.contains("job #5"))
        assertTrue(ask.contains("do not call, text or email anyone"))
        assertFalse("his own step is not handed to her", ask.contains("set the price and send"))
        assertEquals(1, it.prep.count { !it.caroline })
    }
}
