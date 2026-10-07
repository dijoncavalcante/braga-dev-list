package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

class GetShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    operator fun invoke(listId: Long): Flow<ShoppingList?> = repository.observeList(listId)
}
