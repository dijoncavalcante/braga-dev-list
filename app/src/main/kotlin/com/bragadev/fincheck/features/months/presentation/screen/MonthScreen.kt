package com.bragadev.fincheck.features.months.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.CycleExpense
import com.bragadev.fincheck.core.domain.model.FinancialOverview
import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.MonthBill
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.model.groupByFortnight
import com.bragadev.fincheck.features.months.presentation.component.BillBottomSheet
import com.bragadev.fincheck.features.months.presentation.component.BillScopeDialog
import com.bragadev.fincheck.features.months.presentation.component.CycleOverviewCard
import com.bragadev.fincheck.features.months.presentation.component.ExtraIncomeBottomSheet
import com.bragadev.fincheck.features.months.presentation.component.IncomeSettingsBottomSheet
import com.bragadev.fincheck.features.months.presentation.component.IncomeSetupCard
import com.bragadev.fincheck.features.months.presentation.component.MonthHeader
import com.bragadev.fincheck.features.months.presentation.component.MonthSummaryCard
import com.bragadev.fincheck.features.months.presentation.state.MonthUiState
import com.bragadev.fincheck.features.months.presentation.state.PendingBillChange
import com.bragadev.fincheck.features.months.presentation.viewmodel.MonthViewModel
import com.bragadev.fincheck.ui.components.AmountsVisibilityButton
import com.bragadev.fincheck.ui.components.LocalAmountsHidden
import com.bragadev.fincheck.ui.components.SectionExpansion
import com.bragadev.fincheck.ui.components.SectionHeader
import com.bragadev.fincheck.ui.components.SectionMessage
import com.bragadev.fincheck.ui.components.adaptiveContentWidth
import com.bragadev.fincheck.ui.components.label
import com.bragadev.fincheck.ui.components.rememberSectionExpansion
import com.bragadev.fincheck.ui.components.shortLabel
import com.bragadev.fincheck.ui.components.toDisplayAmount
import org.koin.androidx.compose.koinViewModel

/**
 * "Meses": the bills of one month at a time, starting on the current month. Every month is built
 * from the bills' rules, so moving to November (or to next year) just shows that month's bills,
 * income and balance; nothing is copied from one month to the next.
 *
 * [bottomBar] is the app's navigation between "Meses" and "Listas".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthScreen(
    onAboutClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: MonthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalAmountsHidden provides uiState.amountsHidden) {
        Scaffold(
            topBar = {
                MonthTopBar(
                    uiState = uiState,
                    onToggleAmountsVisibility = viewModel::onToggleAmountsVisibility,
                    onIncomeSettingsClick = viewModel::onIncomeSettingsClick,
                    onAboutClick = onAboutClick,
                    onPrivacyPolicyClick = onPrivacyPolicyClick,
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = viewModel::onAddBillClick,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.months_add_bill)) },
                )
            },
            bottomBar = bottomBar,
        ) { padding ->
            when {
                uiState.isLoading -> LoadingState(padding)
                uiState.error != null -> ErrorState(padding)
                else -> MonthContent(padding = padding, uiState = uiState, viewModel = viewModel)
            }
        }
        MonthDialogs(uiState = uiState, viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthTopBar(
    uiState: MonthUiState,
    onToggleAmountsVisibility: () -> Unit,
    onIncomeSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(stringResource(R.string.months_title)) },
        actions = {
            AmountsVisibilityButton(amountsHidden = uiState.amountsHidden, onToggle = onToggleAmountsVisibility)
            IconButton(onClick = onIncomeSettingsClick) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = stringResource(R.string.list_detail_menu_income_settings))
            }
            Box {
                IconButton(onClick = { isMenuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.months_more_options))
                }
                DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.about_title)) },
                        onClick = {
                            isMenuOpen = false
                            onAboutClick()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.privacy_title)) },
                        onClick = {
                            isMenuOpen = false
                            onPrivacyPolicyClick()
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(padding: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(R.string.months_error_message))
    }
}

/** Header, view picker, the month's card and its bills, in the chosen view. */
@Composable
private fun MonthContent(
    padding: PaddingValues,
    uiState: MonthUiState,
    viewModel: MonthViewModel,
) {
    val sectionExpansion = rememberSectionExpansion()
    val rows =
        BillRows(
            selectedMonth = uiState.selectedMonth,
            today = uiState.today,
            onClick = viewModel::onBillClick,
            onPaidChange = viewModel::onBillPaidChange,
        )

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .adaptiveContentWidth(),
        // Extra bottom space so the "Adicionar conta" button never covers the last bill.
        contentPadding = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 88.dp),
    ) {
        item(key = "header") {
            MonthHeader(
                selectedMonth = uiState.selectedMonth,
                currentMonth = uiState.currentMonth,
                canGoBack = uiState.canGoBack,
                strip = uiState.strip,
                onPreviousClick = viewModel::onPreviousMonthClick,
                onNextClick = viewModel::onNextMonthClick,
                onCurrentMonthClick = viewModel::onCurrentMonthClick,
                onMonthClick = viewModel::onMonthSelected,
            )
        }
        item(key = "view_mode") {
            ViewModePicker(selected = uiState.viewMode, onSelected = viewModel::onViewModeSelected)
        }
        item(key = "card") {
            MonthCard(uiState = uiState, onIncomeSettingsClick = viewModel::onIncomeSettingsClick)
        }

        if (uiState.isEmpty) {
            item(key = "empty") { EmptyMonth(uiState.selectedMonth) }
            return@LazyColumn
        }

        val overview = uiState.financialOverview
        when {
            uiState.viewMode == ItemViewMode.FORTNIGHTS -> fortnightSections(uiState.bills, sectionExpansion, rows)
            uiState.viewMode == ItemViewMode.CYCLES && overview != null -> cycleSections(overview, sectionExpansion, rows)
            else ->
                billSection(
                    key = "to_pay",
                    title = { stringResource(R.string.months_section_to_pay, uiState.bills.count { !it.isPaid }) },
                    subtitle = { null },
                    bills = uiState.bills,
                    sectionExpansion = sectionExpansion,
                    rows = rows,
                    emptyMessage = R.string.months_section_all_paid,
                )
        }

        paidSection(
            bills = if (uiState.viewMode == ItemViewMode.CYCLES && overview != null) overview.allBills() else uiState.bills,
            sectionExpansion = sectionExpansion,
            rows = rows,
        )
    }
}

