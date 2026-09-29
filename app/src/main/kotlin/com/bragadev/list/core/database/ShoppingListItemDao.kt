package com.bragadev.list.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListItemDao {

    @Query("SELECT * FROM shopping_list_items WHERE listId = :listId ORDER BY createdAt ASC")
    fun observeByListId(listId: Long): Flow<List<ShoppingListItemEntity>>

    @Insert
    suspend fun insert(item: ShoppingListItemEntity): Long

    @Query("UPDATE shopping_list_items SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun setChecked(itemId: Long, isChecked: Boolean)
}
