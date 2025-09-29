package com.constructionmanager.data.database.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000f\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\r0\fH\'J\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\rH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00050\rH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u0018\u0010\u0012\u001a\u0004\u0018\u00010\t2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\r0\f2\u0006\u0010\u0014\u001a\u00020\u000fH\'J\u001c\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\r0\f2\u0006\u0010\u0016\u001a\u00020\u0005H\'J\u0016\u0010\u0017\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u0018\u001a\u00020\u00032\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\t0\rH\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u001c\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\r0\f2\u0006\u0010\u001c\u001a\u00020\u0005H\'J\u0016\u0010\u001d\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\n\u00a8\u0006\u001e"}, d2 = {"Lcom/constructionmanager/data/database/dao/MaterialDao;", "", "deactivateMaterial", "", "materialId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMaterial", "material", "Lcom/constructionmanager/data/database/entity/MaterialEntity;", "(Lcom/constructionmanager/data/database/entity/MaterialEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllActiveMaterials", "Lkotlinx/coroutines/flow/Flow;", "", "getAllCategories", "Lcom/constructionmanager/domain/model/MaterialCategory;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllSuppliers", "getMaterialById", "getMaterialsByCategory", "category", "getMaterialsBySupplier", "supplier", "insertMaterial", "insertMaterials", "materials", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchMaterials", "searchQuery", "updateMaterial", "app_debug"})
@androidx.room.Dao()
public abstract interface MaterialDao {
    
    @androidx.room.Query(value = "SELECT * FROM materials WHERE isActive = 1 ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.data.database.entity.MaterialEntity>> getAllActiveMaterials();
    
    @androidx.room.Query(value = "SELECT * FROM materials WHERE category = :category AND isActive = 1 ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.data.database.entity.MaterialEntity>> getMaterialsByCategory(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.domain.model.MaterialCategory category);
    
    @androidx.room.Query(value = "SELECT * FROM materials WHERE id = :materialId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMaterialById(@org.jetbrains.annotations.NotNull()
    java.lang.String materialId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.constructionmanager.data.database.entity.MaterialEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM materials WHERE name LIKE \'%\' || :searchQuery || \'%\' OR description LIKE \'%\' || :searchQuery || \'%\' AND isActive = 1")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.data.database.entity.MaterialEntity>> searchMaterials(@org.jetbrains.annotations.NotNull()
    java.lang.String searchQuery);
    
    @androidx.room.Query(value = "SELECT * FROM materials WHERE supplier = :supplier AND isActive = 1 ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.constructionmanager.data.database.entity.MaterialEntity>> getMaterialsBySupplier(@org.jetbrains.annotations.NotNull()
    java.lang.String supplier);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMaterial(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.data.database.entity.MaterialEntity material, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMaterials(@org.jetbrains.annotations.NotNull()
    java.util.List<com.constructionmanager.data.database.entity.MaterialEntity> materials, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateMaterial(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.data.database.entity.MaterialEntity material, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMaterial(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.data.database.entity.MaterialEntity material, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE materials SET isActive = 0 WHERE id = :materialId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deactivateMaterial(@org.jetbrains.annotations.NotNull()
    java.lang.String materialId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT DISTINCT category FROM materials WHERE isActive = 1 ORDER BY category ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllCategories(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<? extends com.constructionmanager.domain.model.MaterialCategory>> $completion);
    
    @androidx.room.Query(value = "SELECT DISTINCT supplier FROM materials WHERE isActive = 1 ORDER BY supplier ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllSuppliers(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion);
}