/** "Todos | Quinzenas | Ciclos": how the bills of the month are grouped. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewModePicker(
    selected: ItemViewMode,
    onSelected: (ItemViewMode) -> Unit,
) {
    val options =
        listOf(
            ItemViewMode.ALL to R.string.list_detail_menu_view_all,
            ItemViewMode.FORTNIGHTS to R.string.list_detail_menu_view_fortnights,
            ItemViewMode.CYCLES to R.string.months_view_cycles,
        )
    SingleChoiceSegmentedButtonRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
    ) {
        options.forEachIndexed { index, (mode, labelRes) ->
            SegmentedButton(
                selected = mode == selected,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(stringResource(labelRes))
            }
        }
    }
}

/** The cycles card in "Ciclos" (or the invitation to set up the income), the month's card otherwise. */
@Composable
private fun MonthCard(
    uiState: MonthUiState,
    onIncomeSettingsClick: () -> Unit,
) {
    val overview = uiState.financialOverview
    when {
        uiState.viewMode != ItemViewMode.CYCLES ->
            MonthSummaryCard(
                overview = uiState.overview,
                hasIncome = uiState.incomeSettings != null,
                onEditIncomeClick = onIncomeSettingsClick,
            )
        overview == null -> IncomeSetupCard(onSetupClick = onIncomeSettingsClick)
        else -> CycleOverviewCard(overview = overview, onEditIncomeClick = onIncomeSettingsClick)
    }
}

@Composable
private fun EmptyMonth(month: YearMonth) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.months_empty_title, month.label()),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.months_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** How each bill row is drawn and what tapping it does; shared by every section. */
private class BillRows(
    val selectedMonth: YearMonth,
    val today: CalendarDate,
    val onClick: (MonthBill) -> Unit,
    val onPaidChange: (MonthBill, Boolean) -> Unit,
)

/** A bill shows up once per month, so its key has both. */
private fun MonthBill.key(section: String) = "${section}_${bill.id}_${month.index}"

/**
 * A collapsible section with the unpaid bills of [bills] (the paid ones go to "Pagas" at the end).
 * [emptyMessage] is shown when the section has no bill at all.
 */
