package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

class GetShoppingListsUseCase(
    private val repository: ShoppingListRepository,
) {
    operator fun invoke(): Flow<List<ShoppingList>> = repository.observeLists()
}
