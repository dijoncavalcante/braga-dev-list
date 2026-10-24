package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.MAX_DESCRIPTION_LENGTH
import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.core.domain.model.toDescription
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import com.bragadev.fincheck.core.util.extensions.MAX_DUE_DAY
import com.bragadev.fincheck.core.util.extensions.MIN_DUE_DAY

/**
 * Adds an item to a list. Rules: a name, a quantity of at least 1, a price never below zero, a due
 * day in 1..31 when given and a description of at most [MAX_DESCRIPTION_LENGTH] characters.
 */
class AddListItemUseCase(
    private val repository: ShoppingListRepository,
) {
    /**
     * Validates and saves the item; returns it with its id, or a validation error without touching
     * the repository.
     *
     * @param description optional note; trimmed, and saved as none when blank.
     */
    suspend operator fun invoke(
        listId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long = 0,
        dueDay: Int? = null,
        description: String? = null,
    ): AppResult<ShoppingListItem> {
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
        return repository.addItem(
            listId = listId,
            name = trimmedName,
            quantity = quantity.coerceAtLeast(1),
            priceInCents = priceInCents.coerceAtLeast(0),
            dueDay = dueDay,
            description = trimmedDescription,
        )
    }
}
