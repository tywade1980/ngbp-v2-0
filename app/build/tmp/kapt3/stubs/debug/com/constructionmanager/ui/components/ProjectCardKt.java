package com.constructionmanager.ui.components;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000>\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a(\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u0007\u001a\u0010\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\nH\u0003\u001a\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0002\u001a\u0010\u0010\u000f\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002\u001a\u0010\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002\u00a8\u0006\u0015"}, d2 = {"ProjectCard", "", "project", "Lcom/constructionmanager/domain/model/Project;", "onClick", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "ProjectStatusChip", "status", "Lcom/constructionmanager/domain/model/ProjectStatus;", "formatCurrency", "", "amount", "Ljava/math/BigDecimal;", "formatPhase", "phase", "Lcom/constructionmanager/domain/model/ConstructionPhase;", "formatProjectType", "type", "Lcom/constructionmanager/domain/model/ProjectType;", "app_debug"})
public final class ProjectCardKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void ProjectCard(@org.jetbrains.annotations.NotNull
    com.constructionmanager.domain.model.Project project, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProjectStatusChip(com.constructionmanager.domain.model.ProjectStatus status) {
    }
    
    private static final java.lang.String formatPhase(com.constructionmanager.domain.model.ConstructionPhase phase) {
        return null;
    }
    
    private static final java.lang.String formatProjectType(com.constructionmanager.domain.model.ProjectType type) {
        return null;
    }
    
    private static final java.lang.String formatCurrency(java.math.BigDecimal amount) {
        return null;
    }
}