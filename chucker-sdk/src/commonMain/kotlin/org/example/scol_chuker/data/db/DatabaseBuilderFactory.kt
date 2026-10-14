package org.example.scol_chuker.data.db

import androidx.room.RoomDatabase

/** Produces a platform-configured Room builder — the only part of persistence that is platform-specific. */
internal expect class DatabaseBuilderFactory {
    fun create(): RoomDatabase.Builder<ChuckerDatabase>
}
