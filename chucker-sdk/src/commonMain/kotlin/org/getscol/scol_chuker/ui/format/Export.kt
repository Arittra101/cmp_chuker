package org.getscol.scol_chuker.ui.format

import org.getscol.scol_chuker.data.db.HttpTransaction
import org.getscol.scol_chuker.plugin.formatDateTime

// What a developer takes out of the inspector: a command they can replay, or text for a ticket.

/** Single-quoted shell strings: the only escape that matters is the quote itself. */
internal fun String.shellEscape(): String = replace("'", "'\\''")

internal fun buildCurl(tx: HttpTransaction): String = buildString {
    append("curl -X ${tx.method} \\\n")
    parseHeaders(tx.requestHeaders)
        // curl recomputes the length itself; passing the captured one can only be wrong.
        .filterNot { it.first.equals("Content-Length", ignoreCase = true) }
        .forEach { (name, value) ->
            append("  -H '$name: ${value.shellEscape()}' \\\n")
        }
    if (!tx.requestBody.isNullOrBlank()) {
        append("  --data-raw '${tx.requestBody.shellEscape()}' \\\n")
    }
    append("  '${tx.url.shellEscape()}'")
}

/** What a tester pastes into a bug report. */
internal fun buildTextReport(tx: HttpTransaction): String = buildString {
    appendLine("${tx.method} ${tx.url}")
    appendLine(if (tx.statusCode == 0) "FAILED: ${tx.statusMessage}" else "${tx.statusCode} ${tx.statusMessage}")
    appendLine("started: ${formatDateTime(tx.requestTime)}")
    appendLine("duration: ${formatDuration(tx.durationMs)}")
    appendLine()
    appendLine("--- request headers ---")
    appendLine(tx.requestHeaders.ifBlank { "(none)" })
    if (!tx.requestBody.isNullOrBlank()) {
        appendLine()
        appendLine("--- request body ---")
        appendLine(tx.requestBody)
    }
    appendLine()
    appendLine("--- response headers ---")
    appendLine(tx.responseHeaders.ifBlank { "(none)" })
    if (!tx.responseBody.isNullOrBlank()) {
        appendLine()
        appendLine("--- response body ---")
        appendLine(tx.responseBody)
    }
}
