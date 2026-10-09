package com.constructionmanager.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.constructionmanager.data.database.dao.MaterialDao
import com.constructionmanager.data.database.dao.ProjectDao
import com.constructionmanager.data.database.entity.MaterialEntity
import com.constructionmanager.data.database.entity.ProjectEntity
import com.constructionmanager.data.database.entity.DatabaseConverters

@Database(
    entities = [
        ProjectEntity::class,
        MaterialEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class ConstructionDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    abstract fun materialDao(): MaterialDao
    
    companion object {
        const val DATABASE_NAME = "construction_manager_database"
    }
}