package org.example.scol_chuker.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * Deliberately NOT the old SQLDelight name (`chucker.db`): on an upgrade Room would find that file,
 * fail its identity check and crash the host app. A fresh name keeps the two from ever meeting.
 */
internal const val CHUCKER_DB_FILE = "chucker_room.db"

@Database(entities = [HttpTransaction::class], version = 1)
@ConstructedBy(ChuckerDatabaseConstructor::class)
internal abstract class ChuckerDatabase : RoomDatabase() {
    abstract fun chuckerDao(): ChuckerDao
}

/** Room generates the `actual` per target — see https://developer.android.com/kotlin/multiplatform/room */
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
internal expect object ChuckerDatabaseConstructor : RoomDatabaseConstructor<ChuckerDatabase> {
    override fun initialize(): ChuckerDatabase
}
