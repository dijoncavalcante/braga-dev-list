package com.bragadev.list.features.listdetail.presentation.state

import com.bragadev.list.core.domain.model.ListSummary
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.toSummary

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

    /** Totals shown at the end of the list; recalculated whenever the items change. */
    val summary: ListSummary get() = items.toSummary()
}
