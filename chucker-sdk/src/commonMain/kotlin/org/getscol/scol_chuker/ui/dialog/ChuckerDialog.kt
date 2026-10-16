package org.getscol.scol_chuker.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.getscol.scol_chuker.ui.detail.TransactionDetailScreen
import org.getscol.scol_chuker.ui.list.TransactionListScreen
import org.getscol.scol_chuker.ui.nav.TransactionDetail
import org.getscol.scol_chuker.ui.nav.TransactionList
import org.getscol.scol_chuker.ui.theme.ChuckerTheme

/**
 * Full-screen dialog that hosts the whole SCOL Chucker UI. Internal — the host app only ever
 * places [org.getscol.scol_chuker.ui.overlay.CmpChuckerOverlay].
 */
@Composable
internal fun ChuckerDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Its own theme: the inspector must read identically inside any host app's styling.
        ChuckerTheme {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.94f),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp,
            ) {
                val navController = rememberNavController()
                val currentEntry by navController.currentBackStackEntryAsState()
                val canGoBack = currentEntry != null && navController.previousBackStackEntry != null

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (canGoBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to call list",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .clickable { navController.popBackStack() }
                                        .padding(8.dp)
                                        .size(20.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                            } else {
                                Spacer(Modifier.width(10.dp))
                            }
                            Column {
                                Text(
                                    text = "SCOL Chucker",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = if (canGoBack) "call detail" else "captured traffic",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close SCOL Chucker",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable(onClick = onDismiss)
                                .padding(8.dp)
                                .size(20.dp),
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
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
    }
}
