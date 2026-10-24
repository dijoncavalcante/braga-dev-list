package com.bragadev.fincheck.features.months.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillBook
import com.bragadev.fincheck.core.domain.model.BillEditScope
import com.bragadev.fincheck.core.domain.model.BillRepetition
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.ExtraIncome
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.MonthBill
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.model.buildFinancialOverview
import com.bragadev.fincheck.core.domain.model.buildMonthOverview
import com.bragadev.fincheck.core.domain.model.nextMonths
import com.bragadev.fincheck.core.domain.model.toDescription
import com.bragadev.fincheck.core.domain.usecase.DeleteBillUseCase
import com.bragadev.fincheck.core.domain.usecase.DeleteExtraIncomeUseCase
import com.bragadev.fincheck.core.domain.usecase.GetAmountsHiddenUseCase
import com.bragadev.fincheck.core.domain.usecase.GetBillBookUseCase
import com.bragadev.fincheck.core.domain.usecase.GetExtraIncomesUseCase
import com.bragadev.fincheck.core.domain.usecase.GetFirstMonthUseCase
import com.bragadev.fincheck.core.domain.usecase.GetIncomeSettingsUseCase
import com.bragadev.fincheck.core.domain.usecase.GetMonthViewModeUseCase
import com.bragadev.fincheck.core.domain.usecase.SaveBillUseCase
import com.bragadev.fincheck.core.domain.usecase.SaveExtraIncomeUseCase
import com.bragadev.fincheck.core.domain.usecase.SaveIncomeSettingsUseCase
import com.bragadev.fincheck.core.domain.usecase.SetAmountsHiddenUseCase
import com.bragadev.fincheck.core.domain.usecase.SetBillPaidUseCase
import com.bragadev.fincheck.core.domain.usecase.SetMonthViewModeUseCase
import com.bragadev.fincheck.features.months.presentation.state.MonthStripEntry
import com.bragadev.fincheck.features.months.presentation.state.MonthUiState
import com.bragadev.fincheck.features.months.presentation.state.PendingBillChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The "Meses" tab: one calendar month at a time, built on the fly from the bills' rules, so any
 * month (this one, the next, next year's) shows its own bills, income and balance.
 */
@Suppress("TooManyFunctions") // one small handler per user action of the screen
class MonthViewModel(
    private val getBillBookUseCase: GetBillBookUseCase,
    private val saveBillUseCase: SaveBillUseCase,
    private val deleteBillUseCase: DeleteBillUseCase,
    private val setBillPaidUseCase: SetBillPaidUseCase,
    private val getIncomeSettingsUseCase: GetIncomeSettingsUseCase,
    private val saveIncomeSettingsUseCase: SaveIncomeSettingsUseCase,
    private val getExtraIncomesUseCase: GetExtraIncomesUseCase,
    private val saveExtraIncomeUseCase: SaveExtraIncomeUseCase,
    private val deleteExtraIncomeUseCase: DeleteExtraIncomeUseCase,
    private val getMonthViewModeUseCase: GetMonthViewModeUseCase,
    private val setMonthViewModeUseCase: SetMonthViewModeUseCase,
    private val getFirstMonthUseCase: GetFirstMonthUseCase,
    private val getAmountsHiddenUseCase: GetAmountsHiddenUseCase,
    private val setAmountsHiddenUseCase: SetAmountsHiddenUseCase,
    /** "Today"; replaceable in tests. */
    private val currentDate: () -> CalendarDate = CalendarDate::today,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MonthUiState(today = currentDate()))
    /** State the tab draws. */
    val uiState: StateFlow<MonthUiState> = _uiState.asStateFlow()

    /** The app opens on the current month. */
    private val selectedMonth = MutableStateFlow(YearMonth.of(currentDate()))

    init {
        viewModelScope.launch {
            val firstMonth = getFirstMonthUseCase(YearMonth.of(currentDate()))
            _uiState.update { it.copy(firstMonth = firstMonth) }
            observeMonth()
        }
        getAmountsHiddenUseCase()
            .onEach { hidden -> _uiState.update { it.copy(amountsHidden = hidden) } }
            .launchIn(viewModelScope)
    }

    // region Month navigation

    /** Opens [month]; months before the first one are never opened. */
    fun onMonthSelected(month: YearMonth) {
        selectedMonth.value = month.coerceAtLeast(_uiState.value.firstMonth)
    }

    /** Opens the month before the one on screen. */
    fun onPreviousMonthClick() = onMonthSelected(selectedMonth.value.plusMonths(-1))

    /** Opens the month after the one on screen. */
    fun onNextMonthClick() = onMonthSelected(selectedMonth.value.plusMonths(1))

    /** Goes back to the current month. */
    fun onCurrentMonthClick() = onMonthSelected(YearMonth.of(currentDate()))

    /** "Visualizar": only one view is active at a time. */
    fun onViewModeSelected(viewMode: ItemViewMode) {
        viewModelScope.launch { setMonthViewModeUseCase(viewMode) }
    }

    /** Eye of the toolbar: hides or shows the amounts (an app-wide preference). */
    fun onToggleAmountsVisibility() {
        val hidden = !_uiState.value.amountsHidden
        viewModelScope.launch { setAmountsHiddenUseCase(hidden) }
    }

    // endregion

    // region Bills

    /** Opens the new bill form. */
    fun onAddBillClick() {
        _uiState.update { it.copy(isAddBillVisible = true) }
    }

    /** Closes the new bill form without saving. */
    fun onDismissAddBill() {
        _uiState.update { it.copy(isAddBillVisible = false) }
    }

    /** A new bill starts in the month on screen. */
    fun onAddBillConfirm(
        name: String,
        priceInCents: Long,
        dueDay: Int?,
        repetition: BillRepetition,
        description: String = "",
    ) {
        val month = selectedMonth.value
        val bill =
            Bill(
                name = name,
                priceInCents = priceInCents,
                dueDay = dueDay,
                startMonth = month,
                endMonth = repetition.endMonth(month),
                description = description.toDescription(),
            )
        viewModelScope.launch {
            when (saveBillUseCase.add(bill)) {
                is AppResult.Success -> onDismissAddBill()
                is AppResult.Error -> Unit // sheet stays open with what the user typed
            }
        }
    }

    /** Opens [bill] in the form. */
    fun onBillClick(bill: MonthBill) {
        _uiState.update { it.copy(editingBill = bill) }
    }

    /** Closes the bill form without saving. */
    fun onDismissEditBill() {
        _uiState.update { it.copy(editingBill = null) }
    }

    /** [repetition] counts from the bill's first month, as the form shows it. */
    fun onEditBillConfirm(
        name: String,
        priceInCents: Long,
        dueDay: Int?,
        repetition: BillRepetition,
        description: String = "",
    ) {
        val original = _uiState.value.editingBill?.bill ?: return
        val edited =
            original.copy(
                name = name,
                priceInCents = priceInCents,
                dueDay = dueDay,
                endMonth = repetition.endMonth(original.startMonth),
                description = description.toDescription(),
            )
        if (original.isRecurring) {
            _uiState.update { it.copy(pendingChange = PendingBillChange.Edit(original, edited)) }
        } else {
            applyChange(PendingBillChange.Edit(original, edited), BillEditScope.THIS_AND_NEXT)
        }
    }

    /** Deletes the bill open in the form; a recurring one first asks which months. */
    fun onDeleteBillConfirm() {
        val bill = _uiState.value.editingBill?.bill ?: return
        if (bill.isRecurring) {
            _uiState.update { it.copy(pendingChange = PendingBillChange.Delete(bill)) }
        } else {
            applyChange(PendingBillChange.Delete(bill), BillEditScope.THIS_AND_NEXT)
        }
    }

    /** Applies the pending change to the months chosen by [scope]. */
    fun onScopeSelected(scope: BillEditScope) {
        val change = _uiState.value.pendingChange ?: return
        applyChange(change, scope)
    }

    /** Back to the form, which still has the user's edits. */
    fun onDismissScope() {
        _uiState.update { it.copy(pendingChange = null) }
    }

    /** Marks [bill] as paid or unpaid in its month only. */
    fun onBillPaidChange(
        bill: MonthBill,
        isPaid: Boolean,
    ) {
        viewModelScope.launch { setBillPaidUseCase(bill.bill.id, bill.month, isPaid) }
    }

    /** Saves or deletes the pending change in the months chosen by [scope]; the form closes on success. */
    private fun applyChange(
        change: PendingBillChange,
        scope: BillEditScope,
    ) {
        val month = _uiState.value.editingBill?.month ?: selectedMonth.value
        viewModelScope.launch {
            val result =
                when (change) {
                    is PendingBillChange.Edit -> saveBillUseCase.edit(change.bill, change.edited, month, scope)
                    is PendingBillChange.Delete -> deleteBillUseCase(change.bill, month, scope)
                }
            when (result) {
                is AppResult.Success -> _uiState.update { it.copy(editingBill = null, pendingChange = null) }
                is AppResult.Error -> _uiState.update { it.copy(pendingChange = null) } // the form stays open
            }
        }
    }

    // endregion

    // region Income ("Como você recebe?")

    /** Opens the "Como você recebe?" sheet. */
    fun onIncomeSettingsClick() {
        _uiState.update { it.copy(isIncomeSettingsVisible = true) }
    }

    /** Closes the "Como você recebe?" sheet without saving. */
    fun onDismissIncomeSettings() {
        _uiState.update { it.copy(isIncomeSettingsVisible = false) }
    }

    /** The sheet stays open if saving fails, keeping what the user typed. */
    fun onIncomeSettingsConfirm(settings: IncomeSettings) {
        viewModelScope.launch {
            when (saveIncomeSettingsUseCase(settings)) {
                is AppResult.Success -> _uiState.update { it.copy(isIncomeSettingsVisible = false) }
                is AppResult.Error -> Unit
            }
        }
    }

    /** Opens the extra income sheet empty, to add a new one. */
    fun onAddExtraIncomeClick() {
        _uiState.update { it.copy(isExtraIncomeSheetVisible = true, editingExtraIncome = null) }
    }

    /** Opens [income] in the extra income sheet. */
    fun onExtraIncomeClick(income: ExtraIncome) {
        _uiState.update { it.copy(isExtraIncomeSheetVisible = true, editingExtraIncome = income) }
    }

    /** Closes the extra income sheet without saving. */
    fun onDismissExtraIncome() {
        _uiState.update { it.copy(isExtraIncomeSheetVisible = false, editingExtraIncome = null) }
    }

    /** Adds a new extra income or updates the one being edited; the sheet stays open on failure. */
    fun onExtraIncomeConfirm(income: ExtraIncome) {
        val id = _uiState.value.editingExtraIncome?.id ?: 0
        viewModelScope.launch {
            when (saveExtraIncomeUseCase(income.copy(id = id))) {
                is AppResult.Success -> onDismissExtraIncome()
                is AppResult.Error -> Unit
            }
        }
    }

    /** Deletes the extra income open in the sheet; the sheet stays open on failure. */
    fun onDeleteExtraIncomeConfirm() {
        val income = _uiState.value.editingExtraIncome ?: return
        viewModelScope.launch {
            when (deleteExtraIncomeUseCase(income.id)) {
                is AppResult.Success -> onDismissExtraIncome()
                is AppResult.Error -> Unit
            }
        }
    }

    // endregion

    /** Collects bills, income, extra incomes, view and month on screen together into the state. */
    private fun observeMonth() {
        combine(
            getBillBookUseCase(),
            getIncomeSettingsUseCase(),
            getExtraIncomesUseCase(),
            getMonthViewModeUseCase(),
            selectedMonth,
        ) { book, settings, extraIncomes, viewMode, month ->
            val today = currentDate()
            MonthData(
                today = today,
                month = month,
                viewMode = viewMode,
                book = book,
                settings = settings,
                extraIncomes = extraIncomes,
            )
        }.onEach { data -> _uiState.update { it.withData(data) } }
            .catch { _uiState.update { it.copy(isLoading = false, error = "months_load_failed") } }
            .launchIn(viewModelScope)
    }

    /** This state updated with [data]: the month's overview, its cycles and the strip of months. */
    private fun MonthUiState.withData(data: MonthData): MonthUiState {
        val overview = buildMonthOverview(data.month, data.today, data.book, data.settings, data.extraIncomes)
        return copy(
            isLoading = false,
            error = null,
            today = data.today,
            selectedMonth = data.month,
            viewMode = data.viewMode,
            overview = overview,
            financialOverview =
                data.settings?.let {
                    buildFinancialOverview(it, data.book, data.month, data.today, data.extraIncomes)
                },
            strip =
                YearMonth.of(data.today).nextMonths().map { month ->
                    val monthOverview =
                        if (month == data.month) {
                            overview
                        } else {
                            buildMonthOverview(month, data.today, data.book, data.settings, data.extraIncomes)
                        }
                    MonthStripEntry(
                        month = month,
                        balanceInCents = monthOverview.projectedBalanceInCents.takeIf { data.settings != null },
                        billsInCents = monthOverview.totals.totalInCents,
                    )
                },
            incomeSettings = data.settings,
            extraIncomes = data.extraIncomes,
        )
    }
}

/** Everything read for one emission, before it is turned into the UI state. */
private data class MonthData(
    /** Today's date when it was read. */
    val today: CalendarDate,
    /** Month on screen. */
    val month: YearMonth,
    /** "Visualizar" choice of the tab. */
    val viewMode: ItemViewMode,
    /** Every bill and month status. */
    val book: BillBook,
    /** Income settings; null until set up. */
    val settings: IncomeSettings?,
    /** Every extra income. */
    val extraIncomes: List<ExtraIncome>,
)
