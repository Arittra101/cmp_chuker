package org.getscol.scol_chuker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "http_transactions")
internal data class HttpTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
