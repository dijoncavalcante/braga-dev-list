package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

/** Items of one list. */
class GetListItemsUseCase(
    private val repository: ShoppingListRepository,
) {
    /** Emits the items of list [listId] again on every change. */
    operator fun invoke(listId: Long): Flow<List<ShoppingListItem>> = repository.observeItems(listId)
}
