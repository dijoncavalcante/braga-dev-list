package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

class DeleteListItemUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(itemId: Long): AppResult<Unit> = repository.deleteItem(itemId)
}
