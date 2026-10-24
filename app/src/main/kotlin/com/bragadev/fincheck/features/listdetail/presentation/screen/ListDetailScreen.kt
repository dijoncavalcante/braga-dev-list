package com.bragadev.fincheck.features.listdetail.presentation.screen

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
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.features.listdetail.presentation.component.AddItemBottomSheet
import com.bragadev.fincheck.features.listdetail.presentation.component.AllCheckedCelebration
import com.bragadev.fincheck.features.listdetail.presentation.component.ListSummaryBar
import com.bragadev.fincheck.features.listdetail.presentation.state.ListDetailUiState
import com.bragadev.fincheck.features.listdetail.presentation.viewmodel.ListDetailViewModel
import com.bragadev.fincheck.ui.components.AmountsVisibilityButton
import com.bragadev.fincheck.ui.components.EmptyStateContent
import com.bragadev.fincheck.ui.components.EmptyStateIllustration
import com.bragadev.fincheck.ui.components.LocalAmountsHidden
import com.bragadev.fincheck.ui.components.SectionExpansion
import com.bragadev.fincheck.ui.components.SectionHeader
import com.bragadev.fincheck.ui.components.adaptiveContentWidth
import com.bragadev.fincheck.ui.components.rememberSectionExpansion
import com.bragadev.fincheck.ui.components.toDisplayAmount
import com.bragadev.fincheck.ui.theme.FinCheckTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * A standalone list (groceries, a trip...): items to check, with quantity and price. Bills with a
 * due date and the money of each month live in the "Meses" tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    listId: Long,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ListDetailViewModel = koinViewModel(parameters = { parametersOf(listId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // The toolbar eye masks every amount on the screen (rows and totals).
    CompositionLocalProvider(LocalAmountsHidden provides uiState.amountsHidden) {
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
            toolbarActions =
                ListDetailToolbarActions(
                    onToggleAmountsVisibility = viewModel::onToggleAmountsVisibility,
                    onSettingsClick = onSettingsClick,
                ),
        )
    }

    ListDetailDialogs(uiState = uiState, viewModel = viewModel)
}

/** Callbacks of the toolbar (eye and settings); every list option lives in the settings screen. */
private data class ListDetailToolbarActions(
    val onToggleAmountsVisibility: () -> Unit = {},
    val onSettingsClick: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListDetailContent(
    uiState: ListDetailUiState,
    onBackClick: () -> Unit,
    onAddItemClick: () -> Unit,
    onDismissAddItemDialog: () -> Unit,
    onAddItemConfirm: (String, Int, Long) -> Unit,
    onItemClick: (ShoppingListItem) -> Unit,
    onDismissEditItemDialog: () -> Unit,
    onEditItemConfirm: (String, Int, Long) -> Unit,
    onDeleteItemConfirm: () -> Unit,
    onItemCheckedChange: (Long, Boolean) -> Unit,
    onRetryClick: () -> Unit,
    toolbarActions: ListDetailToolbarActions,
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
                    AmountsVisibilityButton(
                        amountsHidden = uiState.amountsHidden,
                        onToggle = toolbarActions.onToggleAmountsVisibility,
                    )
                    IconButton(onClick = toolbarActions.onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.list_settings_title),
                        )
                    }
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
            else ->
                ItemsState(
                    padding = padding,
                    items = uiState.items,
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
private fun ErrorState(
    padding: PaddingValues,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
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

/** Unchecked items first (open); checked ones move to a list below (closed). */
@Composable
private fun ItemsState(
    padding: PaddingValues,
    items: List<ShoppingListItem>,
    showPrices: Boolean,
    onItemClick: (ShoppingListItem) -> Unit,
    onItemCheckedChange: (Long, Boolean) -> Unit,
) {
    val sectionExpansion = rememberSectionExpansion()

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
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .adaptiveContentWidth(),
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
            // Everything checked: the list of items to do gives way to a celebration.
            item(key = "all_checked_celebration") {
                AllCheckedCelebration(
                    playConfetti = playConfetti,
                    onConfettiFinished = { playConfetti = false },
                    modifier = Modifier.animateItem(),
                )
            }
        } else {
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
        }

        checkedSection(
            key = "checked",
            items = items.filter { it.isChecked },
            sectionExpansion = sectionExpansion,
            itemRow = itemRow,
        )
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
            Checkbox(checked = item.isChecked, onCheckedChange = onCheckedChange)
            Text(
                // Quantity only when it adds information: "Arroz (2)", but just "Luz" for 1.
                text = if (item.quantity > 1) "${item.name} (${item.quantity})" else item.name,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f),
            )
            if (showPrice && item.priceInCents > 0) {
                Text(
                    text = item.priceInCents.toDisplayAmount(),
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
    FinCheckTheme {
        ListDetailContent(
            uiState =
                ListDetailUiState(
                    isLoading = false,
                    list = ShoppingList(id = 1, name = "Compras do mês", createdAt = 0),
                ),
            onBackClick = {},
            onAddItemClick = {},
            onDismissAddItemDialog = {},
            onAddItemConfirm = { _, _, _ -> },
            onItemClick = {},
            onDismissEditItemDialog = {},
            onEditItemConfirm = { _, _, _ -> },
            onDeleteItemConfirm = {},
            onItemCheckedChange = { _, _ -> },
            onRetryClick = {},
            toolbarActions = ListDetailToolbarActions(),
        )
    }
}

/** Draws one item row; the modifier carries the list's move/appear animation. */
private typealias ItemRow = @Composable (item: ShoppingListItem, modifier: Modifier) -> Unit
