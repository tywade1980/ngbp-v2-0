package com.constructionmanager.ui.screens.onboarding

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.BuildConfig
import com.constructionmanager.data.caroline.CarolineConnector
import com.constructionmanager.data.caroline.CarolineEndpoint
import com.constructionmanager.data.caroline.ConnectorResult
import com.constructionmanager.data.caroline.ConnectorSettings
import com.constructionmanager.data.subscriber.LauncherClient
import com.constructionmanager.data.subscriber.LauncherResult
import com.constructionmanager.data.subscriber.LaunchStatus
import com.constructionmanager.data.subscriber.SubscriberIntake
import com.constructionmanager.data.subscriber.SubscriberStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URI
import java.util.TimeZone
import javax.inject.Inject

enum class OnboardingPane { WELCOME, FORM, STATUS, CONNECT, DONE }

/** The intake as the form holds it — the same fields the subscribe page collects. */
data class IntakeFormState(
    val businessName: String = "",
    val ownerName: String = "",
    val ownerCell: String = "",
    val email: String = "",
    val areaCode: String = "",
    val timezone: String = TimeZone.getDefault().id,
    val tradeBlurb: String = "",
    val greeting: String = "",
    val legalName: String = "",
    val ein: String = "",
    val entityType: String = "LLC",
    val companyType: String = "private",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zip: String = "",
    val website: String = "",
    val industry: String = "REAL ESTATE"
) {
    fun toIntake() = SubscriberIntake(
        businessName = businessName.trim(), ownerName = ownerName.trim(),
        ownerCell = ownerCell.trim(), email = email.trim(), areaCode = areaCode.trim(),
        timezone = timezone.trim(), tradeBlurb = tradeBlurb.trim(), greeting = greeting.trim(),
        legalName = legalName.trim(), ein = ein.trim(), entityType = entityType,
        companyType = companyType, street = street.trim(), city = city.trim(),
        state = state.trim(), zip = zip.trim(), website = website.trim(), industry = industry
    )
}

data class OnboardingUiState(
    val pane: OnboardingPane = OnboardingPane.WELCOME,
    val form: IntakeFormState = IntakeFormState(),
    val submitting: Boolean = false,
    val submitError: String? = null,
    val launch: LaunchStatus? = null,
    val statusError: String? = null,
    val paired: Boolean = false,
    val dismissed: Boolean = false,
    val boundOfficeUrl: String? = null,
    val pairCode: String = "",
    val pairLabel: String = "Owner's phone",
    val pairing: Boolean = false,
    val pairNote: String? = null
)

