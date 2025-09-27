package com.constructionmanager.ui.screens.labor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constructionmanager.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LaborManagementUiState(
    val isLoading: Boolean = true,
    val recentLaborEntries: List<LaborEntry> = emptyList(),
    val workers: List<Worker> = emptyList(),
    val filteredWorkers: List<Worker> = emptyList(),
    val selectedTradeType: TradeType? = null,
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
    // Repository dependencies would be injected here
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LaborManagementUiState())
    val uiState: StateFlow<LaborManagementUiState> = _uiState.asStateFlow()
    
    fun loadLaborData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load mock data for demonstration
                val mockWorkers = createMockWorkers()
                val mockLaborEntries = createMockLaborEntries()
                val mockCostsByTrade = createMockCostsByTrade()
                val mockHourlyRates = createMockHourlyRates()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    workers = mockWorkers,
                    filteredWorkers = mockWorkers,
                    recentLaborEntries = mockLaborEntries,
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
        _uiState.value = _uiState.value.copy(isTimeTracking = true)
        // Start timer logic would go here
    }
    
    fun stopTimeTracking() {
        _uiState.value = _uiState.value.copy(isTimeTracking = false)
        // Stop timer and save entry logic would go here
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