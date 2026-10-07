package com.bragadev.fincheck.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

/**
 * Version FinCheck is first published with. Every install of fincheck.db starts here: the
 * migrations of the pre-release versions 1..8 were removed because no device can ever hold those
 * versions under this app id and file name (they stay in the git history).
 */
const val BASELINE_DATABASE_VERSION = 9

@Database(
    entities = [ShoppingListEntity::class, ShoppingListItemEntity::class, IncomeSettingsEntity::class, ExtraIncomeEntity::class],
    version = 9,
    // Schemas are exported to app/schemas and committed: they are what DatabaseMigrationTest
    // checks every migration against.
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao

    abstract fun shoppingListItemDao(): ShoppingListItemDao

    abstract fun incomeSettingsDao(): IncomeSettingsDao

    abstract fun extraIncomeDao(): ExtraIncomeDao
}

/**
 * Every migration since [BASELINE_DATABASE_VERSION], in order. Used both by the app and by
 * DatabaseMigrationTest, so a migration added here is tested automatically.
 *
 * To change the database: bump `version` in [AppDatabase], add the `Migration(n, n + 1)` here
 * (never edit a published one), build once to export the new schema JSON, commit it and run the
 * instrumented tests. Users' data must survive every update: a broken migration makes the app
 * crash on launch.
 */
val ALL_MIGRATIONS: Array<Migration> = emptyArray()
