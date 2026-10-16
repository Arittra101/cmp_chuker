package org.getscol.scol_chuker.ui.format

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.getscol.scol_chuker.ui.theme.ChuckerColors

/**
 * Bodies are rendered as a list of pre-laid-out lines so the viewer can put them in a LazyColumn:
 * a 20k-line response then costs the same to show as a 20-line one, and nothing ever needs a
 * horizontal scrollbar — every line is already short enough to wrap readably.
 */
internal const val MAX_FORMATTABLE_BODY = 1_000_000

private val lenientJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    allowSpecialFloatingPointValues = true
}

/** Cheap shape check first — `parseToJsonElement` on a 1 MB HTML page is pure waste. */
internal fun parseJsonOrNull(raw: String): JsonElement? {
    val trimmed = raw.trim()
    if (trimmed.length > MAX_FORMATTABLE_BODY) return null
    val looksLikeJson = (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
        (trimmed.startsWith("[") && trimmed.endsWith("]"))
    if (!looksLikeJson) return null
    return runCatching { lenientJson.parseToJsonElement(trimmed) }.getOrNull()
}

internal fun isJsonBody(raw: String): Boolean = parseJsonOrNull(raw) != null

/** Pretty-printed, syntax-coloured JSON, one [AnnotatedString] per line. */
internal fun highlightJson(element: JsonElement, colors: ChuckerColors): List<AnnotatedString> =
    buildAnnotatedString { writeElement(element, colors, 0) }.splitLines()

/** Plain text (or anything that is not JSON) — split as-is, long lines wrap in the viewer. */
internal fun plainLines(raw: String): List<AnnotatedString> =
    raw.trimEnd().split('\n').map { AnnotatedString(it.trimEnd('\r')) }

/** `a=1&b=2` reads far better one pair per line. */
internal fun formUrlEncodedLines(raw: String, colors: ChuckerColors): List<AnnotatedString> =
    raw.trim().split('&').filter { it.isNotBlank() }.map { pair ->
        val name = pair.substringBefore('=')
        val value = pair.substringAfter('=', "")
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.jsonKey)) { append(name) }
            withStyle(SpanStyle(color = colors.punctuation)) { append(" = ") }
            withStyle(SpanStyle(color = colors.jsonString)) { append(decodePercent(value)) }
        }
    }

private fun decodePercent(value: String): String {
    if ('%' !in value && '+' !in value) return value
    val bytes = ArrayList<Byte>(value.length)
    var i = 0
    while (i < value.length) {
        val c = value[i]
        when {
            c == '+' -> { bytes.add(' '.code.toByte()); i++ }
            c == '%' && i + 2 < value.length -> {
                val hex = value.substring(i + 1, i + 3).toIntOrNull(16)
                if (hex == null) { bytes.add(c.code.toByte()); i++ } else { bytes.add(hex.toByte()); i += 3 }
            }
            else -> { bytes.add(c.code.toByte()); i++ }
        }
    }
    return runCatching { bytes.toByteArray().decodeToString() }.getOrDefault(value)
}

// ---------------------------------------------------------------------------
// JSON writer
// ---------------------------------------------------------------------------

private const val INDENT = "  "

private fun AnnotatedString.Builder.writeElement(
    element: JsonElement,
    colors: ChuckerColors,
    depth: Int,
) {
    when (element) {
        is JsonObject -> {
            if (element.isEmpty()) {
                punctuation("{}", colors)
                return
            }
            punctuation("{", colors)
            append('\n')
            val last = element.size - 1
            element.entries.forEachIndexed { index, (key, value) ->
                indent(depth + 1)
                withStyle(SpanStyle(color = colors.jsonKey)) { append("\"${escapeJson(key)}\"") }
                punctuation(": ", colors)
                writeElement(value, colors, depth + 1)
                if (index != last) punctuation(",", colors)
                append('\n')
            }
            indent(depth)
            punctuation("}", colors)
        }

        is JsonArray -> {
            if (element.isEmpty()) {
                punctuation("[]", colors)
                return
            }
            punctuation("[", colors)
            append('\n')
            val last = element.size - 1
            element.forEachIndexed { index, value ->
                indent(depth + 1)
                writeElement(value, colors, depth + 1)
                if (index != last) punctuation(",", colors)
                append('\n')
            }
            indent(depth)
            punctuation("]", colors)
        }

        JsonNull -> withStyle(SpanStyle(color = colors.jsonNull)) { append("null") }

        is JsonPrimitive -> {
            val style = when {
                element.isString -> colors.jsonString
                element.content == "true" || element.content == "false" -> colors.jsonBoolean
                else -> colors.jsonNumber
            }
            val text = if (element.isString) "\"${escapeJson(element.content)}\"" else element.content
            withStyle(SpanStyle(color = style)) { append(text) }
        }
    }
}

private fun AnnotatedString.Builder.indent(depth: Int) {
    repeat(depth) { append(INDENT) }
}

private fun AnnotatedString.Builder.punctuation(text: String, colors: ChuckerColors) {
    withStyle(SpanStyle(color = colors.punctuation)) { append(text) }
}

private fun escapeJson(value: String): String {
    if (value.none { it == '"' || it == '\\' || it < ' ' }) return value
    return buildString(value.length + 8) {
        value.forEach { c ->
            when (c) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (c < ' ') append("\\u" + c.code.toString(16).padStart(4, '0')) else append(c)
            }
        }
    }
}

private fun AnnotatedString.splitLines(): List<AnnotatedString> {
    val lines = ArrayList<AnnotatedString>()
    var start = 0
    while (true) {
        val breakAt = text.indexOf('\n', start)
        if (breakAt < 0) {
            lines.add(subSequence(start, text.length))
            return lines
        }
        lines.add(subSequence(start, breakAt))
        start = breakAt + 1
    }
}
