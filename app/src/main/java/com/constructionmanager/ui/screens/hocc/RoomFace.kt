package com.constructionmanager.ui.screens.hocc

import androidx.compose.ui.graphics.Color
import com.google.gson.JsonObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * THE JOB ROOM'S FACE (Nov 7 2026) - the prototype Tyler approved ("Wade Job Room", claude.ai artifact BxTpnDatbHDu5qiNWJDvG6,
 * "I love it all lets push that into production"), read off the office record instead of written by hand.
 *
 * 0.7.6 shipped the prototype's workings inside the older wheel screens, and on his Fold it "definitely doesn't look like what I was
 * shown". This model is everything the prototype's dashboard shows for one job, so the screen (JobFace.kt) can be drawn exactly the
 * prototype's way, and so the same screen can be rendered with his real records off the phone (Paparazzi) as proof.
 *
 * Every field comes from the record. Where the record has nothing, the field is null or empty and the screen says what is missing
 * ("To find: ..."), the prototype's rule and his: no mock or placeholder data.
 */

/** The prototype's stages, in the order its Leads and jobs list shows them. Colors are the prototype's band colors. */
enum class FaceStage(val label: String, val band: Color) {
    OnSite("On site", Color(0xFF9A3B12)),
    Wrap("Wrapping up", Color(0xFF4A5560)),
    Signed("Signed, not started", Color(0xFF1F4E9A)),
    Estimate("Estimate out", Color(0xFF5A4A9A)),
    Visit("Site visit booked", Color(0xFF2E6B45)),
    Lead("Lead", Color(0xFF2E6B45)),
    Done("Done", Color(0xFF4A5560));

    companion object {
        /** The stages a job room is for (the prototype's list); leads without a visit and done jobs sit apart. */
        val ROOM = listOf(OnSite, Wrap, Signed, Estimate, Visit)

        fun of(status: String?, hasVisitAhead: Boolean): FaceStage = when ((status ?: "").lowercase()) {
            "active", "in_progress", "in progress", "underway", "on_site", "started" -> OnSite
            "punch", "wrap", "wrapping_up", "closeout" -> Wrap
            "approved", "signed", "scheduled", "accepted", "won" -> Signed
            "quoted", "sent", "proposal", "estimating", "estimate" -> Estimate
            "lead", "new", "site_visit", "consult", "inquiry", "" -> if (hasVisitAhead) Visit else Lead
            "complete", "completed", "done", "closed", "paid", "archived", "declined", "cancelled", "canceled" -> Done
            else -> Lead
        }
    }
}

/** One line of a card: a name and its value, with an optional state chip (done / under way / open). */
data class FaceRow(val a: String, val b: String = "", val chip: FaceChip? = null)

enum class FaceChip(val word: String) { Done("Done"), Prog("In progress"), Open("Open"), Ask("Ask") }

/** One line of the daily logs: the day, his words, and the hours that day. */
data class FaceLog(val date: LocalDate, val words: String, val hours: Double?)

/** One call or text with the client. */
data class FaceComm(val date: LocalDate?, val who: String, val words: String)

/** A day of the two-week strip. */
enum class FacePip { Mine, Other, Appt, None }

data class FaceDay(val date: LocalDate, val pip: FacePip)

/** Everything the prototype's job room shows for one job. */
data class RoomFace(
    val projectId: Int,
    val stage: FaceStage,
    val since: String?,
    val name: String,
    val what: String?,
    val addr: String?,
    val city: String?,
    val phone: String?,
    val email: String?,
    val contract: Double?,
    val paid: Double?,
    val balance: Double?,
    val moneyNote: String,
    val draws: List<FaceRow>,
    val nextWhen: String?,
    val nextWhat: String?,
    val needs: List<String>,
    val blocks: List<String>,
    val estimates: List<FaceRow>,
    val scope: List<FaceRow>,
    val scopeNote: String?,
    val selections: List<FaceRow>,
    val logs: List<FaceLog>,
    val photoIds: List<String>,
    val comms: List<FaceComm>,
    val days: List<FaceDay>,
    val upcoming: List<Pair<String, String>>,
    val ganttLine: String?,
    val ganttNote: String?,
    val hoursLine: String,
    val leftLine: String,
    val tonightCount: Int
) {
    val shortName: String get() = name.trim().split(" ").last().ifBlank { name }
    val pctPaid: Int? get() = if (contract != null && contract > 0 && paid != null) minOf(100, Math.round(paid / contract * 100).toInt()) else null
}

/** One job in the Leads and jobs list (the prototype's drawer, and the Jobs tab). */
data class FaceListItem(val projectId: Int, val stage: FaceStage, val title: String, val sub: String)

