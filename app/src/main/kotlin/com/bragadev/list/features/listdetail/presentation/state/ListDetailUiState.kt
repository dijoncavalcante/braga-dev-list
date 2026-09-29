package com.bragadev.list.features.listdetail.presentation.state

import com.bragadev.list.core.domain.model.FortnightGroups
import com.bragadev.list.core.domain.model.ItemSortOrder
import com.bragadev.list.core.domain.model.ListSummary
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.groupByFortnight
import com.bragadev.list.core.domain.model.toSummary

data class ListDetailUiState(
    /** The list itself, with its "Ordem alfabética" / "Mostrar valor" preferences. */
    val list: ShoppingList? = null,
    val isLoading: Boolean = true,
    /** Items already in display order, following the list's "Ordenar por" choice. */
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

    val sortOrder: ItemSortOrder get() = list?.sortOrder ?: ItemSortOrder.ADDED

    val showPrices: Boolean get() = list?.showPrices ?: true

    val groupByFortnight: Boolean get() = list?.groupByFortnight ?: false

    /** Items split into 1ª / 2ª quinzena, used when [groupByFortnight] is on. */
    val fortnightGroups: FortnightGroups get() = items.groupByFortnight()

    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()

    /** Totals shown at the end of the list (always for the whole list); recalculated whenever the items change. */
    val summary: ListSummary get() = items.toSummary()
}
