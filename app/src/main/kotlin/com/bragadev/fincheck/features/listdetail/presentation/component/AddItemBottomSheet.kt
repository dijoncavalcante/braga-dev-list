package com.bragadev.fincheck.features.listdetail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.core.util.extensions.MAX_DUE_DAY
import com.bragadev.fincheck.core.util.extensions.MIN_DUE_DAY
import com.bragadev.fincheck.core.util.extensions.currencyInputToCents
import com.bragadev.fincheck.core.util.extensions.toBrlCurrency
import com.bragadev.fincheck.ui.theme.BragadevlistTheme
import kotlinx.coroutines.launch

/**
 * Item form, shown as a Material 3 modal bottom sheet, used both to add and to edit an item.
 *
 * When [initialItem] is given the fields start with its values. Edits live only in this
 * sheet's local state: they reach [onConfirm] when the user taps save, and are simply
 * dropped when the sheet is cancelled/dismissed (cancel button, swipe down or tap outside).
 *
 * The sheet is not hidden on save/delete: the caller removes it once the operation succeeds,
 * so on failure it stays open with the user's input.
 *
 * When [onDelete] is given (edit mode) a trash button is shown in the header; it asks for
 * confirmation before calling [onDelete], since deleting cannot be undone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemBottomSheet(
    initialItem: ShoppingListItem? = null,
    onConfirm: (name: String, quantity: Int, priceInCents: Long, dueDay: Int?) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val isEditing = initialItem != null
    var name by remember(initialItem) { mutableStateOf(initialItem?.name.orEmpty()) }
    var quantityText by remember(initialItem) { mutableStateOf((initialItem?.quantity ?: 1).toString()) }
    var showNameError by remember(initialItem) { mutableStateOf(false) }
    var priceInCents by remember(initialItem) { mutableLongStateOf(initialItem?.priceInCents ?: 0L) }
    val priceText = priceInCents.toBrlCurrency()
    var dueDay by remember(initialItem) { mutableStateOf(initialItem?.dueDay) }
    var isDueDayMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Cancel button: animate the sheet away before telling the caller, like a swipe down does.
    val hideAndDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        if (isEditing) R.string.list_detail_edit_item_title else R.string.list_detail_add_item_button,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (onDelete != null) {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.list_detail_delete_item),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    showNameError = false
                },
                label = { Text(stringResource(R.string.list_detail_item_name_label)) },
                isError = showNameError,
                supportingText = if (showNameError) {
                    { Text(stringResource(R.string.list_detail_error_empty_item_name)) }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = quantityText,
                onValueChange = { value -> quantityText = value.filter { char -> char.isDigit() } },
                label = { Text(stringResource(R.string.list_detail_item_quantity_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                // Cursor is pinned to the end: digits fill in from the cents, like a cash register.
                value = TextFieldValue(text = priceText, selection = TextRange(priceText.length)),
                onValueChange = { value -> priceInCents = value.text.currencyInputToCents() },
                label = { Text(stringResource(R.string.list_detail_item_price_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            // Bills repeat every month, so only the due day (1..31) is picked, not a full date.
            ExposedDropdownMenuBox(
                expanded = isDueDayMenuExpanded,
                onExpandedChange = { isDueDayMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = dueDay?.let { stringResource(R.string.list_detail_item_due_day_option, it) }.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.list_detail_item_due_date_label)) },
                    placeholder = { Text(stringResource(R.string.list_detail_item_due_day_placeholder)) },
                    trailingIcon = {
                        if (dueDay == null) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDueDayMenuExpanded)
                        } else {
                            IconButton(onClick = { dueDay = null }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(R.string.list_detail_item_due_date_clear),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = isDueDayMenuExpanded,
                    onDismissRequest = { isDueDayMenuExpanded = false },
                ) {
                    (MIN_DUE_DAY..MAX_DUE_DAY).forEach { day ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.list_detail_item_due_day_option, day)) },
                            onClick = {
                                dueDay = day
                                isDueDayMenuExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                TextButton(onClick = hideAndDismiss) {
                    Text(stringResource(R.string.list_detail_dialog_cancel))
                }
                Button(onClick = {
                    if (name.isBlank()) {
                        showNameError = true
                        return@Button
                    }
                    onConfirm(
                        name.trim(),
                        quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        priceInCents,
                        dueDay,
                    )
                }) {
                    Text(
                        stringResource(
                            if (isEditing) R.string.list_detail_dialog_save_changes else R.string.list_detail_dialog_save,
                        ),
                    )
                }
            }
        }
    }

    if (showDeleteConfirmation && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.list_detail_delete_item_title)) },
            text = {
                Text(stringResource(R.string.list_detail_delete_item_message, initialItem?.name.orEmpty()))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.list_detail_delete_item_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.list_detail_dialog_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun AddItemBottomSheetPreview() {
    BragadevlistTheme {
        AddItemBottomSheet(onConfirm = { _, _, _, _ -> }, onDismiss = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun EditItemBottomSheetPreview() {
    BragadevlistTheme {
        AddItemBottomSheet(
            initialItem = ShoppingListItem(
                id = 1,
                listId = 1,
                name = "Conta de luz",
                quantity = 1,
                priceInCents = 18_990,
                dueDay = 10,
                isChecked = false,
                createdAt = 0,
            ),
            onConfirm = { _, _, _, _ -> },
            onDismiss = {},
            onDelete = {},
        )
    }
}
