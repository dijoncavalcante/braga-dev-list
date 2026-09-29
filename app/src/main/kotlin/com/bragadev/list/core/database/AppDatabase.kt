package com.bragadev.list.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ShoppingListEntity::class, ShoppingListItemEntity::class],
    version = 5,
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
