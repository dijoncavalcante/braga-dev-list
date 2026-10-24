package com.bragadev.fincheck.features.listdetail.presentation.state

import com.bragadev.fincheck.core.domain.model.ItemSortOrder
import com.bragadev.fincheck.core.domain.model.ListSummary
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.model.toSummary

data class ListDetailUiState(
    /** The list itself, with its "Ordenar por" / "Mostrar valor" preferences. */
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
    /** "Transformar em contas do mês" dialog, with the months the bills can start from. */
    val isConvertDialogVisible: Boolean = false,
    val convertMonthOptions: List<YearMonth> = emptyList(),
    /** How many bills were just created from the list; consumed by the screen (snackbar). */
    val convertedBillsCount: Int? = null,
    /**
     * Eye of the toolbar, app-wide: amounts show as "R$ ••••". Independent from the list's
     * "Mostrar valor", which decides whether item prices are shown at all.
     */
    val amountsHidden: Boolean = false,
) {
    val listName: String get() = list?.name.orEmpty()

    val sortOrder: ItemSortOrder get() = list?.sortOrder ?: ItemSortOrder.ADDED

    val showPrices: Boolean get() = list?.showPrices ?: true

    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()

    /** Totals shown at the end of the list (always for the whole list); recalculated whenever the items change. */
    val summary: ListSummary get() = items.toSummary()
}
