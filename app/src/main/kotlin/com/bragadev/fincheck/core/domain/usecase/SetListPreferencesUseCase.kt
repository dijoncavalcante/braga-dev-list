package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.ItemSortOrder
import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

/** Saves the "Ordenar por" choice, the "Mostrar valor" toggle and the "Visualizar" (todos / ciclos financeiros / quinzenas) choice of a list. */
class SetListPreferencesUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend fun setSortOrder(listId: Long, sortOrder: ItemSortOrder): AppResult<Unit> =
        repository.setSortOrder(listId, sortOrder)

    suspend fun setShowPrices(listId: Long, enabled: Boolean): AppResult<Unit> =
        repository.setShowPrices(listId, enabled)

    suspend fun setViewMode(listId: Long, viewMode: ItemViewMode): AppResult<Unit> =
        repository.setViewMode(listId, viewMode)
}
