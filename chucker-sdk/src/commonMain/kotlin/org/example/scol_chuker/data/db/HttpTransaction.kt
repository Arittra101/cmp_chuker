package org.example.scol_chuker.data.db

internal data class HttpTransaction(
    val id: Long = 0,
    val method: String,
    val url: String,
    val requestHeaders: String,
    val requestBody: String?,
    val requestTime: Long,
    val statusCode: Int,
    val statusMessage: String,
    val responseHeaders: String,
    val responseBody: String?,
    val responseTime: Long,
    val durationMs: Long,
)
