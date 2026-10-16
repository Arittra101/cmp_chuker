package org.getscol.scol_chuker.data.db

import androidx.room.RoomDatabase

internal expect class DatabaseBuilderFactory {
    fun create(): RoomDatabase.Builder<ChuckerDatabase>
}
