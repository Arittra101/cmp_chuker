package org.example.scol_chuker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.example.scol_chuker.data.db.HttpTransaction
import org.example.scol_chuker.data.repository.TransactionRepository
import org.example.scol_chuker.ui.platform.rememberCopyToClipboard
import org.koin.compose.koinInject

@Composable
internal fun TransactionDetailScreen(
    transactionId: Long,
    repository: TransactionRepository = koinInject(),
) {
    val viewModel = remember { TransactionDetailViewModel(repository, transactionId) }
    val transaction by viewModel.transaction.collectAsState()

    if (transaction == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading…")
        }
        return
    }

    val tx = transaction!!
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Request", "Response", "Curl")
    val copyToClipboard = rememberCopyToClipboard()

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                )
            }
        }

        when (selectedTab) {
            0 -> RequestTab(tx, copyToClipboard)
            1 -> ResponseTab(tx, copyToClipboard)
            2 -> CurlTab(tx, copyToClipboard)
        }
    }
}

@Composable
private fun RequestTab(tx: HttpTransaction, copyToClipboard: (String) -> Unit) {
    ScrollableContent {
        CopyableSection("Method", tx.method, copyToClipboard)
        CopyableSection("URL", tx.url, copyToClipboard)
        CopyableSection("Headers", tx.requestHeaders.ifBlank { "(none)" }, copyToClipboard)
        if (!tx.requestBody.isNullOrBlank()) {
            CopyableSection("Body", tx.requestBody, copyToClipboard)
        }
    }
}

@Composable
private fun ResponseTab(tx: HttpTransaction, copyToClipboard: (String) -> Unit) {
    ScrollableContent {
        CopyableSection(
            label = "Status",
            text = "${tx.statusCode} ${tx.statusMessage}  •  ${tx.durationMs} ms",
            copyToClipboard = copyToClipboard,
        )
        CopyableSection("Headers", tx.responseHeaders.ifBlank { "(none)" }, copyToClipboard)
        if (!tx.responseBody.isNullOrBlank()) {
            CopyableSection("Body", tx.responseBody, copyToClipboard)
        }
    }
}

@Composable
private fun CurlTab(tx: HttpTransaction, copyToClipboard: (String) -> Unit) {
    val curl = buildCurl(tx)
    ScrollableContent {
        CopyableSection("Method", tx.method, copyToClipboard)
        CopyableSection("URL", tx.url, copyToClipboard)
        CopyableSection("Headers", tx.requestHeaders.ifBlank { "(none)" }, copyToClipboard)
        if (!tx.requestBody.isNullOrBlank()) {
            CopyableSection("Body", tx.requestBody, copyToClipboard)
        }
        CopyableSection("Curl Command", curl, copyToClipboard)
    }
}

private fun buildCurl(tx: HttpTransaction): String = buildString {
    append("curl -X ${tx.method}")
    tx.requestHeaders.lines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isNotBlank()) append(" \\\n  -H '$trimmed'")
    }
    if (!tx.requestBody.isNullOrBlank()) {
        append(" \\\n  -d '${tx.requestBody.replace("'", "\\'")}'")
    }
    append(" \\\n  '${tx.url}'")
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

@Composable
private fun ScrollableContent(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        content()
    }
}

@Composable
private fun CopyableSection(
    label: String,
    text: String,
    copyToClipboard: (String) -> Unit,
) {
    var copied by remember(label, text) { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1_500)
            copied = false
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (copied) {
                Text(
                    text = "Copied",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
            IconButton(
                onClick = {
                    copyToClipboard(text)
                    copied = true
                },
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    MonoText(text)
}

@Composable
private fun MonoText(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp),
            )
            .padding(10.dp),
    )
}
