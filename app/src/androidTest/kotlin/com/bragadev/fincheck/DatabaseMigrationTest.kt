package com.bragadev.fincheck

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bragadev.fincheck.core.database.ALL_MIGRATIONS
import com.bragadev.fincheck.core.database.AppDatabase
import com.bragadev.fincheck.core.database.BASELINE_DATABASE_VERSION
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration-test.db"

/**
 * Guards the users' data across app updates: a database created with the oldest published schema
 * must reach the current version through [ALL_MIGRATIONS], ending with exactly the schema the app
 * expects (Room checks it against app/schemas when opening). A migration added to
 * [ALL_MIGRATIONS] is covered here with no extra code.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun baselineDatabaseMigratesToTheCurrentSchemaKeepingItsData() {
        helper.createDatabase(TEST_DB, BASELINE_DATABASE_VERSION).use { db ->
            db.execSQL(
                "INSERT INTO shopping_lists (id, name, createdAt, sortOrder, showPrices, groupByFortnight) " +
                    "VALUES (1, 'Contas', 0, 0, 1, 1)",
            )
            db.execSQL(
                "INSERT INTO shopping_list_items (id, listId, name, quantity, priceInCents, dueDay, isChecked, createdAt) " +
                    "VALUES (1, 1, 'Energia', 1, 15000, 10, 0, 0)",
            )
        }

        // Opens the database exactly like the app does; Room fails here if any migration is missing
        // or leaves a schema different from the current one.
        val database = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            TEST_DB,
        ).addMigrations(*ALL_MIGRATIONS).build()

        try {
            database.openHelper.readableDatabase.query("SELECT name, priceInCents, dueDay FROM shopping_list_items").use { cursor ->
                assertEquals(1, cursor.count)
                cursor.moveToFirst()
                assertEquals("Energia", cursor.getString(0))
                assertEquals(15000, cursor.getLong(1))
                assertEquals(10, cursor.getInt(2))
            }
            // 9 → 10: the bills of the "Meses" tab start empty and accept a bill with its month status.
            val writable = database.openHelper.writableDatabase
            writable.execSQL(
                "INSERT INTO bills (id, name, priceInCents, dueDay, startMonth, endMonth, createdAt) " +
                    "VALUES (1, 'Internet', 10000, 10, 24321, NULL, 0)",
            )
            writable.execSQL("INSERT INTO bill_month_status (billId, month, isPaid, isSkipped) VALUES (1, 24321, 1, 0)")
            writable.query("SELECT COUNT(*) FROM bill_month_status").use { cursor ->
                cursor.moveToFirst()
                assertEquals(1, cursor.getInt(0))
            }
        } finally {
            database.close()
        }
    }
}
