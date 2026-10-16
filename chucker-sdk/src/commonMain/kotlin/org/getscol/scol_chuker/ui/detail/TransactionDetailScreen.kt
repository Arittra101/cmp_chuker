package org.getscol.scol_chuker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.getscol.scol_chuker.data.db.HttpTransaction
import org.getscol.scol_chuker.data.db.isFailure
import org.getscol.scol_chuker.data.repository.TransactionRepository
import org.getscol.scol_chuker.plugin.formatDateTime
import org.getscol.scol_chuker.ui.components.ChuckerChip
import org.getscol.scol_chuker.ui.components.CopyButton
import org.getscol.scol_chuker.ui.components.KeyValueRow
import org.getscol.scol_chuker.ui.components.MetaText
import org.getscol.scol_chuker.ui.components.MethodBadge
import org.getscol.scol_chuker.ui.components.SectionLabel
import org.getscol.scol_chuker.ui.components.StatusPill
import org.getscol.scol_chuker.ui.components.codeBlock
import org.getscol.scol_chuker.ui.format.BodyKind
import org.getscol.scol_chuker.ui.format.FormattedBody
import org.getscol.scol_chuker.ui.format.contentTypeOf
import org.getscol.scol_chuker.ui.format.buildCurl
import org.getscol.scol_chuker.ui.format.buildTextReport
import org.getscol.scol_chuker.ui.format.formatBody
import org.getscol.scol_chuker.ui.format.formatBytes
import org.getscol.scol_chuker.ui.format.formatDuration
import org.getscol.scol_chuker.ui.format.parseHeaders
import org.getscol.scol_chuker.ui.format.plainLines
import org.getscol.scol_chuker.ui.platform.rememberCopyToClipboard
import org.getscol.scol_chuker.ui.theme.ChuckerColors
import org.getscol.scol_chuker.ui.theme.chuckerColors
import org.getscol.scol_chuker.ui.theme.forStatus
import org.koin.compose.koinInject

private val TABS = listOf("Overview", "Request", "Response", "cURL")

@Composable
internal fun TransactionDetailScreen(
    transactionId: Long,
    repository: TransactionRepository = koinInject(),
) {
    val viewModel = viewModel(key = "chucker-detail-$transactionId") {
        TransactionDetailViewModel(repository, transactionId)
    }
    val transaction by viewModel.transaction.collectAsState()
    val copyToClipboard = rememberCopyToClipboard()

    val current = transaction
    if (current == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 2.dp)
        }
        return
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    // One toggle for the whole screen: a developer who wants raw bytes wants them everywhere.
    var pretty by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SummaryHeader(current, copyToClipboard)

        PrimaryTabRow(selectedTabIndex = selectedTab) {
            TABS.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        when (selectedTab) {
            0 -> OverviewTab(current, copyToClipboard)
            1 -> RequestTab(current, pretty, { pretty = it }, copyToClipboard)
            2 -> ResponseTab(current, pretty, { pretty = it }, copyToClipboard)
            else -> CurlTab(current, copyToClipboard)
        }
    }
}

