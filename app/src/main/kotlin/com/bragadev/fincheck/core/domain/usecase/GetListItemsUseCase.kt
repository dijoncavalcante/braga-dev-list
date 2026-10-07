package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

class GetListItemsUseCase(
    private val repository: ShoppingListRepository,
) {
    operator fun invoke(listId: Long): Flow<List<ShoppingListItem>> = repository.observeItems(listId)
}
