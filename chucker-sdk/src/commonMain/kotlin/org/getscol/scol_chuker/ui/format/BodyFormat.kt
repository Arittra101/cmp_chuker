package org.getscol.scol_chuker.ui.format

import androidx.compose.ui.text.AnnotatedString
import org.getscol.scol_chuker.ui.theme.ChuckerColors

internal enum class BodyKind { EMPTY, JSON, FORM, TEXT }

/**
 * A body already laid out as individual lines. Everything the viewer needs is pre-computed so the
 * list can render 20 000 lines of JSON without re-parsing anything on scroll.
 */
internal class FormattedBody(
    val lines: List<AnnotatedString>,
    val kind: BodyKind,
    /** True when there is a "pretty" rendering that differs from the raw bytes. */
    val formattable: Boolean,
    val label: String,
)

internal fun formatBody(
    raw: String?,
    contentType: String?,
    pretty: Boolean,
    colors: ChuckerColors,
): FormattedBody {
    if (raw.isNullOrBlank()) {
        return FormattedBody(emptyList(), BodyKind.EMPTY, formattable = false, label = "empty")
    }

    parseJsonOrNull(raw)?.let { element ->
        return FormattedBody(
            lines = if (pretty) highlightJson(element, colors) else plainLines(raw),
            kind = BodyKind.JSON,
            formattable = true,
            label = "json",
        )
    }

    if (contentType?.contains("x-www-form-urlencoded", ignoreCase = true) == true) {
        return FormattedBody(
            lines = if (pretty) formUrlEncodedLines(raw, colors) else plainLines(raw),
            kind = BodyKind.FORM,
            formattable = true,
            label = "form",
        )
    }

    return FormattedBody(
        lines = plainLines(raw),
        kind = BodyKind.TEXT,
        formattable = false,
        label = contentType?.substringAfter('/')?.take(12) ?: "text",
    )
}
