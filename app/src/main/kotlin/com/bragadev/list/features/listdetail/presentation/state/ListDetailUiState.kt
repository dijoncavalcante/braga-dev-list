package com.bragadev.list.features.listdetail.presentation.state

import com.bragadev.list.core.domain.model.ShoppingListItem

data class ListDetailUiState(
    val listName: String = "",
    val isLoading: Boolean = true,
    val items: List<ShoppingListItem> = emptyList(),
    val error: String? = null,
    val isAddItemDialogVisible: Boolean = false,
    /** Item being edited in the dialog; null = edit dialog closed. */
    val editingItem: ShoppingListItem? = null,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()
}
