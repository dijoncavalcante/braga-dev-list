package com.bragadev.list.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
}
