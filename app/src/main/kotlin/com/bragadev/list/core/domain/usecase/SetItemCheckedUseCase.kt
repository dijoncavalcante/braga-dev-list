package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.repository.ShoppingListRepository

class SetItemCheckedUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(itemId: Long, isChecked: Boolean): AppResult<Unit> =
        repository.setItemChecked(itemId, isChecked)
}
