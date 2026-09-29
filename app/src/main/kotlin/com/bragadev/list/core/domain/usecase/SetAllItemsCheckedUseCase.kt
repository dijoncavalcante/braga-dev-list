package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.repository.ShoppingListRepository

/** "Marcar todos os itens" / "Desmarcar todos os itens". */
class SetAllItemsCheckedUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(listId: Long, isChecked: Boolean): AppResult<Unit> =
        repository.setAllItemsChecked(listId, isChecked)
}
