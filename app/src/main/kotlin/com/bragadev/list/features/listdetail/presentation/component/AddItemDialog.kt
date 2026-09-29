package com.bragadev.list.features.listdetail.presentation.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.util.extensions.currencyInputToCents
import com.bragadev.list.core.util.extensions.toBrDateString
import com.bragadev.list.core.util.extensions.toBrlCurrency
import com.bragadev.list.ui.theme.BragadevlistTheme

/**
 * Item form used both to add and to edit an item.
 *
 * When [initialItem] is given the fields start with its values. Edits live only in this
 * dialog's local state: they reach [onConfirm] when the user taps save, and are simply
 * dropped when the dialog is cancelled/dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    initialItem: ShoppingListItem? = null,
    onConfirm: (name: String, quantity: Int, priceInCents: Long, dueDateMillis: Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val isEditing = initialItem != null
    var name by remember(initialItem) { mutableStateOf(initialItem?.name.orEmpty()) }
    var quantityText by remember(initialItem) { mutableStateOf((initialItem?.quantity ?: 1).toString()) }
    var showNameError by remember(initialItem) { mutableStateOf(false) }
    var priceInCents by remember(initialItem) { mutableLongStateOf(initialItem?.priceInCents ?: 0L) }
    val priceText = priceInCents.toBrlCurrency()
    var dueDateMillis by remember(initialItem) { mutableStateOf(initialItem?.dueDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    // The due date field is read-only: tapping anywhere on it opens the date picker.
    val dueDateInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(dueDateInteractionSource) {
        dueDateInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) showDatePicker = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (isEditing) R.string.list_detail_edit_item_title else R.string.list_detail_add_item_button,
                ),
            )
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
                OutlinedTextField(
                    value = dueDateMillis?.toBrDateString().orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.list_detail_item_due_date_label)) },
                    placeholder = { Text(stringResource(R.string.list_detail_item_due_date_placeholder)) },
                    trailingIcon = {
                        if (dueDateMillis == null) {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = stringResource(R.string.list_detail_item_due_date_pick),
                                )
                            }
                        } else {
                            IconButton(onClick = { dueDateMillis = null }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(R.string.list_detail_item_due_date_clear),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    interactionSource = dueDateInteractionSource,
                    modifier = Modifier.fillMaxWidth(),
                )
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
                    dueDateMillis,
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.list_detail_date_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.list_detail_dialog_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
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
                dueDateMillis = 1_791_590_400_000,
                isChecked = false,
                createdAt = 0,
            ),
            onConfirm = { _, _, _, _ -> },
            onDismiss = {},
        )
    }
}
