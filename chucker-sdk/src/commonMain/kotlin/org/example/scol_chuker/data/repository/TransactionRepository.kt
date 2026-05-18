package org.example.scol_chuker.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.example.scol_chuker.data.db.HttpTransaction
import org.example.scolchuker.ChuckerDatabase
import org.example.scolchuker.Http_transactions

internal class TransactionRepository(private val database: ChuckerDatabase) {

    private val queries get() = database.chuckerQueries

    fun getAll(): Flow<List<HttpTransaction>> =
        queries.selectAll().asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map { it.toHttpTransaction() }
        }

    suspend fun getById(id: Long): HttpTransaction? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toHttpTransaction()
    }

    suspend fun insert(transaction: HttpTransaction) = withContext(Dispatchers.IO) {
        queries.insert(
            method = transaction.method,
            url = transaction.url,
            requestHeaders = transaction.requestHeaders,
            requestBody = transaction.requestBody,
            requestTime = transaction.requestTime,
            statusCode = transaction.statusCode.toLong(),
            statusMessage = transaction.statusMessage,
            responseHeaders = transaction.responseHeaders,
            responseBody = transaction.responseBody,
            responseTime = transaction.responseTime,
            durationMs = transaction.durationMs,
        )
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        queries.clearAll()
    }

    private fun Http_transactions.toHttpTransaction() = HttpTransaction(
        id = id,
        method = method,
        url = url,
        requestHeaders = requestHeaders,
        requestBody = requestBody,
        requestTime = requestTime,
        statusCode = statusCode.toInt(),
        statusMessage = statusMessage,
        responseHeaders = responseHeaders,
        responseBody = responseBody,
        responseTime = responseTime,
        durationMs = durationMs,
    )
}
