package com.constructionmanager.domain.logic

import com.constructionmanager.domain.model.ConstructionPhase
import com.constructionmanager.domain.model.Project
import com.constructionmanager.domain.model.ProjectStatus
import com.constructionmanager.domain.model.ProjectType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/** Audit B16: the old millisecond arithmetic overflowed Int for any date after early 1970. */
class ScheduleTest {
    private fun project(end: LocalDate) = Project(
        id = "p", name = "Maple Kitchen", address = "", city = "", state = "", zipCode = "",
        clientName = "", clientEmail = "", clientPhone = "", projectType = ProjectType.RENOVATION,
        currentPhase = ConstructionPhase.PRE_CONSTRUCTION, startDate = LocalDate(2026, 9, 1), estimatedEndDate = end,
        totalBudget = BigDecimal.ZERO, status = ProjectStatus.ACTIVE,
        createdAt = LocalDateTime(2026, 9, 1, 8, 0), updatedAt = LocalDateTime(2026, 9, 1, 8, 0)
    )

    private val today = LocalDate(2026, 10, 8)

    @Test
    fun `a 2026 end date after today is on schedule`() {
        assertTrue(Schedule.isOnSchedule(project(LocalDate(2026, 12, 1)), today))
        assertTrue(Schedule.isOnSchedule(project(today), today))
    }

    @Test
    fun `a past end date is not`() {
        assertFalse(Schedule.isOnSchedule(project(LocalDate(2026, 10, 7)), today))
    }

    @Test
    fun `the share counts dates, and no projects means no number`() {
        assertEquals(0.5, Schedule.onScheduleShare(listOf(project(LocalDate(2027, 1, 1)), project(LocalDate(2026, 1, 1))), today)!!, 1e-9)
        assertNull(Schedule.onScheduleShare(emptyList(), today))
    }
}
