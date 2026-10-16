package org.getscol.scol_chuker.plugin

/** `14:32:08.411` — the clock column in the request list. */
internal expect fun formatClockTime(epochMillis: Long): String

/** `2026-10-15 14:32:08.411` — the full stamp shown on the detail screen. */
internal expect fun formatDateTime(epochMillis: Long): String
