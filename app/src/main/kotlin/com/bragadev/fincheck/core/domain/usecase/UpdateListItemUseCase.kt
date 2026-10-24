package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.MAX_DESCRIPTION_LENGTH
import com.bragadev.fincheck.core.domain.model.toDescription
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import com.bragadev.fincheck.core.util.extensions.MAX_DUE_DAY
import com.bragadev.fincheck.core.util.extensions.MIN_DUE_DAY

/**
 * Saves the edited fields of an existing item. Same rules as [AddListItemUseCase]:
 * name is required, quantity is at least 1, price is never negative, the due day,
 * when given, is between 1 and 31 and the description has at most
 * [MAX_DESCRIPTION_LENGTH] characters (a blank one is saved as none).
 */
class UpdateListItemUseCase(
    private val repository: ShoppingListRepository,
) {
    /**
     * Validates the new values and saves them in item [itemId]; returns a validation error without touching the repository.
     */
    suspend operator fun invoke(
        itemId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDay: Int?,
        description: String? = null,
    ): AppResult<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return AppResult.Error(AppError.Validation(reason = "invalid_item_name"))
        }
        if (dueDay != null && dueDay !in MIN_DUE_DAY..MAX_DUE_DAY) {
            return AppResult.Error(AppError.Validation(reason = "invalid_due_day"))
        }
        val trimmedDescription = description.toDescription()
        if (trimmedDescription != null && trimmedDescription.length > MAX_DESCRIPTION_LENGTH) {
            return AppResult.Error(AppError.Validation(reason = "invalid_item_description"))
        }
        return repository.updateItem(
            itemId = itemId,
            name = trimmedName,
            quantity = quantity.coerceAtLeast(1),
            priceInCents = priceInCents.coerceAtLeast(0),
            dueDay = dueDay,
            description = trimmedDescription,
        )
    }
}
