package com.bragadev.list.features.listdetail.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.util.extensions.MAX_DUE_DAY
import com.bragadev.list.core.util.extensions.MIN_DUE_DAY
import com.bragadev.list.core.util.extensions.currencyInputToCents
import com.bragadev.list.core.util.extensions.toBrlCurrency
import com.bragadev.list.ui.theme.BragadevlistTheme

/**
 * Item form used both to add and to edit an item.
 *
 * When [initialItem] is given the fields start with its values. Edits live only in this
 * dialog's local state: they reach [onConfirm] when the user taps save, and are simply
 * dropped when the dialog is cancelled/dismissed.
 *
 * When [onDelete] is given (edit mode) a trash button is shown in the title; it asks for
 * confirmation before calling [onDelete], since deleting cannot be undone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    initialItem: ShoppingListItem? = null,
    onConfirm: (name: String, quantity: Int, priceInCents: Long, dueDay: Int?) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val isEditing = initialItem != null
    var name by remember(initialItem) { mutableStateOf(initialItem?.name.orEmpty()) }
    var quantityText by remember(initialItem) { mutableStateOf((initialItem?.quantity ?: 1).toString()) }
    var showNameError by remember(initialItem) { mutableStateOf(false) }
    var priceInCents by remember(initialItem) { mutableLongStateOf(initialItem?.priceInCents ?: 0L) }
    val priceText = priceInCents.toBrlCurrency()
    var dueDay by remember(initialItem) { mutableStateOf(initialItem?.dueDay) }
    var isDueDayMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        if (isEditing) R.string.list_detail_edit_item_title else R.string.list_detail_add_item_button,
                    ),
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
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showNameError = false
                    },
                    label = { Text(stringResource(R.string.list_detail_item_name_label)) },
                    isError = showNameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showNameError) {
                    Text(text = stringResource(R.string.list_detail_error_empty_item_name))
                }
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) {
                    showNameError = true
                    return@TextButton
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
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.list_detail_dialog_cancel))
            }
        },
    )

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

@Preview(showBackground = true)
@Composable
private fun AddItemDialogPreview() {
    BragadevlistTheme {
        AddItemDialog(onConfirm = { _, _, _, _ -> }, onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EditItemDialogPreview() {
    BragadevlistTheme {
        AddItemDialog(
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
