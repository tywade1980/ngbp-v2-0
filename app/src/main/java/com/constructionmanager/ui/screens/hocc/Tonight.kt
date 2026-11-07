package com.constructionmanager.ui.screens.hocc

import com.google.gson.JsonObject
import java.time.LocalDate

/*
 * TONIGHT'S OFFICE WORK (HOCC, Nov 7 2026) - his at-home pre-construction session, across every job and lead.
 *
 * Tyler, Nov 7 2026 (his words): "that nightly planning is for you know when I'm at home doing my office work right I've got
 * estimates to do I've got project Scopes to plan you know those pre-con pre-construction tasks right ... that also translates to
 * ... a daily thing for a job like just because we planned it out on the estimate doesn't mean that that's going to hold true all
 * through the job"; "in all projects all job room all job boards whatever level task not just specifically for that project";
 * "we should do a preliminary automation to start that office work on ... Caroline's side ... so that way the pre-con work can
 * commence ... things that she can do before me without my input"; "Because the ai is my planning partner that saves me thinking time".
 *
 * Three groups, built only from the record (no job is named in this code):
 *  - Estimates to write: leads in lead/estimating with no estimate sent or approved on file;
 *  - Scopes to plan: schedule items with no estimated time; a signed job with tasks but no start date;
 *  - Re-plan live jobs: every active job with work left - tonight's re-plan from today's real hours.
 * Each item carries Caroline's prep: the steps she can do before he sits down, and the one step that is his.
 */

enum class TonightGroup(val title: String) {
    Estimate("Estimates to write and submit"),
    Scope("Scopes and hours to plan"),
    Replan("Re-plan live jobs from today")
}

/** One step of the prep: Caroline's (she can do it before him) or his. */
data class PrepStep(val words: String, val caroline: Boolean)

data class TonightItem(
    /** Stable for the day: what he marks handled and what a prep send is remembered by. */
    val key: String,
    val group: TonightGroup,
    val projectId: Int,
    val jobName: String,
    val title: String,
    val detail: String
) {
    val prep: List<PrepStep> get() = Tonight.prepSteps(group)
}

object Tonight {

    fun prepSteps(g: TonightGroup): List<PrepStep> = when (g) {
        TonightGroup.Estimate -> listOf(
            PrepStep("Pull every call, voicemail, text and recording onto the lead's card", true),
            PrepStep("Draft the scope from what the client asked for", true),
            PrepStep("Draft the task list with baseline hours from published rates and your past jobs", true),
            PrepStep("Material takeoff with current supplier prices", true),
            PrepStep("Draft the estimate in your format, payment schedule set", true),
            PrepStep("Measure, set the price and send", false))
        TonightGroup.Scope -> listOf(
            PrepStep("Look up published production rates for each task", true),
            PrepStep("Pull your logged hours on similar past tasks", true),
            PrepStep("Propose a low and a high for each task", true),
            PrepStep("Settle the hours", false))
        TonightGroup.Replan -> listOf(
            PrepStep("Pull today's daily log, time on site and site visits", true),
            PrepStep("List the tasks today's log says finished", true),
            PrepStep("Reflow the plan and draft tomorrow", true),
            PrepStep("Flag what it's waiting on: materials, subs, client answers", true),
            PrepStep("Confirm tomorrow", false))
    }

    /**
     * What Caroline is asked to do for one item: preliminary work only. She is told plainly not to reach anyone or change
     * anything that speaks for him (prices sent, the schedule, a client message) - her prep lands as drafts on the record.
     */
    fun prepRequest(it: TonightItem): String {
        val steps = it.prep.filter { s -> s.caroline }.joinToString("; ") { s -> s.words.replaceFirstChar { c -> c.lowercase() } }
        return "Tonight's office work prep for the job \"${it.jobName}\" (job #${it.projectId}): ${it.title}. ${it.detail} " +
            "Do the preliminary work before I sit down: $steps. Prep only - do not call, text or email anyone, do not send " +
            "anything, and do not change prices, the schedule or the job's status. Put what you find on the job as drafts or notes, " +
            "and tell me what still needs me."
    }

