package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ItemSortOrder
import com.bragadev.list.core.domain.repository.ShoppingListRepository

/** Saves the "Ordenar por" choice and the "Mostrar valor" / "Mostrar por quinzena" toggles of a list. */
class SetListPreferencesUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend fun setSortOrder(listId: Long, sortOrder: ItemSortOrder): AppResult<Unit> =
        repository.setSortOrder(listId, sortOrder)

    suspend fun setShowPrices(listId: Long, enabled: Boolean): AppResult<Unit> =
        repository.setShowPrices(listId, enabled)

    suspend fun setGroupByFortnight(listId: Long, enabled: Boolean): AppResult<Unit> =
        repository.setGroupByFortnight(listId, enabled)
}
