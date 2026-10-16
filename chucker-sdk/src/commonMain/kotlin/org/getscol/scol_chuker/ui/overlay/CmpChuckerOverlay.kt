package org.getscol.scol_chuker.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.getscol.scol_chuker.data.db.HttpTransaction
import org.getscol.scol_chuker.data.db.isError
import org.getscol.scol_chuker.data.repository.TransactionRepository
import org.getscol.scol_chuker.plugin.currentTimeMillis
import org.getscol.scol_chuker.ui.dialog.ChuckerDialog
import org.getscol.scol_chuker.ui.format.formatDuration
import org.getscol.scol_chuker.ui.format.urlPath
import org.getscol.scol_chuker.ui.theme.ChuckerTheme
import org.getscol.scol_chuker.ui.theme.chuckerColors
import org.getscol.scol_chuker.ui.theme.forStatus
import org.getscol.scol_chuker.ui.theme.statusLabel
import org.koin.mp.KoinPlatform
import kotlin.math.abs
import kotlin.math.roundToInt

private const val LIVE_CALL_VISIBLE_MS = 2_600L

/** Fixed slate rather than a theme colour: the button must stand out over any host UI. */
private val IdleButtonColor = Color(0xFF263041)

/**
 * Drop this into the host app's root composable. It draws the draggable bug button and, when
 * tapped, the whole inspector.
 *
 * The button is not just a launcher: every captured call makes it flash, bumps an unseen counter
 * and briefly shows the call itself, so a developer or tester watching the screen knows traffic
 * happened — and whether it failed — without opening anything.
 */
@Composable
fun CmpChuckerOverlay() {
    // The host may place the overlay before Koin is ready; a debug tool must never crash the app.
    val repository = remember {
        runCatching { KoinPlatform.getKoin().get<TransactionRepository>() }.getOrNull()
    }

    var showInspector by rememberSaveable { mutableStateOf(false) }
    val latestFlow = remember(repository) { repository?.observeLatest() ?: flowOf(null) }
    val latest by latestFlow.collectAsState(null)

    // Rows already in the database when the overlay appeared are history, not news.
    val startedAt = remember { currentTimeMillis() }
    var lastSeenId by remember { mutableStateOf<Long?>(null) }
    var unseen by remember { mutableIntStateOf(0) }
    var unseenHasError by remember { mutableStateOf(false) }
    var liveCall by remember { mutableStateOf<HttpTransaction?>(null) }
    var flashes by remember { mutableIntStateOf(0) }

    LaunchedEffect(latest?.id) {
        val transaction = latest ?: return@LaunchedEffect
        if (transaction.id == lastSeenId) return@LaunchedEffect
        lastSeenId = transaction.id
        if (transaction.requestTime < startedAt) return@LaunchedEffect

        if (!showInspector) {
            unseen++
            if (transaction.isError) unseenHasError = true
        }
        liveCall = transaction
        flashes++
    }

    LaunchedEffect(liveCall?.id) {
        if (liveCall != null) {
            delay(LIVE_CALL_VISIBLE_MS)
            liveCall = null
        }
    }

    ChuckerTheme {
        val colors = chuckerColors
        val density = LocalDensity.current
        val margin = with(density) { 16.dp.toPx() }
        val scope = rememberCoroutineScope()

        var parentSize by remember { mutableStateOf(IntSize.Zero) }
        var buttonSize by remember { mutableStateOf(IntSize.Zero) }
        // Offsets are relative to the bottom-end corner, so the button is correctly parked before
        // anything has been measured — drag only ever moves it left (negative) and up (negative).
        val offsetX = remember { Animatable(0f) }
        val offsetY = remember { Animatable(0f) }
        val flash = remember { Animatable(0f) }

        val dragRangeX = (parentSize.width - buttonSize.width - margin * 2).coerceAtLeast(0f)
        val dragRangeY = (parentSize.height - buttonSize.height - margin * 2).coerceAtLeast(0f)

        LaunchedEffect(flashes) {
            if (flashes == 0) return@LaunchedEffect
            repeat(2) {
                flash.snapTo(0f)
                flash.animateTo(1f, tween(durationMillis = 480, easing = LinearEasing))
            }
            flash.snapTo(0f)
        }

        val accent = when {
            unseenHasError -> colors.serverError
            unseen > 0 -> colors.success
            else -> MaterialTheme.colorScheme.primary
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { parentSize = it },
        ) {
            AnimatedVisibility(
                visible = liveCall != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp),
            ) {
                liveCall?.let { call ->
                    LiveCallChip(call = call, onClick = { showInspector = true })
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                    .onSizeChanged { buttonSize = it }
                    .pointerInput(dragRangeX, dragRangeY) {
                        detectDragGestures(
                            onDrag = { _, drag ->
                                scope.launch {
                                    offsetX.snapTo((offsetX.value + drag.x).coerceIn(-dragRangeX, 0f))
                                    offsetY.snapTo((offsetY.value + drag.y).coerceIn(-dragRangeY, 0f))
                                }
                            },
                            onDragEnd = {
                                // Snap to the nearer side so it never parks over the middle of the UI.
                                scope.launch {
                                    val draggedPastHalf = offsetX.value < -dragRangeX / 2f
                                    offsetX.animateTo(if (draggedPastHalf) -dragRangeX else 0f, spring())
                                }
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                // Expanding ring: the per-request "something just happened" signal.
                if (flash.value > 0f) {
                    Spacer(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(1f + flash.value * 0.7f)
                            .alpha((1f - flash.value) * 0.7f)
                            .border(2.dp, accent, CircleShape),
                    )
                }

                FloatingActionButton(
                    onClick = {
                        showInspector = true
                        unseen = 0
                        unseenHasError = false
                    },
                    modifier = Modifier.scale(1f + 0.08f * (1f - abs(flash.value * 2f - 1f))),
                    containerColor = if (unseen > 0) accent else IdleButtonColor,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Open SCOL Chucker",
                        tint = Color.White,
                    )
                }

                if (unseen > 0) {
                    UnseenBadge(count = unseen, color = accent, modifier = Modifier.align(Alignment.TopEnd))
                }
            }
        }
    }

    if (showInspector) {
        ChuckerDialog(
            onDismiss = {
                showInspector = false
                unseen = 0
                unseenHasError = false
            },
        )
    }
}

@Composable
private fun LiveCallChip(call: HttpTransaction, onClick: () -> Unit) {
    val colors = chuckerColors
    val accent = colors.forStatus(call.statusCode)
    Row(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = statusLabel(call.statusCode),
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            text = "${call.method} ${urlPath(call.url)}",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Text(
            text = formatDuration(call.durationMs),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun UnseenBadge(count: Int, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .offset(x = 2.dp, y = (-2).dp)
            .background(color, CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
            .widthIn(min = 18.dp)
            .padding(horizontal = 5.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 99) "99+" else "$count",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
    }
}
