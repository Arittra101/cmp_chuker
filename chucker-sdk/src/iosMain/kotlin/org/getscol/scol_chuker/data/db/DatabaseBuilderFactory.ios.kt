package org.getscol.scol_chuker.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

internal actual class DatabaseBuilderFactory {
    actual fun create(): RoomDatabase.Builder<ChuckerDatabase> =
        Room.databaseBuilder<ChuckerDatabase>(name = "${documentsDirectory()}/$CHUCKER_DB_FILE")
}

@OptIn(ExperimentalForeignApi::class)
private fun documentsDirectory(): String {
    val url: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(url?.path) { "Could not resolve the iOS documents directory" }
}
