package org.example.scol_chuker.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.example.scol_chuker.ui.detail.TransactionDetailScreen
import org.example.scol_chuker.ui.list.TransactionListScreen
import org.example.scol_chuker.ui.nav.TransactionDetail
import org.example.scol_chuker.ui.nav.TransactionList

/**
 * Full-screen dialog that hosts the entire Network Inspector UI.
 * Internal — the host app never references this directly.
 */
@Composable
internal fun ChuckerDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp,
        ) {
            val navController = rememberNavController()

            Column {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Network Inspector",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close inspector")
                    }
                }

                HorizontalDivider()

                // Navigation lives entirely inside the dialog
                NavHost(
                    navController = navController,
                    startDestination = TransactionList,
                ) {
                    composable<TransactionList> {
                        TransactionListScreen(
                            onTransactionClick = { id ->
                                navController.navigate(TransactionDetail(id))
                            },
                        )
                    }
                    composable<TransactionDetail> { backStackEntry ->
                        val route: TransactionDetail = backStackEntry.toRoute()
                        TransactionDetailScreen(transactionId = route.id)
                    }
                }
            }
        }
    }
}
