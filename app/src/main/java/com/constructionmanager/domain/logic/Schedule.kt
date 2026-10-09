package com.constructionmanager.domain.logic

import com.constructionmanager.domain.model.Project
import kotlinx.datetime.LocalDate

/**
 * "On schedule" as the screens show it: an active project whose estimated end date is today or later.
 *
 * Audit B16: the Dashboard compared now-in-milliseconds with `toEpochDays() * 24 * 60 * 60 * 1000` in Int arithmetic,
 * which overflows for any date after early 1970, so the percentage was garbage. Dates are compared as dates here.
 * The Reports screen used to count every active or completed project as "on schedule"; it uses this too now.
 */
object Schedule {
    fun isOnSchedule(project: Project, today: LocalDate): Boolean = project.estimatedEndDate >= today

    /** Share of [projects] on schedule, 0.0..1.0; null when there are none (the screen shows "—", not 0%). */
    fun onScheduleShare(projects: List<Project>, today: LocalDate): Double? =
        if (projects.isEmpty()) null else projects.count { isOnSchedule(it, today) }.toDouble() / projects.size
}
