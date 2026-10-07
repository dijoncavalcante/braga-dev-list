package com.bragadev.fincheck.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Query(
        """
        SELECT l.id AS id, l.name AS name, l.createdAt AS createdAt,
            l.sortOrder AS sortOrder, l.showPrices AS showPrices,
            l.groupByFortnight AS viewMode,
            COUNT(i.id) AS itemCount
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

    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    suspend fun getById(listId: Long): ShoppingListEntity?

    @Query("UPDATE shopping_lists SET sortOrder = :sortOrder WHERE id = :listId")
    suspend fun setSortOrder(listId: Long, sortOrder: Int)

    @Query("UPDATE shopping_lists SET showPrices = :showPrices WHERE id = :listId")
    suspend fun setShowPrices(listId: Long, showPrices: Boolean)

    @Query("UPDATE shopping_lists SET groupByFortnight = :viewMode WHERE id = :listId")
    suspend fun setViewMode(listId: Long, viewMode: Int)

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
