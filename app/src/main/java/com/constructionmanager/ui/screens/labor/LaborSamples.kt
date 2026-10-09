package com.constructionmanager.ui.screens.labor

import com.constructionmanager.domain.model.LaborEntry
import com.constructionmanager.domain.model.Worker

/**
 * The Labor screen's built-in example data, labelled as such (audit B7; Tyler's Oct 8 option "label them as samples").
 * The totals, the costs and rates by trade, and the example workers and entries are not anyone's records. They stay on
 * the screen so its layout can be seen, and every one of them says "sample". Real (cloud) workers and entries are never
 * marked, and tracked time is never billed to a sample worker.
 */
object LaborSamples {
    const val MARK = " (sample)"
    const val NOTICE = "Sample figures: the totals, costs and rates here, and every worker or entry marked (sample), " +
        "are examples - not your records."

    fun isSample(w: Worker): Boolean = w.lastName.endsWith(MARK) || w.firstName.endsWith(MARK)

    fun label(workers: List<Worker>): List<Worker> = workers.map { if (isSample(it)) it else it.copy(lastName = it.lastName + MARK) }

    fun labelEntries(entries: List<LaborEntry>): List<LaborEntry> =
        entries.map { if (it.workerName.endsWith(MARK)) it else it.copy(workerName = it.workerName + MARK) }

    /** Who tracked time is billed to: the chosen worker, else the first real one, else nobody (never a sample). */
    fun workerForTrackedTime(workers: List<Worker>, selectedId: String?): Worker? {
        val chosen = workers.firstOrNull { it.id == selectedId }
        if (chosen != null && !isSample(chosen)) return chosen
        return workers.firstOrNull { !isSample(it) }
    }
}
