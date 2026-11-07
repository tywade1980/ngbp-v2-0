package com.constructionmanager.ui.screens.hocc

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.data.caroline.CarolineConnector
import com.constructionmanager.data.caroline.ConnectorResult
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/*
 * THE COMPANY PLAN (HOCC, Nov 7 2026) - every live job's plan at once, and tonight's office work across all of them.
 *
 * The job room shows one job; a missed day and tonight's office work are about ALL of them (Tyler, Nov 7 2026: "across all
 * project not just the one"; "in all projects all job room all job boards"). This reads the office's home (the active jobs, the
 * leads, the week's appointments), each active job's schedule, and the estimates, and lays each job out with the plan engine.
 *
 * Caroline's prep: a tap (or, with "start on its own" on, opening the app) sends her one framed request per item through her
 * assist thread - the same door as "Tell Caroline" - asking for preliminary work only (Tonight.prepRequest). What was sent, and
 * what he marked handled tonight, are remembered on this phone for the day; her work itself lands on the record.
 */

data class CompanyPlans(
    val today: LocalDate,
    val jobs: List<Tonight.JobPlanRow>,
    val leads: List<JsonObject>,
    val estimates: List<JsonObject>,
    val appointments: List<JsonObject>
) {
    val plans: List<Plan> get() = jobs.mapNotNull { it.plan }
}

@HiltViewModel
class PlanBoardViewModel @Inject constructor(private val connector: CarolineConnector) : ViewModel() {

    private val _state = MutableStateFlow<SectionState<CompanyPlans>?>(null)
    val state = _state.asStateFlow()

    private val _handled = MutableStateFlow<Set<String>>(emptySet())
    val handled = _handled.asStateFlow()

    /** item key -> the time her prep was sent (HH:mm), today only. */
    private val _prep = MutableStateFlow<Map<String, String>>(emptyMap())
    val prep = _prep.asStateFlow()

    private val _autoPrep = MutableStateFlow(false)
    val autoPrep = _autoPrep.asStateFlow()

    private val _note = MutableStateFlow<String?>(null)
    val note = _note.asStateFlow()

    private var app: Context? = null
    private var loading = false

    private fun prefs() = app?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun dayKey(k: String) = k + "_" + LocalDate.now()

    fun load(context: Context, force: Boolean = false) {
        app = context.applicationContext
        val p = prefs()
        _handled.value = p?.getStringSet(dayKey(KEY_HANDLED), emptySet())?.toSet() ?: emptySet()
        _prep.value = p?.getStringSet(dayKey(KEY_PREP), emptySet())?.mapNotNull { it.split('=', limit = 2).takeIf { x -> x.size == 2 }?.let { x -> x[0] to x[1] } }?.toMap() ?: emptyMap()
        _autoPrep.value = p?.getBoolean(KEY_AUTO, false) ?: false
        if (loading || (!force && _state.value?.data != null && _state.value?.data?.today == LocalDate.now())) return
        loading = true
        _state.value = SectionState(loading = true, data = _state.value?.data)
        viewModelScope.launch {
            val today = LocalDate.now()
            val homeR = connector.getJson("hub/home")
            val home = homeR.valueOrNull()?.asObjOrNull()
            val active = home?.arrOf("active_projects")?.objects().orEmpty()
            val leads = home?.objOf("leads")?.arrOf("jobs")?.objects().orEmpty()
            val appts = home?.arrOf("appointments_week")?.objects().orEmpty()
            val estimates = connector.getJson("estimates").valueOrNull().rowsOf("estimates").objects()
            val rows = active.map { p ->
                val id = p.num("id")?.toInt() ?: 0
                val s = connector.getJson("hub/project/$id/schedule").valueOrNull()?.asObjOrNull()
                val r = JobPlan.fromSchedule(id, p.str("name") ?: "Job #$id", s)
                Tonight.JobPlanRow(p, r.input?.let { JobPlan.compute(it, today) }, r.problem, needsStart = s?.boolOf("needs_start") == true)
            }
            _state.value = SectionState(loading = false,
                error = (homeR as? ConnectorResult.Failed)?.error?.message,
                data = CompanyPlans(today, rows, leads, estimates, appts))
            loading = false
            if (_autoPrep.value) startAll(items())
        }
    }

    fun items(): List<TonightItem> {
        val d = _state.value?.data ?: return emptyList()
        return Tonight.build(d.leads, d.estimates, d.jobs, d.appointments, d.today, _handled.value)
    }

    /** This job's plan replaced by the room's fresher read, so the room and the company view never disagree. */
    fun plansWith(fresh: Plan?): List<Plan> {
        val all = _state.value?.data?.plans.orEmpty()
        return if (fresh == null) all else all.filter { it.input.projectId != fresh.input.projectId } + fresh
    }

    fun markHandled(key: String) {
        _handled.value = _handled.value + key
        prefs()?.edit()?.putStringSet(dayKey(KEY_HANDLED), _handled.value)?.apply()
    }

    fun setAutoPrep(on: Boolean) {
        _autoPrep.value = on
        prefs()?.edit()?.putBoolean(KEY_AUTO, on)?.apply()
        if (on) startAll(items())
    }

    fun startPrep(item: TonightItem) = startAll(listOf(item))

    /** Send Caroline her prep for each item not already sent today, one at a time. */
    fun startAll(items: List<TonightItem>) {
        val todo = items.filter { it.key !in _prep.value }
        if (todo.isEmpty()) return
        viewModelScope.launch {
            var sent = 0
            for (item in todo) {
                when (val r = connector.sendText(Tonight.prepRequest(item))) {
                    is ConnectorResult.Ok, is ConnectorResult.Queued -> {
                        _prep.value = _prep.value + (item.key to LocalTime.now().format(HM))
                        sent++
                    }
                    is ConnectorResult.Failed -> { _note.value = "Caroline didn't take the prep for \"${item.title}\": ${r.error.message}"; break }
                }
                delay(400)
            }
            prefs()?.edit()?.putStringSet(dayKey(KEY_PREP), _prep.value.map { (k, v) -> "$k=$v" }.toSet())?.apply()
            if (sent > 0 && _note.value == null) _note.value = if (sent == 1) "Caroline has the prep. Her work lands on the job." else "Caroline has the prep for $sent items. Her work lands on each job."
        }
    }

    fun clearNote() { _note.value = null }

    companion object {
        const val PREFS = "hocc_tonight"
        const val KEY_HANDLED = "handled"
        const val KEY_PREP = "prep"
        const val KEY_AUTO = "auto_prep"
        private val HM = DateTimeFormatter.ofPattern("h:mm a")
    }
}
