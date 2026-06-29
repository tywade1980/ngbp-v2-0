package com.constructionmanager.ui.screens.labor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.data.cloud.CloudSync
import com.constructionmanager.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

data class LaborManagementUiState(
    val isLoading: Boolean = true,
    val recentLaborEntries: List<LaborEntry> = emptyList(),
    val workers: List<Worker> = emptyList(),
    val filteredWorkers: List<Worker> = emptyList(),
    val selectedTradeType: TradeType? = null,
    val selectedWorkerId: String? = null,
    val isTimeTracking: Boolean = false,
    val currentTrackingDuration: String = "00:00:00",
    val todaysTotalHours: Double = 0.0,
    val todaysTotalCost: Double = 0.0,
    val activeWorkersCount: Int = 0,
    val weeklyLaborCost: Double = 0.0,
    val monthlyLaborCost: Double = 0.0,
    val laborCostsByTrade: Map<String, Double> = emptyMap(),
    val hourlyRatesByTrade: Map<String, Double> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class LaborManagementViewModel @Inject constructor(
    private val cloudSync: CloudSync
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaborManagementUiState())
    val uiState: StateFlow<LaborManagementUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var elapsedSeconds = 0
    
    fun loadLaborData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load mock data for demonstration
                val mockWorkers = createMockWorkers()
                // Cloud workers (persisted) take precedence; sample workers fill out the demo roster.
                val cloudWorkers = cloudSync.pullWorkers().getOrDefault(emptyList())
                val mergedWorkers = (cloudWorkers + mockWorkers).distinctBy { it.id }
                val mockLaborEntries = createMockLaborEntries()
                // Persisted entries (tracked time) take precedence, newest first.
                val cloudEntries = cloudSync.pullLaborEntries().getOrDefault(emptyList())
                val mergedEntries = (cloudEntries + mockLaborEntries)
                    .distinctBy { it.id }
                    .sortedByDescending { it.date }
                val mockCostsByTrade = createMockCostsByTrade()
                val mockHourlyRates = createMockHourlyRates()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    workers = mergedWorkers,
                    filteredWorkers = mergedWorkers,
                    recentLaborEntries = mergedEntries,
                    todaysTotalHours = 64.5,
                    todaysTotalCost = 2580.0,
                    activeWorkersCount = 8,
                    weeklyLaborCost = 18060.0,
                    monthlyLaborCost = 72240.0,
                    laborCostsByTrade = mockCostsByTrade,
                    hourlyRatesByTrade = mockHourlyRates
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load labor data"
                )
            }
        }
    }
    
    fun filterByTradeType(tradeType: TradeType?) {
        val filtered = if (tradeType == null) {
            _uiState.value.workers
        } else {
            _uiState.value.workers.filter { it.tradeTypes.contains(tradeType) }
        }
        
        _uiState.value = _uiState.value.copy(
            selectedTradeType = tradeType,
            filteredWorkers = filtered
        )
    }
    
    fun startTimeTracking() {
        if (_uiState.value.isTimeTracking) return
        elapsedSeconds = 0
        _uiState.value = _uiState.value.copy(isTimeTracking = true, currentTrackingDuration = "00:00:00")
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                elapsedSeconds++
                _uiState.value = _uiState.value.copy(currentTrackingDuration = formatDuration(elapsedSeconds))
            }
        }
    }

    fun stopTimeTracking() {
        timerJob?.cancel()
        timerJob = null
        val seconds = elapsedSeconds
        _uiState.value = _uiState.value.copy(isTimeTracking = false)
        if (seconds > 0) logTrackedTime(seconds)
    }

    fun selectWorker(workerId: String?) {
        _uiState.value = _uiState.value.copy(selectedWorkerId = workerId)
    }

    /** Turns a tracked duration into a persisted LaborEntry attributed to the selected worker. */
    private fun logTrackedTime(seconds: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            val worker = state.workers.firstOrNull { it.id == state.selectedWorkerId }
                ?: state.workers.firstOrNull()
            val hours = seconds / 3600.0
            val rate = worker?.hourlyRate ?: BigDecimal.ZERO
            val overtimeRate = rate.multiply(BigDecimal("1.5"))
            val cost = rate.multiply(BigDecimal.valueOf(hours))
            val tz = TimeZone.currentSystemDefault()
            val nowInstant = Clock.System.now()
            val endLdt = nowInstant.toLocalDateTime(tz)
            val startLdt = nowInstant.minus(seconds.seconds).toLocalDateTime(tz)
            val entry = LaborEntry(
                id = "entry_${System.currentTimeMillis()}",
                projectId = "",
                workerId = worker?.id ?: "",
                workerName = worker?.let { "${it.firstName} ${it.lastName}".trim() }?.ifBlank { "Tracked time" }
                    ?: "Tracked time",
                laborCategoryId = "tracked",
                laborCategory = LaborCategory(
                    id = "tracked",
                    name = "Tracked Time",
                    tradeType = worker?.tradeTypes?.firstOrNull() ?: TradeType.GENERAL_LABOR,
                    skillLevel = worker?.skillLevel ?: SkillLevel.JOURNEYMAN,
                    hourlyRate = rate,
                    overtimeRate = overtimeRate,
                    description = "Time tracked in app"
                ),
                date = endLdt.date,
                startTime = startLdt.time,
                endTime = endLdt.time,
                regularHours = hours,
                hourlyRate = rate,
                overtimeRate = overtimeRate,
                totalCost = cost,
                taskDescription = "Tracked time",
                phase = ConstructionPhase.FRAMING,
                status = LaborEntryStatus.PENDING
            )
            cloudSync.pushLaborEntry(entry)
            _uiState.value = _uiState.value.copy(
                recentLaborEntries = listOf(entry) + _uiState.value.recentLaborEntries,
                todaysTotalHours = _uiState.value.todaysTotalHours + hours,
                todaysTotalCost = _uiState.value.todaysTotalCost + cost.toDouble()
            )
        }
    }

    /** Adds a worker to the roster (in-memory list) and mirrors it to Firestore. */
    fun addWorker(
        firstName: String,
        lastName: String,
        trade: TradeType,
        skillLevel: SkillLevel,
        hourlyRate: String,
        phone: String,
        email: String
    ) {
        if (firstName.isBlank() && lastName.isBlank()) return
        viewModelScope.launch {
            val rate = hourlyRate.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val worker = Worker(
                id = "worker_${System.currentTimeMillis()}",
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim(),
                phone = phone.trim(),
                tradeTypes = listOf(trade),
                skillLevel = skillLevel,
                certifications = emptyList(),
                hourlyRate = rate,
                hireDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            )
            val updatedWorkers = listOf(worker) + _uiState.value.workers
            val selected = _uiState.value.selectedTradeType
            _uiState.value = _uiState.value.copy(
                workers = updatedWorkers,
                filteredWorkers = if (selected == null) updatedWorkers
                else updatedWorkers.filter { it.tradeTypes.contains(selected) },
                activeWorkersCount = updatedWorkers.size
            )
            cloudSync.pushWorker(worker)
        }
    }

    private fun formatDuration(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }
    
    private fun createMockWorkers(): List<Worker> {
        return listOf(
            Worker(
                id = "worker_001",
                firstName = "John",
                lastName = "Smith",
                email = "john.smith@construction.com",
                phone = "(555) 123-4567",
                tradeTypes = listOf(TradeType.CARPENTER),
                skillLevel = SkillLevel.JOURNEYMAN,
                certifications = listOf(
                    Certification(
                        name = "OSHA 30",
                        issuingOrganization = "OSHA",
                        issueDate = kotlinx.datetime.LocalDate(2023, 1, 15),
                        expirationDate = kotlinx.datetime.LocalDate(2026, 1, 15),
                        certificationNumber = "OSHA30-2023-001"
                    )
                ),
                hourlyRate = java.math.BigDecimal("32.50"),
                hireDate = kotlinx.datetime.LocalDate(2022, 3, 1)
            ),
            Worker(
                id = "worker_002",
                firstName = "Maria",
                lastName = "Rodriguez",
                email = "maria.rodriguez@construction.com",
                phone = "(555) 234-5678",
                tradeTypes = listOf(TradeType.ELECTRICIAN),
                skillLevel = SkillLevel.MASTER,
                certifications = listOf(),
                hourlyRate = java.math.BigDecimal("45.00"),
                hireDate = kotlinx.datetime.LocalDate(2021, 6, 15)
            ),
            Worker(
                id = "worker_003",
                firstName = "David",
                lastName = "Johnson",
                email = "david.johnson@construction.com",
                phone = "(555) 345-6789",
                tradeTypes = listOf(TradeType.PLUMBER),
                skillLevel = SkillLevel.JOURNEYMAN,
                certifications = listOf(),
                hourlyRate = java.math.BigDecimal("38.75"),
                hireDate = kotlinx.datetime.LocalDate(2020, 9, 10)
            )
        )
    }
    
    private fun createMockLaborEntries(): List<LaborEntry> {
        return listOf(
            LaborEntry(
                id = "entry_001",
                projectId = "project_001",
                workerId = "worker_001",
                workerName = "John Smith",
                laborCategoryId = "cat_001",
                laborCategory = LaborCategory(
                    id = "cat_001",
                    name = "Framing Carpenter",
                    tradeType = TradeType.CARPENTER,
                    skillLevel = SkillLevel.JOURNEYMAN,
                    hourlyRate = java.math.BigDecimal("32.50"),
                    overtimeRate = java.math.BigDecimal("48.75"),
                    description = "Skilled framing carpentry work"
                ),
                date = kotlinx.datetime.LocalDate(2025, 9, 27),
                startTime = kotlinx.datetime.LocalTime(7, 0),
                endTime = kotlinx.datetime.LocalTime(15, 30),
                regularHours = 8.0,
                overtimeHours = 0.5,
                hourlyRate = java.math.BigDecimal("32.50"),
                overtimeRate = java.math.BigDecimal("48.75"),
                totalCost = java.math.BigDecimal("284.38"),
                taskDescription = "Framing second floor walls",
                phase = ConstructionPhase.FRAMING,
                status = LaborEntryStatus.APPROVED
            )
        )
    }
    
    private fun createMockCostsByTrade(): Map<String, Double> {
        return mapOf(
            "CARPENTER" to 15600.0,
            "ELECTRICIAN" to 12800.0,
            "PLUMBER" to 9200.0,
            "HVAC_TECHNICIAN" to 8400.0,
            "CONCRETE_FINISHER" to 6800.0,
            "DRYWALL_INSTALLER" to 5200.0
        )
    }
    
    private fun createMockHourlyRates(): Map<String, Double> {
        return mapOf(
            "CARPENTER" to 32.50,
            "ELECTRICIAN" to 45.00,
            "PLUMBER" to 38.75,
            "HVAC_TECHNICIAN" to 42.00,
            "CONCRETE_FINISHER" to 28.50,
            "DRYWALL_INSTALLER" to 26.00
        )
    }
}