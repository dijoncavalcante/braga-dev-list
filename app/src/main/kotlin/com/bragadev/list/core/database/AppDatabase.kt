package com.bragadev.list.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ShoppingListEntity::class, ShoppingListItemEntity::class],
    version = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao

    abstract fun shoppingListItemDao(): ShoppingListItemDao
}

/**
 * v1 -> v2: adds the item unit price (in cents). Existing items get 0 (no price).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE shopping_list_items ADD COLUMN priceInCents INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * v2 -> v3: adds the optional item due date. Existing items get null (no due date).
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE shopping_list_items ADD COLUMN dueDateMillis INTEGER")
    }
}

/**
 * v3 -> v4: the full due date is replaced by just the due day of the month (1..31).
 * Existing dates keep their day (10/10/2026 -> 10). Dates were stored as UTC midnight,
 * so the day is read in UTC.
 *
 * SQLite on older Android versions (minSdk 24) has no DROP COLUMN, so the table is
 * recreated with the new schema and the rows are copied over.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `shopping_list_items_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `listId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `quantity` INTEGER NOT NULL,
                `priceInCents` INTEGER NOT NULL DEFAULT 0,
                `dueDay` INTEGER,
                `isChecked` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`listId`) REFERENCES `shopping_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `shopping_list_items_new`
                (`id`, `listId`, `name`, `quantity`, `priceInCents`, `dueDay`, `isChecked`, `createdAt`)
            SELECT
                `id`, `listId`, `name`, `quantity`, `priceInCents`,
                CASE WHEN `dueDateMillis` IS NULL THEN NULL
                     ELSE CAST(strftime('%d', `dueDateMillis` / 1000, 'unixepoch') AS INTEGER)
                END,
                `isChecked`, `createdAt`
            FROM `shopping_list_items`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `shopping_list_items`")
        db.execSQL("ALTER TABLE `shopping_list_items_new` RENAME TO `shopping_list_items`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_list_items_listId` ON `shopping_list_items` (`listId`)")
    }
}

/**
 * v4 -> v5: per-list display preferences of the list screen menu ("Ordem alfabética"
 * off and "Mostrar valor" on for every existing list).
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE shopping_lists ADD COLUMN sortAlphabetically INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE shopping_lists ADD COLUMN showPrices INTEGER NOT NULL DEFAULT 1")
    }
}

/** v5 -> v6: "Mostrar por quinzena" preference, off for every existing list. */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE shopping_lists ADD COLUMN groupByFortnight INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * v6 -> v7: the "Ordem alfabética" on/off is replaced by a single "Ordenar por" choice
 * (0 = ordem de adição, 1 = ordem alfabética, 2 = vencimento), so two orders can never
 * be on at the same time. Lists that were A→Z stay A→Z; the others keep the order added.
 *
 * SQLite on older Android versions (minSdk 24) has no DROP/RENAME COLUMN, so the table
 * is recreated and the rows copied with the same ids. Room only turns foreign keys on after
 * migrations run; still, if they are on, dropping the old table would cascade-delete the
 * items, so they are backed up first and restored afterwards.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val foreignKeysOn = db.query("PRAGMA foreign_keys").use { it.moveToFirst() && it.getInt(0) == 1 }
        if (foreignKeysOn) {
            db.execSQL("CREATE TEMP TABLE `items_backup` AS SELECT * FROM `shopping_list_items`")
        }
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `shopping_lists_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `sortOrder` INTEGER NOT NULL DEFAULT 0,
                `showPrices` INTEGER NOT NULL DEFAULT 1,
                `groupByFortnight` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `shopping_lists_new` (`id`, `name`, `createdAt`, `sortOrder`, `showPrices`, `groupByFortnight`)
            SELECT `id`, `name`, `createdAt`,
                CASE WHEN `sortAlphabetically` = 1 THEN 1 ELSE 0 END,
                `showPrices`, `groupByFortnight`
            FROM `shopping_lists`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `shopping_lists`")
        db.execSQL("ALTER TABLE `shopping_lists_new` RENAME TO `shopping_lists`")
        if (foreignKeysOn) {
            db.execSQL("INSERT OR IGNORE INTO `shopping_list_items` SELECT * FROM `items_backup`")
            db.execSQL("DROP TABLE `items_backup`")
        }
    }
}
