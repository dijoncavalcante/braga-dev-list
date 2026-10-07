package com.bragadev.fincheck.core.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATABASE_NAME = "fincheck.db"

/**
 * Koin module providing the Room database instance and its DAOs.
 */
val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(*ALL_MIGRATIONS)
            .build()
    }
    single { get<AppDatabase>().shoppingListDao() }
    single { get<AppDatabase>().shoppingListItemDao() }
    single { get<AppDatabase>().incomeSettingsDao() }
    single { get<AppDatabase>().extraIncomeDao() }
}
