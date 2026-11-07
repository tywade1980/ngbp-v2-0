package com.constructionmanager.ui.screens.hocc

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.BuildConfig
import com.constructionmanager.data.caroline.CallSummary
import com.constructionmanager.data.caroline.CarolineConnector
import com.constructionmanager.data.caroline.ConnectorResult
import com.constructionmanager.data.caroline.RoomWriters
import com.constructionmanager.data.location.LocationFix
import com.constructionmanager.data.location.LocationReader
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

/** One screen's read of the record: loading, the refusal in words when there was one, and what came back. */
data class SectionState<T>(val loading: Boolean = false, val error: String? = null, val data: T? = null)

data class HubData(
    val briefing: String?,
    val day: JsonObject?,
    val projects: JsonArray,
    val calls: List<CallSummary>,
    val home: JsonObject?
)

data class SiteData(val where: JsonObject?, val projects: JsonArray)

data class JobDetail(
    val id: Int,
    val card: JsonObject?,
    val estimate: JsonElement?,
    val takeoffs: JsonElement?,
    val tasks: JsonElement?,
    val hours: JsonElement?
)

data class ScheduleData(val sheet: JsonObject?, val day: JsonObject?, val appointments: JsonArray)

data class ChatMessage(val fromOwner: Boolean, val text: String, val at: String?)

data class ChatData(val messages: List<ChatMessage>, val repliesOn: Boolean?)

data class OfficeData(
    val messages: JsonArray,
    val contacts: JsonArray,
    val estimates: JsonElement?,
    val issues: JsonArray
)

data class UpdateInfo(
    val code: Int?,
    val name: String?,
    val notes: String?,
    val sha256: String?,
    val size: Long?,
    val downloading: Boolean = false,
    val downloadedPath: String? = null,
    val error: String? = null
) {
    val isNewer: Boolean get() = (code ?: 0) > BuildConfig.VERSION_CODE
}

data class LocationUiState(
    val fix: LocationFix? = null,
    val note: String? = null,
    val reading: Boolean = false,
    val reported: Boolean = false
)

/**
 * Every HOCC screen reads the one record through here - the same connector, the same sign-in, no second
 * source. A section that the record refuses says so in the server's words; a section that loads shows the
 * record's rows, counted from the rows themselves.
 */
