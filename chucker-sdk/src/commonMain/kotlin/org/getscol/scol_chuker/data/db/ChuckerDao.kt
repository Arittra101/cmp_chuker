package org.getscol.scol_chuker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ChuckerDao {

    @Query("SELECT * FROM http_transactions ORDER BY requestTime DESC")
    fun selectAll(): Flow<List<HttpTransaction>>

    @Query("SELECT * FROM http_transactions WHERE id = :id")
    suspend fun selectById(id: Long): HttpTransaction?

    @Insert
    suspend fun insert(transaction: HttpTransaction)

    @Query("DELETE FROM http_transactions")
    suspend fun clearAll()
}
