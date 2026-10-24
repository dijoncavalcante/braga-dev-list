package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

/** Max length of a list name, shared by create and rename. */
const val MAX_LIST_NAME_LENGTH = 60

/** Creates a list. The name is trimmed, required and up to [MAX_LIST_NAME_LENGTH] characters. */
class CreateShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    /** Validates [name] and creates the list; returns it with its id. */
    suspend operator fun invoke(name: String): AppResult<ShoppingList> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty() || trimmedName.length > MAX_LIST_NAME_LENGTH) {
            return AppResult.Error(AppError.Validation(reason = "invalid_list_name"))
        }
        return repository.createList(trimmedName)
    }
}
