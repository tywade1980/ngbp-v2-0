package com.constructionmanager.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b3\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u00ad\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\u0003\u0012\u0006\u0010\n\u001a\u00020\u0003\u0012\u0006\u0010\u000b\u001a\u00020\u0003\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u0012\u0006\u0010\u0012\u001a\u00020\u0011\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0011\u0012\u0006\u0010\u0014\u001a\u00020\u0015\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0015\u0012\u0006\u0010\u0017\u001a\u00020\u0018\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u0003\u0012\u0006\u0010\u001a\u001a\u00020\u001b\u0012\u0006\u0010\u001c\u001a\u00020\u001b\u00a2\u0006\u0002\u0010\u001dJ\t\u00109\u001a\u00020\u0003H\u00c6\u0003J\t\u0010:\u001a\u00020\rH\u00c6\u0003J\t\u0010;\u001a\u00020\u000fH\u00c6\u0003J\t\u0010<\u001a\u00020\u0011H\u00c6\u0003J\t\u0010=\u001a\u00020\u0011H\u00c6\u0003J\u000b\u0010>\u001a\u0004\u0018\u00010\u0011H\u00c6\u0003J\t\u0010?\u001a\u00020\u0015H\u00c6\u0003J\t\u0010@\u001a\u00020\u0015H\u00c6\u0003J\t\u0010A\u001a\u00020\u0018H\u00c6\u0003J\t\u0010B\u001a\u00020\u0003H\u00c6\u0003J\t\u0010C\u001a\u00020\u001bH\u00c6\u0003J\t\u0010D\u001a\u00020\u0003H\u00c6\u0003J\t\u0010E\u001a\u00020\u001bH\u00c6\u0003J\t\u0010F\u001a\u00020\u0003H\u00c6\u0003J\t\u0010G\u001a\u00020\u0003H\u00c6\u0003J\t\u0010H\u001a\u00020\u0003H\u00c6\u0003J\t\u0010I\u001a\u00020\u0003H\u00c6\u0003J\t\u0010J\u001a\u00020\u0003H\u00c6\u0003J\t\u0010K\u001a\u00020\u0003H\u00c6\u0003J\t\u0010L\u001a\u00020\u0003H\u00c6\u0003J\u00d3\u0001\u0010M\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u00112\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00112\b\b\u0002\u0010\u0014\u001a\u00020\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u00152\b\b\u0002\u0010\u0017\u001a\u00020\u00182\b\b\u0002\u0010\u0019\u001a\u00020\u00032\b\b\u0002\u0010\u001a\u001a\u00020\u001b2\b\b\u0002\u0010\u001c\u001a\u00020\u001bH\u00c6\u0001J\u0013\u0010N\u001a\u00020O2\b\u0010P\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010Q\u001a\u00020RH\u00d6\u0001J\t\u0010S\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0013\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010!R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010!R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010!R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010!R\u0011\u0010\u001a\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u0011\u0010\u0016\u001a\u00020\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010)R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010+R\u0011\u0010\u0012\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u001fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010!R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010!R\u0011\u0010\u0019\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010!R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010\u001fR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010!R\u0011\u0010\u0017\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00105R\u0011\u0010\u0014\u001a\u00020\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010)R\u0011\u0010\u001c\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010\'R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010!\u00a8\u0006T"}, d2 = {"Lcom/constructionmanager/domain/model/Project;", "", "id", "", "name", "address", "city", "state", "zipCode", "clientName", "clientEmail", "clientPhone", "projectType", "Lcom/constructionmanager/domain/model/ProjectType;", "currentPhase", "Lcom/constructionmanager/domain/model/ConstructionPhase;", "startDate", "Lkotlinx/datetime/LocalDate;", "estimatedEndDate", "actualEndDate", "totalBudget", "Ljava/math/BigDecimal;", "currentCost", "status", "Lcom/constructionmanager/domain/model/ProjectStatus;", "notes", "createdAt", "Lkotlinx/datetime/LocalDateTime;", "updatedAt", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/constructionmanager/domain/model/ProjectType;Lcom/constructionmanager/domain/model/ConstructionPhase;Lkotlinx/datetime/LocalDate;Lkotlinx/datetime/LocalDate;Lkotlinx/datetime/LocalDate;Ljava/math/BigDecimal;Ljava/math/BigDecimal;Lcom/constructionmanager/domain/model/ProjectStatus;Ljava/lang/String;Lkotlinx/datetime/LocalDateTime;Lkotlinx/datetime/LocalDateTime;)V", "getActualEndDate", "()Lkotlinx/datetime/LocalDate;", "getAddress", "()Ljava/lang/String;", "getCity", "getClientEmail", "getClientName", "getClientPhone", "getCreatedAt", "()Lkotlinx/datetime/LocalDateTime;", "getCurrentCost", "()Ljava/math/BigDecimal;", "getCurrentPhase", "()Lcom/constructionmanager/domain/model/ConstructionPhase;", "getEstimatedEndDate", "getId", "getName", "getNotes", "getProjectType", "()Lcom/constructionmanager/domain/model/ProjectType;", "getStartDate", "getState", "getStatus", "()Lcom/constructionmanager/domain/model/ProjectStatus;", "getTotalBudget", "getUpdatedAt", "getZipCode", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class Project {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String name = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String address = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String city = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String state = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String zipCode = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String clientName = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String clientEmail = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String clientPhone = null;
    @org.jetbrains.annotations.NotNull
    private final com.constructionmanager.domain.model.ProjectType projectType = null;
    @org.jetbrains.annotations.NotNull
    private final com.constructionmanager.domain.model.ConstructionPhase currentPhase = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.datetime.LocalDate startDate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.datetime.LocalDate estimatedEndDate = null;
    @org.jetbrains.annotations.Nullable
    private final kotlinx.datetime.LocalDate actualEndDate = null;
    @org.jetbrains.annotations.NotNull
    private final java.math.BigDecimal totalBudget = null;
    @org.jetbrains.annotations.NotNull
    private final java.math.BigDecimal currentCost = null;
    @org.jetbrains.annotations.NotNull
    private final com.constructionmanager.domain.model.ProjectStatus status = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String notes = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.datetime.LocalDateTime createdAt = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.datetime.LocalDateTime updatedAt = null;
    
    public Project(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    java.lang.String address, @org.jetbrains.annotations.NotNull
    java.lang.String city, @org.jetbrains.annotations.NotNull
    java.lang.String state, @org.jetbrains.annotations.NotNull
    java.lang.String zipCode, @org.jetbrains.annotations.NotNull
    java.lang.String clientName, @org.jetbrains.annotations.NotNull
    java.lang.String clientEmail, @org.jetbrains.annotations.NotNull
    java.lang.String clientPhone, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.ProjectType projectType, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.ConstructionPhase currentPhase, @org.jetbrains.annotations.NotNull
    kotlinx.datetime.LocalDate startDate, @org.jetbrains.annotations.NotNull
    kotlinx.datetime.LocalDate estimatedEndDate, @org.jetbrains.annotations.Nullable
    kotlinx.datetime.LocalDate actualEndDate, @org.jetbrains.annotations.NotNull
    java.math.BigDecimal totalBudget, @org.jetbrains.annotations.NotNull
    java.math.BigDecimal currentCost, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.ProjectStatus status, @org.jetbrains.annotations.NotNull
    java.lang.String notes, @org.jetbrains.annotations.NotNull
    kotlinx.datetime.LocalDateTime createdAt, @org.jetbrains.annotations.NotNull
    kotlinx.datetime.LocalDateTime updatedAt) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCity() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getZipCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getClientName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getClientEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getClientPhone() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.ProjectType getProjectType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.ConstructionPhase getCurrentPhase() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.datetime.LocalDate getStartDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.datetime.LocalDate getEstimatedEndDate() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final kotlinx.datetime.LocalDate getActualEndDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.math.BigDecimal getTotalBudget() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.math.BigDecimal getCurrentCost() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.ProjectStatus getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getNotes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.datetime.LocalDateTime getCreatedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.datetime.LocalDateTime getUpdatedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.ProjectType component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.ConstructionPhase component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.datetime.LocalDate component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.datetime.LocalDate component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final kotlinx.datetime.LocalDate component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.math.BigDecimal component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.math.BigDecimal component16() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.ProjectStatus component17() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component18() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.datetime.LocalDateTime component19() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.datetime.LocalDateTime component20() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.domain.model.Project copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    java.lang.String address, @org.jetbrains.annotations.NotNull
    java.lang.String city, @org.jetbrains.annotations.NotNull
    java.lang.String state, @org.jetbrains.annotations.NotNull
    java.lang.String zipCode, @org.jetbrains.annotations.NotNull
    java.lang.String clientName, @org.jetbrains.annotations.NotNull
    java.lang.String clientEmail, @org.jetbrains.annotations.NotNull
    java.lang.String clientPhone, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.ProjectType projectType, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.ConstructionPhase currentPhase, @org.jetbrains.annotations.NotNull
    kotlinx.datetime.LocalDate startDate, @org.jetbrains.annotations.NotNull
    kotlinx.datetime.LocalDate estimatedEndDate, @org.jetbrains.annotations.Nullable
    kotlinx.datetime.LocalDate actualEndDate, @org.jetbrains.annotations.NotNull
    java.math.BigDecimal totalBudget, @org.jetbrains.annotations.NotNull
    java.math.BigDecimal currentCost, @org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.ProjectStatus status, @org.jetbrains.annotations.NotNull
    java.lang.String notes, @org.jetbrains.annotations.NotNull
    kotlinx.datetime.LocalDateTime createdAt, @org.jetbrains.annotations.NotNull
    kotlinx.datetime.LocalDateTime updatedAt) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}