private fun LazyListScope.billSection(
    key: String,
    title: @Composable () -> String,
    subtitle: @Composable () -> String?,
    bills: List<MonthBill>,
    sectionExpansion: SectionExpansion,
    rows: BillRows,
    @StringRes emptyMessage: Int,
    expandedByDefault: Boolean = true,
) {
    val unpaid = bills.filterNot { it.isPaid }
    val isExpanded = sectionExpansion.isExpanded(key, expandedByDefault)
    item(key = "header_$key") {
        val paidCount = bills.size - unpaid.size
        val progress = pluralStringResource(R.plurals.months_section_paid_progress, paidCount, paidCount, bills.size)
        val extra = subtitle()
        SectionHeader(
            title = title(),
            subtitle = if (extra != null) "$extra · $progress" else progress,
            isExpanded = isExpanded,
            onToggle = { sectionExpansion.toggle(key) },
            modifier = Modifier.animateItem(),
        )
    }
    if (!isExpanded) return
    when {
        bills.isEmpty() -> item(key = "empty_$key") { SectionMessage(emptyMessage, Modifier.animateItem()) }
        unpaid.isEmpty() ->
            item(key = "all_paid_$key") {
                SectionMessage(R.string.months_section_all_paid, Modifier.animateItem())
            }
        else -> items(items = unpaid, key = { it.key(key) }) { bill -> BillRow(bill, rows, Modifier.animateItem()) }
    }
}

/** "Quinzenas": bills due on days 1–15, on days 16–31 and those without a due day. */
private fun LazyListScope.fortnightSections(
    bills: List<MonthBill>,
    sectionExpansion: SectionExpansion,
    rows: BillRows,
) {
    val groups = bills.groupByFortnight { it.bill.dueDay }
    billSection(
        key = "first_fortnight",
        title = { stringResource(R.string.list_detail_first_fortnight) },
        subtitle = { stringResource(R.string.list_detail_first_fortnight_days) },
        bills = groups.firstFortnight,
        sectionExpansion = sectionExpansion,
        rows = rows,
        emptyMessage = R.string.months_fortnight_empty,
    )
    billSection(
        key = "second_fortnight",
        title = { stringResource(R.string.list_detail_second_fortnight) },
        subtitle = { stringResource(R.string.list_detail_second_fortnight_days) },
        bills = groups.secondFortnight,
        sectionExpansion = sectionExpansion,
        rows = rows,
        emptyMessage = R.string.months_fortnight_empty,
    )
    if (groups.withoutDueDay.isNotEmpty()) {
        billSection(
            key = "without_due_day",
            title = { stringResource(R.string.list_detail_without_due_day) },
            subtitle = { null },
            bills = groups.withoutDueDay,
            sectionExpansion = sectionExpansion,
            rows = rows,
            emptyMessage = R.string.months_fortnight_empty,
        )
    }
}

/**
 * "Ciclos": what the payment that opens the cycle has to cover until the next one. A cycle can
 * span two months, so it may hold bills of the next month too (marked with their month).
 */
private fun LazyListScope.cycleSections(
    overview: FinancialOverview,
    sectionExpansion: SectionExpansion,
    rows: BillRows,
) {
    billSection(
        key = "current_bills",
        title = { stringResource(R.string.cycle_bills_section) },
        subtitle = {
            stringResource(R.string.cycle_period, overview.current.startDate.toDayMonth(), overview.current.endDate.toDayMonth())
        },
        bills = overview.current.bills.map(CycleExpense::bill),
        sectionExpansion = sectionExpansion,
        rows = rows,
        emptyMessage = R.string.cycle_section_empty,
    )
    if (overview.current.otherExpenses.isNotEmpty()) {
        billSection(
            key = "current_other",
            title = { stringResource(R.string.cycle_other_expenses_section) },
            subtitle = { stringResource(R.string.cycle_other_expenses_subtitle) },
            bills = overview.current.otherExpenses.map(CycleExpense::bill),
            sectionExpansion = sectionExpansion,
            rows = rows,
            emptyMessage = R.string.cycle_section_empty,
        )
    }
    billSection(
        key = "next_bills",
        title = { stringResource(R.string.cycle_next_bills_section) },
        subtitle = {
            stringResource(R.string.cycle_period, overview.next.startDate.toDayMonth(), overview.next.endDate.toDayMonth())
        },
        bills = overview.next.bills.map(CycleExpense::bill),
        sectionExpansion = sectionExpansion,
        rows = rows,
        emptyMessage = R.string.cycle_section_empty,
        expandedByDefault = false,
    )
}

/** Every bill of both cycles, each month's occurrence once. */
private fun FinancialOverview.allBills(): List<MonthBill> =
    (current.bills + current.otherExpenses + next.bills).map(CycleExpense::bill).distinctBy { it.bill.id to it.month }

