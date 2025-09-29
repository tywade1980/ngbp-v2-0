package com.constructionmanager.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0012\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bH&J\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\tH\u00a6@\u00a2\u0006\u0002\u0010\rJ\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00050\tH\u00a6@\u00a2\u0006\u0002\u0010\rJ\u0018\u0010\u000f\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010\u0011\u001a\u00020\fH&J\u001c\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010\u0013\u001a\u00020\u0005H&J\u000e\u0010\u0014\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\nH\u00a6@\u00a2\u0006\u0002\u0010\u0017J\u001c\u0010\u0018\u001a\u00020\u00032\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u00a6@\u00a2\u0006\u0002\u0010\u001aJ\u001c\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010\u001c\u001a\u00020\u0005H&J\u0016\u0010\u001d\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\nH\u00a6@\u00a2\u0006\u0002\u0010\u0017\u00a8\u0006\u001e"}, d2 = {"Lcom/constructionmanager/domain/repository/MaterialRepository;", "", "deactivateMaterial", "", "materialId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllActiveMaterials", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/constructionmanager/domain/model/Material;", "getAllCategories", "Lcom/constructionmanager/domain/model/MaterialCategory;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllSuppliers", "getMaterialById", "getMaterialsByCategory", "category", "getMaterialsBySupplier", "supplier", "initializeSeedData", "insertMaterial", "material", "(Lcom/constructionmanager/domain/model/Material;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertMaterials", "materials", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchMaterials", "searchQuery", "updateMaterial", "app_release"})
public abstract interface MaterialRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Material>> getAllActiveMaterials();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Material>> getMaterialsByCategory(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.MaterialCategory category);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMaterialById(@org.jetbrains.annotations.NotNull()
    java.lang.String materialId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.constructionmanager.domain.model.Material> $completion);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Material>> searchMaterials(@org.jetbrains.annotations.NotNull()
    java.lang.String searchQuery);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Material>> getMaterialsBySupplier(@org.jetbrains.annotations.NotNull()
    java.lang.String supplier);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMaterial(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Material material, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMaterials(@org.jetbrains.annotations.NotNull()
    java.util.List<com.constructionmanager.domain.model.Material> materials, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateMaterial(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Material material, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deactivateMaterial(@org.jetbrains.annotations.NotNull()
    java.lang.String materialId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllCategories(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<? extends com.constructionmanager.domain.model.MaterialCategory>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllSuppliers(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object initializeSeedData(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}