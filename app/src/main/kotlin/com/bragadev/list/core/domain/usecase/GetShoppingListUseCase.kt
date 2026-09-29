package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

class GetShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    operator fun invoke(listId: Long): Flow<ShoppingList?> = repository.observeList(listId)
}
