package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.repository.ShoppingListRepository

private const val MAX_LIST_NAME_LENGTH = 60

class CreateShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(name: String): AppResult<ShoppingList> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty() || trimmedName.length > MAX_LIST_NAME_LENGTH) {
            return AppResult.Error(AppError.Validation(reason = "invalid_list_name"))
        }
        return repository.createList(trimmedName)
    }
}
