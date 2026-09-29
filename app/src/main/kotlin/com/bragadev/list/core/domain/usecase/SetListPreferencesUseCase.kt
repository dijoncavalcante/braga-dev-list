package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.repository.ShoppingListRepository

/** Saves the "Ordem alfabética" and "Mostrar valor" toggles of a list. */
class SetListPreferencesUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend fun setSortAlphabetically(listId: Long, enabled: Boolean): AppResult<Unit> =
        repository.setSortAlphabetically(listId, enabled)

    suspend fun setShowPrices(listId: Long, enabled: Boolean): AppResult<Unit> =
        repository.setShowPrices(listId, enabled)
}
