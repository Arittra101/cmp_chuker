package org.getscol.scol_chuker.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.getscol.scol_chuker.data.db.TransactionSummary
import org.getscol.scol_chuker.data.db.isError
import org.getscol.scol_chuker.data.db.isFailure
import org.getscol.scol_chuker.data.repository.TransactionRepository
import org.getscol.scol_chuker.plugin.formatClockTime
import org.getscol.scol_chuker.ui.components.ChuckerChip
import org.getscol.scol_chuker.ui.components.MetaText
import org.getscol.scol_chuker.ui.components.MethodBadge
import org.getscol.scol_chuker.ui.components.StatusPill
import org.getscol.scol_chuker.ui.format.formatBytes
import org.getscol.scol_chuker.ui.format.formatDuration
import org.getscol.scol_chuker.ui.format.urlHost
import org.getscol.scol_chuker.ui.format.urlPath
import org.getscol.scol_chuker.ui.theme.chuckerColors
import org.getscol.scol_chuker.ui.theme.forStatus
import org.koin.compose.koinInject

/** Anything slower than this gets a warning tint — the number a tester is told to watch. */
private const val SLOW_CALL_MS = 1_000L

@Composable
internal fun TransactionListScreen(
    onTransactionClick: (Long) -> Unit,
    repository: TransactionRepository = koinInject(),
) {
    val viewModel = viewModel { TransactionListViewModel(repository) }
    val transactions by viewModel.transactions.collectAsState()
    val query by viewModel.query.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val total by viewModel.totalCount.collectAsState()
    val errors by viewModel.errorCount.collectAsState()

    var confirmClear by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SearchField(value = query, onValueChange = viewModel::onQueryChange)

        FilterRow(selected = filter, onSelect = viewModel::onFilterChange)

        StatsRow(
            total = total,
            errors = errors,
            shown = transactions?.size ?: 0,
            filtered = query.isNotBlank() || filter != StatusFilter.ALL,
            onClear = { confirmClear = true },
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        val rows = transactions
        if (rows == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            }
        } else if (rows.isEmpty()) {
            EmptyState(hasFilter = query.isNotBlank() || filter != StatusFilter.ALL, captured = total)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(rows, key = { it.id }) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction.id) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear captured traffic?") },
            text = { Text("All $total recorded calls will be deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        confirmClear = false
                    },
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .background(chuckerColors.chip, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = "search url, method, status, body…",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onValueChange("") },
            )
        }
    }
}

@Composable
private fun FilterRow(selected: StatusFilter, onSelect: (StatusFilter) -> Unit) {
    val colors = chuckerColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatusFilter.entries.forEach { entry ->
            ChuckerChip(
                label = entry.label,
                selected = entry == selected,
                onClick = { onSelect(entry) },
                accent = when (entry) {
                    StatusFilter.ALL -> MaterialTheme.colorScheme.primary
                    StatusFilter.SUCCESS -> colors.success
                    StatusFilter.REDIRECT -> colors.redirect
                    StatusFilter.CLIENT_ERROR -> colors.clientError
                    StatusFilter.SERVER_ERROR -> colors.serverError
                    StatusFilter.FAILED -> colors.failure
                },
            )
        }
    }
}

@Composable
private fun StatsRow(
    total: Int,
    errors: Int,
    shown: Int,
    filtered: Boolean,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MetaText("$total calls")
        if (errors > 0) {
            MetaText("$errors failing", color = chuckerColors.serverError)
        }
        if (filtered) {
            MetaText("· $shown shown")
        }
        Box(Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = "Clear captured traffic",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clickable(onClick = onClear)
                .padding(8.dp)
                .size(18.dp),
        )
    }
}

@Composable
private fun EmptyState(hasFilter: Boolean, captured: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = if (hasFilter) "No calls match this filter" else "No traffic captured yet",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))
            MetaText(
                text = when {
                    hasFilter && captured > 0 -> "$captured calls recorded — widen the filter"
                    hasFilter -> "nothing recorded yet"
                    else -> "fire a request from the app and it lands here"
                },
            )
        }
    }
}

@Composable
private fun TransactionRow(transaction: TransactionSummary, onClick: () -> Unit) {
    val colors = chuckerColors
    val accent = colors.forStatus(transaction.statusCode)
    val slow = transaction.durationMs >= SLOW_CALL_MS

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .background(if (transaction.isError) accent.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background),
    ) {
        // Status rail: the column a tester scans down to spot red.
        Spacer(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(accent),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MethodBadge(transaction.method)
                StatusPill(transaction.statusCode)
                Text(
                    text = urlPath(transaction.url),
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(3.dp))
            MetaText(urlHost(transaction.url))
            Spacer(Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetaText(formatClockTime(transaction.requestTime))
                MetaText("·")
                MetaText(
                    text = formatDuration(transaction.durationMs),
                    color = if (slow) colors.clientError else null,
                )
                MetaText("·")
                if (transaction.isFailure) {
                    MetaText(
                        text = transaction.statusMessage.take(60),
                        color = colors.failure,
                    )
                } else {
                    MetaText(formatBytes(transaction.responseBodySize))
                }
            }
        }
    }
}
