package org.getscol.scol_chuker

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point — hosts the shared [SampleApp]. Consumed by `iosApp/ContentView.swift`.
 *
 * iOS has no Application class, so this is where the SDK gets initialised — it must happen
 * before the first request, since `InspectorPlugin` resolves its repository from Koin.
 */
fun MainViewController(): UIViewController {
    CmpChucker.init()
    return ComposeUIViewController {
        SampleApp()
    }
}
