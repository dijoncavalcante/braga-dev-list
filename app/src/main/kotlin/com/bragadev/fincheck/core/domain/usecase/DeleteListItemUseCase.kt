package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

/** Deletes one item of a list. */
class DeleteListItemUseCase(
    private val repository: ShoppingListRepository,
) {
    /** Deletes the item with [itemId]. */
    suspend operator fun invoke(itemId: Long): AppResult<Unit> = repository.deleteItem(itemId)
}
