package com.constructionmanager.ui.screens.labor

import com.constructionmanager.domain.model.SkillLevel
import com.constructionmanager.domain.model.TradeType
import com.constructionmanager.domain.model.Worker
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/** Audit B7: example numbers are labelled, and tracked time is never billed to an example worker. */
class LaborSamplesTest {
    private fun w(id: String, last: String) = Worker(
        id = id, firstName = "Pat", lastName = last, email = "", phone = "", tradeTypes = listOf(TradeType.CARPENTER),
        skillLevel = SkillLevel.JOURNEYMAN, certifications = emptyList(), hourlyRate = BigDecimal("40"), hireDate = LocalDate(2026, 1, 1)
    )

    @Test
    fun `examples carry the mark, once`() {
        val labelled = LaborSamples.label(listOf(w("worker_001", "Smith")))
        assertEquals("Smith (sample)", labelled.single().lastName)
        assertEquals(labelled, LaborSamples.label(labelled))
        assertTrue(LaborSamples.NOTICE.contains("not your records"))
    }

    @Test
    fun `tracked time goes to the chosen real worker, else the first real one, else nobody`() {
        val sample = LaborSamples.label(listOf(w("worker_001", "Smith"))).single()
        val real = w("cloud_7", "Wade")
        assertEquals("cloud_7", LaborSamples.workerForTrackedTime(listOf(sample, real), null)?.id)
        assertEquals("cloud_7", LaborSamples.workerForTrackedTime(listOf(sample, real), "worker_001")?.id)
        assertNull(LaborSamples.workerForTrackedTime(listOf(sample), null))
    }
}