object RoomFaces {
    private val DAY: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM d", Locale.US)
    private val MDAY: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    fun fmt(d: LocalDate): String = d.format(DAY)
    fun fmtShort(d: LocalDate): String = d.format(MDAY)

    private fun day(raw: String?): LocalDate? = raw?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    /** The street and the rest of an address ("6184 Enke Ct" / "Dublin, OH 43017"). */
    fun splitAddress(raw: String?): Pair<String?, String?> {
        val a = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null to null
        val i = a.indexOf(',')
        return if (i < 0) a to null else a.substring(0, i).trim() to a.substring(i + 1).trim().ifBlank { null }
    }

    /** What the job is, in a few words: the project's name unless it only repeats the client's, else the scope's first line. */
    fun what(project: JsonObject?): String? {
        val name = project?.str("name")?.trim()
        val client = project?.str("client_name", "client")?.trim()
        val scope1 = project?.str("scope")?.lines()?.map { it.trim().trimStart('-', '•', '·').trim() }?.firstOrNull { it.isNotBlank() }
        val nameIsJustClient = name == null || client != null && (name.equals(client, true) || name.contains(" - from a call") ||
            name.startsWith("Lead from", true) || name.equals("$client job", true) || name.lowercase().removeSuffix(" job") == client.lowercase())
        return (if (!nameIsJustClient) name else null) ?: scope1 ?: name
    }

    private fun hasVisitAhead(project: JsonObject?, today: LocalDate): Boolean =
        project?.arrOf("appointments")?.objects().orEmpty().any { (day(it.str("start_at", "start", "at", "when")) ?: LocalDate.MIN) >= today && it.str("status") != "cancelled" }

    fun listItem(p: JsonObject, today: LocalDate): FaceListItem? {
        val id = p.num("id")?.toInt() ?: return null
        val (street, _) = splitAddress(p.str("address"))
        val who = p.str("client_name", "client")?.takeIf { it.isNotBlank() } ?: p.str("name") ?: "Job #$id"
        val title = listOfNotNull(street, who).joinToString(" · ")
        return FaceListItem(id, FaceStage.of(p.str("status"), hasVisitAhead(p, today)), title, what(p) ?: "")
    }

    /** The Leads and jobs list: the room stages first, in the prototype's order, then the leads; done jobs are left off. */
    fun list(projects: List<JsonObject>, today: LocalDate): List<Pair<FaceStage, List<FaceListItem>>> {
        val items = projects.mapNotNull { listItem(it, today) }
        return (FaceStage.ROOM + FaceStage.Lead).map { st -> st to items.filter { it.stage == st } }.filter { it.second.isNotEmpty() }
    }

