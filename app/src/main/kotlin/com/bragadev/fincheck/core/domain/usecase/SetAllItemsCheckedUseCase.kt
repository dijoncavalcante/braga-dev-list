package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

/** "Marcar todos os itens" / "Desmarcar todos os itens". */
class SetAllItemsCheckedUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(listId: Long, isChecked: Boolean): AppResult<Unit> =
        repository.setAllItemsChecked(listId, isChecked)
}
