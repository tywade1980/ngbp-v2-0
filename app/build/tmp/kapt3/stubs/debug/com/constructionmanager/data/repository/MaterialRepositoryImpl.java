package com.constructionmanager.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0012\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0014\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000bH\u0016J\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\fH\u0096@\u00a2\u0006\u0002\u0010\u0010J\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\b0\fH\u0096@\u00a2\u0006\u0002\u0010\u0010J\u0018\u0010\u0012\u001a\u0004\u0018\u00010\r2\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u001c\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000b2\u0006\u0010\u0014\u001a\u00020\u000fH\u0016J\u001c\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000b2\u0006\u0010\u0016\u001a\u00020\bH\u0016J\u000e\u0010\u0017\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0018\u001a\u00020\u00062\u0006\u0010\u0019\u001a\u00020\rH\u0096@\u00a2\u0006\u0002\u0010\u001aJ\u001c\u0010\u001b\u001a\u00020\u00062\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u0096@\u00a2\u0006\u0002\u0010\u001dJ\u001c\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000b2\u0006\u0010\u001f\u001a\u00020\bH\u0016J\u0016\u0010 \u001a\u00020\u00062\u0006\u0010\u0019\u001a\u00020\rH\u0096@\u00a2\u0006\u0002\u0010\u001aJ\f\u0010!\u001a\u00020\r*\u00020\"H\u0002J\f\u0010#\u001a\u00020\"*\u00020\rH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/constructionmanager/data/repository/MaterialRepositoryImpl;", "Lcom/constructionmanager/domain/repository/MaterialRepository;", "materialDao", "Lcom/constructionmanager/data/database/dao/MaterialDao;", "(Lcom/constructionmanager/data/database/dao/MaterialDao;)V", "deactivateMaterial", "", "materialId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllActiveMaterials", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/constructionmanager/domain/model/Material;", "getAllCategories", "Lcom/constructionmanager/domain/model/MaterialCategory;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllSuppliers", "getMaterialById", "getMaterialsByCategory", "category", "getMaterialsBySupplier", "supplier", "initializeSeedData", "insertMaterial", "material", "(Lcom/constructionmanager/domain/model/Material;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertMaterials", "materials", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchMaterials", "searchQuery", "updateMaterial", "toDomainModel", "Lcom/constructionmanager/data/database/entity/MaterialEntity;", "toEntity", "app_debug"})
public final class MaterialRepositoryImpl implements com.constructionmanager.domain.repository.MaterialRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.constructionmanager.data.database.dao.MaterialDao materialDao = null;
    
    @javax.inject.Inject()
    public MaterialRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.data.database.dao.MaterialDao materialDao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Material>> getAllActiveMaterials() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Material>> getMaterialsByCategory(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.MaterialCategory category) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getMaterialById(@org.jetbrains.annotations.NotNull()
    java.lang.String materialId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.constructionmanager.domain.model.Material> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Material>> searchMaterials(@org.jetbrains.annotations.NotNull()
    java.lang.String searchQuery) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.domain.model.Material>> getMaterialsBySupplier(@org.jetbrains.annotations.NotNull()
    java.lang.String supplier) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object insertMaterial(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Material material, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object insertMaterials(@org.jetbrains.annotations.NotNull()
    java.util.List<com.constructionmanager.domain.model.Material> materials, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateMaterial(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.Material material, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deactivateMaterial(@org.jetbrains.annotations.NotNull()
    java.lang.String materialId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getAllCategories(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<? extends com.constructionmanager.domain.model.MaterialCategory>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getAllSuppliers(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object initializeSeedData(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final com.constructionmanager.domain.model.Material toDomainModel(com.constructionmanager.data.database.entity.MaterialEntity $this$toDomainModel) {
        return null;
    }
    
    private final com.constructionmanager.data.database.entity.MaterialEntity toEntity(com.constructionmanager.domain.model.Material $this$toEntity) {
        return null;
    }
}