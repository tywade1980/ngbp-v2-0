package com.constructionmanager.ui.screens.projects;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0000\u001a&\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001aD\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\u00052\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\b\b\u0002\u0010\r\u001a\u00020\u000eH\u0007\u001a.\u0010\u000f\u001a\u00020\u00012\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a@\u0010\u0012\u001a\u00020\u00012\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a\u0016\u0010\u0015\u001a\u00020\u00012\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00050\u0017H\u0003\u00a8\u0006\u0018"}, d2 = {"ActionButton", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "text", "", "onClick", "Lkotlin/Function0;", "ProjectDetailsScreen", "projectId", "onNavigateBack", "onNavigateToDocumentation", "onNavigateToWorkflows", "viewModel", "Lcom/constructionmanager/ui/screens/projects/ProjectDetailsViewModel;", "ProjectHeaderCard", "project", "Lcom/constructionmanager/domain/model/Project;", "QuickActionsSection", "onAddTimeEntry", "onAddExpense", "RecentActivityCard", "activities", "", "app_debug"})
public final class ProjectDetailsScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ProjectDetailsScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String projectId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToDocumentation, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToWorkflows, @org.jetbrains.annotations.NotNull()
    com.constructionmanager.ui.screens.projects.ProjectDetailsViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void ProjectHeaderCard(com.constructionmanager.domain.model.Project project, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToDocumentation, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToWorkflows) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void QuickActionsSection(kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToDocumentation, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToWorkflows, kotlin.jvm.functions.Function0<kotlin.Unit> onAddTimeEntry, kotlin.jvm.functions.Function0<kotlin.Unit> onAddExpense) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void ActionButton(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String text, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void RecentActivityCard(java.util.List<java.lang.String> activities) {
    }
}