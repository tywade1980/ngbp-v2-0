package com.constructionmanager.data.database.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\n\bg\u0018\u00002\u00020\u0001J\u0019\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0019\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\fH\'J\u001b\u0010\u000e\u001a\u0004\u0018\u00010\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\nJ\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0013J\u001c\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\f2\u0006\u0010\u0011\u001a\u00020\u0012H\'J!\u0010\u0015\u001a\u0004\u0018\u00010\u00162\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00120\rH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0018J!\u0010\u0019\u001a\u0004\u0018\u00010\u00162\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00120\rH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0018J\u0019\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u001f\u0010\u001b\u001a\u00020\u00032\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00050\rH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0018J\u001c\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\f2\u0006\u0010\u001e\u001a\u00020\tH\'J\u0019\u0010\u001f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006 "}, d2 = {"Lcom/constructionmanager/data/database/dao/ProjectDao;", "", "deleteProject", "", "project", "Lcom/constructionmanager/data/database/entity/ProjectEntity;", "(Lcom/constructionmanager/data/database/entity/ProjectEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteProjectById", "projectId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllProjects", "Lkotlinx/coroutines/flow/Flow;", "", "getProjectById", "getProjectCountByStatus", "", "status", "Lcom/constructionmanager/domain/model/ProjectStatus;", "(Lcom/constructionmanager/domain/model/ProjectStatus;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getProjectsByStatus", "getTotalBudgetByStatuses", "", "statuses", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTotalCurrentCostByStatuses", "insertProject", "insertProjects", "projects", "searchProjects", "searchQuery", "updateProject", "app_debug"})
@androidx.room.Dao
public abstract interface ProjectDao {
    
    @androidx.room.Query(value = "SELECT * FROM projects ORDER BY updatedAt DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.data.database.entity.ProjectEntity>> getAllProjects();
    
    @androidx.room.Query(value = "SELECT * FROM projects WHERE status = :status ORDER BY updatedAt DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.data.database.entity.ProjectEntity>> getProjectsByStatus(@org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.ProjectStatus status);
    
    @androidx.room.Query(value = "SELECT * FROM projects WHERE id = :projectId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getProjectById(@org.jetbrains.annotations.NotNull
    java.lang.String projectId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.constructionmanager.data.database.entity.ProjectEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM projects WHERE name LIKE \'%\' || :searchQuery || \'%\' OR address LIKE \'%\' || :searchQuery || \'%\' OR clientName LIKE \'%\' || :searchQuery || \'%\'")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.data.database.entity.ProjectEntity>> searchProjects(@org.jetbrains.annotations.NotNull
    java.lang.String searchQuery);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertProject(@org.jetbrains.annotations.NotNull
    com.constructionmanager.data.database.entity.ProjectEntity project, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertProjects(@org.jetbrains.annotations.NotNull
    java.util.List<com.constructionmanager.data.database.entity.ProjectEntity> projects, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateProject(@org.jetbrains.annotations.NotNull
    com.constructionmanager.data.database.entity.ProjectEntity project, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteProject(@org.jetbrains.annotations.NotNull
    com.constructionmanager.data.database.entity.ProjectEntity project, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM projects WHERE id = :projectId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteProjectById(@org.jetbrains.annotations.NotNull
    java.lang.String projectId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM projects WHERE status = :status")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getProjectCountByStatus(@org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.ProjectStatus status, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "SELECT SUM(CAST(totalBudget AS REAL)) FROM projects WHERE status IN (:statuses)")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getTotalBudgetByStatuses(@org.jetbrains.annotations.NotNull
    java.util.List<? extends com.constructionmanager.domain.model.ProjectStatus> statuses, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Double> $completion);
    
    @androidx.room.Query(value = "SELECT SUM(CAST(currentCost AS REAL)) FROM projects WHERE status IN (:statuses)")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getTotalCurrentCostByStatuses(@org.jetbrains.annotations.NotNull
    java.util.List<? extends com.constructionmanager.domain.model.ProjectStatus> statuses, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Double> $completion);
}