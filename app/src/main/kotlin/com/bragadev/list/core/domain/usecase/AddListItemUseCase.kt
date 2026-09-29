package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import com.bragadev.list.core.util.extensions.MAX_DUE_DAY
import com.bragadev.list.core.util.extensions.MIN_DUE_DAY

class AddListItemUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(
        listId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long = 0,
        dueDay: Int? = null,
    ): AppResult<ShoppingListItem> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return AppResult.Error(AppError.Validation(reason = "invalid_item_name"))
        }
        if (dueDay != null && dueDay !in MIN_DUE_DAY..MAX_DUE_DAY) {
            return AppResult.Error(AppError.Validation(reason = "invalid_due_day"))
        }
        return repository.addItem(
            listId = listId,
            name = trimmedName,
            quantity = quantity.coerceAtLeast(1),
            priceInCents = priceInCents.coerceAtLeast(0),
            dueDay = dueDay,
        )
    }
}
