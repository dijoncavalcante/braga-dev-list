package com.bragadev.list.features.listdetail.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.ItemSortOrder

/**
 * Three dots of the list screen toolbar.
 *
 * "Ordenar por" is a single choice (radio buttons), so two orders can never be active at
 * once. It and the toggles ("Mostrar valor", "Mostrar por quinzena") keep the menu open so
 * the user sees the change; actions close it. Actions that would do nothing are disabled (e.g.
 * "Marcar todos" when everything is already checked).
 */
@Composable
fun ListDetailMenu(
    sortOrder: ItemSortOrder,
    showPrices: Boolean,
    groupByFortnight: Boolean,
    hasItems: Boolean,
    hasCheckedItems: Boolean,
    hasUncheckedItems: Boolean,
    onSortOrderSelected: (ItemSortOrder) -> Unit,
    onShowPricesToggle: () -> Unit,
    onGroupByFortnightToggle: () -> Unit,
    onUncheckAllClick: () -> Unit,
    onCheckAllClick: () -> Unit,
    onDeleteItemsClick: () -> Unit,
    onRenameListClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.list_detail_menu_description),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Text(
                text = stringResource(R.string.list_detail_menu_sort_by),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            SortOrderItem(
                label = stringResource(R.string.list_detail_menu_sort_added),
                selected = sortOrder == ItemSortOrder.ADDED,
                onClick = { onSortOrderSelected(ItemSortOrder.ADDED) },
            )
            SortOrderItem(
                label = stringResource(R.string.list_detail_menu_sort_alphabetically),
                selected = sortOrder == ItemSortOrder.ALPHABETICAL,
                onClick = { onSortOrderSelected(ItemSortOrder.ALPHABETICAL) },
            )
            SortOrderItem(
                label = stringResource(R.string.list_detail_menu_sort_due_day),
                selected = sortOrder == ItemSortOrder.DUE_DAY,
                onClick = { onSortOrderSelected(ItemSortOrder.DUE_DAY) },
            )
            HorizontalDivider()
            ToggleItem(
                icon = Icons.Outlined.AttachMoney,
                label = stringResource(R.string.list_detail_menu_show_prices),
                checked = showPrices,
                onToggle = onShowPricesToggle,
            )
            ToggleItem(
                icon = Icons.Outlined.CalendarMonth,
                label = stringResource(R.string.list_detail_menu_group_by_fortnight),
                checked = groupByFortnight,
                onToggle = onGroupByFortnightToggle,
            )
            HorizontalDivider()
            ActionItem(
                icon = Icons.Outlined.CheckBoxOutlineBlank,
                label = stringResource(R.string.list_detail_menu_uncheck_all),
                enabled = hasCheckedItems,
                onClick = { expanded = false; onUncheckAllClick() },
            )
            ActionItem(
                icon = Icons.Outlined.CheckBox,
                label = stringResource(R.string.list_detail_menu_check_all),
                enabled = hasUncheckedItems,
                onClick = { expanded = false; onCheckAllClick() },
            )
            ActionItem(
                icon = Icons.Outlined.DeleteSweep,
                label = stringResource(R.string.list_detail_menu_delete_items),
                enabled = hasItems,
                isDestructive = true,
                onClick = { expanded = false; onDeleteItemsClick() },
            )
            HorizontalDivider()
            ActionItem(
                icon = Icons.Outlined.Edit,
                label = stringResource(R.string.home_rename_title),
                onClick = { expanded = false; onRenameListClick() },
            )
            ActionItem(
                icon = Icons.Outlined.Share,
                label = stringResource(R.string.home_option_share),
                onClick = { expanded = false; onShareClick() },
            )
        }
    }
}

@Composable
private fun SortOrderItem(label: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        // The whole row selects; the radio button only mirrors the state.
        leadingIcon = { RadioButton(selected = selected, onClick = null) },
        onClick = onClick,
    )
}

@Composable
private fun ToggleItem(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        // The whole row toggles; the switch only mirrors the state.
        trailingIcon = { Switch(checked = checked, onCheckedChange = null) },
        onClick = onToggle,
    )
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
) {
    val colors = if (isDestructive) {
        MenuDefaults.itemColors(
            textColor = MaterialTheme.colorScheme.error,
            leadingIconColor = MaterialTheme.colorScheme.error,
        )
    } else {
        MenuDefaults.itemColors()
    }
    DropdownMenuItem(
        text = { Text(label) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        onClick = onClick,
        enabled = enabled,
        colors = colors,
    )
}
