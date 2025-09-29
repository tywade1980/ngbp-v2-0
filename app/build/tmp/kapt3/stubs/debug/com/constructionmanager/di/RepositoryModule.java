package com.constructionmanager.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\'J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\'\u00a8\u0006\u000f"}, d2 = {"Lcom/constructionmanager/di/RepositoryModule;", "", "()V", "bindAuthRepository", "Lcom/constructionmanager/domain/repository/AuthRepository;", "authRepositoryImpl", "Lcom/constructionmanager/data/repository/AuthRepositoryImpl;", "bindMaterialRepository", "Lcom/constructionmanager/domain/repository/MaterialRepository;", "materialRepositoryImpl", "Lcom/constructionmanager/data/repository/MaterialRepositoryImpl;", "bindProjectRepository", "Lcom/constructionmanager/domain/repository/ProjectRepository;", "projectRepositoryImpl", "Lcom/constructionmanager/data/repository/ProjectRepositoryImpl;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class RepositoryModule {
    
    public RepositoryModule() {
        super();
    }
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.constructionmanager.domain.repository.ProjectRepository bindProjectRepository(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.data.repository.ProjectRepositoryImpl projectRepositoryImpl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.constructionmanager.domain.repository.MaterialRepository bindMaterialRepository(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.data.repository.MaterialRepositoryImpl materialRepositoryImpl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.constructionmanager.domain.repository.AuthRepository bindAuthRepository(@org.jetbrains.annotations.NotNull()
    com.constructionmanager.data.repository.AuthRepositoryImpl authRepositoryImpl);
}