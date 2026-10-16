package org.getscol.scol_chuker.plugin

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()

internal actual fun formatClockTime(epochMillis: Long): String =
    format(epochMillis, "HH:mm:ss.SSS")

internal actual fun formatDateTime(epochMillis: Long): String =
    format(epochMillis, "yyyy-MM-dd HH:mm:ss.SSS")

private fun format(epochMillis: Long, pattern: String): String =
    java.text.SimpleDateFormat(pattern, java.util.Locale.US).format(java.util.Date(epochMillis))
