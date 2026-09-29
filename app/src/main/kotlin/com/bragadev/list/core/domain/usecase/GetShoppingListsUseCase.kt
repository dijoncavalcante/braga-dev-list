package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

class GetShoppingListsUseCase(
    private val repository: ShoppingListRepository,
) {
    operator fun invoke(): Flow<List<ShoppingList>> = repository.observeLists()
}
