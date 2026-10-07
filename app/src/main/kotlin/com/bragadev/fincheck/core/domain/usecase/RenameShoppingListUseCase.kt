package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

/** Same name rules as [CreateShoppingListUseCase]: required and up to [MAX_LIST_NAME_LENGTH] chars. */
class RenameShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(listId: Long, name: String): AppResult<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty() || trimmedName.length > MAX_LIST_NAME_LENGTH) {
            return AppResult.Error(AppError.Validation(reason = "invalid_list_name"))
        }
        return repository.renameList(listId, trimmedName)
    }
}
