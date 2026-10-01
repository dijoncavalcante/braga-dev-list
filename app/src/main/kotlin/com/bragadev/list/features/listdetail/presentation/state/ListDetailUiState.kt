package com.bragadev.list.features.listdetail.presentation.state

import com.bragadev.list.core.domain.model.CalendarDate
import com.bragadev.list.core.domain.model.ExtraIncome
import com.bragadev.list.core.domain.model.FinancialOverview
import com.bragadev.list.core.domain.model.IncomeSettings
import com.bragadev.list.core.domain.model.ItemSortOrder
import com.bragadev.list.core.domain.model.ListSummary
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.toSummary

data class ListDetailUiState(
    /** The list itself, with its "Ordenar por" / "Mostrar valor" / "Visualizar" preferences. */
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
    /** How the user receives their income; null until they set it up. Shared by every list. */
    val incomeSettings: IncomeSettings? = null,
    /** Day the cycles below were calculated for. */
    val today: CalendarDate = CalendarDate.today(),
    /** Current and next financial cycles of this list; null without [incomeSettings]. */
    val financialOverview: FinancialOverview? = null,
    val isIncomeSettingsVisible: Boolean = false,
    /** Extra incomes ("Outras entradas"), shared by every list; optional. */
    val extraIncomes: List<ExtraIncome> = emptyList(),
    val isExtraIncomeSheetVisible: Boolean = false,
    /** Extra income open in the sheet; null = adding a new one. */
    val editingExtraIncome: ExtraIncome? = null,
    /**
     * Eye of the toolbar, app-wide: amounts show as "R$ ••••". Independent from the list's
     * "Mostrar valor", which decides whether item prices are shown at all.
     */
    val amountsHidden: Boolean = false,
) {
    val listName: String get() = list?.name.orEmpty()

    val sortOrder: ItemSortOrder get() = list?.sortOrder ?: ItemSortOrder.ADDED

    val showPrices: Boolean get() = list?.showPrices ?: true

    /** "Visualizar: Ciclos financeiros"; otherwise "Todos". */
    val groupByCycle: Boolean get() = list?.groupByCycle ?: false

    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()

    /** Totals shown at the end of the list (always for the whole list); recalculated whenever the items change. */
    val summary: ListSummary get() = items.toSummary()
}
