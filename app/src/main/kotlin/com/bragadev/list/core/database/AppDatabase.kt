package com.bragadev.list.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ShoppingListEntity::class, ShoppingListItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao

    abstract fun shoppingListItemDao(): ShoppingListItemDao
}
