package com.bragadev.fincheck.core.domain.repository

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.ItemSortOrder
import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain-owned contract. The Data layer implements this; Presentation only ever
 * depends on this interface, never on the Room/DAO implementation.
 */
interface ShoppingListRepository {
    /** Every list with its item count, newest first. */
    fun observeLists(): Flow<List<ShoppingList>>

    /** The list with [listId]; emits null once it is deleted. */
    fun observeList(listId: Long): Flow<ShoppingList?>

    /** Items of list [listId], in the order they were added. */
    fun observeItems(listId: Long): Flow<List<ShoppingListItem>>

    /** Creates a list named [name] and returns it with its id. */
    suspend fun createList(name: String): AppResult<ShoppingList>

    /** Renames list [listId] to [name]. */
    suspend fun renameList(listId: Long, name: String): AppResult<Unit>

    /** Saves the "Ordenar por" choice of list [listId]. */
    suspend fun setSortOrder(listId: Long, sortOrder: ItemSortOrder): AppResult<Unit>

    /** Saves the "Mostrar valor" toggle of list [listId]. */
    suspend fun setShowPrices(listId: Long, showPrices: Boolean): AppResult<Unit>

    /** Saves the "Visualizar" choice of list [listId]. */
    suspend fun setViewMode(listId: Long, viewMode: ItemViewMode): AppResult<Unit>

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

    /** Adds an unchecked item to list [listId] and returns it with its id. */
    suspend fun addItem(
        listId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDay: Int?,
        description: String?,
    ): AppResult<ShoppingListItem>

    /** Saves the editable fields of item [itemId]; its checked state and creation time are kept. */
    suspend fun updateItem(
        itemId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDay: Int?,
        description: String?,
    ): AppResult<Unit>

    /** Deletes the item with [itemId]. */
    suspend fun deleteItem(itemId: Long): AppResult<Unit>

    /** Checks or unchecks the item with [itemId]. */
    suspend fun setItemChecked(itemId: Long, isChecked: Boolean): AppResult<Unit>
}