@Composable
private fun SummaryHeader(tx: HttpTransaction, copyToClipboard: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MethodBadge(tx.method)
            StatusPill(tx.statusCode)
            MetaText(
                text = if (tx.isFailure) "failed" else tx.statusMessage.ifBlank { "—" },
                color = chuckerColors.forStatus(tx.statusCode),
            )
            Box(Modifier.weight(1f))
            CopyButton(text = tx.url, copyToClipboard = copyToClipboard, label = "URL")
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = tx.url,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MetaText(formatDuration(tx.durationMs))
            MetaText("·")
            MetaText("↑ ${formatBytes(tx.requestBodySize)}")
            MetaText("·")
            MetaText("↓ ${formatBytes(tx.responseBodySize)}")
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

// ---------------------------------------------------------------------------
// Tabs
// ---------------------------------------------------------------------------

@Composable
private fun OverviewTab(tx: HttpTransaction, copyToClipboard: (String) -> Unit) {
    val report = remember(tx.id) { buildTextReport(tx) }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        item { Spacer(Modifier.height(10.dp)) }

        if (tx.isFailure) {
            item { FailureCard(tx.statusMessage) }
        }

        item {
            SectionLabel("Overview") {
                CopyButton(text = report, copyToClipboard = copyToClipboard, label = "Report")
            }
        }
        item { KeyValueRow("URL", tx.url) }
        item { KeyValueRow("Method", tx.method) }
        item {
            KeyValueRow(
                name = "Status",
                value = if (tx.isFailure) "failed — ${tx.statusMessage}" else "${tx.statusCode} ${tx.statusMessage}",
            )
        }
        item { KeyValueRow("Started", formatDateTime(tx.requestTime)) }
        item { KeyValueRow("Completed", formatDateTime(tx.responseTime)) }
        item { KeyValueRow("Duration", formatDuration(tx.durationMs)) }
        item {
            KeyValueRow(
                name = "Request body",
                value = "${formatBytes(tx.requestBodySize)} · ${contentTypeOf(tx.requestHeaders) ?: "no content-type"}",
            )
        }
        item {
            KeyValueRow(
                name = "Response body",
                value = "${formatBytes(tx.responseBodySize)} · ${contentTypeOf(tx.responseHeaders) ?: "no content-type"}",
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun RequestTab(
    tx: HttpTransaction,
    pretty: Boolean,
    onPrettyChange: (Boolean) -> Unit,
    copyToClipboard: (String) -> Unit,
) {
    val colors = chuckerColors
    val headers = remember(tx.id) { parseHeaders(tx.requestHeaders) }
    val contentType = remember(tx.id) { contentTypeOf(tx.requestHeaders) }
    val body = remember(tx.id, pretty, colors) {
        formatBody(tx.requestBody, contentType, pretty, colors)
    }

    var headersExpanded by rememberSaveable(tx.id) { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        // Body first: it is what the call is actually about; headers are reference material.
        bodySection(
            title = "Request body",
            body = body,
            raw = tx.requestBody,
            size = tx.requestBodySize,
            pretty = pretty,
            onPrettyChange = onPrettyChange,
            copyToClipboard = copyToClipboard,
            colors = colors,
            key = "req-${tx.id}-$pretty",
        )
        headerSection(
            title = "Request headers",
            headers = headers,
            raw = tx.requestHeaders,
            expanded = headersExpanded,
            onToggle = { headersExpanded = !headersExpanded },
            copyToClipboard = copyToClipboard,
        )
    }
}

@Composable
private fun ResponseTab(
    tx: HttpTransaction,
    pretty: Boolean,
    onPrettyChange: (Boolean) -> Unit,
    copyToClipboard: (String) -> Unit,
) {
    val colors = chuckerColors
    val headers = remember(tx.id) { parseHeaders(tx.responseHeaders) }
    val contentType = remember(tx.id) { contentTypeOf(tx.responseHeaders) }
    val body = remember(tx.id, pretty, colors) {
        formatBody(tx.responseBody, contentType, pretty, colors)
    }

    var headersExpanded by rememberSaveable(tx.id) { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        bodySection(
            title = "Response body",
            body = body,
            raw = tx.responseBody,
            size = tx.responseBodySize,
            pretty = pretty,
            onPrettyChange = onPrettyChange,
            copyToClipboard = copyToClipboard,
            colors = colors,
            key = "res-${tx.id}-$pretty",
        )
        headerSection(
            title = "Response headers",
            headers = headers,
            raw = tx.responseHeaders,
            expanded = headersExpanded,
            onToggle = { headersExpanded = !headersExpanded },
            copyToClipboard = copyToClipboard,
        )
    }
}

@Composable
private fun CurlTab(tx: HttpTransaction, copyToClipboard: (String) -> Unit) {
    val colors = chuckerColors
    val curl = remember(tx.id) { buildCurl(tx) }
    val lines = remember(curl) { plainLines(curl) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        item { Spacer(Modifier.height(10.dp)) }
        item {
            SectionLabel("Reproduce this call") {
                CopyButton(text = curl, copyToClipboard = copyToClipboard, label = "Copy cURL")
            }
        }
        item { Spacer(Modifier.height(6.dp)) }
        codeBlock(lines = lines, colors = colors, showLineNumbers = false, key = "curl-${tx.id}")
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ---------------------------------------------------------------------------
// Sections
// ---------------------------------------------------------------------------

private fun LazyListScope.headerSection(
    title: String,
    headers: List<Pair<String, String>>,
    raw: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    copyToClipboard: (String) -> Unit,
) {
    item { Spacer(Modifier.height(10.dp)) }
    item {
        // Collapsed by default: twenty CDN headers should never push the body off the screen.
        SectionLabel(
            text = "$title (${headers.size})",
            modifier = Modifier.clickable(onClick = onToggle),
            leading = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse $title" else "Expand $title",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 6.dp).size(16.dp),
                )
            },
        ) {
            if (headers.isNotEmpty()) {
                CopyButton(text = raw, copyToClipboard = copyToClipboard)
            }
        }
    }
    if (headers.isEmpty()) {
        item { MetaText("— none —", modifier = Modifier.padding(vertical = 6.dp)) }
    } else if (expanded) {
        // No item keys: a response may legitimately repeat a header name (Set-Cookie).
        items(headers) { (name, value) ->
            KeyValueRow(name, value)
        }
    }
    item { Spacer(Modifier.height(20.dp)) }
}

private fun LazyListScope.bodySection(
    title: String,
    body: FormattedBody,
    raw: String?,
    size: Long,
    pretty: Boolean,
    onPrettyChange: (Boolean) -> Unit,
    copyToClipboard: (String) -> Unit,
    colors: ChuckerColors,
    key: String,
) {
    item { Spacer(Modifier.height(14.dp)) }
    item {
        SectionLabel(title) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (body.formattable) {
                    ChuckerChip("pretty", selected = pretty, onClick = { onPrettyChange(true) })
                    ChuckerChip("raw", selected = !pretty, onClick = { onPrettyChange(false) })
                }
                if (!raw.isNullOrBlank()) {
                    CopyButton(text = raw, copyToClipboard = copyToClipboard)
                }
            }
        }
    }
    item {
        Row(
            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MetaText(body.label)
            MetaText("·")
            MetaText(formatBytes(size))
            if (body.lines.size > 1) {
                MetaText("·")
                MetaText("${body.lines.size} lines")
            }
        }
    }

    if (body.kind == BodyKind.EMPTY) {
        item { MetaText("— no body —", modifier = Modifier.padding(bottom = 16.dp)) }
        return
    }

    codeBlock(
        lines = body.lines,
        colors = colors,
        showLineNumbers = body.lines.size > 1,
        key = key,
    )
    item { Spacer(Modifier.height(20.dp)) }
}

@Composable
private fun FailureCard(message: String) {
    val colors = chuckerColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.failure.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        Text(
            text = "REQUEST FAILED",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = colors.failure,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = message.ifBlank { "No response was received." },
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
