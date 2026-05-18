package org.example.scol_chuker.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIPasteboard

@Composable
internal actual fun rememberCopyToClipboard(): (String) -> Unit = remember {
    { text -> UIPasteboard.generalPasteboard.string = text }
}