/**
 * The subscriber front door's brain: intake -> platform launch -> watch it
 * build -> bind this phone to the tenant's OWN Caroline -> pair.
 *
 * Resume is the default, not a feature: the launch token lives in
 * [SubscriberStore], so closing the app mid-launch reopens on the status
 * pane, still watching the same launch.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val launcher: LauncherClient,
    private val store: SubscriberStore,
    private val connector: CarolineConnector,
    private val connectorSettings: ConnectorSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    init {
        val token = store.launchToken
        _uiState.update {
            it.copy(
                paired = connector.isPaired,
                dismissed = store.skipped,
                boundOfficeUrl = store.boundOfficeUrl,
                pane = when {
                    token != null && store.launchCompleted -> OnboardingPane.CONNECT
                    token != null -> OnboardingPane.STATUS
                    else -> OnboardingPane.WELCOME
                }
            )
        }
        if (token != null && !store.launchCompleted) startPolling()
        if (token != null && store.launchCompleted) refreshStatus()
    }

    fun updateForm(form: IntakeFormState) = _uiState.update { it.copy(form = form, submitError = null) }

    fun startIntake() = _uiState.update { it.copy(pane = OnboardingPane.FORM) }

    fun backToWelcome() = _uiState.update { it.copy(pane = OnboardingPane.WELCOME) }

    /** The owner's escape hatch: this phone already belongs to a running Caroline. */
    fun skip() {
        store.skipped = true
        _uiState.update { it.copy(dismissed = true) }
    }

    fun submit() {
        val form = _uiState.value.form
        val missing = listOf(
            "business name" to form.businessName, "owner name" to form.ownerName,
            "owner cell" to form.ownerCell, "email" to form.email, "area code" to form.areaCode,
            "EIN (tax id)" to form.ein, "street address" to form.street,
            "city" to form.city, "state" to form.state, "ZIP" to form.zip
        ).filter { it.second.isBlank() }.map { it.first }
        if (missing.isNotEmpty()) {
            _uiState.update { it.copy(submitError = "Still needed: ${missing.joinToString(", ")}.") }
            return
        }
        _uiState.update { it.copy(submitting = true, submitError = null) }
        viewModelScope.launch {
            when (val r = launcher.submit(form.toIntake())) {
                is LauncherResult.Ok -> {
                    store.launchToken = r.value.token
                    store.businessName = form.businessName.trim()
                    _uiState.update { it.copy(submitting = false, pane = OnboardingPane.STATUS) }
                    startPolling()
                }
                is LauncherResult.Failed ->
                    _uiState.update { it.copy(submitting = false, submitError = r.message) }
            }
        }
    }

    private fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                refreshStatus()
                val launch = _uiState.value.launch
                if (launch != null && launch.isTerminal) break
                delay(POLL_MS)
            }
        }
    }

    fun refreshStatus() {
        val token = store.launchToken ?: return
        viewModelScope.launch {
            when (val r = launcher.status(token)) {
                is LauncherResult.Ok -> {
                    _uiState.update { it.copy(launch = r.value, statusError = null) }
                    if (r.value.isDone) saveBinding(r.value)
                }
                is LauncherResult.Failed -> _uiState.update { it.copy(statusError = r.message) }
            }
        }
    }

    /**
     * The launch landed: this phone's Caroline is now the TENANT's, on the
     * tenant's VM. The office URL is derived from the confirmation's own CRM
     * URL — system-issued, never typed, never the platform's address.
     */
    private fun saveBinding(launch: LaunchStatus) {
        val conf = launch.confirmation ?: return
        if (store.launchCompleted) return
        val officeUrl = officeUrlFor(conf.crmUrl) ?: return
        store.boundOfficeUrl = officeUrl
        store.boundCrmUrl = conf.crmUrl
        store.businessName = launch.businessName
        store.businessNumber = conf.businessNumber
        store.launchCompleted = true
        connectorSettings.baseUrl = officeUrl
        _uiState.update { it.copy(boundOfficeUrl = officeUrl) }
    }

    fun goConnect() = _uiState.update { it.copy(pane = OnboardingPane.CONNECT, pairNote = null) }

    fun backToStatus() = _uiState.update { it.copy(pane = OnboardingPane.STATUS) }

    fun setPairCode(code: String) = _uiState.update { it.copy(pairCode = code) }

    fun setPairLabel(label: String) = _uiState.update { it.copy(pairLabel = label) }

    /**
     * Pair this phone with the tenant's Caroline, with a one-time code the
     * owner makes in their own Office. The server address is already the
     * tenant's (set at binding) and stays the tenant's whatever the outcome —
     * a mistyped code is not a reason to point the phone anywhere else.
     */
    fun pair() {
        val officeUrl = store.boundOfficeUrl
        if (officeUrl == null) {
            _uiState.update { it.copy(pairNote = "Your system is not bound to this phone yet — wait for the launch to finish.") }
            return
        }
        val code = _uiState.value.pairCode.trim()
        if (code.isEmpty()) {
            _uiState.update { it.copy(pairNote = "Type the one-time code from your Office.") }
            return
        }
        connectorSettings.baseUrl = officeUrl
        _uiState.update { it.copy(pairing = true, pairNote = null) }
        viewModelScope.launch {
            val r = connector.pair(code, _uiState.value.pairLabel, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME)
            val note = when (r) {
                is ConnectorResult.Ok -> "Paired. This phone now talks to your Caroline."
                is ConnectorResult.Failed -> "Not paired: ${r.error.message}"
                is ConnectorResult.Queued -> "Not paired."
            }
            _uiState.update {
                it.copy(
                    pairing = false,
                    paired = connector.isPaired,
                    pairNote = note,
                    pane = if (connector.isPaired) OnboardingPane.DONE else OnboardingPane.CONNECT
                )
            }
        }
    }

    companion object {
        const val POLL_MS = 10_000L

        /** https://<their VM>/app -> https://<their VM>/api/ (the connector's base for that tenant). */
        fun officeUrlFor(crmUrl: String): String? {
            val uri = try { URI(crmUrl) } catch (e: Exception) { return null }
            val host = uri.host ?: return null
            val origin = "${uri.scheme ?: "https"}://$host"
            return when (val checked = CarolineEndpoint.normalize(origin)) {
                is CarolineEndpoint.Checked.Ok -> checked.url
                is CarolineEndpoint.Checked.Refused -> null
            }
        }
    }
}
