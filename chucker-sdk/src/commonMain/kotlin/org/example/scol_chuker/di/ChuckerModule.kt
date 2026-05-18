package org.example.scol_chuker.di

import org.example.scol_chuker.data.db.DatabaseDriverFactory
import org.example.scolchuker.ChuckerDatabase
import org.example.scol_chuker.data.repository.TransactionRepository
import org.koin.dsl.module

internal fun chuckerModule(driverFactory: DatabaseDriverFactory) = module {
    single {
        ChuckerDatabase(driverFactory.createDriver())
    }
    single { TransactionRepository(get()) }
}
