package com.constructionmanager.di

import com.constructionmanager.data.repository.AuthRepositoryImpl
import com.constructionmanager.data.repository.MaterialRepositoryImpl
import com.constructionmanager.data.repository.ProjectRepositoryImpl
import com.constructionmanager.domain.repository.AuthRepository
import com.constructionmanager.domain.repository.MaterialRepository
import com.constructionmanager.domain.repository.ProjectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindProjectRepository(
        projectRepositoryImpl: ProjectRepositoryImpl
    ): ProjectRepository
    
    @Binds
    @Singleton
    abstract fun bindMaterialRepository(
        materialRepositoryImpl: MaterialRepositoryImpl
    ): MaterialRepository
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}