/** "Pagas (n)": the paid bills, in a list that starts closed. Hidden when nothing is paid. */
private fun LazyListScope.paidSection(
    bills: List<MonthBill>,
    sectionExpansion: SectionExpansion,
    rows: BillRows,
) {
    val paid = bills.filter { it.isPaid }
    if (paid.isEmpty()) return
    val key = "paid"
    val isExpanded = sectionExpansion.isExpanded(key, expandedByDefault = false)
    item(key = "header_$key") {
        SectionHeader(
            title = stringResource(R.string.months_section_paid, paid.size),
            subtitle = null,
            isExpanded = isExpanded,
            onToggle = { sectionExpansion.toggle(key) },
            modifier = Modifier.animateItem(),
        )
    }
    if (isExpanded) {
        items(items = paid, key = { it.key(key) }) { bill -> BillRow(bill, rows, Modifier.animateItem()) }
    }
}

/**
 * One bill: paid checkbox, name, "Vence dia 25" (in red, "Venceu dia 10", once overdue) with how it
 * repeats, and its amount. A bill of another month (in a cycle that spans two) names its month.
 */
@Composable
private fun BillRow(
    bill: MonthBill,
    rows: BillRows,
    modifier: Modifier = Modifier,
) {
    val isOverdue = bill.isOverdue(rows.today)
    Card(
        onClick = { rows.onClick(bill) },
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = bill.isPaid, onCheckedChange = { rows.onPaidChange(bill, it) })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.name,
                    textDecoration = if (bill.isPaid) TextDecoration.LineThrough else TextDecoration.None,
                )
                Text(
                    text = billDetails(bill, isOverdue, showMonth = bill.month != rows.selectedMonth),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (bill.amountInCents > 0) {
                Text(
                    text = bill.amountInCents.toDisplayAmount(),
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (bill.isPaid) TextDecoration.LineThrough else TextDecoration.None,
                )
            }
        }
    }
}

/** "Vence dia 25 · todo mês", "Venceu dia 10 · 3 de 6", "Sem vencimento", "Vence dia 5 · nov/2026". */
@Composable
private fun billDetails(
    bill: MonthBill,
    isOverdue: Boolean,
    showMonth: Boolean,
): String {
    val day = bill.dueDate?.day
    val due =
        when {
            day == null -> stringResource(R.string.bill_no_due_day)
            isOverdue -> stringResource(R.string.bill_overdue_day, day)
            else -> stringResource(R.string.bill_due_day, day)
        }
    val installment = bill.installment
    val repeats =
        when {
            installment != null -> stringResource(R.string.bill_installment, installment.number, installment.count)
            bill.bill.endMonth == null -> stringResource(R.string.bill_every_month)
            else -> null
        }
    return listOfNotNull(due, repeats, if (showMonth) bill.month.shortLabel() else null).joinToString(" · ")
}

/** Bill form, "which months?" question and income sheets. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthDialogs(
    uiState: MonthUiState,
    viewModel: MonthViewModel,
) {
    if (uiState.isAddBillVisible) {
        BillBottomSheet(
            startMonth = uiState.selectedMonth,
            onConfirm = viewModel::onAddBillConfirm,
            onDismiss = viewModel::onDismissAddBill,
        )
    }

    uiState.editingBill?.let { editing ->
        BillBottomSheet(
            startMonth = editing.bill.startMonth,
            initialBill = editing.bill,
            month = editing.month,
            onConfirm = viewModel::onEditBillConfirm,
            onDismiss = viewModel::onDismissEditBill,
            onDelete = viewModel::onDeleteBillConfirm,
        )
    }

    uiState.pendingChange?.let { change ->
        BillScopeDialog(
            billName = change.bill.name,
            month = uiState.editingBill?.month ?: uiState.selectedMonth,
            isDelete = change is PendingBillChange.Delete,
            onScopeSelected = viewModel::onScopeSelected,
            onDismiss = viewModel::onDismissScope,
        )
    }

    if (uiState.isIncomeSettingsVisible) {
        IncomeSettingsBottomSheet(
            initialSettings = uiState.incomeSettings,
            today = uiState.today,
            extraIncomes = uiState.extraIncomes,
            onConfirm = viewModel::onIncomeSettingsConfirm,
            onDismiss = viewModel::onDismissIncomeSettings,
            onAddExtraIncomeClick = viewModel::onAddExtraIncomeClick,
            onExtraIncomeClick = viewModel::onExtraIncomeClick,
        )
    }

    // After the income sheet, so it opens on top of it.
    if (uiState.isExtraIncomeSheetVisible) {
        ExtraIncomeBottomSheet(
            initialIncome = uiState.editingExtraIncome,
            today = uiState.today,
            onConfirm = viewModel::onExtraIncomeConfirm,
            onDismiss = viewModel::onDismissExtraIncome,
            onDelete = viewModel::onDeleteExtraIncomeConfirm,
        )
    }
}
