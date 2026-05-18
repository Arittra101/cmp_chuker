package org.example.scol_chuker.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
internal actual fun rememberCopyToClipboard(): (String) -> Unit {
    val clipboardManager = LocalClipboardManager.current
    return remember(clipboardManager) {
        { text -> clipboardManager.setText(AnnotatedString(text)) }
    }
}
