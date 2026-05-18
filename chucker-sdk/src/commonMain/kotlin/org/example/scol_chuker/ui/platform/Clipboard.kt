package org.example.scol_chuker.ui.platform

import androidx.compose.runtime.Composable

@Composable
internal expect fun rememberCopyToClipboard(): (String) -> Unit
