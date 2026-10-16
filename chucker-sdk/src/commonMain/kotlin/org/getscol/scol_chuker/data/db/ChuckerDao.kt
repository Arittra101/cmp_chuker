package org.getscol.scol_chuker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ChuckerDao {

    /**
     * Filtering runs in SQLite, not in the UI: a tester searching for an order id needs it matched
     * inside bodies too, and that is a scan no composable should be doing on every keystroke.
     * `statusCode` 0 marks a failed call, so `BETWEEN 0 AND 999` is simply "everything".
     */
    @Query(
        """
        SELECT id, method, url, statusCode, statusMessage, requestTime, durationMs,
               requestBodySize, responseBodySize
        FROM http_transactions
        WHERE statusCode BETWEEN :minStatus AND :maxStatus
          AND (:query = ''
               OR url LIKE '%' || :query || '%'
               OR method LIKE '%' || :query || '%'
               OR CAST(statusCode AS TEXT) LIKE '%' || :query || '%'
               OR requestBody LIKE '%' || :query || '%'
               OR responseBody LIKE '%' || :query || '%')
        ORDER BY requestTime DESC
        """
    )
    fun search(query: String, minStatus: Int, maxStatus: Int): Flow<List<TransactionSummary>>

    @Query("SELECT COUNT(*) FROM http_transactions")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM http_transactions WHERE statusCode = 0 OR statusCode >= 400")
    fun observeErrorCount(): Flow<Int>

    /** The floating button watches this one row — it never needs the whole table. */
    @Query("SELECT * FROM http_transactions ORDER BY requestTime DESC LIMIT 1")
    fun observeLatest(): Flow<HttpTransaction?>

    @Query("SELECT * FROM http_transactions WHERE id = :id")
    suspend fun selectById(id: Long): HttpTransaction?

    @Insert
    suspend fun insert(transaction: HttpTransaction)

    @Query("DELETE FROM http_transactions")
    suspend fun clearAll()
}
