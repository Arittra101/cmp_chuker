package org.getscol.scol_chuker.data.db

/**
 * The list only needs the scannable columns. Selecting whole rows there would pull every captured
 * request *and* response body into memory on each insert — a multi-megabyte download would stall
 * the list for a row that shows nine short fields.
 */
internal data class TransactionSummary(
    val id: Long,
    val method: String,
    val url: String,
    val statusCode: Int,
    val statusMessage: String,
    val requestTime: Long,
    val durationMs: Long,
    val requestBodySize: Long,
    val responseBodySize: Long,
)

internal val TransactionSummary.isFailure: Boolean get() = statusCode == 0
internal val TransactionSummary.isError: Boolean get() = statusCode == 0 || statusCode >= 400
