package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.repository.ShoppingListRepository

/**
 * Saves the edited fields of an existing item. Same rules as [AddListItemUseCase]:
 * name is required, quantity is at least 1 and price is never negative.
 */
class UpdateListItemUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(
        itemId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDateMillis: Long?,
    ): AppResult<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return AppResult.Error(AppError.Validation(reason = "invalid_item_name"))
        }
        return repository.updateItem(
            itemId = itemId,
            name = trimmedName,
            quantity = quantity.coerceAtLeast(1),
            priceInCents = priceInCents.coerceAtLeast(0),
            dueDateMillis = dueDateMillis,
        )
    }
}
