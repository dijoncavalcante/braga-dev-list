package com.bragadev.fincheck.features.listdetail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.ExtraIncome
import com.bragadev.fincheck.core.domain.model.ExtraIncomeRecurrence
import com.bragadev.fincheck.ui.theme.BragadevlistTheme
import kotlinx.coroutines.launch

/**
 * Form of one extra income ("Outras entradas"): name, amount and whether it arrives every month
 * on the same day or only once, on a date.
 *
 * When [initialIncome] is given (editing), a trash button asks for confirmation before calling
 * [onDelete]. Like the other sheets, it is not hidden on save/delete: the caller removes it once
 * the operation succeeds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraIncomeBottomSheet(
    initialIncome: ExtraIncome?,
    today: CalendarDate,
    onConfirm: (ExtraIncome) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val initialRecurrence = initialIncome?.recurrence
    var name by remember { mutableStateOf(initialIncome?.name.orEmpty()) }
    var amountInCents by remember { mutableLongStateOf(initialIncome?.amountInCents ?: 0L) }
    var isMonthly by remember { mutableStateOf(initialRecurrence !is ExtraIncomeRecurrence.Once) }
    var day by remember { mutableStateOf((initialRecurrence as? ExtraIncomeRecurrence.Monthly)?.dayOfMonth) }
    var date by remember { mutableStateOf((initialRecurrence as? ExtraIncomeRecurrence.Once)?.date) }
    var showErrors by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val hideAndDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
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
                        if (initialIncome == null) R.string.extra_income_add else R.string.extra_income_edit_title,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (initialIncome != null) {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.extra_income_delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.extra_income_name_label)) },
                placeholder = { Text(stringResource(R.string.extra_income_name_placeholder)) },
                isError = showErrors && name.isBlank(),
                supportingText = if (showErrors && name.isBlank()) {
                    { Text(stringResource(R.string.extra_income_error_name)) }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            CurrencyField(
                amountInCents = amountInCents,
                onAmountChange = { amountInCents = it },
                isError = showErrors && amountInCents <= 0,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.extra_income_recurrence_question),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            RecurrenceChoice(isMonthly = isMonthly, onSelect = { isMonthly = it })
            if (isMonthly) {
                DayDropdownField(
                    day = day,
                    onDaySelected = { day = it },
                    isError = showErrors && day == null,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                DatePickerField(
                    date = date,
                    label = stringResource(R.string.extra_income_date_label),
                    onDateSelected = { date = it },
                    initialPickerDate = today,
                    errorText = if (showErrors && date == null) stringResource(R.string.extra_income_error_date) else null,
                    modifier = Modifier.fillMaxWidth(),
                )
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
                    val chosenDay = day
                    val chosenDate = date
                    val recurrence = when {
                        isMonthly && chosenDay != null -> ExtraIncomeRecurrence.Monthly(chosenDay)
                        !isMonthly && chosenDate != null -> ExtraIncomeRecurrence.Once(chosenDate)
                        else -> null
                    }
                    if (name.isBlank() || amountInCents <= 0 || recurrence == null) {
                        showErrors = true
                        return@Button
                    }
                    onConfirm(ExtraIncome(name = name.trim(), amountInCents = amountInCents, recurrence = recurrence))
                }) {
                    Text(stringResource(R.string.list_detail_dialog_save_changes))
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.extra_income_delete_title)) },
            text = { Text(stringResource(R.string.extra_income_delete_message, initialIncome?.name.orEmpty())) },
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

/** "Todo mês, no mesmo dia" or "Apenas uma vez". */
@Composable
private fun RecurrenceChoice(isMonthly: Boolean, onSelect: (isMonthly: Boolean) -> Unit) {
    Column(modifier = Modifier.selectableGroup()) {
        listOf(true to R.string.extra_income_monthly, false to R.string.extra_income_once).forEach { (monthly, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = isMonthly == monthly, role = Role.RadioButton) { onSelect(monthly) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // The whole row selects; the radio button only mirrors the state.
                RadioButton(selected = isMonthly == monthly, onClick = null)
                Text(text = stringResource(label), modifier = Modifier.padding(start = 12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun ExtraIncomeBottomSheetPreview() {
    BragadevlistTheme {
        ExtraIncomeBottomSheet(
            initialIncome = ExtraIncome(1, "Aluguel", 120_000, ExtraIncomeRecurrence.Monthly(15)),
            today = CalendarDate(2026, 10, 1),
            onConfirm = {},
            onDismiss = {},
            onDelete = {},
        )
    }
}
