package com.bragadev.fincheck.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/** Room access to the `shopping_lists` table, including copying a list with its items. */
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
    /** Every list with its item count, newest first, in a single query. */
    fun observeListsWithItemCount(): Flow<List<ShoppingListWithItemCount>>

    /** The list with [listId]; emits null once it is deleted. */
    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    fun observeById(listId: Long): Flow<ShoppingListEntity?>

    /** Saves a new list and returns its generated id. */
    @Insert
    suspend fun insert(list: ShoppingListEntity): Long

    /** Changes the name of list [listId]. */
    @Query("UPDATE shopping_lists SET name = :name WHERE id = :listId")
    suspend fun rename(listId: Long, name: String)

    /** The list with [listId], read once; null when it does not exist. */
    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    suspend fun getById(listId: Long): ShoppingListEntity?

    /**
     * Saves the "Ordenar por" choice of the list, as an [com.bragadev.fincheck.core.domain.model.ItemSortOrder.code].
     */
    @Query("UPDATE shopping_lists SET sortOrder = :sortOrder WHERE id = :listId")
    suspend fun setSortOrder(listId: Long, sortOrder: Int)

    /** Saves the "Mostrar valor" toggle of the list. */
    @Query("UPDATE shopping_lists SET showPrices = :showPrices WHERE id = :listId")
    suspend fun setShowPrices(listId: Long, showPrices: Boolean)

    /** Saves the "Visualizar" choice of the list, as an [com.bragadev.fincheck.core.domain.model.ItemViewMode.code]. */
    @Query("UPDATE shopping_lists SET groupByFortnight = :viewMode WHERE id = :listId")
    suspend fun setViewMode(listId: Long, viewMode: Int)

    /** Items are removed together with the list (ON DELETE CASCADE). */
    @Query("DELETE FROM shopping_lists WHERE id = :listId")
    suspend fun deleteById(listId: Long)

    /** Copies every item of [sourceListId] into [targetListId], unchecked, keeping their order. */
    @Query(
        """
        INSERT INTO shopping_list_items (listId, name, quantity, priceInCents, dueDay, isChecked, createdAt, description)
        SELECT :targetListId, name, quantity, priceInCents, dueDay, 0, createdAt, description
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