    /** One job's plan as Tonight reads it: the project row, and its plan or the reason it has none. */
    data class JobPlanRow(val project: JsonObject, val plan: Plan?, val problem: String?, val needsStart: Boolean)

    fun build(
        leads: List<JsonObject>,
        estimates: List<JsonObject>,
        jobs: List<JobPlanRow>,
        appointments: List<JsonObject>,
        today: LocalDate,
        handled: Set<String>
    ): List<TonightItem> {
        val out = mutableListOf<TonightItem>()
        fun add(i: TonightItem) { if (i.key !in handled) out += i }
        val sentOrApproved = estimates.filter { (s(it, "status") ?: "").lowercase() in setOf("sent", "approved", "accepted", "signed") }
            .mapNotNull { n(it, "project_id")?.toInt() }.toSet()

        // ---- estimates to write: leads with nothing sent
        leads.filter { (s(it, "status") ?: "") in setOf("lead", "estimating") }.forEach { p ->
            val id = n(p, "id")?.toInt() ?: return@forEach
            if (id in sentOrApproved) return@forEach
            val who = s(p, "client_name") ?: s(p, "name") ?: "Job #$id"
            val visit = appointments.filter { n(it, "project_id")?.toInt() == id }.mapNotNull { s(it, "start_at") }.minOrNull()
            val scope = s(p, "scope")?.let { if (it.length > 140) it.take(140).trimEnd() + "…" else it }
            val detail = listOfNotNull(
                visit?.let { "Site visit on the calendar: ${it.take(16).replace('T', ' ')}." },
                scope?.let { "On file: $it" },
                if (s(p, "status") == "estimating") "Marked estimating; nothing sent yet." else "Still a lead; no estimate on file."
            ).joinToString(" ")
            add(TonightItem("est-$id", TonightGroup.Estimate, id, s(p, "name") ?: who, "Write and submit $who's estimate", detail))
        }

        jobs.forEach { row ->
            val p = row.project
            val id = n(p, "id")?.toInt() ?: return@forEach
            val name = s(p, "name") ?: "Job #$id"
            val plan = row.plan
            // ---- scopes to plan
            if (plan != null) {
                val un = plan.bars.filter { it.task.estHours == null && it.task.state != TaskState.DONE }
                if (un.isNotEmpty())
                    add(TonightItem("hrs-$id-${un.size}", TonightGroup.Scope, id, name,
                        "Plan hours for ${countLabel(un.size, "$name task")}",
                        un.take(6).joinToString("; ") { it.task.title } + (if (un.size > 6) "; and ${un.size - 6} more" else "") +
                            ". Caroline looks up published production rates and your past hours before you sit down."))
            }
            if (row.needsStart)
                add(TonightItem("start-$id", TonightGroup.Scope, id, name, "Pick $name's start date",
                    "Nothing lays out on the calendar until there's a first day."))
            // ---- re-plan every live job with work left
            if (plan != null && plan.leftHours > 0 && (s(p, "status") ?: "") in setOf("active", "approved", "punch")) {
                val prog = plan.bars.filter { it.task.state == TaskState.IN_PROGRESS }.map { it.task.title }
                val detail = buildString {
                    append("${JobPlan.hrs(plan.leftHours)} left, projected finish ${plan.finish} (${JobPlan.varianceWords(plan)}). ")
                    if (prog.isNotEmpty()) append("In progress: ${prog.take(3).joinToString("; ")}. ")
                    append("Log today's real hours on the tasks you worked and tomorrow's plan moves with them.")
                }
                add(TonightItem("rp-$id-$today", TonightGroup.Replan, id, name, "Re-plan $name from today", detail))
            }
        }
        return out.sortedBy { it.group.ordinal }
    }

    private fun countLabel(n: Int, one: String): String = if (n == 1) "1 $one" else "$n ${one}s"
    private fun n(o: JsonObject, k: String): Double? = o.get(k)?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asDouble }.getOrNull() }
    private fun s(o: JsonObject, k: String): String? = o.get(k)?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asString }.getOrNull() }?.takeIf { it.isNotBlank() }
}
