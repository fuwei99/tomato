package com.tomato.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, TaskListEntity::class, PomodoroSessionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class TomatoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskListDao(): TaskListDao
    abstract fun sessionDao(): SessionDao

    companion object {
        fun build(context: Context): TomatoDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                TomatoDatabase::class.java,
                "tomato.db"
            ).build()
    }
}
