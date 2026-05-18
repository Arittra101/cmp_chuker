package org.example.scol_chuker

import android.content.Context
import org.example.scol_chuker.CmpChucker.koinModule
import org.example.scol_chuker.data.db.DatabaseDriverFactory
import org.example.scol_chuker.di.chuckerModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Public entry point for the CMP Chucker SDK.
 *
 * Call this once in your Application class (or equivalent):
 *
 *   CmpChucker.init(context)
 */
object CmpChucker {

    /**
     * Initialises the SDK.
     *
     * - If your app already uses Koin, call [koinModule] and `loadModules(listOf(CmpChucker.koinModule(context)))`.
     * - If your app does NOT use Koin, call this and Chucker will start its own isolated Koin
     *   instance automatically.
     *
     * @param context Any Android [Context] — the SDK always uses [applicationContext] internally.
     */
    /** Koin module for apps that manage their own [startKoin]. */
    fun koinModule(context: Context): Module =
        chuckerModule(DatabaseDriverFactory(context.applicationContext))

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
