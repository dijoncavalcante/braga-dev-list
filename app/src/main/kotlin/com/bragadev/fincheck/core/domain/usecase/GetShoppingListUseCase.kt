package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

/** One list with its preferences. */
class GetShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    /** Emits list [listId]; null once it is deleted. */
    operator fun invoke(listId: Long): Flow<ShoppingList?> = repository.observeList(listId)
}
