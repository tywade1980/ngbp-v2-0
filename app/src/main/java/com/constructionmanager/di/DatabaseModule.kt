package com.constructionmanager.di

import android.content.Context
import androidx.room.Room
import com.constructionmanager.data.database.ConstructionDatabase
import com.constructionmanager.data.database.dao.MaterialDao
import com.constructionmanager.data.database.dao.ProjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideConstructionDatabase(
        @ApplicationContext context: Context
    ): ConstructionDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ConstructionDatabase::class.java,
            ConstructionDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideProjectDao(database: ConstructionDatabase): ProjectDao {
        return database.projectDao()
    }
    
    @Provides
    fun provideMaterialDao(database: ConstructionDatabase): MaterialDao {
        return database.materialDao()
    }
}