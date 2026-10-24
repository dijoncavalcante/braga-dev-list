package com.bragadev.fincheck.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Room access to the `shopping_list_items` table. */
@Dao
interface ShoppingListItemDao {

    /** Items of list [listId], in the order they were added; emits again on every change. */
    @Query("SELECT * FROM shopping_list_items WHERE listId = :listId ORDER BY createdAt ASC")
    fun observeByListId(listId: Long): Flow<List<ShoppingListItemEntity>>

    /** Saves a new item and returns its generated id. */
    @Insert
    suspend fun insert(item: ShoppingListItemEntity): Long

    /** Updates only the editable fields; isChecked and createdAt are preserved. */
    @Query(
        """
        UPDATE shopping_list_items
        SET name = :name, quantity = :quantity, priceInCents = :priceInCents, dueDay = :dueDay,
            description = :description
        WHERE id = :itemId
        """,
    )
    suspend fun updateDetails(
        itemId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDay: Int?,
        description: String?,
    )

    /** Deletes the item with [itemId]. */
    @Query("DELETE FROM shopping_list_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Long)

    /** Checks or unchecks every item of list [listId] at once. */
    @Query("UPDATE shopping_list_items SET isChecked = :isChecked WHERE listId = :listId")
    suspend fun setAllChecked(listId: Long, isChecked: Boolean)

    /** Deletes the checked items of list [listId]. */
    @Query("DELETE FROM shopping_list_items WHERE listId = :listId AND isChecked = 1")
    suspend fun deleteCheckedByListId(listId: Long)

    /** Deletes every item of list [listId]. */
    @Query("DELETE FROM shopping_list_items WHERE listId = :listId")
    suspend fun deleteAllByListId(listId: Long)

    /** Checks or unchecks the item with [itemId]. */
    @Query("UPDATE shopping_list_items SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun setChecked(itemId: Long, isChecked: Boolean)
}
