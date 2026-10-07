package com.bragadev.fincheck.features.listdetail.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RemoveDone
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.ItemSortOrder
import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.features.listdetail.presentation.state.ListDetailUiState
import com.bragadev.fincheck.features.listdetail.presentation.viewmodel.ListDetailViewModel
import com.bragadev.fincheck.ui.theme.BragadevlistTheme
import kotlinx.coroutines.launch

/**
 * Settings of one list, reached from the gear of the list screen. It replaces the old three-dots
 * menu: every option is a labeled row grouped by what it changes, with a short explanation.
 *
 * [viewModel] is the list screen's own instance (scoped to its back stack entry), so a change
 * made here is already on the list when the user goes back.
 */
@Composable
fun ListSettingsScreen(
    viewModel: ListDetailViewModel,
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val checkedAllMessage = stringResource(R.string.list_settings_checked_all_done)
    val uncheckedAllMessage = stringResource(R.string.list_settings_unchecked_all_done)

    ListSettingsContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        actions = ListSettingsActions(
            onBackClick = onBackClick,
            onSortOrderSelected = viewModel::onSortOrderSelected,
            onShowPricesToggle = viewModel::onShowPricesToggle,
            onViewModeSelected = viewModel::onViewModeSelected,
            onIncomeSettingsClick = viewModel::onIncomeSettingsClick,
            onCheckAllClick = {
                viewModel.onCheckAllClick()
                scope.launch { snackbarHostState.showSnackbar(checkedAllMessage) }
            },
            onUncheckAllClick = {
                viewModel.onUncheckAllClick()
                scope.launch { snackbarHostState.showSnackbar(uncheckedAllMessage) }
            },
            onDeleteItemsClick = viewModel::onDeleteItemsClick,
            onRenameListClick = viewModel::onRenameListClick,
            onShareClick = viewModel::onShareClick,
            onAboutClick = onAboutClick,
        ),
    )

    ListDetailDialogs(uiState = uiState, viewModel = viewModel)
}

/** Everything the settings rows can do, grouped to keep [ListSettingsContent] readable. */
private data class ListSettingsActions(
    val onBackClick: () -> Unit = {},
    val onSortOrderSelected: (ItemSortOrder) -> Unit = {},
    val onShowPricesToggle: () -> Unit = {},
    val onViewModeSelected: (ItemViewMode) -> Unit = {},
    val onIncomeSettingsClick: () -> Unit = {},
    val onCheckAllClick: () -> Unit = {},
    val onUncheckAllClick: () -> Unit = {},
    val onDeleteItemsClick: () -> Unit = {},
    val onRenameListClick: () -> Unit = {},
    val onShareClick: () -> Unit = {},
    val onAboutClick: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListSettingsContent(
    uiState: ListDetailUiState,
    snackbarHostState: SnackbarHostState,
    actions: ListSettingsActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.list_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            // Which list these settings change (the toolbar keeps a single-line title).
            Text(
                text = stringResource(R.string.list_settings_subtitle, uiState.listName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            DisplaySection(uiState = uiState, actions = actions)
            SectionDivider()
            IncomeSection(actions = actions)
            SectionDivider()
            ItemsSection(uiState = uiState, actions = actions)
            SectionDivider()
            ListSection(actions = actions)
            SectionDivider()
            SectionTitle(R.string.list_settings_section_app)
            ActionRow(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.about_title),
                description = stringResource(R.string.list_settings_about_description),
                onClick = actions.onAboutClick,
                showChevron = true,
            )
        }
    }
}

/** "Exibição": order, prices and how the items are grouped. */
@Composable
private fun DisplaySection(uiState: ListDetailUiState, actions: ListSettingsActions) {
    SectionTitle(R.string.list_settings_section_display)

    SubsectionTitle(R.string.list_detail_menu_sort_by)
    Column(modifier = Modifier.selectableGroup()) {
        ChoiceRow(
            title = stringResource(R.string.list_detail_menu_sort_added),
            selected = uiState.sortOrder == ItemSortOrder.ADDED,
            onClick = { actions.onSortOrderSelected(ItemSortOrder.ADDED) },
        )
        ChoiceRow(
            title = stringResource(R.string.list_detail_menu_sort_alphabetically),
            selected = uiState.sortOrder == ItemSortOrder.ALPHABETICAL,
            onClick = { actions.onSortOrderSelected(ItemSortOrder.ALPHABETICAL) },
        )
        ChoiceRow(
            title = stringResource(R.string.list_detail_menu_sort_due_day),
            selected = uiState.sortOrder == ItemSortOrder.DUE_DAY,
            onClick = { actions.onSortOrderSelected(ItemSortOrder.DUE_DAY) },
        )
    }

    SubsectionTitle(R.string.list_detail_menu_view)
    Column(modifier = Modifier.selectableGroup()) {
        ChoiceRow(
            title = stringResource(R.string.list_detail_menu_view_all),
            description = stringResource(R.string.list_settings_view_all_description),
            selected = uiState.viewMode == ItemViewMode.ALL,
            onClick = { actions.onViewModeSelected(ItemViewMode.ALL) },
        )
        ChoiceRow(
            title = stringResource(R.string.list_detail_menu_view_fortnights),
            description = stringResource(R.string.list_settings_view_fortnights_description),
            selected = uiState.viewMode == ItemViewMode.FORTNIGHTS,
            onClick = { actions.onViewModeSelected(ItemViewMode.FORTNIGHTS) },
        )
        ChoiceRow(
            title = stringResource(R.string.list_detail_menu_view_cycles),
            description = stringResource(R.string.list_settings_view_cycles_description),
            selected = uiState.viewMode == ItemViewMode.CYCLES,
            onClick = { actions.onViewModeSelected(ItemViewMode.CYCLES) },
        )
    }

    ToggleRow(
        icon = Icons.Outlined.AttachMoney,
        title = stringResource(R.string.list_detail_menu_show_prices),
        description = stringResource(R.string.list_settings_show_prices_description),
        checked = uiState.showPrices,
        onToggle = actions.onShowPricesToggle,
    )
}

