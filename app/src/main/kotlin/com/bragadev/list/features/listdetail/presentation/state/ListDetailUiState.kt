package com.bragadev.list.features.listdetail.presentation.state

import com.bragadev.list.core.domain.model.ListSummary
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.toSummary

data class ListDetailUiState(
    /** The list itself, with its "Ordem alfabética" / "Mostrar valor" preferences. */
    val list: ShoppingList? = null,
    val isLoading: Boolean = true,
    /** Items already in display order (A→Z or order added, following [list]). */
    val items: List<ShoppingListItem> = emptyList(),
    val error: String? = null,
    val isAddItemDialogVisible: Boolean = false,
    /** Item being edited in the dialog; null = edit dialog closed. */
    val editingItem: ShoppingListItem? = null,
    val isRenameDialogVisible: Boolean = false,
    val showRenameError: Boolean = false,
    val isDeleteItemsDialogVisible: Boolean = false,
    /** Text ready to be handed to the Android share sheet; consumed by the screen. */
    val pendingShareText: String? = null,
) {
    val listName: String get() = list?.name.orEmpty()

    val sortAlphabetically: Boolean get() = list?.sortAlphabetically ?: false

    val showPrices: Boolean get() = list?.showPrices ?: true

    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()

    /** Totals shown at the end of the list; recalculated whenever the items change. */
    val summary: ListSummary get() = items.toSummary()
}
