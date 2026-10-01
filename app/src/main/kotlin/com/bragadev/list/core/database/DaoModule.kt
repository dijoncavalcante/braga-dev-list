package com.bragadev.list.core.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATABASE_NAME = "braga-dev-list.db"

/**
 * Koin module providing the Room database instance and its DAOs.
 */
val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
            )
            .build()
    }
    single { get<AppDatabase>().shoppingListDao() }
    single { get<AppDatabase>().shoppingListItemDao() }
    single { get<AppDatabase>().incomeSettingsDao() }
    single { get<AppDatabase>().extraIncomeDao() }
}
