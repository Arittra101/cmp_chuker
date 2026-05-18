package org.example.scol_chuker.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.scol_chuker.data.db.HttpTransaction
import org.example.scol_chuker.data.repository.TransactionRepository
import org.koin.compose.koinInject

@Composable
internal fun TransactionListScreen(
    onTransactionClick: (Long) -> Unit,
    repository: TransactionRepository = koinInject(),
) {
    val viewModel = remember { TransactionListViewModel(repository) }
    val transactions by viewModel.transactions.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${transactions.size} requests",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IconButton(onClick = { viewModel.clearAll() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear all",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        if (transactions.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(transactions, key = { it.id }) { tx ->
                    TransactionRow(
                        transaction = tx,
                        onClick = { onTransactionClick(tx.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🌐", fontSize = 48.sp)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "No requests captured yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TransactionRow(transaction: HttpTransaction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Method badge
        MethodBadge(method = transaction.method)
        Spacer(modifier = Modifier.width(10.dp))
        // URL + status
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.url,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusBadge(code = transaction.statusCode)
                Text(
                    text = "${transaction.durationMs} ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MethodBadge(method: String) {
    val color = when (method.uppercase()) {
        "GET"    -> Color(0xFF2196F3)
        "POST"   -> Color(0xFF4CAF50)
        "PUT"    -> Color(0xFFFF9800)
        "DELETE" -> Color(0xFFF44336)
        "PATCH"  -> Color(0xFF9C27B0)
        else     -> Color(0xFF607D8B)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = method.uppercase(),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun StatusBadge(code: Int) {
    val color = when {
        code in 200..299 -> Color(0xFF4CAF50)
        code in 300..399 -> Color(0xFF2196F3)
        code in 400..499 -> Color(0xFFFF9800)
        code >= 500      -> Color(0xFFF44336)
        else             -> Color(0xFF607D8B)
    }
    Text(
        text = "$code",
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Monospace,
    )
}

// ---------------------------------------------------------------------------
// Multiplatform remember helper (avoids pulling in lifecycle-viewmodel-compose
// just for a simple ViewModel; for full SavedState use viewModel() from koin)
// ---------------------------------------------------------------------------
@Composable
private fun <T> remember(init: () -> T): T = androidx.compose.runtime.remember { init() }
