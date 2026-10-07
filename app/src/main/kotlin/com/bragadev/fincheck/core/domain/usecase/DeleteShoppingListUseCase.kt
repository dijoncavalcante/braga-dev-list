package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

/** Deletes a list together with all of its items. */
class DeleteShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(listId: Long): AppResult<Unit> = repository.deleteList(listId)
}
