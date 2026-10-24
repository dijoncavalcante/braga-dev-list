package com.bragadev.fincheck.features.listdetail.presentation.state

import com.bragadev.fincheck.core.domain.model.ItemSortOrder
import com.bragadev.fincheck.core.domain.model.ListSummary
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.model.toSummary

/** State of a list's screen, its settings screen and their dialogs. */
data class ListDetailUiState(
    /** The list itself, with its "Ordenar por" / "Mostrar valor" preferences. */
    val list: ShoppingList? = null,
    /** True until the list and its items are read for the first time. */
    val isLoading: Boolean = true,
    /** Items already in display order, following the list's "Ordenar por" choice. */
    val items: List<ShoppingListItem> = emptyList(),
    /** Message shown when the list could not be read; null otherwise. */
    val error: String? = null,
    /** The add item sheet is open. */
    val isAddItemDialogVisible: Boolean = false,
    /** Item being edited in the dialog; null = edit dialog closed. */
    val editingItem: ShoppingListItem? = null,
    /** The rename dialog is open. */
    val isRenameDialogVisible: Boolean = false,
    /** The rename dialog shows that the name is empty or too long. */
    val showRenameError: Boolean = false,
    /** The "Excluir itens" dialog is open. */
    val isDeleteItemsDialogVisible: Boolean = false,
    /** Text ready to be handed to the Android share sheet; consumed by the screen. */
    val pendingShareText: String? = null,
    /** "Transformar em contas do mês" dialog, with the months the bills can start from. */
    val isConvertDialogVisible: Boolean = false,
    /** Months the bills can start from: the current one and the next ones. */
    val convertMonthOptions: List<YearMonth> = emptyList(),
    /** How many bills were just created from the list; consumed by the screen (snackbar). */
    val convertedBillsCount: Int? = null,
    /**
     * Eye of the toolbar, app-wide: amounts show as "R$ ••••". Independent from the list's
     * "Mostrar valor", which decides whether item prices are shown at all.
     */
    val amountsHidden: Boolean = false,
) {
    /** Name of the list; empty while loading. */
    val listName: String get() = list?.name.orEmpty()

    /** "Ordenar por" choice of the list. */
    val sortOrder: ItemSortOrder get() = list?.sortOrder ?: ItemSortOrder.ADDED

    /** "Mostrar valor" toggle of the list. */
    val showPrices: Boolean get() = list?.showPrices ?: true

    /** Loaded with no error and no item: the empty state is shown. */
    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()

    /** Totals shown at the end of the list (always for the whole list); recalculated whenever the items change. */
    val summary: ListSummary get() = items.toSummary()
}
