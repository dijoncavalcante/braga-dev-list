package com.bragadev.list.features.listdetail.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.bragadev.list.core.util.extensions.currencyInputToCents
import com.bragadev.list.core.util.extensions.toBrlCurrency
import com.bragadev.list.ui.theme.BragadevlistTheme

@Composable
fun AddItemDialog(
    onConfirm: (name: String, quantity: Int, priceInCents: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var showNameError by remember { mutableStateOf(false) }
    var priceInCents by remember { mutableLongStateOf(0L) }
    val priceText = priceInCents.toBrlCurrency()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.list_detail_add_item_button)) },
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
                )
            }) {
                Text(stringResource(R.string.list_detail_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.list_detail_dialog_cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun AddItemDialogPreview() {
    BragadevlistTheme {
        AddItemDialog(onConfirm = { _, _, _ -> }, onDismiss = {})
    }
}
