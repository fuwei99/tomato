package com.tomato.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, TaskListEntity::class, PomodoroSessionEntity::class],
    version = 2,                 // v1 -> v2：任务表加类型/计时/高级设置字段，会话表加 mode/breakSec
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
            )
                // demo 阶段（还没有真实用户数据）：结构变更直接重建库，
                // 启动后 seedIfEmpty() 会重新写入种子数据。
                // 正式发版前必须改为显式 Migration。
                .fallbackToDestructiveMigration()
                .build()
    }
}
