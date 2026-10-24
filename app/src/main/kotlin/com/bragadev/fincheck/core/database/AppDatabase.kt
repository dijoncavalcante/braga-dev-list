package com.bragadev.fincheck.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version FinCheck is first published with. Every install of fincheck.db starts here: the
 * migrations of the pre-release versions 1..8 were removed because no device can ever hold those
 * versions under this app id and file name (they stay in the git history).
 */
const val BASELINE_DATABASE_VERSION = 9

@Database(
    entities = [
        ShoppingListEntity::class,
        ShoppingListItemEntity::class,
        IncomeSettingsEntity::class,
        ExtraIncomeEntity::class,
        BillEntity::class,
        BillMonthStatusEntity::class,
    ],
    version = 12,
    // Schemas are exported to app/schemas and committed: they are what DatabaseMigrationTest
    // checks every migration against.
    exportSchema = true,
)
/** The app's single Room database (`fincheck.db`): lists, items, income and the bills of the "Meses" tab. */
abstract class AppDatabase : RoomDatabase() {
    /** Lists and their preferences. */
    abstract fun shoppingListDao(): ShoppingListDao

    /** Items of the lists. */
    abstract fun shoppingListItemDao(): ShoppingListItemDao

    /** The single income settings row. */
    abstract fun incomeSettingsDao(): IncomeSettingsDao

    /** Extra incomes, besides the main income. */
    abstract fun extraIncomeDao(): ExtraIncomeDao

    /** Bills of the "Meses" tab and their month statuses. */
    abstract fun billDao(): BillDao
}

/**
 * 9 → 10: bills of the "Meses" tab, stored as rules, and what each one did in each month. Lists and
 * their items are left untouched: they keep working as standalone lists.
 */
val MIGRATION_9_10 =
    object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `bills` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, `priceInCents` INTEGER NOT NULL, `dueDay` INTEGER, " +
                    "`startMonth` INTEGER NOT NULL, `endMonth` INTEGER, `createdAt` INTEGER NOT NULL)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `bill_month_status` (`billId` INTEGER NOT NULL, `month` INTEGER NOT NULL, " +
                    "`isPaid` INTEGER NOT NULL, `isSkipped` INTEGER NOT NULL, PRIMARY KEY(`billId`, `month`), " +
                    "FOREIGN KEY(`billId`) REFERENCES `bills`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bill_month_status_billId` ON `bill_month_status` (`billId`)")
        }
    }

/** 10 → 11: an optional description on each list item; existing items start without one. */
val MIGRATION_10_11 =
    object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `shopping_list_items` ADD COLUMN `description` TEXT")
        }
    }

/** 11 → 12: an optional description on each bill; existing bills start without one. */
val MIGRATION_11_12 =
    object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `bills` ADD COLUMN `description` TEXT")
        }
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
val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
