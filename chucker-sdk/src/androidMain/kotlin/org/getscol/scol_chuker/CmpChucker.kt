package org.getscol.scol_chuker

import android.content.Context
import org.getscol.scol_chuker.data.db.DatabaseBuilderFactory
import org.getscol.scol_chuker.di.chuckerModule
import org.koin.core.context.GlobalContext

object CmpChucker {
    fun init(context: Context) {
        val existingKoin = GlobalContext.getOrNull()
        val module = chuckerModule(DatabaseBuilderFactory(context.applicationContext))
        existingKoin?.loadModules(listOf(module))
    }
}