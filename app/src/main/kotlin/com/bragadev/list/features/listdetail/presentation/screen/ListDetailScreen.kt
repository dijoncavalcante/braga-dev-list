package com.bragadev.list.features.listdetail.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.CalendarDate
import com.bragadev.list.core.domain.model.CycleExpense
import com.bragadev.list.core.domain.model.FinancialOverview
import com.bragadev.list.core.domain.model.ItemSortOrder
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.util.extensions.shareText
import com.bragadev.list.core.util.extensions.toBrlCurrency
import com.bragadev.list.features.home.presentation.component.RenameListDialog
import com.bragadev.list.features.listdetail.presentation.component.AddItemBottomSheet
import com.bragadev.list.features.listdetail.presentation.component.AllCheckedCelebration
import com.bragadev.list.features.listdetail.presentation.component.DeleteItemsDialog
import com.bragadev.list.features.listdetail.presentation.component.ExtraIncomeBottomSheet
import com.bragadev.list.features.listdetail.presentation.component.FinancialCycleCard
import com.bragadev.list.features.listdetail.presentation.component.IncomeSettingsBottomSheet
import com.bragadev.list.features.listdetail.presentation.component.IncomeSetupCard
import com.bragadev.list.features.listdetail.presentation.component.ListDetailMenu
import com.bragadev.list.features.listdetail.presentation.component.ListSummaryBar
import com.bragadev.list.features.listdetail.presentation.state.ListDetailUiState
import com.bragadev.list.features.listdetail.presentation.viewmodel.ListDetailViewModel
import com.bragadev.list.ui.components.EmptyStateContent
import com.bragadev.list.ui.components.EmptyStateIllustration
import com.bragadev.list.ui.theme.BragadevlistTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    listId: Long,
    onBackClick: () -> Unit,
    viewModel: ListDetailViewModel = koinViewModel(parameters = { parametersOf(listId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // "Compartilhar": hand the list text to the Android share sheet.
    val shareChooserTitle = stringResource(R.string.home_option_share)
    LaunchedEffect(uiState.pendingShareText) {
        val text = uiState.pendingShareText ?: return@LaunchedEffect
        context.shareText(text, shareChooserTitle)
        viewModel.onShareHandled()
    }

    ListDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onAddItemClick = viewModel::onAddItemClick,
        onDismissAddItemDialog = viewModel::onDismissAddItemDialog,
        onAddItemConfirm = viewModel::onAddItemConfirm,
        onItemClick = viewModel::onItemClick,
        onDismissEditItemDialog = viewModel::onDismissEditItemDialog,
        onEditItemConfirm = viewModel::onEditItemConfirm,
        onDeleteItemConfirm = viewModel::onDeleteItemConfirm,
        onItemCheckedChange = viewModel::onItemCheckedChange,
        onRetryClick = viewModel::retry,
        menuActions = ListDetailMenuActions(
            onSortOrderSelected = viewModel::onSortOrderSelected,
            onShowPricesToggle = viewModel::onShowPricesToggle,
            onGroupByCycleSelected = viewModel::onGroupByCycleSelected,
            onIncomeSettingsClick = viewModel::onIncomeSettingsClick,
            onUncheckAllClick = viewModel::onUncheckAllClick,
            onCheckAllClick = viewModel::onCheckAllClick,
            onDeleteItemsClick = viewModel::onDeleteItemsClick,
            onRenameListClick = viewModel::onRenameListClick,
            onShareClick = viewModel::onShareClick,
        ),
    )

    if (uiState.isRenameDialogVisible) {
        uiState.list?.let { list ->
            RenameListDialog(
                list = list,
                showError = uiState.showRenameError,
                onNameChanged = viewModel::onRenameNameChanged,
                onConfirm = viewModel::onRenameConfirm,
                onDismiss = viewModel::onDismissRename,
            )
        }
    }

    if (uiState.isDeleteItemsDialogVisible) {
        DeleteItemsDialog(
            checkedCount = uiState.summary.checked.count,
            totalCount = uiState.summary.total.count,
            onDeleteChecked = { viewModel.onDeleteItemsConfirm(onlyChecked = true) },
            onDeleteAll = { viewModel.onDeleteItemsConfirm(onlyChecked = false) },
            onDismiss = viewModel::onDismissDeleteItems,
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

/**
 * "Visualizar: Ciclos financeiros". [overview] is null until the user sets up their income;
 * the items are then shown as in "Todos", below an invitation to set it up.
 */
private data class CycleView(val overview: FinancialOverview?)

/** Callbacks of the toolbar three-dots menu, grouped to keep [ListDetailContent] readable. */
private data class ListDetailMenuActions(
    val onSortOrderSelected: (ItemSortOrder) -> Unit = {},
    val onShowPricesToggle: () -> Unit = {},
    val onGroupByCycleSelected: (Boolean) -> Unit = {},
    val onIncomeSettingsClick: () -> Unit = {},
    val onUncheckAllClick: () -> Unit = {},
    val onCheckAllClick: () -> Unit = {},
    val onDeleteItemsClick: () -> Unit = {},
    val onRenameListClick: () -> Unit = {},
    val onShareClick: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListDetailContent(
    uiState: ListDetailUiState,
    onBackClick: () -> Unit,
    onAddItemClick: () -> Unit,
    onDismissAddItemDialog: () -> Unit,
    onAddItemConfirm: (String, Int, Long, Int?) -> Unit,
    onItemClick: (ShoppingListItem) -> Unit,
    onDismissEditItemDialog: () -> Unit,
    onEditItemConfirm: (String, Int, Long, Int?) -> Unit,
    onDeleteItemConfirm: () -> Unit,
    onItemCheckedChange: (Long, Boolean) -> Unit,
    onRetryClick: () -> Unit,
    menuActions: ListDetailMenuActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.listName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    val summary = uiState.summary
                    ListDetailMenu(
                        sortOrder = uiState.sortOrder,
                        showPrices = uiState.showPrices,
                        groupByCycle = uiState.groupByCycle,
                        hasItems = summary.total.count > 0,
                        hasCheckedItems = summary.checked.count > 0,
                        hasUncheckedItems = summary.unchecked.count > 0,
                        onSortOrderSelected = menuActions.onSortOrderSelected,
                        onShowPricesToggle = menuActions.onShowPricesToggle,
                        onGroupByCycleSelected = menuActions.onGroupByCycleSelected,
                        onIncomeSettingsClick = menuActions.onIncomeSettingsClick,
                        onUncheckAllClick = menuActions.onUncheckAllClick,
                        onCheckAllClick = menuActions.onCheckAllClick,
                        onDeleteItemsClick = menuActions.onDeleteItemsClick,
                        onRenameListClick = menuActions.onRenameListClick,
                        onShareClick = menuActions.onShareClick,
                    )
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddItemClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.list_detail_add_item_button)) },
            )
        },
        // Totals pinned to the bottom so they stay visible however long the list is.
        // They are always for the whole list, in both views.
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                ListSummaryBar(summary = uiState.summary, showAmounts = uiState.showPrices)
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(padding)
            uiState.error != null -> ErrorState(padding, onRetryClick)
            uiState.isEmpty -> EmptyState(padding)
            else -> ItemsState(
                padding = padding,
                items = uiState.items,
                cycleView = if (uiState.groupByCycle) CycleView(uiState.financialOverview) else null,
                showPrices = uiState.showPrices,
                onItemClick = onItemClick,
                onItemCheckedChange = onItemCheckedChange,
                onIncomeSettingsClick = menuActions.onIncomeSettingsClick,
            )
        }

        if (uiState.isAddItemDialogVisible) {
            AddItemBottomSheet(onConfirm = onAddItemConfirm, onDismiss = onDismissAddItemDialog)
        }

        uiState.editingItem?.let { item ->
            AddItemBottomSheet(
                initialItem = item,
                onConfirm = onEditItemConfirm,
                onDismiss = onDismissEditItemDialog,
                onDelete = onDeleteItemConfirm,
            )
        }
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(padding: PaddingValues) {
    EmptyStateContent(
        title = stringResource(R.string.list_detail_empty_title),
        description = stringResource(R.string.list_detail_empty_description),
        padding = padding,
    ) {
        EmptyStateIllustration(icon = Icons.AutoMirrored.Outlined.PlaylistAdd)
    }
}

