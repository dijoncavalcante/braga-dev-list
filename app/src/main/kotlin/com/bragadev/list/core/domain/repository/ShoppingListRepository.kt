package com.bragadev.list.core.domain.repository

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ItemSortOrder
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

    suspend fun setSortOrder(listId: Long, sortOrder: ItemSortOrder): AppResult<Unit>

    suspend fun setShowPrices(listId: Long, showPrices: Boolean): AppResult<Unit>

    suspend fun setGroupByFortnight(listId: Long, groupByFortnight: Boolean): AppResult<Unit>

    /** Checks or unchecks every item of the list at once. */
    suspend fun setAllItemsChecked(listId: Long, isChecked: Boolean): AppResult<Unit>

    /** Deletes the checked items of the list, or all of them when [onlyChecked] is false. */
    suspend fun deleteItems(listId: Long, onlyChecked: Boolean): AppResult<Unit>

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
