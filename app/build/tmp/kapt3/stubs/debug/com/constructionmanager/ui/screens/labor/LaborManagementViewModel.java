package com.constructionmanager.ui.screens.labor;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0014\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\r0\u000bH\u0002J\u0014\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\r0\u000bH\u0002J\u000e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010H\u0002J\u000e\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u0010H\u0002J\u0010\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017J\u0006\u0010\u0018\u001a\u00020\u0015J\u0006\u0010\u0019\u001a\u00020\u0015J\u0006\u0010\u001a\u001a\u00020\u0015R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\u001b"}, d2 = {"Lcom/constructionmanager/ui/screens/labor/LaborManagementViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/constructionmanager/ui/screens/labor/LaborManagementUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "createMockCostsByTrade", "", "", "", "createMockHourlyRates", "createMockLaborEntries", "", "Lcom/constructionmanager/domain/model/LaborEntry;", "createMockWorkers", "Lcom/constructionmanager/domain/model/Worker;", "filterByTradeType", "", "tradeType", "Lcom/constructionmanager/domain/model/TradeType;", "loadLaborData", "startTimeTracking", "stopTimeTracking", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LaborManagementViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.constructionmanager.ui.screens.labor.LaborManagementUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.constructionmanager.ui.screens.labor.LaborManagementUiState> uiState = null;
    
    @javax.inject.Inject()
    public LaborManagementViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.constructionmanager.ui.screens.labor.LaborManagementUiState> getUiState() {
        return null;
    }
    
    public final void loadLaborData() {
    }
    
    public final void filterByTradeType(@org.jetbrains.annotations.Nullable()
    com.constructionmanager.domain.model.TradeType tradeType) {
    }
    
    public final void startTimeTracking() {
    }
    
    public final void stopTimeTracking() {
    }
    
    private final java.util.List<com.constructionmanager.domain.model.Worker> createMockWorkers() {
        return null;
    }
    
    private final java.util.List<com.constructionmanager.domain.model.LaborEntry> createMockLaborEntries() {
        return null;
    }
    
    private final java.util.Map<java.lang.String, java.lang.Double> createMockCostsByTrade() {
        return null;
    }
    
    private final java.util.Map<java.lang.String, java.lang.Double> createMockHourlyRates() {
        return null;
    }
}