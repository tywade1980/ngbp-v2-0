package com.constructionmanager.ui.screens.hocc

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.data.caroline.CarolineConnector
import com.constructionmanager.data.caroline.ConnectorResult
import com.constructionmanager.data.caroline.ConnectorSettings
import com.constructionmanager.data.location.LocationFix
import com.constructionmanager.data.location.LocationReader
import com.constructionmanager.data.caroline.RoomWriters
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/*
 * THE ROOM (HOCC 0.6, Nov 2 2026) - one job as one room, the job screen of this app.
 *
 * Tyler's rulings this room is built to (Nov 1-2 2026, his words on the receipt):
 *  - "starts with the address that's a room ... in that room you have everything that room needs"
 *  - folded: the cards stacked "in the order in which I want to receive them", reorderable;
 *    opened: "hub and spoke ... the center card is ... almost like a dashboard ... quick access buttons"
 *  - the phone carries "what pertains to that actual job", the most recent evidence; the Office is the desk
 *  - "a native app is the job but the right one with the right screens the right cards the right rooms";
 *    a screen that only displays "serves me zero" - every card here WRITES
 *  - time in and time out, hours derived from the log (code_requests #8)
 *  - Enter never sends (the Samsung keyboard's backspace sits above Enter)
 *
 * Reads are the web room's reads (webapp/c/screens/jobs.js roomView). Writes ride the Office doors a paired
 * owner's phone passes (#1180): hours and the clock, photos, takeoff lines, scope, the assist thread, the
 * project update. After every write the room reads the record again: the answer is never the state.
 */

/** One card of the room. Keys are stable (the order is saved by key); labels are what the card is called. */
enum class RoomCard(val key: String, val label: String) {
    Vitals("vitals", "Client"),
    Caroline("caroline", "Caroline"),
    Estimate("estimate", "Estimates"),
    Schedule("schedule", "Job schedule"),
    Scope("scope", "Scope"),
    Selections("materials", "Selections"),
    Photos("photos", "Photos"),
    Logs("hours", "Daily logs"),
    Punch("punch", "Punch list");

    companion object {
        /** The phone order of his design file (Oct 9 2026): client, Caroline, estimates, schedule, scope, selections, photos, logs. */
        val DEFAULT = listOf(Vitals, Caroline, Estimate, Schedule, Scope, Selections, Photos, Logs, Punch)
        fun byKey(k: String): RoomCard? = values().firstOrNull { it.key == k }
    }
}

data class RoomData(
    val id: Int,
    val card: JsonObject?,            // GET projects/{id}  (the project + what hangs off it)
    val client: JsonObject?,          // GET cards/{phone}  (the client card: calls, texts, need)
    val billing: JsonObject?,         // GET hub/project/{id}/billing
    val schedule: JsonObject?,        // GET hub/project/{id}/schedule (sched_* family)
    val legacySchedule: JsonElement?, // GET projects/{id}/schedule-items if the route exists; else null (schedule_items family)
    val hours: JsonElement?,          // GET hours?project_id=
    val clock: JsonObject?,           // GET clock
    val takeoffs: JsonElement?,       // GET projects/{id}/takeoffs
    val takeoff: JsonObject?,         // GET takeoffs/{id} - the first takeoff in full (its lines)
    val estimates: JsonElement?,      // GET estimates (fleet; filtered to this job)
    val estimate: JsonElement?,       // GET projects/{id}/estimate (current)
    val materials: JsonElement?,      // GET projects/{id}/materials
    val attachments: JsonElement?,    // GET attachments?kind=project&ref=
    val tasks: JsonElement?,          // GET projects/{id}/tasks
    val thread: JsonElement?          // GET assist/thread
)

