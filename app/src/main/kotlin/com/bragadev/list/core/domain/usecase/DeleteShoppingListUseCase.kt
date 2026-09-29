package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.repository.ShoppingListRepository

/** Deletes a list together with all of its items. */
class DeleteShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(listId: Long): AppResult<Unit> = repository.deleteList(listId)
}
