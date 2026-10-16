package org.getscol.scol_chuker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.getscol.scol_chuker.ui.theme.ChuckerColors
import org.getscol.scol_chuker.ui.theme.chuckerColors
import org.getscol.scol_chuker.ui.theme.forMethod
import org.getscol.scol_chuker.ui.theme.forStatus
import org.getscol.scol_chuker.ui.theme.statusLabel

@Composable
internal fun MethodBadge(method: String, modifier: Modifier = Modifier) {
    val color = chuckerColors.forMethod(method)
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = method.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
internal fun StatusPill(code: Int, modifier: Modifier = Modifier) {
    val color = chuckerColors.forStatus(code)
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = statusLabel(code),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
    }
}

/** Muted monospace metadata — timings, sizes, counts. */
@Composable
internal fun MetaText(text: String, modifier: Modifier = Modifier, color: Color? = null) {
    Text(
        text = text,
        modifier = modifier,
        color = color ?: MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
    )
}

@Composable
internal fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit = {},
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f))
        trailing()
    }
}

/** A chip-sized toggle; used for filters and for the pretty/raw switch. */
@Composable
internal fun ChuckerChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color? = null,
) {
    val tint = accent ?: MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .background(
                if (selected) tint.copy(alpha = 0.18f) else chuckerColors.chip,
                RoundedCornerShape(6.dp),
            )
            .border(
                width = 1.dp,
                color = if (selected) tint.copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Copy button that confirms in place — testers need to know the paste buffer really changed. */
@Composable
internal fun CopyButton(
    text: String,
    copyToClipboard: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Copy",
) {
    var copied by remember(text) { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1_600)
            copied = false
        }
    }

    val tint = if (copied) chuckerColors.success else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .background(chuckerColors.chip, RoundedCornerShape(6.dp))
            .clickable {
                copyToClipboard(text)
                copied = true
            }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = if (copied) "Copied" else label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = tint,
        )
    }
}

@Composable
internal fun KeyValueRow(name: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = name,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = chuckerColors.jsonKey,
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ---------------------------------------------------------------------------
// Code viewer
// ---------------------------------------------------------------------------

/**
 * Emits a body as lazy rows: a gutter with line numbers plus wrapped, coloured code. Lines wrap,
 * so a body is read top-to-bottom like a file — never dragged sideways to find the closing brace.
 */
internal fun LazyListScope.codeBlock(
    lines: List<AnnotatedString>,
    colors: ChuckerColors,
    showLineNumbers: Boolean,
    key: String,
) {
    item(key = "$key-top") { CodeEdge(colors, top = true) }
    itemsIndexed(lines, key = { index, _ -> "$key-$index" }) { index, line ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.codeBackground)
                .padding(horizontal = 10.dp),
        ) {
            if (showLineNumbers) {
                Text(
                    text = "${index + 1}",
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.End,
                    color = colors.gutter,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 17.sp,
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = line,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
    item(key = "$key-bottom") { CodeEdge(colors, top = false) }
}

@Composable
private fun CodeEdge(colors: ChuckerColors, top: Boolean) {
    val shape = if (top) {
        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    } else {
        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
    }
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(colors.codeBackground, shape),
    )
}
