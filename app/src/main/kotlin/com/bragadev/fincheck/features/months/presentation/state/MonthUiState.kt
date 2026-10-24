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
    /** The month. */
    val month: YearMonth,
    /** Income minus bills of the month; null until the user sets up their income. */
    val balanceInCents: Long?,
    /** Sum of the month's bills, in cents. */
    val billsInCents: Long,
)

/** A change to a recurring bill waiting for the user to say which months it applies to. */
sealed interface PendingBillChange {
    /** The bill as it is saved. */
    val bill: Bill

    /** Saving the form's values over [bill]. */
    data class Edit(
        override val bill: Bill,
        /** The form's values. */
        val edited: Bill,
    ) : PendingBillChange

    /** Deleting [bill]. */
    data class Delete(
        override val bill: Bill,
    ) : PendingBillChange
}

/** State of the "Meses" tab and of its sheets and dialogs. */
data class MonthUiState(
    /** True until the month is read for the first time. */
    val isLoading: Boolean = true,
    /** Message shown when the month could not be read; null otherwise. */
    val error: String? = null,
    /** Today's date. */
    val today: CalendarDate = CalendarDate.today(),
    /** First month that can be opened: the month the user first opened the tab. */
    val firstMonth: YearMonth = YearMonth.of(today),
    /** Month on screen. */
    val selectedMonth: YearMonth = YearMonth.of(today),
    /** "Visualizar" of the tab: todos, quinzenas or ciclos financeiros. */
    val viewMode: ItemViewMode = ItemViewMode.ALL,
    /** Bills, income and balance of [selectedMonth]. */
    val overview: MonthOverview = MonthOverview(month = selectedMonth, today = today, bills = emptyList()),
    /** Cycles of [selectedMonth]; null without [incomeSettings]. */
    val financialOverview: FinancialOverview? = null,
    /** The current month and the next ones, as the strip at the top shows them. */
    val strip: List<MonthStripEntry> = emptyList(),
    /** How the user receives their income; null until it is set up. */
    val incomeSettings: IncomeSettings? = null,
    /** The user's extra incomes. */
    val extraIncomes: List<ExtraIncome> = emptyList(),
    /** Eye of the toolbar: amounts show as "R$ ••••". */
    val amountsHidden: Boolean = false,
    /** The new bill form is open. */
    val isAddBillVisible: Boolean = false,
    /** Bill open in the form; null = form closed. */
    val editingBill: MonthBill? = null,
    /** Asking "Só este mês" or "Este e os próximos"; null = no question open. */
    val pendingChange: PendingBillChange? = null,
    /** The "Como você recebe?" sheet is open. */
    val isIncomeSettingsVisible: Boolean = false,
    /** The extra income sheet is open. */
    val isExtraIncomeSheetVisible: Boolean = false,
    /** Extra income open in the sheet; null = adding a new one. */
    val editingExtraIncome: ExtraIncome? = null,
) {
    /** The month of [today]. */
    val currentMonth: YearMonth get() = YearMonth.of(today)

    /** Bills of [selectedMonth]. */
    val bills: List<MonthBill> get() = overview.bills

    /** Whether there is a month before [selectedMonth] to go back to. */
    val canGoBack: Boolean get() = selectedMonth > firstMonth

    /** Loaded with no error and no bill: the empty state is shown. */
    val isEmpty: Boolean get() = !isLoading && error == null && bills.isEmpty()
}
