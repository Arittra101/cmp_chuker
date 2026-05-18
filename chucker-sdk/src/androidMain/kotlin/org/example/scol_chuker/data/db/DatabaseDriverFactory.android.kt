package org.example.scol_chuker.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.example.scolchuker.ChuckerDatabase

internal actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(ChuckerDatabase.Schema, context.applicationContext, "chucker.db")
}
