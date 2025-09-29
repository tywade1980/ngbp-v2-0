package com.constructionmanager.ui.screens.materials;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0014\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BW\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0005\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\t\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\u0002\u0010\rJ\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u000f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u000f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\t0\u0005H\u00c6\u0003J\u000b\u0010\u001b\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\fH\u00c6\u0003J[\u0010\u001d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00052\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00c6\u0001J\u0013\u0010\u001e\u001a\u00020\u00032\b\u0010\u001f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010 \u001a\u00020!H\u00d6\u0001J\t\u0010\"\u001a\u00020\fH\u00d6\u0001R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0013R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000fR\u0013\u0010\n\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006#"}, d2 = {"Lcom/constructionmanager/ui/screens/materials/MaterialsUiState;", "", "isLoading", "", "materials", "", "Lcom/constructionmanager/domain/model/Material;", "filteredMaterials", "categories", "Lcom/constructionmanager/domain/model/MaterialCategory;", "selectedCategory", "error", "", "(ZLjava/util/List;Ljava/util/List;Ljava/util/List;Lcom/constructionmanager/domain/model/MaterialCategory;Ljava/lang/String;)V", "getCategories", "()Ljava/util/List;", "getError", "()Ljava/lang/String;", "getFilteredMaterials", "()Z", "getMaterials", "getSelectedCategory", "()Lcom/constructionmanager/domain/model/MaterialCategory;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "hashCode", "", "toString", "app_release"})
public final class MaterialsUiState {
    private final boolean isLoading = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.constructionmanager.domain.model.Material> materials = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.constructionmanager.domain.model.Material> filteredMaterials = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.constructionmanager.domain.model.MaterialCategory> categories = null;
    @org.jetbrains.annotations.Nullable()
    private final com.constructionmanager.domain.model.MaterialCategory selectedCategory = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String error = null;
    
    public MaterialsUiState(boolean isLoading, @org.jetbrains.annotations.NotNull()
    java.util.List<com.constructionmanager.domain.model.Material> materials, @org.jetbrains.annotations.NotNull()
    java.util.List<com.constructionmanager.domain.model.Material> filteredMaterials, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends com.constructionmanager.domain.model.MaterialCategory> categories, @org.jetbrains.annotations.Nullable()
    com.constructionmanager.domain.model.MaterialCategory selectedCategory, @org.jetbrains.annotations.Nullable()
    java.lang.String error) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.constructionmanager.domain.model.Material> getMaterials() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.constructionmanager.domain.model.Material> getFilteredMaterials() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.constructionmanager.domain.model.MaterialCategory> getCategories() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.constructionmanager.domain.model.MaterialCategory getSelectedCategory() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getError() {
        return null;
    }
    
    public MaterialsUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.constructionmanager.domain.model.Material> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.constructionmanager.domain.model.Material> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.constructionmanager.domain.model.MaterialCategory> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.constructionmanager.domain.model.MaterialCategory component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.constructionmanager.ui.screens.materials.MaterialsUiState copy(boolean isLoading, @org.jetbrains.annotations.NotNull()
    java.util.List<com.constructionmanager.domain.model.Material> materials, @org.jetbrains.annotations.NotNull()
    java.util.List<com.constructionmanager.domain.model.Material> filteredMaterials, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends com.constructionmanager.domain.model.MaterialCategory> categories, @org.jetbrains.annotations.Nullable()
    com.constructionmanager.domain.model.MaterialCategory selectedCategory, @org.jetbrains.annotations.Nullable()
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