    /**
     * The face of one job from the room's reads. [plan] is this job's plan from the office schedule (null when the job has none),
     * [plans] every live job's plan (for the strip's other jobs), [tonight] tonight's office work across the company.
     */
    fun build(d: RoomData, plan: Plan?, plans: List<Plan>, tonight: List<TonightItem>, today: LocalDate): RoomFace {
        val c = d.card
        val appts = c?.arrOf("appointments")?.objects().orEmpty().filter { it.str("status") != "cancelled" }
            .mapNotNull { a -> day(a.str("start_at", "start", "at", "when"))?.let { it to a } }.sortedBy { it.first }
        val stage = FaceStage.of(c?.str("status"), appts.any { it.first >= today })
        val (street, city) = splitAddress(c?.str("address"))

        // money: the books first, then the estimate's total, then the job's budget - said which
        val bill = d.billing?.objOf("bill")
        val est = d.estimate?.asObjOrNull()
        val estTotal = est?.objOf("totals")?.num("total", "grand_total") ?: est?.num("total")
        val contract = bill?.str("total")?.toDoubleOrNull() ?: estTotal ?: c?.num("budget")
        val paid = bill?.str("paid")?.toDoubleOrNull()
        val balance = bill?.str("balance")?.toDoubleOrNull() ?: (if (contract != null && paid != null) contract - paid else null)
        val moneyNote = when {
            bill != null -> "From the books" + (d.billing?.str("stage")?.let { ": $it" } ?: "") + "."
            estTotal != null -> "The estimate's total; nothing billed on record yet."
            c?.num("budget") != null -> "The job's budget; no estimate or bill on record."
            else -> "No money on record for this job yet."
        }
        val draws = (est?.arrOf("payments") ?: bill?.arrOf("payments", "draws")).let { arr ->
            arr?.objects().orEmpty().map { p ->
                val label = listOfNotNull(p.str("label", "name", "when", "milestone"), p.num("percent", "pct")?.let { "${fmtN(it)}%" },
                    p.num("amount")?.let { money(it) }).joinToString(" · ")
                val st = p.str("status", "state")
                FaceRow(label.ifBlank { "Payment" }, humanStatus(st) ?: "", if (st?.lowercase() in setOf("paid", "received")) FaceChip.Done else null)
            }
        }

        // next: the next appointment on the job, else the next task the plan lays out
        val nextAppt = appts.firstOrNull { it.first >= today }
        val nextBar = plan?.bars?.firstOrNull { it.task.state != TaskState.DONE && it.remHours > 0 }
        val nextWhen = nextAppt?.let { fmt(it.first) + (it.second.str("start_at", "start")?.let { s -> clock(s)?.let { " · $it" } } ?: "") }
            ?: nextBar?.let { b -> plan.idxDate(b.s0).let { fmt(it) } }
        val nextWhat = nextAppt?.second?.str("name", "title", "what", "summary") ?: nextBar?.task?.title

        // needs you: tonight's office work on this job (the record's open questions), in his words
        val mine = tonight.filter { it.projectId == d.id }
        val needs = mine.map { it.title }

        val estRows = buildList {
            val cur = est
            if (cur != null) {
                val title = cur.str("number", "label", "title") ?: "Current estimate"
                add(FaceRow(title, listOfNotNull(estTotal?.let { money(it) }, humanStatus(cur.str("status"))).joinToString(" · ")))
            }
            d.estimates?.rowsOf("estimates")?.objects().orEmpty().filter { it.num("project_id")?.toInt() == d.id && it.num("id") != cur?.num("id") }
                .take(2).forEach { e -> add(FaceRow(e.str("title") ?: "Estimate #${e.num("id")?.toInt()}", listOfNotNull(e.num("total")?.let { money(it) }, humanStatus(e.str("status"))).joinToString(" · "))) }
            if (plan != null && plan.leftHours > 0) add(FaceRow("Hours left", "about ${JobPlan.hrs(plan.leftHours)}"))
        }

        // scope: the written scope line by line; else the schedule's phases with their state; else the job's tasks
        val written = (c?.str("scope") ?: "").lines().map { it.trim().trimStart('-', '•', '·').trim() }.filter { it.isNotBlank() }
        val takeoffLines = d.takeoff?.arrOf("lines")?.objects().orEmpty().mapNotNull { it.str("description", "title", "item") }
        val tasks = d.tasks?.rowsOf("tasks")?.objects().orEmpty()
        val (scope, scopeNote) = when {
            written.size >= 2 || (written.size == 1 && plan == null && tasks.isEmpty()) -> written.map { FaceRow(it) } to null
            plan != null && plan.bars.isNotEmpty() -> phaseRows(plan) to "From the job's schedule."
            tasks.isNotEmpty() -> tasks.map { t -> FaceRow(t.str("title") ?: "Task", chip = if (t.str("status") == "Completed") FaceChip.Done else FaceChip.Open) } to "From the job's task list."
            takeoffLines.isNotEmpty() -> takeoffLines.map { FaceRow(it) } to "From the takeoff."
            else -> written.map { FaceRow(it) } to null
        }

        val sel = d.materials?.rowsOf("materials", "items")?.objects().orEmpty().map { m ->
            val q = m.num("qty")?.let { fmtN(it) + (m.str("unit")?.let { u -> " $u" } ?: "") }
            FaceRow(listOfNotNull(q, m.str("item", "name", "title")).joinToString(" "), humanStatus(m.str("status")) ?: "",
                if (m.str("status") == "needed") FaceChip.Ask else null)
        }

        // daily logs: the record's hours entries, his words kept as he said them
        // his own words for the day first; GPS-only days say so in a line ("On site by GPS, 3.79 hr"), never the raw trip list
        val logs = d.hours?.rowsOf("entries")?.objects().orEmpty().mapNotNull { e ->
            val dd = day(e.str("work_date", "started_at")) ?: return@mapNotNull null
            Triple(dd, e, e.str("source_kind") == "location")
        }.groupBy { it.first }.map { (dd, rows) ->
            val words = rows.filter { !it.third }.mapNotNull { it.second.str("note")?.takeIf { n -> n.isNotBlank() } }
            val gps = rows.filter { it.third }.sumOf { it.second.num("hours") ?: 0.0 }
            val logged = rows.filter { !it.third }.sumOf { it.second.num("hours") ?: 0.0 }
            val line = words.joinToString(" · ").ifBlank { if (gps > 0) "On site by GPS, ${JobPlan.hrs(gps)}." else "" }
            FaceLog(dd, line, if (logged > 0) logged else gps)
        }.sortedByDescending { it.date }

        val photos = d.attachments?.rowsOf("attachments", "items")?.objects().orEmpty()
            .filter { (it.str("media_type", "content_type", "mime") ?: "").startsWith("image") }.mapNotNull { it.str("item_id", "id") }

        val comms = d.comms?.rowsOf("items", "log", "rows")?.objects().orEmpty().take(2).map { m ->
            FaceComm(day(m.str("at", "created_at", "time")), if (m.str("kind") == "call") "Call" else "Text",
                m.str("summary", "text", "body", "words") ?: "")
        }

        // the two-week strip, from yesterday: this job's planned days, the other jobs' days, and fixed appointments
        val days = (0 until 14).map { i ->
            val dd = today.minusDays(1).plusDays(i.toLong())
            val ap = appts.any { it.first == dd }
            val mineDay = plan != null && JobPlan.projectedOn(plan, dd).rows.isNotEmpty()
            val other = plans.any { it.input.projectId != d.id && JobPlan.projectedOn(it, dd).rows.isNotEmpty() }
            FaceDay(dd, if (ap) FacePip.Appt else if (mineDay) FacePip.Mine else if (other) FacePip.Other else FacePip.None)
        }
        val upcoming = buildList {
            appts.filter { it.first >= today }.take(2).forEach { (dd, a) -> add(fmt(dd) + " · fixed appointment" to (a.str("name", "title", "what") ?: "Appointment")) }
            if (plan != null) {
                var dd = today
                var n = 0
                while (n < 3 && dd <= today.plusDays(21)) {
                    val day = JobPlan.projectedOn(plan, dd)
                    if (day.rows.isNotEmpty()) { add(fmt(dd) + " · " + JobPlan.hrs(day.hours) to day.rows.joinToString("; ") { it.bar.task.title }); n++ }
                    dd = dd.plusDays(1)
                }
            }
        }
        val ganttLine = plan?.let { p ->
            if (!p.scheduled) null else "Gantt · projected finish ${fmt(p.finish)}"
        }
        val ganttNote = plan?.let { p ->
            listOfNotNull(JobPlan.varianceWords(p).replaceFirstChar { it.uppercase() }, "${JobPlan.hrs(p.leftHours)} left",
                p.unplanned.takeIf { it > 0 }?.let { "$it tasks need hours" }).joinToString(" · ")
        }
        val hourRows = d.hours?.rowsOf("entries")?.objects().orEmpty()
        val totalH = hourRows.sumOf { it.num("hours") ?: 0.0 }
        val lastDay = hourRows.mapNotNull { day(it.str("work_date")) }.maxOrNull()
        val hoursLine = if (hourRows.isEmpty()) "0 hr" else "${JobPlan.hrs(totalH)} logged" + (lastDay?.let { " through ${fmtShort(it)}" } ?: "")
        val leftLine = plan?.let { if (it.leftHours > 0) "about ${JobPlan.hrs(it.leftHours)} of tool time" else "nothing left on the schedule" }
            ?: "no schedule on record yet"

        val since = listOfNotNull(
            day(c?.str("start_date"))?.let { "Started ${fmtShort(it)}" },
            if (c?.str("start_date") == null) day(c?.str("created_at"))?.let { "On file since ${fmtShort(it)}" } else null
        ).firstOrNull()

        return RoomFace(
            projectId = d.id, stage = stage, since = since,
            name = c?.str("client_name", "client")?.takeIf { it.isNotBlank() } ?: c?.str("name") ?: "Job #${d.id}",
            what = what(c), addr = street, city = city,
            phone = c?.str("client_phone", "phone"), email = c?.str("client_email", "email") ?: d.client?.str("email"),
            contract = contract, paid = paid, balance = balance, moneyNote = moneyNote, draws = draws,
            nextWhen = nextWhen, nextWhat = nextWhat, needs = needs, blocks = emptyList(),
            estimates = estRows, scope = scope, scopeNote = scopeNote, selections = sel, logs = logs, photoIds = photos, comms = comms,
            days = days, upcoming = upcoming, ganttLine = ganttLine, ganttNote = ganttNote, hoursLine = hoursLine, leftLine = leftLine,
            tonightCount = mine.size
        )
    }

    /** The schedule's phases as scope lines, each with its state (done when every task is done, under way when any has time). */
    fun phaseRows(plan: Plan): List<FaceRow> = plan.bars.groupBy { it.task.phaseName }.map { (name, bars) ->
        val st = when {
            bars.all { it.task.state == TaskState.DONE } -> FaceChip.Done
            bars.any { it.task.state != TaskState.OPEN } -> FaceChip.Prog
            else -> FaceChip.Open
        }
        FaceRow(name, chip = st)
    }

    private fun clock(raw: String): String? = runCatching {
        java.time.OffsetDateTime.parse(raw).atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalTime()
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
    }.getOrNull()

    private fun fmtN(x: Double): String = if (x == Math.floor(x)) x.toLong().toString() else String.format(Locale.US, "%.2f", x).trimEnd('0').trimEnd('.')
}