@HiltViewModel
class HoccViewModel @Inject constructor(
    private val connector: CarolineConnector,
    private val writers: RoomWriters
) : ViewModel() {

    private val _hub = MutableStateFlow(SectionState<HubData>())
    val hub = _hub.asStateFlow()

    private val _site = MutableStateFlow(SectionState<SiteData>())
    val site = _site.asStateFlow()

    private val _locationUi = MutableStateFlow(LocationUiState())
    val locationUi = _locationUi.asStateFlow()

    private val _jobs = MutableStateFlow(SectionState<JsonArray>())
    val jobs = _jobs.asStateFlow()

    private val _jobDetail = MutableStateFlow<SectionState<JobDetail>?>(null)
    val jobDetail = _jobDetail.asStateFlow()

    private val _schedule = MutableStateFlow(SectionState<ScheduleData>())
    val schedule = _schedule.asStateFlow()

    private val _chat = MutableStateFlow(SectionState<ChatData>())
    val chat = _chat.asStateFlow()

    private val _chatCalls = MutableStateFlow<List<CallSummary>>(emptyList())
    val chatCalls = _chatCalls.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending = _sending.asStateFlow()

    private val _chatNote = MutableStateFlow<String?>(null)
    val chatNote = _chatNote.asStateFlow()

    private val _office = MutableStateFlow(SectionState<OfficeData>())
    val office = _office.asStateFlow()

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo = _updateInfo.asStateFlow()

    private fun failedOf(vararg rs: ConnectorResult<*>): String? =
        rs.filterIsInstance<ConnectorResult.Failed>().firstOrNull()?.error?.message

    // ------------------------------------------------------------------ Hub

    fun loadHub() {
        if (_hub.value.loading) return
        _hub.value = _hub.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val briefing = connector.briefing()
            val day = connector.getJson("day")
            val projects = connector.getJson("projects")
            val calls = connector.calls(8)
            val home = connector.getJson("hub/home")
            val data = HubData(
                briefing = briefing.valueOrNull()?.text,
                day = day.valueOrNull()?.asObjOrNull(),
                projects = projects.valueOrNull()?.rowsOf("projects") ?: JsonArray(),
                calls = calls.valueOrNull() ?: emptyList(),
                home = home.valueOrNull()?.asObjOrNull()
            )
            val dead = data.briefing == null && data.day == null && data.projects.size() == 0
            _hub.value = SectionState(loading = false, error = if (dead) failedOf(briefing, day, projects) else null, data = data)
        }
    }

    // ------------------------------------------------------------------ Site + location

    fun loadSite() {
        if (_site.value.loading) return
        _site.value = _site.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val where = connector.getJson("owner/location")
            val projects = connector.getJson("projects")
            val data = SiteData(
                where = where.valueOrNull()?.asObjOrNull(),
                projects = projects.valueOrNull()?.rowsOf("projects") ?: JsonArray()
            )
            val dead = data.where == null && data.projects.size() == 0
            _site.value = SectionState(loading = false, error = if (dead) failedOf(where, projects) else null, data = data)
        }
    }

    /** Read this phone's location with its own permission, show it, and put the reading on file. */
    fun readLocation(context: Context) {
        if (_locationUi.value.reading) return
        _locationUi.value = _locationUi.value.copy(reading = true, note = null)
        viewModelScope.launch {
            val app = context.applicationContext
            val fix = LocationReader.current(app)
            if (fix == null) {
                _locationUi.value = _locationUi.value.copy(
                    reading = false,
                    note = if (LocationReader.hasPermission(app))
                        "The phone did not return a location. Check that Location is turned on, then try again."
                    else "Location permission is not granted yet - allow it and this reads where you are."
                )
                return@launch
            }
            val atIso = java.time.Instant.ofEpochMilli(fix.atMillis).toString()
            val r = connector.reportLocation(fix.lat, fix.lng, fix.accuracyM, atIso, UUID.randomUUID().toString())
            val stored = r.valueOrNull()?.asObjOrNull()?.boolOf("stored") == true
            _locationUi.value = LocationUiState(
                fix = fix,
                note = when {
                    stored -> "On file."
                    r is ConnectorResult.Failed -> "Read fine, but the office did not take it: ${r.error.message}"
                    else -> "Sent to the office."
                },
                reading = false,
                reported = stored
            )
            loadSite()
        }
    }

    // ------------------------------------------------------------------ Jobs

    fun loadJobs(force: Boolean = false) {
        if (_jobs.value.loading && !force) return
        _jobs.value = _jobs.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val projects = connector.getJson("projects")
            _jobs.value = SectionState(
                loading = false,
                error = failedOf(projects),
                data = projects.valueOrNull()?.rowsOf("projects") ?: JsonArray()
            )
        }
    }

    fun openJob(id: Int) {
        _jobDetail.value = SectionState(loading = true)
        viewModelScope.launch {
            val card = connector.getJson("projects/$id")
            val est = connector.getJson("projects/$id/estimate")
            val tk = connector.getJson("projects/$id/takeoffs")
            val tasks = connector.getJson("projects/$id/tasks")
            val hours = connector.getJson("hours?project_id=$id&days=365")
            _jobDetail.value = SectionState(
                loading = false,
                error = failedOf(card),
                data = JobDetail(id, card.valueOrNull()?.asObjOrNull(), est.valueOrNull(), tk.valueOrNull(), tasks.valueOrNull(), hours.valueOrNull())
            )
        }
    }

    fun closeJob() {
        _jobDetail.value = null
    }

    // ------------------------------------------------------------------ Schedule

    fun loadSchedule() {
        if (_schedule.value.loading) return
        _schedule.value = _schedule.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val sheet = connector.getJson("day/sheet")
            val day = connector.getJson("day")
            val appts = connector.getJson("appointments?days=14")
            val data = ScheduleData(
                sheet = sheet.valueOrNull()?.asObjOrNull(),
                day = day.valueOrNull()?.asObjOrNull(),
                appointments = appts.valueOrNull()?.rowsOf("events", "appointments") ?: JsonArray()
            )
            val dead = data.sheet == null && data.day == null
            _schedule.value = SectionState(loading = false, error = if (dead) failedOf(sheet, day) else null, data = data)
        }
    }

    // ------------------------------------------------------------------ Caroline (chat + calls)

    fun loadCaroline() {
        if (_chat.value.loading) return
        _chat.value = _chat.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { connector.flushOutbox() }
            val t = connector.assistThread(60)
            val c = connector.calls(10)
            _chatCalls.value = c.valueOrNull() ?: emptyList()
            _chat.value = SectionState(loading = false, error = failedOf(t), data = parseThread(t.valueOrNull()))
        }
    }

    private fun parseThread(el: JsonElement?): ChatData {
        val o = el?.asObjOrNull()
        val rows = o?.arrOf("messages")?.objects().orEmpty()
        val parsed = rows.map { m ->
            val dir = m.str("direction", "from", "sender", "role", "who", "speaker")
            val fromOwner = when {
                dir != null -> dir.contains("owner", ignoreCase = true) || dir.contains("inbound", ignoreCase = true) ||
                    dir.equals("in", ignoreCase = true) || dir.contains("tyler", ignoreCase = true)
                else -> m.boolOf("owner") ?: m.boolOf("from_owner") ?: false
            }
            ChatMessage(fromOwner, m.str("text", "body", "message") ?: "", m.str("at", "created_at", "said_at", "time"))
        }.filter { it.text.isNotBlank() }
        return ChatData(parsed, o?.boolOf("replies"))
    }

    fun sendChat(text: String) {
        val body = text.trim()
        if (body.isEmpty() || _sending.value) return
        _sending.value = true
        _chatNote.value = null
        // his words go on the thread the moment he sends them; the record's copy replaces this line on the next read
        // (Tyler, Nov 4 2026: "doesnt transfer to conversation display below")
        val now = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).toString()
        val cur = _chat.value.data ?: ChatData(emptyList(), null)
        _chat.value = _chat.value.copy(data = cur.copy(messages = cur.messages + ChatMessage(true, body, now)))
        viewModelScope.launch {
            when (val r = connector.sendText(body)) {
                is ConnectorResult.Ok -> {
                    val turnId = r.value.turnId
                    if (turnId != null) pollTurn(turnId)
                }
                is ConnectorResult.Queued ->
                    _chatNote.value = "No connection right now - your message is queued on this phone and sends itself when you're back in coverage."
                is ConnectorResult.Failed -> _chatNote.value = r.error.message
            }
            _sending.value = false
            loadCaroline()
        }
    }

    private suspend fun pollTurn(turnId: String) {
        repeat(30) {
            delay(2500)
            val o = connector.assistTurn(turnId).valueOrNull()?.asObjOrNull() ?: return@repeat
            when (o.str("status", "state")) {
                "done", "complete", "completed", "failed", "error" -> return
            }
        }
    }

    fun clearChatNote() {
        _chatNote.value = null
    }

    // ------------------------------------------------------------------ Office + app update

    /** One read of an Office door, for a card that shows what it says. */
    suspend fun readJson(path: String): JsonObject? = connector.getJson(path).valueOrNull()?.asObjOrNull()

    fun loadOffice() {
        if (_office.value.loading) return
        _office.value = _office.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val messages = connector.getJson("messages?limit=40")
            val contacts = connector.getJson("contacts?limit=100")
            val estimates = connector.getJson("estimates")
            val issues = connector.getJson("issues?status=open&limit=50")
            val data = OfficeData(
                messages = messages.valueOrNull()?.rowsOf("messages") ?: JsonArray(),
                contacts = contacts.valueOrNull()?.rowsOf("contacts") ?: JsonArray(),
                estimates = estimates.valueOrNull(),
                issues = issues.valueOrNull()?.rowsOf("issues") ?: JsonArray()
            )
            _office.value = SectionState(loading = false, error = failedOf(messages, contacts, estimates, issues), data = data)
            checkUpdate()
        }
    }

    fun checkUpdate() {
        viewModelScope.launch {
            val r = connector.hoccLatest()
            val o = r.valueOrNull()?.asObjOrNull()
            // The route answers {package, latest: {version_code, ...}} - the build facts live under "latest".
            val latest = o?.objOf("latest") ?: o
            _updateInfo.value = if (latest == null) {
                UpdateInfo(null, null, null, null, null, error = failedOf(r))
            } else {
                UpdateInfo(
                    code = latest.num("version_code")?.toInt(),
                    name = latest.str("version_name"),
                    notes = latest.str("notes"),
                    sha256 = latest.str("sha256"),
                    size = latest.num("size")?.toLong()
                )
            }
        }
    }

    fun downloadUpdate(context: Context) {
        val info = _updateInfo.value ?: return
        if (info.downloading) return
        _updateInfo.value = info.copy(downloading = true, error = null, downloadedPath = null)
        viewModelScope.launch {
            val target = UpdateInstall.apkFile(context)
            when (val r = connector.downloadLatestApk(target, info.sha256)) {
                is ConnectorResult.Ok -> _updateInfo.value = info.copy(downloading = false, downloadedPath = r.value.absolutePath)
                is ConnectorResult.Failed -> _updateInfo.value = info.copy(downloading = false, error = r.error.message)
                is ConnectorResult.Queued -> _updateInfo.value = info.copy(downloading = false, error = "The download could not start.")
            }
        }
    }

    // ------------------------------------------------------------------ edit, delete, archive, restore (Tyler, Nov 6 2026)
    // His words: "Editing, deleting, archiving. Can't do any of it from anywhere yet." Every delete goes to the record's
    // trash and comes back from the Trash card; archive keeps a record for good, off the active lists. A write is sent once
    // (RoomWriters), then the list is read again - the answer is never taken as the state.

    /** The last write's words - the office's own, a refusal included. */
    data class OfficeNote(val text: String, val isError: Boolean)

    private val _officeNote = MutableStateFlow<OfficeNote?>(null)
    val officeNote = _officeNote.asStateFlow()

    private val _officeBusy = MutableStateFlow(false)
    val officeBusy = _officeBusy.asStateFlow()

    fun clearOfficeNote() { _officeNote.value = null }

    private fun officeWrite(block: suspend () -> ConnectorResult<JsonElement>, ok: String, then: () -> Unit) {
        if (_officeBusy.value) return
        _officeBusy.value = true
        _officeNote.value = null
        viewModelScope.launch {
            _officeNote.value = when (val r = block()) {
                is ConnectorResult.Ok -> OfficeNote(r.value.asObjOrNull()?.str("say") ?: ok, false)
                is ConnectorResult.Failed -> OfficeNote(r.error.message, true)
                is ConnectorResult.Queued -> OfficeNote("Queued on this phone; it sends when you're back in coverage.", false)
            }
            _officeBusy.value = false
            then()
        }
    }

    // ---- the client book (the hub contacts door: one card per phone)
    private val _book = MutableStateFlow(SectionState<List<JsonObject>>())
    val book = _book.asStateFlow()
    private var bookQuery = ""

    fun loadBook(q: String = bookQuery) {
        bookQuery = q
        _book.value = _book.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val r = connector.getJson("hub/contacts?q=" + android.net.Uri.encode(q.trim()))
            _book.value = SectionState(loading = false, error = failedOf(r), data = r.valueOrNull()?.rowsOf("contacts")?.objects().orEmpty())
        }
    }

    fun editContact(phone: String, name: String, email: String, address: String, need: String) {
        val body = JsonObject().apply {
            addProperty("name", name.trim()); addProperty("email", email.trim()); addProperty("address", address.trim())
            if (need.isNotBlank()) addProperty("need", need.trim())
        }
        officeWrite({ writers.patch("hub/contacts/" + android.net.Uri.encode(phone), body) }, "Card saved.") { loadBook() }
    }

    fun archiveContact(phone: String, archive: Boolean) =
        officeWrite({ writers.post("hub/contacts/" + android.net.Uri.encode(phone) + if (archive) "/archive" else "/unarchive", JsonObject()) },
            if (archive) "Archived - off the active cards, kept on file." else "Back on the active cards.") { loadBook() }

    fun deleteContact(phone: String) =
        officeWrite({ writers.delete("hub/contacts/" + android.net.Uri.encode(phone)) }, "Card deleted - restore it from the Trash.") { loadBook(); loadTrash() }

    // ---- the trash
    private val _trash = MutableStateFlow(SectionState<List<JsonObject>>())
    val trash = _trash.asStateFlow()

    fun loadTrash() {
        _trash.value = _trash.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val r = connector.getJson("trash?limit=100")
            _trash.value = SectionState(loading = false, error = failedOf(r), data = r.valueOrNull()?.rowsOf("items")?.objects().orEmpty())
        }
    }

    fun restoreFromTrash(trashId: Long) =
        officeWrite({ writers.post("trash/$trashId/restore", JsonObject()) }, "Restored.") { loadTrash(); loadBook(); loadJobs(force = true) }

    // ---- archived jobs: kept on the record, out of the lists; Reopen puts one back at the standing it had
    fun reopenJob(projectId: Int) =
        officeWrite({ writers.post("projects/$projectId/unarchive", JsonObject()) }, "Back on the active list.") { loadJobs(force = true) }

    fun archiveJob(projectId: Int) =
        officeWrite({ writers.post("projects/$projectId/archive", JsonObject()) }, "Archived - kept on the record.") { loadJobs(force = true) }

    // ---- booked visits (the record's appointments, with their ids - the calendar copy follows every change)
    private val _visits = MutableStateFlow(SectionState<List<JsonObject>>())
    val visits = _visits.asStateFlow()

    fun loadVisits() {
        _visits.value = _visits.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val r = connector.getJson("visits?days=60&past_days=7")
            _visits.value = SectionState(loading = false, error = failedOf(r), data = r.valueOrNull()?.rowsOf("visits")?.objects().orEmpty())
        }
    }

    /** Edit a visit: send only what changed; nothing is required. start is "YYYY-MM-DD HH:MM" on this phone's clock. */
    fun editVisit(id: Long, name: String?, phone: String?, address: String?, forWhat: String?, start: String?) {
        val body = JsonObject().apply {
            name?.let { addProperty("name", it) }; phone?.let { addProperty("phone", it) }
            address?.let { addProperty("address", it) }; forWhat?.let { addProperty("project", it) }
            start?.takeIf { it.isNotBlank() }?.let { addProperty("start", it) }
        }
        officeWrite({ writers.patch("visits/$id", body) }, "Visit updated.") { loadVisits() }
    }

    fun cancelVisit(id: Long) =
        officeWrite({ writers.post("visits/$id/cancel", JsonObject()) }, "Visit cancelled.") { loadVisits() }

    fun restoreVisit(id: Long) =
        officeWrite({ writers.post("visits/$id/restore", JsonObject()) }, "Visit booked again.") { loadVisits() }

    /** Sign this phone out: the server revokes the token, the phone forgets it, and the front door comes back. */
    fun signOut(activity: Activity) {
        viewModelScope.launch {
            runCatching { connector.signOut() }
            activity.recreate()
        }
    }
}
