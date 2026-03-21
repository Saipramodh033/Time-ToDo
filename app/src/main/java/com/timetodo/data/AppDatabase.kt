package com.timetodo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.timetodo.data.dao.GroupDao
import com.timetodo.data.dao.TaskDao
import com.timetodo.data.dao.TaskExecutionDao
import com.timetodo.data.entity.Group
import com.timetodo.data.entity.Task
import com.timetodo.data.entity.TaskExecution

@Database(
    entities = [Task::class, Group::class, TaskExecution::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun groupDao(): GroupDao
    abstract fun taskExecutionDao(): TaskExecutionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_manager_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
