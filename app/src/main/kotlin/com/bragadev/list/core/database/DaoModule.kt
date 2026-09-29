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
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, DATABASE_NAME).build()
    }
    single { get<AppDatabase>().shoppingListDao() }
    single { get<AppDatabase>().shoppingListItemDao() }
}
