package org.getscol.scol_chuker

import org.getscol.scol_chuker.data.db.DatabaseBuilderFactory
import org.getscol.scol_chuker.di.chuckerModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatform

object CmpChucker {
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true

        val existingKoin = KoinPlatform.getKoinOrNull()
        val module = chuckerModule(DatabaseBuilderFactory())
        existingKoin?.loadModules(listOf(module))
    }
}
