package org.example.scol_chuker.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.example.scolchuker.ChuckerDatabase

internal actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(ChuckerDatabase.Schema, "chucker.db")
}
