package com.bragadev.list.features.home.presentation.state

import com.bragadev.list.core.domain.model.ShoppingList

data class HomeUiState(
    val isLoading: Boolean = true,
    val lists: List<ShoppingList> = emptyList(),
    val error: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && lists.isEmpty()
}
