package com.bragadev.fincheck.features.months.presentation.state

import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.ExtraIncome
import com.bragadev.fincheck.core.domain.model.FinancialOverview
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.MonthBill
import com.bragadev.fincheck.core.domain.model.MonthOverview
import com.bragadev.fincheck.core.domain.model.YearMonth

/** One month of the strip at the top: its balance (or bills total, without income) at a glance. */
data class MonthStripEntry(
    val month: YearMonth,
    /** Income minus bills of the month; null until the user sets up their income. */
    val balanceInCents: Long?,
    val billsInCents: Long,
)

/** A change to a recurring bill waiting for the user to say which months it applies to. */
sealed interface PendingBillChange {
    val bill: Bill

    data class Edit(
        override val bill: Bill,
        val edited: Bill,
    ) : PendingBillChange

    data class Delete(
        override val bill: Bill,
    ) : PendingBillChange
}

data class MonthUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val today: CalendarDate = CalendarDate.today(),
    /** First month that can be opened: the month the user first opened the tab. */
    val firstMonth: YearMonth = YearMonth.of(today),
    val selectedMonth: YearMonth = YearMonth.of(today),
    /** "Visualizar" of the tab: todos, quinzenas or ciclos financeiros. */
    val viewMode: ItemViewMode = ItemViewMode.ALL,
    /** Bills, income and balance of [selectedMonth]. */
    val overview: MonthOverview = MonthOverview(month = selectedMonth, today = today, bills = emptyList()),
    /** Cycles of [selectedMonth]; null without [incomeSettings]. */
    val financialOverview: FinancialOverview? = null,
    /** The current month and the next ones, as the strip at the top shows them. */
    val strip: List<MonthStripEntry> = emptyList(),
    val incomeSettings: IncomeSettings? = null,
    val extraIncomes: List<ExtraIncome> = emptyList(),
    val amountsHidden: Boolean = false,
    val isAddBillVisible: Boolean = false,
    /** Bill open in the form; null = form closed. */
    val editingBill: MonthBill? = null,
    /** Asking "Só este mês" or "Este e os próximos"; null = no question open. */
    val pendingChange: PendingBillChange? = null,
    val isIncomeSettingsVisible: Boolean = false,
    val isExtraIncomeSheetVisible: Boolean = false,
    /** Extra income open in the sheet; null = adding a new one. */
    val editingExtraIncome: ExtraIncome? = null,
) {
    val currentMonth: YearMonth get() = YearMonth.of(today)

    val bills: List<MonthBill> get() = overview.bills

    val canGoBack: Boolean get() = selectedMonth > firstMonth

    val isEmpty: Boolean get() = !isLoading && error == null && bills.isEmpty()
}
