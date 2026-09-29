package com.bragadev.list.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Query(
        """
        SELECT l.id AS id, l.name AS name, l.createdAt AS createdAt, COUNT(i.id) AS itemCount
        FROM shopping_lists l
        LEFT JOIN shopping_list_items i ON i.listId = l.id
        GROUP BY l.id
        ORDER BY l.createdAt DESC
        """,
    )
    fun observeListsWithItemCount(): Flow<List<ShoppingListWithItemCount>>

    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    fun observeById(listId: Long): Flow<ShoppingListEntity?>

    @Insert
    suspend fun insert(list: ShoppingListEntity): Long

    @Query("UPDATE shopping_lists SET name = :name WHERE id = :listId")
    suspend fun rename(listId: Long, name: String)

    /** Items are removed together with the list (ON DELETE CASCADE). */
    @Query("DELETE FROM shopping_lists WHERE id = :listId")
    suspend fun deleteById(listId: Long)

    /** Copies every item of [sourceListId] into [targetListId], unchecked, keeping their order. */
    @Query(
        """
        INSERT INTO shopping_list_items (listId, name, quantity, priceInCents, dueDay, isChecked, createdAt)
        SELECT :targetListId, name, quantity, priceInCents, dueDay, 0, createdAt
        FROM shopping_list_items
        WHERE listId = :sourceListId
        """,
    )
    suspend fun copyItems(sourceListId: Long, targetListId: Long)

    /** Creates [copy] and copies the items of [sourceListId] into it, atomically. */
    @Transaction
    suspend fun duplicate(sourceListId: Long, copy: ShoppingListEntity): Long {
        val newListId = insert(copy)
        copyItems(sourceListId = sourceListId, targetListId = newListId)
        return newListId
    }
}
