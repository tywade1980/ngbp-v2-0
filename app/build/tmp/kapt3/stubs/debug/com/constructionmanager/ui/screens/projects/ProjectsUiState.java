package com.constructionmanager.ui.screens.projects;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0012\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BG\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u000f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u000b\u0010\u0018\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\u000b\u0010\u0019\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003JK\u0010\u001a\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000bH\u00c6\u0001J\u0013\u0010\u001b\u001a\u00020\u00032\b\u0010\u001c\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001d\u001a\u00020\u001eH\u00d6\u0001J\t\u0010\u001f\u001a\u00020\u000bH\u00d6\u0001R\u0013\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0011R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0010R\u0013\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014\u00a8\u0006 "}, d2 = {"Lcom/constructionmanager/ui/screens/projects/ProjectsUiState;", "", "isLoading", "", "projects", "", "Lcom/constructionmanager/domain/model/Project;", "filteredProjects", "selectedStatus", "Lcom/constructionmanager/domain/model/ProjectStatus;", "error", "", "(ZLjava/util/List;Ljava/util/List;Lcom/constructionmanager/domain/model/ProjectStatus;Ljava/lang/String;)V", "getError", "()Ljava/lang/String;", "getFilteredProjects", "()Ljava/util/List;", "()Z", "getProjects", "getSelectedStatus", "()Lcom/constructionmanager/domain/model/ProjectStatus;", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class ProjectsUiState {
    private final boolean isLoading = false;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.constructionmanager.domain.model.Project> projects = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.constructionmanager.domain.model.Project> filteredProjects = null;
    @org.jetbrains.annotations.Nullable
    private final com.constructionmanager.domain.model.ProjectStatus selectedStatus = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String error = null;
    
    public ProjectsUiState(boolean isLoading, @org.jetbrains.annotations.NotNull
    java.util.List<com.constructionmanager.domain.model.Project> projects, @org.jetbrains.annotations.NotNull
    java.util.List<com.constructionmanager.domain.model.Project> filteredProjects, @org.jetbrains.annotations.Nullable
    com.constructionmanager.domain.model.ProjectStatus selectedStatus, @org.jetbrains.annotations.Nullable
    java.lang.String error) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.constructionmanager.domain.model.Project> getProjects() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.constructionmanager.domain.model.Project> getFilteredProjects() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.constructionmanager.domain.model.ProjectStatus getSelectedStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getError() {
        return null;
    }
    
    public ProjectsUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.constructionmanager.domain.model.Project> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.constructionmanager.domain.model.Project> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.constructionmanager.domain.model.ProjectStatus component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.constructionmanager.ui.screens.projects.ProjectsUiState copy(boolean isLoading, @org.jetbrains.annotations.NotNull
    java.util.List<com.constructionmanager.domain.model.Project> projects, @org.jetbrains.annotations.NotNull
    java.util.List<com.constructionmanager.domain.model.Project> filteredProjects, @org.jetbrains.annotations.Nullable
    com.constructionmanager.domain.model.ProjectStatus selectedStatus, @org.jetbrains.annotations.Nullable
    java.lang.String error) {
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