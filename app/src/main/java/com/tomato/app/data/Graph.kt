package com.tomato.app.data

import android.content.Context
import com.tomato.app.data.db.TomatoDatabase

/**
 * 极简服务定位器（M0 阶段够用，后面可换成 Hilt/Koin）。
 * 必须在 Application / MainActivity 启动前调用 [init]。
 */
object Graph {

    lateinit var database: TomatoDatabase
        private set

    val repository: TomatoRepository by lazy { TomatoRepository(database) }

    fun init(context: Context) {
        if (::database.isInitialized) return
        database = TomatoDatabase.build(context)
    }

    /** 首启写入与截图一致的种子数据，之后完全由用户数据接管 */
    suspend fun ensureSeed() = repository.seedIfEmpty()
}
