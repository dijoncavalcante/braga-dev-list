package com.bragadev.fincheck.features.listdetail.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.MAX_PAY_DAY
import com.bragadev.fincheck.core.domain.model.MIN_PAY_DAY
import com.bragadev.fincheck.core.util.extensions.currencyInputToCents
import com.bragadev.fincheck.core.util.extensions.toBrlCurrency

/*
 * Fields shared by the income forms ("Como você recebe?" and "Outras entradas").
 */

private const val MILLIS_PER_DAY = 86_400_000L

/** Day of the month (1..31) picked from a dropdown; [day] is null until chosen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayDropdownField(
    day: Int?,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = day?.toString().orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.income_day_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            isError = isError,
            supportingText = if (isError) {
                { Text(stringResource(R.string.income_error_day)) }
            } else {
                null
            },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            for (option in MIN_PAY_DAY..MAX_PAY_DAY) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.list_detail_item_due_day_option, option)) },
                    onClick = {
                        onDaySelected(option)
                        isExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

/** Amount in R$; digits fill in from the cents, like a cash register. */
@Composable
internal fun CurrencyField(
    amountInCents: Long,
    onAmountChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val text = amountInCents.toBrlCurrency()
    OutlinedTextField(
        // Cursor pinned to the end so new digits always go to the cents.
        value = TextFieldValue(text = text, selection = TextRange(text.length)),
        onValueChange = { value -> onAmountChange(value.text.currencyInputToCents()) },
        label = { Text(stringResource(R.string.income_amount_label)) },
        isError = isError,
        supportingText = if (isError) {
            { Text(stringResource(R.string.income_error_amount)) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

/** Read-only date field ("25/10/2026") with a calendar button that opens the date picker. */
@Composable
internal fun DatePickerField(
    date: CalendarDate?,
    label: String,
    onDateSelected: (CalendarDate) -> Unit,
    initialPickerDate: CalendarDate,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    errorText: String? = null,
) {
    var showPicker by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = date?.toDayMonthYear().orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        isError = errorText != null,
        supportingText = (errorText ?: supportingText)?.let { text -> { Text(text) } },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = stringResource(R.string.income_pick_date),
                )
            }
        },
        singleLine = true,
        modifier = modifier,
    )
    if (showPicker) {
        CalendarDatePickerDialog(
            initialDate = date ?: initialPickerDate,
            onDateSelected = onDateSelected,
            onDismiss = { showPicker = false },
        )
    }
}

/** Material date picker; it works in UTC millis, which map exactly to [CalendarDate] epoch days. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarDatePickerDialog(
    initialDate: CalendarDate,
    onDateSelected: (CalendarDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDate.toEpochDay() * MILLIS_PER_DAY)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    onDateSelected(CalendarDate.fromEpochDay(Math.floorDiv(millis, MILLIS_PER_DAY)))
                }
                onDismiss()
            }) {
                Text(stringResource(R.string.income_date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.list_detail_dialog_cancel)) }
        },
    ) {
        DatePicker(state = state)
    }
}
