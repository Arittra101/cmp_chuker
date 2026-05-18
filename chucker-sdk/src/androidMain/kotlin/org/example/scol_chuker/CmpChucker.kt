package org.example.scol_chuker

import android.content.Context
import org.example.scol_chuker.data.db.DatabaseDriverFactory
import org.example.scol_chuker.di.chuckerModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

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
     * - If your app already uses Koin, just load [chuckerModule] in your own [startKoin] block
     *   (`modules(appModule, chuckerModule)`) and skip calling this.
     * - If your app does NOT use Koin, call this and Chucker will start its own isolated Koin
     *   instance automatically.
     *
     * @param context Any Android [Context] — the SDK always uses [applicationContext] internally.
     */
    fun init(context: Context) {
        // If Koin is already running (host app started it), load our module into it.
        val existingKoin = GlobalContext.getOrNull()
        val driverFactory = DatabaseDriverFactory(context.applicationContext)
        val module = chuckerModule(driverFactory)
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
