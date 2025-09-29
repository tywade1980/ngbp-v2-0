package com.constructionmanager.ui.screens.dashboard;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u001e\b\u0086\b\u0018\u00002\u00020\u0001Bs\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\b\u0012\b\b\u0002\u0010\n\u001a\u00020\b\u0012\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f\u0012\u0014\b\u0002\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00050\u000f\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\u0002\u0010\u0013J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\t\u0010#\u001a\u00020\u0005H\u00c6\u0003J\t\u0010$\u001a\u00020\u0005H\u00c6\u0003J\t\u0010%\u001a\u00020\bH\u00c6\u0003J\t\u0010&\u001a\u00020\bH\u00c6\u0003J\t\u0010\'\u001a\u00020\bH\u00c6\u0003J\u000f\u0010(\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u00c6\u0003J\u0015\u0010)\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00050\u000fH\u00c6\u0003J\u000b\u0010*\u001a\u0004\u0018\u00010\u0012H\u00c6\u0003Jw\u0010+\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\b2\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0014\b\u0002\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00050\u000f2\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u00c6\u0001J\u0013\u0010,\u001a\u00020\u00032\b\u0010-\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010.\u001a\u00020\u0005H\u00d6\u0001J\t\u0010/\u001a\u00020\u0012H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0015R\u0011\u0010\t\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u001bR\u0011\u0010\n\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0018R\u001d\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00050\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0018\u00a8\u00060"}, d2 = {"Lcom/constructionmanager/ui/screens/dashboard/DashboardUiState;", "", "isLoading", "", "activeProjectsCount", "", "completedProjectsCount", "totalBudget", "", "currentCosts", "onSchedulePercentage", "recentProjects", "", "Lcom/constructionmanager/domain/model/Project;", "phaseDistribution", "", "Lcom/constructionmanager/domain/model/ConstructionPhase;", "error", "", "(ZIIDDDLjava/util/List;Ljava/util/Map;Ljava/lang/String;)V", "getActiveProjectsCount", "()I", "getCompletedProjectsCount", "getCurrentCosts", "()D", "getError", "()Ljava/lang/String;", "()Z", "getOnSchedulePercentage", "getPhaseDistribution", "()Ljava/util/Map;", "getRecentProjects", "()Ljava/util/List;", "getTotalBudget", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class DashboardUiState {
    private final boolean isLoading = false;
    private final int activeProjectsCount = 0;
    private final int completedProjectsCount = 0;
    private final double totalBudget = 0.0;
    private final double currentCosts = 0.0;
    private final double onSchedulePercentage = 0.0;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.constructionmanager.domain.model.Project> recentProjects = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<com.constructionmanager.domain.model.ConstructionPhase, java.lang.Integer> phaseDistribution = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String error = null;
    
    public DashboardUiState(boolean isLoading, int activeProjectsCount, int completedProjectsCount, double totalBudget, double currentCosts, double onSchedulePercentage, @org.jetbrains.annotations.NotNull()
    java.util.List<com.constructionmanager.domain.model.Project> recentProjects, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.constructionmanager.domain.model.ConstructionPhase, java.lang.Integer> phaseDistribution, @org.jetbrains.annotations.Nullable()
    java.lang.String error) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    public final int getActiveProjectsCount() {
        return 0;
    }
    
    public final int getCompletedProjectsCount() {
        return 0;
    }
    
    public final double getTotalBudget() {
        return 0.0;
    }
    
    public final double getCurrentCosts() {
        return 0.0;
    }
    
    public final double getOnSchedulePercentage() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.constructionmanager.domain.model.Project> getRecentProjects() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.constructionmanager.domain.model.ConstructionPhase, java.lang.Integer> getPhaseDistribution() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getError() {
        return null;
    }
    
    public DashboardUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    public final double component4() {
        return 0.0;
    }
    
    public final double component5() {
        return 0.0;
    }
    
    public final double component6() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.constructionmanager.domain.model.Project> component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<com.constructionmanager.domain.model.ConstructionPhase, java.lang.Integer> component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.ui.screens.dashboard.DashboardUiState copy(boolean isLoading, int activeProjectsCount, int completedProjectsCount, double totalBudget, double currentCosts, double onSchedulePercentage, @org.jetbrains.annotations.NotNull()
    java.util.List<com.constructionmanager.domain.model.Project> recentProjects, @org.jetbrains.annotations.NotNull()
    java.util.Map<com.constructionmanager.domain.model.ConstructionPhase, java.lang.Integer> phaseDistribution, @org.jetbrains.annotations.Nullable()
    java.lang.String error) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}