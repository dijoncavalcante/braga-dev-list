package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import com.bragadev.fincheck.core.util.extensions.MAX_DUE_DAY
import com.bragadev.fincheck.core.util.extensions.MIN_DUE_DAY

/**
 * Saves the edited fields of an existing item. Same rules as [AddListItemUseCase]:
 * name is required, quantity is at least 1, price is never negative and the due day,
 * when given, is between 1 and 31.
 */
class UpdateListItemUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(
        itemId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDay: Int?,
    ): AppResult<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return AppResult.Error(AppError.Validation(reason = "invalid_item_name"))
        }
        if (dueDay != null && dueDay !in MIN_DUE_DAY..MAX_DUE_DAY) {
            return AppResult.Error(AppError.Validation(reason = "invalid_due_day"))
        }
        return repository.updateItem(
            itemId = itemId,
            name = trimmedName,
            quantity = quantity.coerceAtLeast(1),
            priceInCents = priceInCents.coerceAtLeast(0),
            dueDay = dueDay,
        )
    }
}
