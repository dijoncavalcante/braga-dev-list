package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

class SetItemCheckedUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(itemId: Long, isChecked: Boolean): AppResult<Unit> =
        repository.setItemChecked(itemId, isChecked)
}
