package org.getscol.scol_chuker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One captured call. [statusCode] `0` means the call never produced a response (DNS failure,
 * timeout, TLS error…) and [statusMessage] then holds the error instead — a failed request is
 * exactly the one a tester most needs to see, so it is stored like any other.
 */
@Entity(tableName = "http_transactions")
internal data class HttpTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val method: String,
    val url: String,
    val requestHeaders: String,
    val requestBody: String?,
    val requestBodySize: Long = 0,
    val requestTime: Long,
    val statusCode: Int,
    val statusMessage: String,
    val responseHeaders: String,
    val responseBody: String?,
    val responseBodySize: Long = 0,
    val responseTime: Long,
    val durationMs: Long,
)

// Extensions, not members: Room's processor only ever sees columns on the entity itself.
internal val HttpTransaction.isFailure: Boolean get() = statusCode == 0
internal val HttpTransaction.isError: Boolean get() = statusCode == 0 || statusCode >= 400
