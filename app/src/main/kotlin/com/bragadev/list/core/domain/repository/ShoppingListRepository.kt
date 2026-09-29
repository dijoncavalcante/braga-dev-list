package com.bragadev.list.core.domain.repository

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain-owned contract. The Data layer implements this; Presentation only ever
 * depends on this interface, never on the Room/DAO implementation.
 */
interface ShoppingListRepository {
    fun observeLists(): Flow<List<ShoppingList>>

    fun observeList(listId: Long): Flow<ShoppingList?>

    fun observeItems(listId: Long): Flow<List<ShoppingListItem>>

    suspend fun createList(name: String): AppResult<ShoppingList>

    suspend fun renameList(listId: Long, name: String): AppResult<Unit>

    /** Deletes the list and all of its items. */
    suspend fun deleteList(listId: Long): AppResult<Unit>

    /** Creates a new list named [newName] with a copy of every item of [listId] (unchecked). */
    suspend fun duplicateList(listId: Long, newName: String): AppResult<ShoppingList>

    /** Current items of the list, read once (e.g. to share them). */
    suspend fun getItems(listId: Long): AppResult<List<ShoppingListItem>>

    suspend fun addItem(
        listId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDay: Int?,
    ): AppResult<ShoppingListItem>

    suspend fun updateItem(
        itemId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDay: Int?,
    ): AppResult<Unit>

    suspend fun deleteItem(itemId: Long): AppResult<Unit>

    suspend fun setItemChecked(itemId: Long, isChecked: Boolean): AppResult<Unit>
}
