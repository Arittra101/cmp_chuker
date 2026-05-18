package org.example.scol_chuker.ui.overlay

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.example.scol_chuker.ui.dialog.ChuckerDialog
import kotlin.math.roundToInt

/**
 * Drop this into your root composable — it adds a draggable floating bug button
 * that opens the Network Inspector dialog.
 *
 *   setContent {
 *       YourTheme {
 *           YourApp()
 *           CmpChuckerOverlay()   // ← add this
 *       }
 *   }
 */
@Composable
fun CmpChuckerOverlay() {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var showInspector by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { showInspector = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                },
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = "Open Network Inspector",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }

    if (showInspector) {
        ChuckerDialog(onDismiss = { showInspector = false })
    }
}