/** A short line the room shows after a write: what the door said, in its words. */
data class RoomNote(val text: String, val isError: Boolean)

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val connector: CarolineConnector,
    private val writers: RoomWriters,
    private val settings: ConnectorSettings
) : ViewModel() {

    /** Where the phone is, against the record's places (fieldclock places/at): matched = the room you are standing in. */
    data class Here(val fix: LocationFix? = null, val reading: Boolean = false, val matched: JsonObject? = null,
                    val nearest: List<JsonObject> = emptyList(), val note: String? = null)

    private val _here = MutableStateFlow(Here())
    val here = _here.asStateFlow()

    /** The address of a photo on the record, and the header that opens it (the paired phone's own token). */
    fun photoUrl(itemId: String): String = settings.baseUrl.trimEnd('/') + "/attachments/$itemId/download"
    fun authHeader(): String? = settings.deviceToken?.let { "Bearer $it" }

    fun locate(context: Context) {
        if (_here.value.reading) return
        _here.value = _here.value.copy(reading = true, note = null)
        viewModelScope.launch {
            val app = context.applicationContext
            val fix = LocationReader.current(app)
            if (fix == null) {
                _here.value = Here(note = if (LocationReader.hasPermission(app)) "The phone gave no location. Check that Location is on." else "Location permission is not granted yet.")
                return@launch
            }
            val atIso = java.time.Instant.ofEpochMilli(fix.atMillis).toString()
            connector.reportLocation(fix.lat, fix.lng, fix.accuracyM, atIso, java.util.UUID.randomUUID().toString())
            when (val r = connector.getJson("fieldclock/places/at?lat=${fix.lat}&lng=${fix.lng}")) {
                is ConnectorResult.Ok -> {
                    val o = r.value.asObjOrNull()
                    _here.value = Here(fix = fix, matched = o?.objOf("matched"), nearest = o?.arrOf("nearest")?.objects().orEmpty())
                }
                is ConnectorResult.Failed -> _here.value = Here(fix = fix, note = "Read the phone's fix, but the office did not answer: ${r.error.message}")
                else -> _here.value = Here(fix = fix)
            }
        }
    }

    private val _room = MutableStateFlow<SectionState<RoomData>?>(null)
    val room = _room.asStateFlow()

    private val _busy = MutableStateFlow<String?>(null)   // which card is writing, by key
    val busy = _busy.asStateFlow()

    private val _note = MutableStateFlow<RoomNote?>(null)
    val note = _note.asStateFlow()

    private val _order = MutableStateFlow(RoomCard.DEFAULT)
    val order = _order.asStateFlow()

    private var currentId: Int? = null

    // ------------------------------------------------------------------ the order of the cards (his, per device)

    fun loadOrder(context: Context) {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ORDER, null)
        if (saved.isNullOrBlank()) return
        val keys = saved.split(',')
        val cards = keys.mapNotNull { RoomCard.byKey(it) }
        val missing = RoomCard.DEFAULT.filter { it !in cards }
        _order.value = cards + missing
    }

    fun move(context: Context, card: RoomCard, up: Boolean) {
        val cur = _order.value.toMutableList()
        val i = cur.indexOf(card)
        val j = if (up) i - 1 else i + 1
        if (i < 0 || j < 0 || j >= cur.size) return
        cur[i] = cur[j].also { cur[j] = cur[i] }
        _order.value = cur
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ORDER, cur.joinToString(",") { it.key }).apply()
    }

    // ------------------------------------------------------------------ the read

    private var appContext: Context? = null
    fun remember(context: Context) { appContext = context.applicationContext }
    fun lastJob(context: Context): Int? = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_LAST, -1).takeIf { it > 0 }

    fun open(id: Int) {
        currentId = id
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putInt(KEY_LAST, id)?.apply()
        _room.value = SectionState(loading = true, data = _room.value?.data?.takeIf { it.id == id })
        viewModelScope.launch { read(id) }
    }

    fun refresh() {
        val id = currentId ?: return
        viewModelScope.launch { read(id) }
    }

    private suspend fun read(id: Int) {
        val card = connector.getJson("projects/$id")
        val cardObj = card.valueOrNull()?.asObjOrNull()
        val phone = cardObj?.str("client_phone", "phone")
        val client = if (phone != null) connector.getJson("cards/" + Uri.encode(phone)).valueOrNull()?.asObjOrNull() else null
        val billing = connector.getJson("hub/project/$id/billing").valueOrNull()?.asObjOrNull()
        val schedule = connector.getJson("hub/project/$id/schedule").valueOrNull()?.asObjOrNull()
        val hours = connector.getJson("hours?project_id=$id&days=3650").valueOrNull()
        val clock = connector.getJson("clock").valueOrNull()?.asObjOrNull()
        val takeoffs = connector.getJson("projects/$id/takeoffs").valueOrNull()
        val firstTk = takeoffs?.rowsOf("takeoffs")?.objects()?.firstOrNull()?.num("id")?.toInt()
        val takeoff = if (firstTk != null) connector.getJson("takeoffs/$firstTk").valueOrNull()?.asObjOrNull() else null
        val estimates = connector.getJson("estimates").valueOrNull()
        val estimate = connector.getJson("projects/$id/estimate").valueOrNull()
        val materials = connector.getJson("projects/$id/materials").valueOrNull()
        val atts = connector.getJson("attachments?kind=project&ref=$id").valueOrNull()
        val tasks = connector.getJson("projects/$id/tasks").valueOrNull()
        val thread = connector.getJson("assist/thread?limit=40").valueOrNull()
        _room.value = SectionState(
            loading = false,
            error = (card as? ConnectorResult.Failed)?.error?.message,
            data = RoomData(id, cardObj, client, billing, schedule, null, hours, clock, takeoffs, takeoff, estimates, estimate, materials, atts, tasks, thread)
        )
    }

    // ------------------------------------------------------------------ the writers

    private fun write(cardKey: String, block: suspend () -> ConnectorResult<JsonElement>, okText: (JsonElement) -> String) {
        if (_busy.value != null) return
        _busy.value = cardKey
        _note.value = null
        viewModelScope.launch {
            when (val r = block()) {
                is ConnectorResult.Ok -> _note.value = RoomNote(okText(r.value), false)
                is ConnectorResult.Failed -> _note.value = RoomNote(r.error.message, true)
                is ConnectorResult.Queued -> _note.value = RoomNote("Queued on this phone; it sends when you're back in coverage.", false)
            }
            _busy.value = null
            currentId?.let { read(it) }
        }
    }

    /** Hours: time in and time out, hours derived by the record (code_requests #8). start/end are HH:MM local; date YYYY-MM-DD. */
    fun logHours(projectId: Int, date: String, start: String, end: String, category: String, note: String) {
        val body = JsonObject().apply {
            addProperty("work_date", date)
            addProperty("start", start)
            addProperty("end", end)
            addProperty("category", category.ifBlank { "field" })
            addProperty("note", note)
        }
        write(RoomCard.Logs.key, { writers.post("projects/$projectId/hours", body) }) { r ->
            val o = r.asObjOrNull()
            o?.str("say", "note") ?: "Logged on the job."
        }
    }

    /** Hours by count, when he says a number instead of in/out. */
    fun logHoursCount(projectId: Int, date: String, hours: String, category: String, note: String) {
        val body = JsonObject().apply {
            addProperty("work_date", date)
            addProperty("hours", hours)
            addProperty("category", category.ifBlank { "field" })
            addProperty("note", note)
        }
        write(RoomCard.Logs.key, { writers.post("projects/$projectId/hours", body) }) { r -> r.asObjOrNull()?.str("say", "note") ?: "Logged on the job." }
    }

    fun clockIn(projectId: Int) =
        write(RoomCard.Logs.key, { writers.post("projects/$projectId/clock/in", JsonObject().apply { addProperty("note", "") }) }) { "On the clock." }

    fun clockOut(projectId: Int) =
        write(RoomCard.Logs.key, { writers.post("projects/$projectId/clock/out", JsonObject()) }) { r -> r.asObjOrNull()?.str("say", "note") ?: "Off the clock; the hours stand on the job." }

    fun editHours(entryId: Long, hours: String?, note: String?, remove: Boolean) {
        if (remove) return deleteHours(entryId)
        val body = JsonObject().apply {
            if (hours != null) addProperty("hours", hours.toDoubleOrNull() ?: 0.0)
            if (note != null) addProperty("note", note)
        }
        write(RoomCard.Logs.key, { writers.patch("hours/$entryId", body) }) { "Entry corrected." }
    }

    // ------------------------------------------------------------------ delete and archive (Tyler, Nov 6 2026)
    // Every delete below goes to the record's trash (the office's trash.py): Office > Trash puts it back exactly.
    // The screen asks first (ConfirmDelete); the office's own guards still refuse what may not go, in their words.

    private fun trashSay(r: JsonElement, fallback: String): String = r.asObjOrNull()?.str("say") ?: fallback

    /** One hours entry -> the trash. */
    fun deleteHours(entryId: Long) =
        write(RoomCard.Logs.key, { writers.delete("hours/$entryId") }) { trashSay(it, "Entry deleted - restore it from Office > Trash.") }

    /** A punch item's words. */
    fun editTask(taskId: Long, title: String) =
        write(RoomCard.Punch.key, { writers.patch("tasks/$taskId", JsonObject().apply { addProperty("title", title) }) }) { "Punch item saved." }

    /** A punch item -> the trash. */
    fun deleteTask(taskId: Long) =
        write(RoomCard.Punch.key, { writers.delete("tasks/$taskId") }) { trashSay(it, "Punch item deleted - restore it from Office > Trash.") }

    /** A selection / material line: its item, how many, the unit. Blank fields are left as they are. */
    fun editMaterial(materialId: Long, item: String, qty: String, unit: String) {
        val body = JsonObject().apply {
            if (item.isNotBlank()) addProperty("item", item.trim())
            qty.toDoubleOrNull()?.let { addProperty("qty", it) }
            if (unit.isNotBlank()) addProperty("unit", unit.trim())
        }
        write(RoomCard.Selections.key, { writers.patch("materials/$materialId", body) }) { "Selection saved." }
    }

    /** A selection / material line logged by mistake -> the trash. */
    fun deleteMaterial(materialId: Long) =
        write(RoomCard.Selections.key, { writers.delete("materials/$materialId") }) { trashSay(it, "Selection deleted - restore it from Office > Trash.") }

    /** A photo or file off this job: the file stays in the library, its place on the job goes to the trash. */
    fun removePhoto(projectId: Int, itemId: String) =
        write(RoomCard.Photos.key, { writers.delete("attachments/" + Uri.encode(itemId) + "?kind=project&ref=$projectId") }) { trashSay(it, "Taken off the job - restore it from Office > Trash.") }

    /** A draft estimate -> the trash (the office refuses a sent or approved one, in its words). */
    fun deleteEstimate(estimateId: Long) =
        write(RoomCard.Estimate.key, { writers.delete("hub/estimates/$estimateId") }) { trashSay(it, "Estimate deleted - restore it from Office > Trash.") }

    /** Archive the job: kept on the record for good, off the active lists, its standing remembered for Reopen. */
    fun archiveJob(projectId: Int, reason: String = "") =
        write(RoomCard.Vitals.key, { writers.post("projects/$projectId/archive", JsonObject().apply { addProperty("reason", reason) }) }) { trashSay(it, "Archived - kept on the record.") }

    /** Reopen an archived job at the standing it had. */
    fun unarchiveJob(projectId: Int) =
        write(RoomCard.Vitals.key, { writers.post("projects/$projectId/unarchive", JsonObject()) }) { trashSay(it, "Back on the active list.") }

    /** Delete the job -> the trash. Only a draft with nothing recorded under it deletes; anything else is refused with the
     *  office's reason (its exit is Archive). On success the room has nothing left to show, so [onGone] leaves it. */
    fun deleteJob(projectId: Int, onGone: () -> Unit) {
        if (_busy.value != null) return
        _busy.value = RoomCard.Vitals.key
        _note.value = null
        viewModelScope.launch {
            when (val r = writers.delete("hub/projects/$projectId")) {
                is ConnectorResult.Ok -> { _note.value = RoomNote(trashSay(r.value, "Job deleted - restore it from Office > Trash."), false); _busy.value = null; onGone(); return@launch }
                is ConnectorResult.Failed -> _note.value = RoomNote(r.error.message, true)
                is ConnectorResult.Queued -> _note.value = RoomNote("Queued on this phone.", false)
            }
            _busy.value = null
            currentId?.let { read(it) }
        }
    }

    /** Photo: the bytes of the picture to the attachments door, filed to this job. */
    fun addPhoto(context: Context, projectId: Int, uri: Uri, filename: String) {
        write(RoomCard.Photos.key, {
            val bytes = withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }
            if (bytes == null || bytes.isEmpty()) ConnectorResult.Failed(com.constructionmanager.data.caroline.ConnectorError.BadResponse("The photo could not be read from the phone."))
            else {
                val type = context.contentResolver.getType(uri) ?: "image/jpeg"
                writers.upload("attachments?kind=project&ref=$projectId&filename=" + Uri.encode(filename), bytes, type)
            }
        }) { "Photo filed to the job." }
    }

    /** A takeoff line - the scope's own store (takeoffs.py: every line carries its basis; an OPEN line has no number). */
    fun addTakeoffLine(takeoffId: Int, section: String, item: String, qty: String, unit: String, basis: String, note: String) {
        val body = JsonObject().apply {
            addProperty("section", section)
            addProperty("item", item)
            val q = qty.toDoubleOrNull()
            if (q != null) addProperty("qty", q)
            if (unit.isNotBlank()) addProperty("unit", unit)
            addProperty("qty_basis", if (q == null) "open" else basis.ifBlank { "stated" })
            if (note.isNotBlank()) addProperty("qty_note", note)
        }
        write(RoomCard.Scope.key, { writers.post("takeoffs/$takeoffId/lines", body) }) { "Line added to the scope." }
    }

    fun editTakeoffLine(lineId: Long, item: String?, qty: String?, unit: String?, note: String?) {
        val body = JsonObject().apply {
            if (item != null) addProperty("item", item)
            if (qty != null) { val q = qty.toDoubleOrNull(); if (q != null) addProperty("qty", q) }
            if (unit != null) addProperty("unit", unit)
            if (note != null) addProperty("qty_note", note)
        }
        write(RoomCard.Scope.key, { writers.patch("takeoff-lines/$lineId", body) }) { "Line corrected." }
    }

    fun deleteTakeoffLine(lineId: Long) =
        write(RoomCard.Scope.key, { writers.delete("takeoff-lines/$lineId") }) { "Line removed." }

    /** No takeoff on the job yet: make the scope's store so lines have somewhere to land. */
    fun createTakeoff(projectId: Int, title: String) =
        write(RoomCard.Scope.key, { writers.post("projects/$projectId/takeoffs", JsonObject().apply { addProperty("title", title.ifBlank { "Detailed scope" }) }) }) { "Scope started on this job." }

    /** The job's description - the seed the detailed scope grows from (project update door). */
    fun saveScopeText(projectId: Int, scope: String) =
        write(RoomCard.Scope.key, { writers.post("projects/$projectId/update", JsonObject().apply { addProperty("scope", scope) }) }) { "Description saved on the job." }

    /** A note on the job: the record's own stamp shape, never edited away. */
    fun addNote(projectId: Int, text: String) =
        write(RoomCard.Logs.key, { writers.post("projects/$projectId/update", JsonObject().apply { addProperty("note", text) }) }) { "Note on the job." }

    /** Edit the job's own fields (name, status, phase, budget, address). Empty strings are left alone by the door. */
    fun editJob(projectId: Int, name: String?, status: String?, phase: String?, budget: String?, address: String?) {
        val body = JsonObject().apply {
            if (!name.isNullOrBlank()) addProperty("name", name)
            if (!status.isNullOrBlank()) addProperty("status", status)
            if (!phase.isNullOrBlank()) addProperty("current_phase", phase)
            if (!budget.isNullOrBlank()) budget.toDoubleOrNull()?.let { addProperty("budget", it) }
            if (!address.isNullOrBlank()) addProperty("address", address)
        }
        write(RoomCard.Vitals.key, { writers.post("projects/$projectId/update", body) }) { "Job updated." }
    }

    /** A new lead, in the record from the phone (POST projects - the Office's own create door). Answers with the new id. */
    fun newLead(name: String, phone: String, address: String, need: String, onMade: (Int) -> Unit) {
        val body = JsonObject().apply {
            addProperty("client_name", name)
            if (phone.isNotBlank()) addProperty("client_phone", phone)
            if (address.isNotBlank()) addProperty("address", address)
            addProperty("name", name.ifBlank { address.ifBlank { "New lead" } })
            if (need.isNotBlank()) addProperty("scope", need)
            addProperty("status", "lead")
        }
        if (_busy.value != null) return
        _busy.value = RoomCard.Vitals.key
        _note.value = null
        viewModelScope.launch {
            when (val r = writers.post("projects", body)) {
                is ConnectorResult.Ok -> {
                    val nid = r.value.asObjOrNull()?.let { it.num("id") ?: it.objOf("project")?.num("id") }?.toInt()
                    _note.value = RoomNote(if (nid != null) "Lead on file." else "The office answered but gave no job number - read the leads again.", nid == null)
                    if (nid != null) onMade(nid)
                }
                is ConnectorResult.Failed -> _note.value = RoomNote(r.error.message, true)
                is ConnectorResult.Queued -> _note.value = RoomNote("Queued on this phone.", false)
            }
            _busy.value = null
        }
    }

    /** Award: the lead signed - status approved, the record's word for it. */
    fun award(projectId: Int) = setStatus(projectId, "approved")

    /** Remove: a lead that is not a job - status cancelled, with his reason kept verbatim; nothing is deleted from the record. */
    fun remove(projectId: Int, reason: String) =
        write(RoomCard.Vitals.key, { writers.post("projects/$projectId/update", JsonObject().apply { addProperty("status", "cancelled"); addProperty("note", "Removed from the board: " + reason.ifBlank { "no reason given" }) }) }) { "Removed from the board (kept on file as cancelled)." }

    /** The lead's life: its status word, through the update door (the record's own words: projects.STATUSES). */
    fun setStatus(projectId: Int, status: String) =
        write(RoomCard.Vitals.key, { writers.post("projects/$projectId/update", JsonObject().apply { addProperty("status", status) }) }) { "Now: " + (Board.LIFE.firstOrNull { it.first == status }?.second ?: status) + "." }

    /** What they need - on the client card (the hub contacts door, the #1078 field). */
    fun saveNeed(phone: String, need: String) =
        write(RoomCard.Vitals.key, { writers.patch("hub/contacts/" + Uri.encode(phone), JsonObject().apply { addProperty("need", need) }) }) { "Saved on the client's card." }

    /** Materials: log one item on the job (QuickLog). */
    fun logMaterial(projectId: Int, text: String) =
        write(RoomCard.Selections.key, { writers.post("projects/$projectId/materials", JsonObject().apply { addProperty("text", text) }) }) { "Material logged." }

    fun setMaterialStatus(materialId: Long, status: String) =
        write(RoomCard.Selections.key, { writers.patch("materials/$materialId", JsonObject().apply { addProperty("status", status) }) }) { "Marked ${humanStatus(status)?.lowercase()}." }

    /** Punch list: add / complete / reopen (the Office task doors). */
    fun addTask(projectId: Int, title: String) =
        write(RoomCard.Punch.key, { writers.post("projects/$projectId/tasks", JsonObject().apply { addProperty("title", title) }) }) { "Added to the punch list." }

    fun flipTask(taskId: Long, completed: Boolean) =
        write(RoomCard.Punch.key, { writers.patch("tasks/$taskId", JsonObject().apply { addProperty("status", if (completed) "Completed" else "Open") }) }) { if (completed) "Done." else "Reopened." }

    /** Estimate: a new draft on the builder from this job (the create door Lane 1 built). */
    fun newEstimate(projectId: Int, title: String, scope: String?) {
        val body = JsonObject().apply {
            addProperty("title", title)
            if (!scope.isNullOrBlank()) addProperty("scope", scope)
        }
        write(RoomCard.Estimate.key, { writers.post("projects/$projectId/estimate", body) }) { r ->
            val d = r.asObjOrNull()?.objOf("data") ?: r.asObjOrNull()
            d?.str("note") ?: "Estimate started on this job."
        }
    }

    /** Billing: an approved estimate becomes the bill (the hub's explicit convert, never automatic). */
    fun convertBilling(projectId: Int) =
        write(RoomCard.Estimate.key, { writers.post("hub/project/$projectId/billing/convert", JsonObject()) }) { "The approved estimate is now the bill." }

    /** Schedule: the hub doors - generate from scope, replan, set the start. */
    fun generateSchedule(projectId: Int) =
        write(RoomCard.Schedule.key, { writers.post("hub/project/$projectId/schedule/generate", JsonObject()) }) { "Schedule laid out from the scope." }

    fun replanSchedule(projectId: Int) =
        write(RoomCard.Schedule.key, { writers.post("hub/project/$projectId/schedule/replan", JsonObject()) }) { "Replanned." }

    fun setStart(projectId: Int, date: String) =
        write(RoomCard.Schedule.key, { writers.post("hub/project/$projectId/schedule/start", JsonObject().apply { addProperty("start_date", date) }) }) { "Start date set." }

    // ------------------------------------------------------------------ the Gantt's doors (Tyler, Nov 7 2026: the task-level Gantt)
    // Every one is the office's own schedule writer through the hub door (schedule.py: one writer, each change kept in sched_edits
    // with its before and after). The office replans after an item edit; the room reads the record again, and the plan engine
    // (JobPlan) reflows the projection from what came back - the answer is never the state.

    /** A task's estimated time, in his hours (the record keeps whole minutes). */
    fun setItemEstimate(itemId: Long, hours: String) {
        val h = hours.trim().toDoubleOrNull() ?: return
        val body = JsonObject().apply { addProperty("est_minutes", Math.round(h * 60).toInt()) }
        write(RoomCard.Schedule.key, { writers.patch("hub/items/$itemId", body) }) { "Estimate set to ${JobPlan.hrs(h)}; the plan moves with it." }
    }

    /** A task finished (or reopened). Only offered when the office's read carries finished_at on its items - the screen never
     *  calls a door the record doesn't have. */
    fun finishItem(itemId: Long, finished: Boolean) =
        write(RoomCard.Schedule.key, { writers.patch("hub/items/$itemId", JsonObject().apply { addProperty("finished", finished) }) }) {
            if (finished) "Marked finished; the rest of the plan reflows." else "Reopened."
        }

    /** Say which task an hours row was for (the hours themselves are not touched). itemId null unties it. */
    fun linkHours(entryId: Long, itemId: Long?) =
        write(RoomCard.Schedule.key, {
            writers.post("hub/hours/$entryId/link", JsonObject().apply { if (itemId == null) add("item_id", com.google.gson.JsonNull.INSTANCE) else addProperty("item_id", itemId) })
        }) { if (itemId == null) "Untied from the task." else "Tied to the task; its real hours count on the plan." }

    /** Hours on one task: logged on the job (the hours door), then tied to the task when the office answers with the entry's id. */
    fun logHoursOnItem(projectId: Int, itemId: Long, date: String, hours: String, note: String) {
        val body = JsonObject().apply {
            addProperty("work_date", date)
            addProperty("hours", hours)
            addProperty("category", "field")
            addProperty("note", note)
        }
        write(RoomCard.Schedule.key, {
            when (val r = writers.post("projects/$projectId/hours", body)) {
                is ConnectorResult.Ok -> {
                    val o = r.value.asObjOrNull()
                    val eid = (o?.num("id", "time_entry_id", "entry_id") ?: o?.objOf("entry")?.num("id") ?: o?.objOf("time_entry")?.num("id"))?.toLong()
                    if (eid == null) ConnectorResult.Ok(sayObj("Logged on the job. The office didn't name the entry, so tie it to the task from Time on site."))
                    else when (val l = writers.post("hub/hours/$eid/link", JsonObject().apply { addProperty("item_id", itemId) })) {
                        is ConnectorResult.Ok -> ConnectorResult.Ok(sayObj("Logged and tied to the task."))
                        is ConnectorResult.Failed -> ConnectorResult.Ok(sayObj("Logged on the job, but tying it to the task was refused: ${l.error.message}"))
                        is ConnectorResult.Queued -> l
                    }
                }
                else -> r
            }
        }) { r -> r.asObjOrNull()?.str("say") ?: "Logged." }
    }

    private fun sayObj(text: String): JsonElement = JsonObject().apply { addProperty("say", text) }

    /** Caroline, about this job: the assist thread, framed with the job so she knows the room you're in. */
    fun tellCaroline(projectId: Int, jobName: String, text: String) {
        if (_busy.value != null) return
        _busy.value = RoomCard.Caroline.key
        _note.value = null
        viewModelScope.launch {
            val framed = "About the job \"$jobName\" (job #$projectId): $text"
            when (val r = connector.sendText(framed)) {
                is ConnectorResult.Ok -> {
                    val turn = r.value.turnId
                    var reply: String? = null
                    if (turn != null) {
                        // wait for her turn to finish (return@repeat only skipped one round before - it waited the full minute every time)
                        for (i in 0 until 36) {
                            delay(2500)
                            val o = connector.assistTurn(turn).valueOrNull()?.asObjOrNull()
                            val st = o?.str("status", "state")
                            if (st != null && st != "working" && st != "running" && st != "pending") { reply = o.str("reply", "text"); break }
                        }
                    }
                    // her answer shows right under the box (it also lands on the thread in the Caroline tab)
                    _note.value = reply?.takeIf { it.isNotBlank() }?.let { RoomNote("Caroline: $it", false) }
                }
                is ConnectorResult.Queued -> _note.value = RoomNote("No connection - queued on this phone; it sends when you're back in coverage.", false)
                is ConnectorResult.Failed -> _note.value = RoomNote(r.error.message, true)
            }
            _busy.value = null
            currentId?.let { read(it) }
        }
    }

    fun clearNote() { _note.value = null }

    companion object {
        const val PREFS = "hocc_room"
        const val KEY_ORDER = "card_order"
        const val KEY_LAST = "last_job"
    }
}

/** The estimate rows of the fleet list that belong to this job. */
fun RoomData.myEstimates(): List<JsonObject> =
    (estimates?.rowsOf("estimates") ?: JsonArray()).objects().filter { it.num("project_id")?.toInt() == id }
