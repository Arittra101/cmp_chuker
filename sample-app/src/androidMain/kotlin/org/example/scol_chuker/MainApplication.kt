package org.example.scol_chuker

import android.app.Application

/** Android entry point for SDK init — iOS does the same in `MainViewController`. */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Init the SDK — sets up Koin + the Room database
        CmpChucker.init(this)
    }
}
