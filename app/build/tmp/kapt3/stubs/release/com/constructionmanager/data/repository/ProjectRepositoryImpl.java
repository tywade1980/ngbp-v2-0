package com.constructionmanager.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0014\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u000bH\u0016J\u0018\u0010\r\u001a\u0004\u0018\u00010\b2\u0006\u0010\u000e\u001a\u00020\u000fH\u0096@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014H\u0096@\u00a2\u0006\u0002\u0010\u0015J\u001c\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u000b2\u0006\u0010\u0013\u001a\u00020\u0014H\u0016J\u001c\u0010\u0017\u001a\u00020\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00140\fH\u0096@\u00a2\u0006\u0002\u0010\u001aJ\u001c\u0010\u001b\u001a\u00020\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00140\fH\u0096@\u00a2\u0006\u0002\u0010\u001aJ\u0016\u0010\u001c\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u001c\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u000b2\u0006\u0010\u001e\u001a\u00020\u000fH\u0016J\u0016\u0010\u001f\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\f\u0010 \u001a\u00020\b*\u00020!H\u0002J\f\u0010\"\u001a\u00020!*\u00020\bH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/constructionmanager/data/repository/ProjectRepositoryImpl;", "Lcom/constructionmanager/domain/repository/ProjectRepository;", "projectDao", "Lcom/constructionmanager/data/database/dao/ProjectDao;", "(Lcom/constructionmanager/data/database/dao/ProjectDao;)V", "deleteProject", "", "project", "Lcom/constructionmanager/domain/model/Project;", "(Lcom/constructionmanager/domain/model/Project;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllProjects", "Lkotlinx/coroutines/flow/Flow;", "", "getProjectById", "projectId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getProjectCountByStatus", "", "status", "Lcom/constructionmanager/domain/model/ProjectStatus;", "(Lcom/constructionmanager/domain/model/ProjectStatus;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getProjectsByStatus", "getTotalBudgetByStatuses", "", "statuses", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTotalCurrentCostByStatuses", "insertProject", "searchProjects", "searchQuery", "updateProject", "toDomainModel", "Lcom/constructionmanager/data/database/entity/ProjectEntity;", "toEntity", "app_release"})
public final class ProjectRepositoryImpl implements com.constructionmanager.domain.repository.ProjectRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.constructionmanager.data.database.dao.ProjectDao projectDao = null;
    
    @javax.inject.Inject()
    public ProjectRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.data.database.dao.ProjectDao projectDao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Project>> getAllProjects() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Project>> getProjectsByStatus(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.ProjectStatus status) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getProjectById(@org.jetbrains.annotations.NotNull()
    java.lang.String projectId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.constructionmanager.domain.model.Project> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Project>> searchProjects(@org.jetbrains.annotations.NotNull()
    java.lang.String searchQuery) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object insertProject(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Project project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateProject(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Project project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteProject(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Project project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getProjectCountByStatus(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.ProjectStatus status, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getTotalBudgetByStatuses(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends com.constructionmanager.domain.model.ProjectStatus> statuses, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Double> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getTotalCurrentCostByStatuses(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends com.constructionmanager.domain.model.ProjectStatus> statuses, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Double> $completion) {
        return null;
    }
    
    private final com.constructionmanager.domain.model.Project toDomainModel(com.constructionmanager.data.database.entity.ProjectEntity $this$toDomainModel) {
        return null;
    }
    
    private final com.constructionmanager.data.database.entity.ProjectEntity toEntity(com.constructionmanager.domain.model.Project $this$toEntity) {
        return null;
    }
}