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
import com.bragadev.list.core.domain.model.FortnightGroups
import com.bragadev.list.core.domain.model.ItemSortOrder
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.util.extensions.shareText
import com.bragadev.list.core.util.extensions.toBrlCurrency
import com.bragadev.list.features.home.presentation.component.RenameListDialog
import com.bragadev.list.features.listdetail.presentation.component.AddItemBottomSheet
import com.bragadev.list.features.listdetail.presentation.component.AllCheckedCelebration
import com.bragadev.list.features.listdetail.presentation.component.DeleteItemsDialog
import com.bragadev.list.features.listdetail.presentation.component.ListDetailMenu
import com.bragadev.list.features.listdetail.presentation.component.ListSummaryBar
import com.bragadev.list.features.listdetail.presentation.state.ListDetailUiState
import com.bragadev.list.features.listdetail.presentation.viewmodel.ListDetailViewModel
import com.bragadev.list.ui.components.EmptyStateContent
import com.bragadev.list.ui.components.EmptyStateIllustration
import com.bragadev.list.ui.theme.BragadevlistTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
            onGroupByFortnightToggle = viewModel::onGroupByFortnightToggle,
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
}

/** Callbacks of the toolbar three-dots menu, grouped to keep [ListDetailContent] readable. */
private data class ListDetailMenuActions(
    val onSortOrderSelected: (ItemSortOrder) -> Unit = {},
    val onShowPricesToggle: () -> Unit = {},
    val onGroupByFortnightToggle: () -> Unit = {},
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
                        groupByFortnight = uiState.groupByFortnight,
                        hasItems = summary.total.count > 0,
                        hasCheckedItems = summary.checked.count > 0,
                        hasUncheckedItems = summary.unchecked.count > 0,
                        onSortOrderSelected = menuActions.onSortOrderSelected,
                        onShowPricesToggle = menuActions.onShowPricesToggle,
                        onGroupByFortnightToggle = menuActions.onGroupByFortnightToggle,
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
                fortnightGroups = if (uiState.groupByFortnight) uiState.fortnightGroups else null,
                showPrices = uiState.showPrices,
                onItemClick = onItemClick,
                onItemCheckedChange = onItemCheckedChange,
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
    fortnightGroups: FortnightGroups?,
    showPrices: Boolean,
    onItemClick: (ShoppingListItem) -> Unit,
    onItemCheckedChange: (Long, Boolean) -> Unit,
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
        val itemRow: ItemRow = { item, modifier ->
            ShoppingListItemRow(
                item = item,
                showPrice = showPrices,
                onClick = { onItemClick(item) },
                onCheckedChange = { checked -> onItemCheckedChange(item.id, checked) },
                modifier = modifier,
            )
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
        } else if (fortnightGroups == null) {
            // Complete view: unchecked items first (open); checked ones move to a list below (closed).
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
                items(items = unchecked, key = { it.id }) { item -> itemRow(item, Modifier.animateItem()) }
            }
        } else {
            // "Mostrar por quinzena": one list per fortnight, split by due day.
            fortnightSection(
                key = "first_fortnight",
                titleRes = R.string.list_detail_first_fortnight,
                subtitleRes = R.string.list_detail_first_fortnight_days,
                items = fortnightGroups.firstFortnight,
                sectionExpansion = sectionExpansion,
                itemRow = itemRow,
            )
            fortnightSection(
                key = "second_fortnight",
                titleRes = R.string.list_detail_second_fortnight,
                subtitleRes = R.string.list_detail_second_fortnight_days,
                items = fortnightGroups.secondFortnight,
                sectionExpansion = sectionExpansion,
                itemRow = itemRow,
            )
            if (fortnightGroups.withoutDueDay.isNotEmpty()) {
                fortnightSection(
                    key = "without_due_day",
                    titleRes = R.string.list_detail_without_due_day,
                    subtitleRes = null,
                    items = fortnightGroups.withoutDueDay,
                    sectionExpansion = sectionExpansion,
                    itemRow = itemRow,
                )
            }
        }

        // Checked items leave their list (or fortnight) and gather in one list at the end, closed by default.
        checkedSection(
            key = "checked",
            items = items.filter { it.isChecked },
            sectionExpansion = sectionExpansion,
            itemRow = itemRow,
        )
    }
}

@Composable
private fun ShoppingListItemRow(
    item: ShoppingListItem,
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
                item.dueDay?.let { dueDay ->
                    Text(
                        text = stringResource(R.string.list_detail_item_due_day_format, dueDay),
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


/** Draws one item row; the modifier carries the list's move/appear animation. */
private typealias ItemRow = @Composable (item: ShoppingListItem, modifier: Modifier) -> Unit

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
 * One section of the "Mostrar por quinzena" view: a collapsible header (title, due days
 * covered and how many of its items are checked) followed by its unchecked items only; the
 * checked ones are shown in the single "Marcados" list at the end of the screen.
 */
private fun LazyListScope.fortnightSection(
    key: String,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int?,
    items: List<ShoppingListItem>,
    sectionExpansion: SectionExpansion,
    itemRow: ItemRow,
) {
    val (unchecked, checked) = items.partition { !it.isChecked }
    val isExpanded = sectionExpansion.isExpanded(key, expandedByDefault = true)
    item(key = "header_$key") {
        val days = subtitleRes?.let { stringResource(it) }
        val progress = stringResource(R.string.list_detail_fortnight_checked, checked.size, items.size)
        SectionHeader(
            title = stringResource(titleRes),
            subtitle = if (days != null) "$days · $progress" else progress,
            isExpanded = isExpanded,
            onToggle = { sectionExpansion.toggle(key) },
            modifier = Modifier.animateItem(),
        )
    }
    if (!isExpanded) return

    when {
        items.isEmpty() -> item(key = "empty_$key") {
            SectionMessage(R.string.list_detail_fortnight_empty, Modifier.animateItem())
        }
        unchecked.isEmpty() -> item(key = "all_checked_$key") {
            SectionMessage(R.string.list_detail_section_all_checked, Modifier.animateItem())
        }
        else -> items(items = unchecked, key = { it.id }) { item -> itemRow(item, Modifier.animateItem()) }
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
        items(items = items, key = { it.id }) { item -> itemRow(item, Modifier.animateItem()) }
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
