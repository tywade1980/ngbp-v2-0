package com.constructionmanager.data.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\'\u0018\u0000 \u00072\u00020\u0001:\u0002\u0007\bB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\t"}, d2 = {"Lcom/constructionmanager/data/database/ConstructionDatabase;", "Landroidx/room/RoomDatabase;", "()V", "materialDao", "Lcom/constructionmanager/data/database/dao/MaterialDao;", "projectDao", "Lcom/constructionmanager/data/database/dao/ProjectDao;", "Companion", "DatabaseCallback", "app_release"})
@androidx.room.Database(entities = {com.constructionmanager.data.database.entity.ProjectEntity.class, com.constructionmanager.data.database.entity.MaterialEntity.class}, version = 1, exportSchema = true)
@androidx.room.TypeConverters(value = {com.constructionmanager.data.database.entity.DatabaseConverters.class})
public abstract class ConstructionDatabase extends androidx.room.RoomDatabase {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DATABASE_NAME = "construction_manager_database";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.constructionmanager.data.database.ConstructionDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final com.constructionmanager.data.database.ConstructionDatabase.Companion Companion = null;
    
    public ConstructionDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.constructionmanager.data.database.dao.ProjectDao projectDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.constructionmanager.data.database.dao.MaterialDao materialDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/constructionmanager/data/database/ConstructionDatabase$Companion;", "", "()V", "DATABASE_NAME", "", "INSTANCE", "Lcom/constructionmanager/data/database/ConstructionDatabase;", "getDatabase", "context", "Landroid/content/Context;", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.constructionmanager.data.database.ConstructionDatabase getDatabase(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/constructionmanager/data/database/ConstructionDatabase$DatabaseCallback;", "Landroidx/room/RoomDatabase$Callback;", "()V", "app_release"})
    static final class DatabaseCallback extends androidx.room.RoomDatabase.Callback {
        
        public DatabaseCallback() {
            super();
        }
    }
}