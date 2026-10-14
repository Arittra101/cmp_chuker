package org.example.scol_chuker.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

internal actual class DatabaseBuilderFactory(private val context: Context) {
    actual fun create(): RoomDatabase.Builder<ChuckerDatabase> {
        val appContext = context.applicationContext
        return Room.databaseBuilder<ChuckerDatabase>(
            context = appContext,
            name = appContext.getDatabasePath(CHUCKER_DB_FILE).absolutePath,
        )
    }
}
