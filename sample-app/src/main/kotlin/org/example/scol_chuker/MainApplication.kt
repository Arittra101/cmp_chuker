package org.example.scol_chuker

import android.app.Application
import org.example.scol_chuker.CmpChucker

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Init the SDK — sets up Koin + SQLDelight DB
        CmpChucker.init(this)
    }
}
