package com.constructionmanager.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\b\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\bH&J\u0018\u0010\n\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u000b\u001a\u00020\fH\u00a6@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u00a6@\u00a2\u0006\u0002\u0010\u0012J\u001c\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\b2\u0006\u0010\u0010\u001a\u00020\u0011H&J\u001c\u0010\u0014\u001a\u00020\u00152\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00110\tH\u00a6@\u00a2\u0006\u0002\u0010\u0017J\u001c\u0010\u0018\u001a\u00020\u00152\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00110\tH\u00a6@\u00a2\u0006\u0002\u0010\u0017J\u0016\u0010\u0019\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\b2\u0006\u0010\u001b\u001a\u00020\fH&J\u0016\u0010\u001c\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u001d"}, d2 = {"Lcom/constructionmanager/domain/repository/ProjectRepository;", "", "deleteProject", "", "project", "Lcom/constructionmanager/domain/model/Project;", "(Lcom/constructionmanager/domain/model/Project;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllProjects", "Lkotlinx/coroutines/flow/Flow;", "", "getProjectById", "projectId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getProjectCountByStatus", "", "status", "Lcom/constructionmanager/domain/model/ProjectStatus;", "(Lcom/constructionmanager/domain/model/ProjectStatus;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getProjectsByStatus", "getTotalBudgetByStatuses", "", "statuses", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTotalCurrentCostByStatuses", "insertProject", "searchProjects", "searchQuery", "updateProject", "app_release"})
public abstract interface ProjectRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Project>> getAllProjects();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Project>> getProjectsByStatus(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.ProjectStatus status);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getProjectById(@org.jetbrains.annotations.NotNull()
    java.lang.String projectId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.constructionmanager.domain.model.Project> $completion);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Project>> searchProjects(@org.jetbrains.annotations.NotNull()
    java.lang.String searchQuery);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertProject(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Project project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProject(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Project project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteProject(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Project project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getProjectCountByStatus(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.ProjectStatus status, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTotalBudgetByStatuses(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends com.constructionmanager.domain.model.ProjectStatus> statuses, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Double> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTotalCurrentCostByStatuses(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends com.constructionmanager.domain.model.ProjectStatus> statuses, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Double> $completion);
}