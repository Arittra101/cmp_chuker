package org.getscol.scol_chuker.data.repository

import kotlinx.coroutines.flow.Flow
import org.getscol.scol_chuker.data.db.ChuckerDao
import org.getscol.scol_chuker.data.db.HttpTransaction

internal class TransactionRepository(private val dao: ChuckerDao) {

    fun getAll(): Flow<List<HttpTransaction>> = dao.selectAll()

    suspend fun getById(id: Long): HttpTransaction? = dao.selectById(id)

    suspend fun insert(transaction: HttpTransaction) = dao.insert(transaction)

    suspend fun clearAll() = dao.clearAll()
}
