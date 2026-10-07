package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

/** "Excluir itens": removes only the checked items, or every item of the list. */
class DeleteListItemsUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(listId: Long, onlyChecked: Boolean): AppResult<Unit> =
        repository.deleteItems(listId, onlyChecked)
}
