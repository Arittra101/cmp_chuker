package org.getscol.scol_chuker.ui.format

internal fun formatBytes(bytes: Long): String = when {
    bytes <= 0L -> "0 B"
    bytes < 1_024 -> "$bytes B"
    bytes < 1_024 * 1_024 -> "${oneDecimal(bytes / 1_024.0)} kB"
    else -> "${oneDecimal(bytes / (1_024.0 * 1_024.0))} MB"
}

internal fun formatDuration(millis: Long): String = when {
    millis < 1_000 -> "$millis ms"
    millis < 60_000 -> "${oneDecimal(millis / 1_000.0)} s"
    else -> "${millis / 60_000}m ${(millis % 60_000) / 1_000}s"
}

private fun oneDecimal(value: Double): String {
    val scaled = (value * 10).toLong()
    return "${scaled / 10}.${scaled % 10}"
}

/** `https://api.example.com/v1/users?page=2` -> `api.example.com` */
internal fun urlHost(url: String): String {
    val afterScheme = url.substringAfter("://", url)
    return afterScheme.substringBefore('/').substringBefore('?')
}

/** …and the part a developer actually scans for: `/v1/users?page=2` */
internal fun urlPath(url: String): String {
    val afterScheme = url.substringAfter("://", url)
    val slash = afterScheme.indexOf('/')
    return if (slash < 0) "/" else afterScheme.substring(slash)
}

/** Headers are stored as `Name: value` lines by the capture plugin. */
internal fun parseHeaders(raw: String): List<Pair<String, String>> =
    raw.lines()
        .filter { it.isNotBlank() }
        .map { line ->
            val name = line.substringBefore(':').trim()
            val value = line.substringAfter(':', "").trim()
            name to value
        }

internal fun headerValue(raw: String, name: String): String? =
    parseHeaders(raw).firstOrNull { it.first.equals(name, ignoreCase = true) }?.second

internal fun contentTypeOf(headers: String): String? =
    headerValue(headers, "Content-Type")?.substringBefore(';')?.trim()?.takeIf { it.isNotEmpty() }
