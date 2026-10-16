package org.getscol.scol_chuker.data.repository

import kotlinx.coroutines.flow.Flow
import org.getscol.scol_chuker.data.db.ChuckerDao
import org.getscol.scol_chuker.data.db.HttpTransaction
import org.getscol.scol_chuker.data.db.TransactionSummary

internal class TransactionRepository(private val dao: ChuckerDao) {

    fun search(query: String, minStatus: Int, maxStatus: Int): Flow<List<TransactionSummary>> =
        dao.search(query.trim(), minStatus, maxStatus)

    fun observeCount(): Flow<Int> = dao.observeCount()

    fun observeErrorCount(): Flow<Int> = dao.observeErrorCount()

    fun observeLatest(): Flow<HttpTransaction?> = dao.observeLatest()

    suspend fun getById(id: Long): HttpTransaction? = dao.selectById(id)

    suspend fun insert(transaction: HttpTransaction) = dao.insert(transaction)

    suspend fun clearAll() = dao.clearAll()
}
