package org.example.scol_chuker

import android.content.Context
import org.example.scol_chuker.data.db.DatabaseBuilderFactory
import org.example.scol_chuker.di.chuckerModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

object CmpChucker {

    private fun koinModule(context: Context): Module =
        chuckerModule(DatabaseBuilderFactory(context.applicationContext))

    fun init(context: Context) {
        // If Koin is already running (host app started it), load our module into it.
        val existingKoin = GlobalContext.getOrNull()
        val module = koinModule(context)
        if (existingKoin != null) {
            existingKoin.loadModules(listOf(module))
        } else {
            startKoin {
                androidContext(context.applicationContext)
                modules(module)
            }
        }
    }

}
