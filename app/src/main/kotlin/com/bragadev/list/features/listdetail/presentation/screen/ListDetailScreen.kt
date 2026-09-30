package com.bragadev.list.features.listdetail.presentation.screen

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.FortnightGroups
import com.bragadev.list.core.domain.model.ItemSortOrder
import com.bragadev.list.core.domain.model.ListSummary
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.util.extensions.shareText
import com.bragadev.list.core.util.extensions.toBrlCurrency
import com.bragadev.list.features.home.presentation.component.RenameListDialog
import com.bragadev.list.features.listdetail.presentation.component.AddItemBottomSheet
import com.bragadev.list.features.listdetail.presentation.component.DeleteItemsDialog
import com.bragadev.list.features.listdetail.presentation.component.ListDetailMenu
import com.bragadev.list.features.listdetail.presentation.component.ListSummaryFooter
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
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(padding)
            uiState.error != null -> ErrorState(padding, onRetryClick)
            uiState.isEmpty -> EmptyState(padding)
            else -> ItemsState(
                padding = padding,
                items = uiState.items,
                fortnightGroups = if (uiState.groupByFortnight) uiState.fortnightGroups else null,
                summary = uiState.summary,
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
    summary: ListSummary,
    showPrices: Boolean,
    onItemClick: (ShoppingListItem) -> Unit,
    onItemCheckedChange: (Long, Boolean) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        // Extra bottom space so the "Adicionar item" button never covers the totals.
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
    ) {
        val itemRow: @Composable (ShoppingListItem) -> Unit = { item ->
            ShoppingListItemRow(
                item = item,
                showPrice = showPrices,
                onClick = { onItemClick(item) },
                onCheckedChange = { checked -> onItemCheckedChange(item.id, checked) },
            )
        }

        if (fortnightGroups == null) {
            // Complete view: every item in a single list.
            items(items = items, key = { it.id }) { item -> itemRow(item) }
        } else {
            // "Mostrar por quinzena": one list per fortnight, split by due day.
            fortnightSection(
                key = "first_fortnight",
                titleRes = R.string.list_detail_first_fortnight,
                subtitleRes = R.string.list_detail_first_fortnight_days,
                items = fortnightGroups.firstFortnight,
                itemRow = itemRow,
            )
            fortnightSection(
                key = "second_fortnight",
                titleRes = R.string.list_detail_second_fortnight,
                subtitleRes = R.string.list_detail_second_fortnight_days,
                items = fortnightGroups.secondFortnight,
                itemRow = itemRow,
            )
            if (fortnightGroups.withoutDueDay.isNotEmpty()) {
                fortnightSection(
                    key = "without_due_day",
                    titleRes = R.string.list_detail_without_due_day,
                    subtitleRes = null,
                    items = fortnightGroups.withoutDueDay,
                    itemRow = itemRow,
                )
            }
        }

        // Totals stay the same in both views: always for the whole list.
        item(key = "summary") {
            ListSummaryFooter(summary = summary, showAmounts = showPrices)
        }
    }
}

@Composable
private fun ShoppingListItemRow(
    item: ShoppingListItem,
    showPrice: Boolean,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    // Tapping the card opens the edit dialog; the checkbox keeps toggling on its own.
    Card(
        onClick = onClick,
        modifier = Modifier
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

/**
 * One section of the "Mostrar por quinzena" view: a header (title, due days covered and
 * how many of its items are checked) followed by its items, or a short message when empty.
 */
private fun LazyListScope.fortnightSection(
    key: String,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int?,
    items: List<ShoppingListItem>,
    itemRow: @Composable (ShoppingListItem) -> Unit,
) {
    item(key = "header_$key") {
        FortnightHeader(
            title = stringResource(titleRes),
            days = subtitleRes?.let { stringResource(it) },
            checkedCount = items.count { it.isChecked },
            totalCount = items.size,
        )
    }
    if (items.isEmpty()) {
        item(key = "empty_$key") {
            Text(
                text = stringResource(R.string.list_detail_fortnight_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    } else {
        items(items = items, key = { it.id }) { item -> itemRow(item) }
    }
}

@Composable
private fun FortnightHeader(title: String, days: String?, checkedCount: Int, totalCount: Int) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        val progress = stringResource(R.string.list_detail_fortnight_checked, checkedCount, totalCount)
        Text(
            text = if (days != null) "$days · $progress" else progress,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