/** "Recebimento": the income behind the financial cycles view. */
@Composable
private fun IncomeSection(actions: ListSettingsActions) {
    SectionTitle(R.string.list_settings_section_income)
    ActionRow(
        icon = Icons.Outlined.CalendarMonth,
        title = stringResource(R.string.list_detail_menu_income_settings),
        description = stringResource(R.string.list_settings_income_description),
        onClick = actions.onIncomeSettingsClick,
        showChevron = true,
    )
}

/**
 * "Itens": bulk actions, each disabled when it would do nothing. Their icons are actions (✓✓ /
 * undo), not checkboxes: a ticked box next to "Marcar todos" read as "already done". The line below
 * each one tells how many items it would change, or why it is disabled.
 */
@Composable
private fun ItemsSection(uiState: ListDetailUiState, actions: ListSettingsActions) {
    val summary = uiState.summary
    val uncheckedCount = summary.unchecked.count
    val checkedCount = summary.checked.count
    SectionTitle(R.string.list_settings_section_items)
    ActionRow(
        icon = Icons.Outlined.DoneAll,
        title = stringResource(R.string.list_detail_menu_check_all),
        description = if (uncheckedCount > 0) {
            pluralStringResource(R.plurals.list_settings_unchecked_count, uncheckedCount, uncheckedCount)
        } else {
            stringResource(R.string.list_settings_all_checked)
        },
        enabled = uncheckedCount > 0,
        onClick = actions.onCheckAllClick,
    )
    ActionRow(
        icon = Icons.Outlined.RemoveDone,
        title = stringResource(R.string.list_detail_menu_uncheck_all),
        description = if (checkedCount > 0) {
            pluralStringResource(R.plurals.list_settings_checked_count, checkedCount, checkedCount)
        } else {
            stringResource(R.string.list_settings_none_checked)
        },
        enabled = checkedCount > 0,
        onClick = actions.onUncheckAllClick,
    )
    ActionRow(
        icon = Icons.Outlined.DeleteSweep,
        title = stringResource(R.string.list_detail_menu_delete_items),
        description = stringResource(R.string.list_settings_delete_items_description),
        enabled = summary.total.count > 0,
        isDestructive = true,
        onClick = actions.onDeleteItemsClick,
    )
}

/** "Lista": the list itself. */
@Composable
private fun ListSection(actions: ListSettingsActions) {
    SectionTitle(R.string.list_settings_section_list)
    ActionRow(
        icon = Icons.Outlined.Edit,
        title = stringResource(R.string.home_rename_title),
        onClick = actions.onRenameListClick,
    )
    ActionRow(
        icon = Icons.Outlined.Share,
        title = stringResource(R.string.home_option_share),
        description = stringResource(R.string.list_settings_share_description),
        onClick = actions.onShareClick,
    )
}

@Composable
private fun SectionTitle(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun SubsectionTitle(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
}

/** One option of a single choice: the whole row selects, the radio button only mirrors the state. */
@Composable
private fun ChoiceRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    description: String? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = description?.let { { Text(it) } },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}

/** An on/off setting: the whole row toggles, the switch only mirrors the state. */
@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = { Icon(imageVector = icon, contentDescription = null) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = { onToggle() }),
    )
}

/** Something to do, or another screen/sheet to open ([showChevron]). */
@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    description: String? = null,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    showChevron: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = when {
        !enabled -> colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)
        isDestructive -> colorScheme.error
        else -> colorScheme.onSurface
    }
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = description?.let { { Text(it) } },
        leadingContent = { Icon(imageVector = icon, contentDescription = null) },
        trailingContent = if (showChevron) {
            { Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) }
        } else {
            null
        },
        colors = ListItemDefaults.colors(
            headlineColor = contentColor,
            leadingIconColor = contentColor,
            supportingColor = if (enabled) colorScheme.onSurfaceVariant else contentColor,
        ),
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    )
}

/** Material's alpha for disabled content. */
private const val DISABLED_ALPHA = 0.38f

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ListSettingsPreview() {
    BragadevlistTheme {
        ListSettingsContent(
            uiState = ListDetailUiState(
                isLoading = false,
                list = ShoppingList(id = 1, name = "Contas de outubro", createdAt = 0),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            actions = ListSettingsActions(),
        )
    }
}