@Composable
private fun ErrorState(padding: PaddingValues, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.list_detail_error_message))
        Button(onClick = onRetryClick) {
            Text(text = stringResource(R.string.list_detail_retry_button))
        }
    }
}

@Composable
private fun ItemsState(
    padding: PaddingValues,
    items: List<ShoppingListItem>,
    cycleView: CycleView?,
    showPrices: Boolean,
    onItemClick: (ShoppingListItem) -> Unit,
    onItemCheckedChange: (Long, Boolean) -> Unit,
    onIncomeSettingsClick: () -> Unit,
) {
    // Only the sections the user opened/closed are stored; the rest keep their default.
    var toggledSections by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val sectionExpansion = SectionExpansion(toggledSections) { key ->
        toggledSections = if (key in toggledSections) toggledSections - key else toggledSections + key
    }

    // Confetti only when the user checks the last item now, not when a finished list is opened.
    val isAllChecked = items.all { it.isChecked }
    var wasAllChecked by remember { mutableStateOf(isAllChecked) }
    var playConfetti by remember { mutableStateOf(false) }
    LaunchedEffect(isAllChecked) {
        if (isAllChecked && !wasAllChecked) playConfetti = true
        if (!isAllChecked) playConfetti = false
        wasAllChecked = isAllChecked
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        // Extra bottom space so the "Adicionar item" button never covers the last item.
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
    ) {
        val itemRow: ItemRow = { item, dueDate, modifier ->
            ShoppingListItemRow(
                item = item,
                dueDate = dueDate,
                showPrice = showPrices,
                onClick = { onItemClick(item) },
                onCheckedChange = { checked -> onItemCheckedChange(item.id, checked) },
                modifier = modifier,
            )
        }
        val overview = cycleView?.overview

        if (cycleView != null) {
            item(key = "cycle_current_card") {
                if (overview == null) {
                    IncomeSetupCard(onSetupClick = onIncomeSettingsClick, modifier = Modifier.animateItem())
                } else {
                    FinancialCycleCard(
                        title = stringResource(R.string.cycle_current_title),
                        cycle = overview.current,
                        today = overview.today,
                        insights = overview.insights(),
                        onEditIncomeClick = onIncomeSettingsClick,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }

        if (isAllChecked) {
            // Everything checked: in both views the lists of items to do give way to a celebration.
            item(key = "all_checked_celebration") {
                AllCheckedCelebration(
                    playConfetti = playConfetti,
                    onConfettiFinished = { playConfetti = false },
                    modifier = Modifier.animateItem(),
                )
            }
        } else if (overview == null) {
            // "Todos" (or cycles not set up yet): unchecked items first (open); checked ones move to a list below (closed).
            val unchecked = items.filterNot { it.isChecked }
            val uncheckedKey = "unchecked"
            val isUncheckedExpanded = sectionExpansion.isExpanded(uncheckedKey, expandedByDefault = true)
            item(key = "header_$uncheckedKey") {
                SectionHeader(
                    title = stringResource(R.string.list_detail_section_unchecked, unchecked.size),
                    subtitle = null,
                    isExpanded = isUncheckedExpanded,
                    onToggle = { sectionExpansion.toggle(uncheckedKey) },
                    modifier = Modifier.animateItem(),
                )
            }
            if (isUncheckedExpanded) {
                items(items = unchecked, key = { it.id }) { item -> itemRow(item, null, Modifier.animateItem()) }
            }
        } else {
            // "Ciclos financeiros": what the current payment has to cover until the next one.
            cycleSection(
                key = "current_bills",
                title = { stringResource(R.string.cycle_bills_section) },
                subtitle = {
                    stringResource(
                        R.string.cycle_period,
                        overview.current.startDate.toDayMonth(),
                        overview.current.endDate.toDayMonth(),
                    )
                },
                expenses = overview.current.bills,
                sectionExpansion = sectionExpansion,
                itemRow = itemRow,
            )
            if (overview.current.otherExpenses.isNotEmpty()) {
                cycleSection(
                    key = "current_other",
                    title = { stringResource(R.string.cycle_other_expenses_section) },
                    subtitle = { stringResource(R.string.cycle_other_expenses_subtitle) },
                    expenses = overview.current.otherExpenses,
                    sectionExpansion = sectionExpansion,
                    itemRow = itemRow,
                )
            }
        }

        if (overview != null) {
            nextCycleSection(overview = overview, sectionExpansion = sectionExpansion, itemRow = itemRow)
        }

        // Checked items leave their list (or cycle) and gather in one list at the end, closed by default.
        checkedSection(
            key = "checked",
            items = items.filter { it.isChecked },
            sectionExpansion = sectionExpansion,
            itemRow = itemRow,
        )
    }
}

/**
 * Card of the next cycle followed by its bills, closed by default. The bills work like the
 * current cycle's: tap to edit, checkbox to check (checked ones move to "Marcados"). An item is
 * checked as a whole, so a bill that falls in both cycles shows the same state in both.
 */
private fun LazyListScope.nextCycleSection(
    overview: FinancialOverview,
    sectionExpansion: SectionExpansion,
    itemRow: ItemRow,
) {
    val next = overview.next
    item(key = "cycle_next_card") {
        FinancialCycleCard(
            title = stringResource(R.string.cycle_next_title),
            cycle = next,
            today = overview.today,
            modifier = Modifier
                .animateItem()
                .padding(top = 16.dp),
        )
    }
    cycleSection(
        key = "next_bills",
        title = { stringResource(R.string.cycle_next_bills_section) },
        subtitle = { stringResource(R.string.cycle_period, next.startDate.toDayMonth(), next.endDate.toDayMonth()) },
        expenses = next.bills,
        sectionExpansion = sectionExpansion,
        itemRow = itemRow,
        expandedByDefault = false,
    )
}

@Composable
private fun ShoppingListItemRow(
    item: ShoppingListItem,
    dueDate: CalendarDate?,
    showPrice: Boolean,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tapping the card opens the edit dialog; the checkbox keeps toggling on its own.
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = item.isChecked, onCheckedChange = onCheckedChange)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    // Quantity only when it adds information: "Arroz (2)", but just "Luz" for 1.
                    text = if (item.quantity > 1) "${item.name} (${item.quantity})" else item.name,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                )
                val dueText = when {
                    dueDate != null -> stringResource(R.string.cycle_item_due_date, dueDate.toDayMonth())
                    item.dueDay != null -> stringResource(R.string.list_detail_item_due_day_format, item.dueDay)
                    else -> null
                }
                dueText?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (showPrice && item.priceInCents > 0) {
                Text(
                    text = item.priceInCents.toBrlCurrency(),
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListDetailEmptyPreview() {
    BragadevlistTheme {
        ListDetailContent(
            uiState = ListDetailUiState(
                isLoading = false,
                list = ShoppingList(id = 1, name = "Compras do mês", createdAt = 0),
            ),
            onBackClick = {},
            onAddItemClick = {},
            onDismissAddItemDialog = {},
            onAddItemConfirm = { _, _, _, _ -> },
            onItemClick = {},
            onDismissEditItemDialog = {},
            onEditItemConfirm = { _, _, _, _ -> },
            onDeleteItemConfirm = {},
            onItemCheckedChange = { _, _ -> },
            onRetryClick = {},
            menuActions = ListDetailMenuActions(),
        )
    }
}

/**
 * Draws one item row; [dueDate] is the date the item falls due in a financial cycle (null = show
 * just its due day) and the modifier carries the list's move/appear animation.
 */
private typealias ItemRow = @Composable (item: ShoppingListItem, dueDate: CalendarDate?, modifier: Modifier) -> Unit

/**
 * Open/closed state of the collapsible sections. Lists of items to do start open and lists
 * of checked items start closed; [toggled] holds the keys the user flipped from that default.
 */
private class SectionExpansion(
    private val toggled: Set<String>,
    private val onToggle: (key: String) -> Unit,
) {
    fun isExpanded(key: String, expandedByDefault: Boolean): Boolean = expandedByDefault != (key in toggled)

    fun toggle(key: String) = onToggle(key)
}

/**
 * One section of a cycle in the "Ciclos financeiros" view: a collapsible header (title, period
 * and how many of its items are checked) followed by its unchecked items only, each with its due
 * date in the cycle; the checked ones are shown in the single "Marcados" list at the end.
 */
private fun LazyListScope.cycleSection(
    key: String,
    title: @Composable () -> String,
    subtitle: @Composable () -> String,
    expenses: List<CycleExpense>,
    sectionExpansion: SectionExpansion,
    itemRow: ItemRow,
    expandedByDefault: Boolean = true,
) {
    val (unchecked, checked) = expenses.partition { !it.item.isChecked }
    val isExpanded = sectionExpansion.isExpanded(key, expandedByDefault)
    item(key = "header_$key") {
        val progress = stringResource(R.string.list_detail_section_checked_progress, checked.size, expenses.size)
        SectionHeader(
            title = title(),
            subtitle = "${subtitle()} · $progress",
            isExpanded = isExpanded,
            onToggle = { sectionExpansion.toggle(key) },
            modifier = Modifier.animateItem(),
        )
    }
    if (!isExpanded) return

    when {
        expenses.isEmpty() -> item(key = "empty_$key") {
            SectionMessage(R.string.cycle_section_empty, Modifier.animateItem())
        }
        unchecked.isEmpty() -> item(key = "all_checked_$key") {
            SectionMessage(R.string.list_detail_section_all_checked, Modifier.animateItem())
        }
        // An item can fall due twice in a long cycle, so the key also has the date.
        else -> items(items = unchecked, key = { "${key}_${it.item.id}_${it.dueDate?.toEpochDay()}" }) { expense ->
            itemRow(expense.item, expense.dueDate, Modifier.animateItem())
        }
    }
}

/** "Marcados (n)": the checked items, in a list that starts closed. Hidden when nothing is checked. */
private fun LazyListScope.checkedSection(
    key: String,
    items: List<ShoppingListItem>,
    sectionExpansion: SectionExpansion,
    itemRow: ItemRow,
) {
    if (items.isEmpty()) return
    val isExpanded = sectionExpansion.isExpanded(key, expandedByDefault = false)
    item(key = "header_$key") {
        SectionHeader(
            title = stringResource(R.string.list_detail_section_checked, items.size),
            subtitle = null,
            isExpanded = isExpanded,
            onToggle = { sectionExpansion.toggle(key) },
            modifier = Modifier.animateItem(),
        )
    }
    if (isExpanded) {
        items(items = items, key = { it.id }) { item -> itemRow(item, null, Modifier.animateItem()) }
    }
}

/** Tappable header of a collapsible section, with an arrow showing whether it is open. */
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 0f else -90f, label = "arrowRotation")
    val stateDescription = stringResource(
        if (isExpanded) R.string.list_detail_section_collapse else R.string.list_detail_section_expand,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClickLabel = stateDescription, onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.rotate(arrowRotation),
        )
    }
}

@Composable
private fun SectionMessage(@StringRes textRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
