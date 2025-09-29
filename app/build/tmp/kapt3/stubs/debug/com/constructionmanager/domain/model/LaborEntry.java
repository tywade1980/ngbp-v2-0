package com.constructionmanager.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b3\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u00b1\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\r\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0010\u0012\u0006\u0010\u0011\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0010\u0012\u0006\u0010\u0013\u001a\u00020\u0014\u0012\u0006\u0010\u0015\u001a\u00020\u0014\u0012\u0006\u0010\u0016\u001a\u00020\u0014\u0012\u0006\u0010\u0017\u001a\u00020\u0003\u0012\u0006\u0010\u0018\u001a\u00020\u0019\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u001c\u001a\u00020\u001d\u00a2\u0006\u0002\u0010\u001eJ\t\u0010;\u001a\u00020\u0003H\u00c6\u0003J\t\u0010<\u001a\u00020\u0010H\u00c6\u0003J\t\u0010=\u001a\u00020\u0010H\u00c6\u0003J\t\u0010>\u001a\u00020\u0010H\u00c6\u0003J\t\u0010?\u001a\u00020\u0014H\u00c6\u0003J\t\u0010@\u001a\u00020\u0014H\u00c6\u0003J\t\u0010A\u001a\u00020\u0014H\u00c6\u0003J\t\u0010B\u001a\u00020\u0003H\u00c6\u0003J\t\u0010C\u001a\u00020\u0019H\u00c6\u0003J\t\u0010D\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010E\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010F\u001a\u00020\u0003H\u00c6\u0003J\t\u0010G\u001a\u00020\u001dH\u00c6\u0003J\t\u0010H\u001a\u00020\u0003H\u00c6\u0003J\t\u0010I\u001a\u00020\u0003H\u00c6\u0003J\t\u0010J\u001a\u00020\u0003H\u00c6\u0003J\t\u0010K\u001a\u00020\tH\u00c6\u0003J\t\u0010L\u001a\u00020\u000bH\u00c6\u0003J\t\u0010M\u001a\u00020\rH\u00c6\u0003J\t\u0010N\u001a\u00020\rH\u00c6\u0003J\u00d3\u0001\u0010O\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\r2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00102\b\b\u0002\u0010\u0012\u001a\u00020\u00102\b\b\u0002\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00142\b\b\u0002\u0010\u0016\u001a\u00020\u00142\b\b\u0002\u0010\u0017\u001a\u00020\u00032\b\b\u0002\u0010\u0018\u001a\u00020\u00192\b\b\u0002\u0010\u001a\u001a\u00020\u00032\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u001c\u001a\u00020\u001dH\u00c6\u0001J\u0013\u0010P\u001a\u00020Q2\b\u0010R\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010S\u001a\u00020TH\u00d6\u0001J\t\u0010U\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\u000e\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010 R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010+R\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010 R\u0011\u0010\u001a\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010 R\u0011\u0010\u0012\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\"R\u0011\u0010\u0015\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010(R\u0011\u0010\u0018\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010 R\u0011\u0010\u0011\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010\"R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010&R\u0011\u0010\u001c\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u00106R\u0011\u0010\u0017\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010 R\u0011\u0010\u0016\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010(R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010 R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010 \u00a8\u0006V"}, d2 = {"Lcom/constructionmanager/domain/model/LaborEntry;", "", "id", "", "projectId", "workerId", "workerName", "laborCategoryId", "laborCategory", "Lcom/constructionmanager/domain/model/LaborCategory;", "date", "Lkotlinx/datetime/LocalDate;", "startTime", "Lkotlinx/datetime/LocalTime;", "endTime", "breakDuration", "", "regularHours", "overtimeHours", "hourlyRate", "Ljava/math/BigDecimal;", "overtimeRate", "totalCost", "taskDescription", "phase", "Lcom/constructionmanager/domain/model/ConstructionPhase;", "notes", "approvedBy", "status", "Lcom/constructionmanager/domain/model/LaborEntryStatus;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/constructionmanager/domain/model/LaborCategory;Lkotlinx/datetime/LocalDate;Lkotlinx/datetime/LocalTime;Lkotlinx/datetime/LocalTime;DDDLjava/math/BigDecimal;Ljava/math/BigDecimal;Ljava/math/BigDecimal;Ljava/lang/String;Lcom/constructionmanager/domain/model/ConstructionPhase;Ljava/lang/String;Ljava/lang/String;Lcom/constructionmanager/domain/model/LaborEntryStatus;)V", "getApprovedBy", "()Ljava/lang/String;", "getBreakDuration", "()D", "getDate", "()Lkotlinx/datetime/LocalDate;", "getEndTime", "()Lkotlinx/datetime/LocalTime;", "getHourlyRate", "()Ljava/math/BigDecimal;", "getId", "getLaborCategory", "()Lcom/constructionmanager/domain/model/LaborCategory;", "getLaborCategoryId", "getNotes", "getOvertimeHours", "getOvertimeRate", "getPhase", "()Lcom/constructionmanager/domain/model/ConstructionPhase;", "getProjectId", "getRegularHours", "getStartTime", "getStatus", "()Lcom/constructionmanager/domain/model/LaborEntryStatus;", "getTaskDescription", "getTotalCost", "getWorkerId", "getWorkerName", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class LaborEntry {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String projectId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String workerId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String workerName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String laborCategoryId = null;
    @org.jetbrains.annotations.NotNull()
    private final com.constructionmanager.domain.model.LaborCategory laborCategory = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.datetime.LocalDate date = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.datetime.LocalTime startTime = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.datetime.LocalTime endTime = null;
    private final double breakDuration = 0.0;
    private final double regularHours = 0.0;
    private final double overtimeHours = 0.0;
    @org.jetbrains.annotations.NotNull()
    private final java.math.BigDecimal hourlyRate = null;
    @org.jetbrains.annotations.NotNull()
    private final java.math.BigDecimal overtimeRate = null;
    @org.jetbrains.annotations.NotNull()
    private final java.math.BigDecimal totalCost = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String taskDescription = null;
    @org.jetbrains.annotations.NotNull()
    private final com.constructionmanager.domain.model.ConstructionPhase phase = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String notes = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String approvedBy = null;
    @org.jetbrains.annotations.NotNull()
    private final com.constructionmanager.domain.model.LaborEntryStatus status = null;
    
    public LaborEntry(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String projectId, @org.jetbrains.annotations.NotNull()
    java.lang.String workerId, @org.jetbrains.annotations.NotNull()
    java.lang.String workerName, @org.jetbrains.annotations.NotNull()
    java.lang.String laborCategoryId, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.LaborCategory laborCategory, @org.jetbrains.annotations.NotNull()
    kotlinx.datetime.LocalDate date, @org.jetbrains.annotations.NotNull()
    kotlinx.datetime.LocalTime startTime, @org.jetbrains.annotations.NotNull()
    kotlinx.datetime.LocalTime endTime, double breakDuration, double regularHours, double overtimeHours, @org.jetbrains.annotations.NotNull()
    java.math.BigDecimal hourlyRate, @org.jetbrains.annotations.NotNull()
    java.math.BigDecimal overtimeRate, @org.jetbrains.annotations.NotNull()
    java.math.BigDecimal totalCost, @org.jetbrains.annotations.NotNull()
    java.lang.String taskDescription, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.ConstructionPhase phase, @org.jetbrains.annotations.NotNull()
    java.lang.String notes, @org.jetbrains.annotations.Nullable()
    java.lang.String approvedBy, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.LaborEntryStatus status) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getProjectId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getWorkerId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getWorkerName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLaborCategoryId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.LaborCategory getLaborCategory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.datetime.LocalDate getDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.datetime.LocalTime getStartTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.datetime.LocalTime getEndTime() {
        return null;
    }
    
    public final double getBreakDuration() {
        return 0.0;
    }
    
    public final double getRegularHours() {
        return 0.0;
    }
    
    public final double getOvertimeHours() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.math.BigDecimal getHourlyRate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.math.BigDecimal getOvertimeRate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.math.BigDecimal getTotalCost() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTaskDescription() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.ConstructionPhase getPhase() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getNotes() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getApprovedBy() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.LaborEntryStatus getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final double component10() {
        return 0.0;
    }
    
    public final double component11() {
        return 0.0;
    }
    
    public final double component12() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.math.BigDecimal component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.math.BigDecimal component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.math.BigDecimal component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.ConstructionPhase component17() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component18() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component19() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.LaborEntryStatus component20() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.LaborCategory component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.datetime.LocalDate component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.datetime.LocalTime component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.datetime.LocalTime component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.domain.model.LaborEntry copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String projectId, @org.jetbrains.annotations.NotNull()
    java.lang.String workerId, @org.jetbrains.annotations.NotNull()
    java.lang.String workerName, @org.jetbrains.annotations.NotNull()
    java.lang.String laborCategoryId, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.LaborCategory laborCategory, @org.jetbrains.annotations.NotNull()
    kotlinx.datetime.LocalDate date, @org.jetbrains.annotations.NotNull()
    kotlinx.datetime.LocalTime startTime, @org.jetbrains.annotations.NotNull()
    kotlinx.datetime.LocalTime endTime, double breakDuration, double regularHours, double overtimeHours, @org.jetbrains.annotations.NotNull()
    java.math.BigDecimal hourlyRate, @org.jetbrains.annotations.NotNull()
    java.math.BigDecimal overtimeRate, @org.jetbrains.annotations.NotNull()
    java.math.BigDecimal totalCost, @org.jetbrains.annotations.NotNull()
    java.lang.String taskDescription, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.ConstructionPhase phase, @org.jetbrains.annotations.NotNull()
    java.lang.String notes, @org.jetbrains.annotations.Nullable()
    java.lang.String approvedBy, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.LaborEntryStatus status) {
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