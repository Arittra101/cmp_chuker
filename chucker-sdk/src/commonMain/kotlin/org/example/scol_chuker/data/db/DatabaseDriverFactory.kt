package org.example.scol_chuker.data.db

import app.cash.sqldelight.db.SqlDriver

internal expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
