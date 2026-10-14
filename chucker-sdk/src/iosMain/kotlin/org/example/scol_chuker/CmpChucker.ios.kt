package org.example.scol_chuker

import org.example.scol_chuker.data.db.DatabaseBuilderFactory
import org.example.scol_chuker.di.chuckerModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatform

/**
 * Public entry point for the CMP Chucker SDK on iOS.
 *
 * Call this once before the first HTTP request — typically at the top of your
 * `ComposeUIViewController { }` body:
 *
 *   CmpChucker.init()
 */
object CmpChucker {

    private var initialized = false

    /** Koin module for apps that manage their own [startKoin]. */
    fun koinModule(): Module = chuckerModule(DatabaseBuilderFactory())

    /**
     * Initialises the SDK.
     *
     * - If your app already uses Koin, call [koinModule] and pass it to your own `startKoin`.
     * - If your app does NOT use Koin, call this and Chucker will start its own Koin instance.
     */
    fun init() {
        if (initialized) return
        initialized = true
        // If Koin is already running (host app started it), load our module into it.
        val existingKoin = KoinPlatform.getKoinOrNull()
        val module = koinModule()
        if (existingKoin != null) {
            existingKoin.loadModules(listOf(module))
        } else {
            startKoin {
                modules(module)
            }
        }
    }
}
