package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

private const val COPY_PREFIX = "Cópia de "

/**
 * Makes a copy of a list with all of its items, e.g. to reuse last month's bills.
 * The copy is named "Cópia de <name>" (cut to [MAX_LIST_NAME_LENGTH]) and every item
 * starts unchecked.
 */
class DuplicateShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(list: ShoppingList): AppResult<ShoppingList> {
        val copyName = (COPY_PREFIX + list.name).take(MAX_LIST_NAME_LENGTH).trim()
        return repository.duplicateList(listId = list.id, newName = copyName)
    }
}
