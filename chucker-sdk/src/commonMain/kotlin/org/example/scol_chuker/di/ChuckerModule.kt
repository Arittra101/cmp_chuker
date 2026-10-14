package org.example.scol_chuker.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.example.scol_chuker.data.db.ChuckerDatabase
import org.example.scol_chuker.data.db.DatabaseBuilderFactory
import org.example.scol_chuker.data.repository.TransactionRepository
import org.koin.dsl.module

internal fun chuckerModule(builderFactory: DatabaseBuilderFactory) = module {
    single<ChuckerDatabase> {
        builderFactory.create()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            // Captured traffic is disposable debug data — never block a host app on a migration.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { TransactionRepository(get<ChuckerDatabase>().chuckerDao()) }
}
