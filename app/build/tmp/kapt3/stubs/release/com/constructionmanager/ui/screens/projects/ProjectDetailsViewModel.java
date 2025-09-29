package com.constructionmanager.ui.screens.projects;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u0016\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u00112\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u000e\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0012R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0016"}, d2 = {"Lcom/constructionmanager/ui/screens/projects/ProjectDetailsViewModel;", "Landroidx/lifecycle/ViewModel;", "projectRepository", "Lcom/constructionmanager/domain/repository/ProjectRepository;", "(Lcom/constructionmanager/domain/repository/ProjectRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/constructionmanager/ui/screens/projects/ProjectDetailsUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "calculatePhaseProgress", "", "project", "Lcom/constructionmanager/domain/model/Project;", "getRecentActivities", "", "", "loadProject", "", "projectId", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ProjectDetailsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.constructionmanager.domain.repository.ProjectRepository projectRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.constructionmanager.ui.screens.projects.ProjectDetailsUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.constructionmanager.ui.screens.projects.ProjectDetailsUiState> uiState = null;
    
    @javax.inject.Inject()
    public ProjectDetailsViewModel(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.repository.ProjectRepository projectRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.constructionmanager.ui.screens.projects.ProjectDetailsUiState> getUiState() {
        return null;
    }
    
    public final void loadProject(@org.jetbrains.annotations.NotNull()
    java.lang.String projectId) {
    }
    
    private final float calculatePhaseProgress(com.constructionmanager.domain.model.Project project) {
        return 0.0F;
    }
    
    private final java.util.List<java.lang.String> getRecentActivities(com.constructionmanager.domain.model.Project project) {
        return null;
    }
}