package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

/** Every list of the "Listas" tab. */
class GetShoppingListsUseCase(
    private val repository: ShoppingListRepository,
) {
    /** Emits every list with its item count, newest first. */
    operator fun invoke(): Flow<List<ShoppingList>> = repository.observeLists()
}
