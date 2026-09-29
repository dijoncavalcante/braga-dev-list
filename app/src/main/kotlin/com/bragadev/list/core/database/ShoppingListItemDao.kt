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

    /** Updates only the editable fields; isChecked and createdAt are preserved. */
    @Query(
        """
        UPDATE shopping_list_items
        SET name = :name, quantity = :quantity, priceInCents = :priceInCents, dueDateMillis = :dueDateMillis
        WHERE id = :itemId
        """,
    )
    suspend fun updateDetails(
        itemId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDateMillis: Long?,
    )

    @Query("UPDATE shopping_list_items SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun setChecked(itemId: Long, isChecked: Boolean)
}
