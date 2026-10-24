package com.bragadev.fincheck.features.months.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillRepetition
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.util.extensions.MAX_DUE_DAY
import com.bragadev.fincheck.core.util.extensions.MIN_DUE_DAY
import com.bragadev.fincheck.core.util.extensions.currencyInputToCents
import com.bragadev.fincheck.core.util.extensions.toBrlCurrency
import com.bragadev.fincheck.ui.components.label
import com.bragadev.fincheck.ui.components.shortLabel
import com.bragadev.fincheck.ui.theme.FinCheckTheme
import kotlinx.coroutines.launch

/** Fewest months of "Por alguns meses": one month is "Só este mês". */
private const val MIN_REPEAT_MONTHS = 2

/** Most months of "Por alguns meses"; longer than that is "Todo mês". */
private const val MAX_REPEAT_MONTHS = 120

/** Months "Por alguns meses" starts with, e.g. a purchase in 6 installments. */
private const val DEFAULT_REPEAT_MONTHS = 6

/** "Repete?" options, in the order they are shown. */
private enum class RepetitionChoice { ONCE, MONTHLY, FOR_MONTHS }

/**
 * Bill form, used both to add and to edit a bill of the "Meses" tab.
 *
 * The period line under "Repete?" tells exactly which months the bill will be in
 * ("out/2026 → mar/2027"), so "6 meses" is never ambiguous. [startMonth] is the month a new bill
 * starts in, or the first month of the bill being edited: the repetition always counts from it.
 *
 * As in the item form, nothing is saved until the user taps save, and the sheet stays open when
 * saving fails; [onDelete] (edit mode) asks for confirmation first.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillBottomSheet(
    startMonth: YearMonth,
    onConfirm: (name: String, priceInCents: Long, dueDay: Int?, repetition: BillRepetition) -> Unit,
    onDismiss: () -> Unit,
    initialBill: Bill? = null,
    /** Month on screen, named in the delete confirmation. */
    month: YearMonth = startMonth,
    onDelete: (() -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    val isEditing = initialBill != null
    var name by rememberSaveable(initialBill) { mutableStateOf(initialBill?.name.orEmpty()) }
    var showNameError by rememberSaveable(initialBill) { mutableStateOf(false) }
    var priceInCents by rememberSaveable(initialBill) { mutableLongStateOf(initialBill?.priceInCents ?: 0L) }
    val priceText = priceInCents.toBrlCurrency()
    var dueDay by rememberSaveable(initialBill) { mutableStateOf(initialBill?.dueDay) }
    val initialRepetition = initialBill?.repetition ?: BillRepetition.Monthly
    var choice by rememberSaveable(initialBill) { mutableStateOf(initialRepetition.toChoice()) }
    var monthCount by rememberSaveable(initialBill) {
        mutableIntStateOf((initialRepetition as? BillRepetition.ForMonths)?.count ?: DEFAULT_REPEAT_MONTHS)
    }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    val repetition =
        when (choice) {
            RepetitionChoice.ONCE -> BillRepetition.Once
            RepetitionChoice.MONTHLY -> BillRepetition.Monthly
            RepetitionChoice.FOR_MONTHS -> BillRepetition.ForMonths(monthCount)
        }

    // Cancel button: animate the sheet away before telling the caller, like a swipe down does.
    val hideAndDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(if (isEditing) R.string.bill_edit_title else R.string.bill_add_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (onDelete != null) {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.bill_delete),
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
                label = { Text(stringResource(R.string.bill_name_label)) },
                placeholder = { Text(stringResource(R.string.bill_name_placeholder)) },
                isError = showNameError,
                supportingText =
                    if (showNameError) {
                        { Text(stringResource(R.string.bill_error_empty_name)) }
                    } else {
                        null
                    },
                singleLine = true,
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
            DueDayField(dueDay = dueDay, onDueDayChange = { dueDay = it })
            RepetitionField(
                choice = choice,
                onChoiceChange = { choice = it },
                monthCount = monthCount,
                onMonthCountChange = { monthCount = it.coerceIn(MIN_REPEAT_MONTHS, MAX_REPEAT_MONTHS) },
            )
            PeriodText(startMonth = startMonth, endMonth = repetition.endMonth(startMonth))
            Row(
                modifier =
                    Modifier
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
                    onConfirm(name.trim(), priceInCents, dueDay, repetition)
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
            title = { Text(stringResource(R.string.bill_delete_title)) },
            text = { Text(stringResource(R.string.bill_delete_message, initialBill?.name.orEmpty(), month.label())) },
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

/** Day of the month (1..31) the bill is due, or none ("Outras despesas"). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDayField(
    dueDay: Int?,
    onDueDayChange: (Int?) -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = isMenuExpanded, onExpandedChange = { isMenuExpanded = it }) {
        OutlinedTextField(
            value = dueDay?.let { stringResource(R.string.list_detail_item_due_day_option, it) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.list_detail_item_due_date_label)) },
            placeholder = { Text(stringResource(R.string.list_detail_item_due_day_placeholder)) },
            supportingText =
                if (dueDay == null) {
                    { Text(stringResource(R.string.bill_due_day_hint)) }
                } else {
                    null
                },
            trailingIcon = {
                if (dueDay == null) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded)
                } else {
                    IconButton(onClick = { onDueDayChange(null) }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.list_detail_item_due_date_clear),
                        )
                    }
                }
            },
            singleLine = true,
            modifier =
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = isMenuExpanded, onDismissRequest = { isMenuExpanded = false }) {
            (MIN_DUE_DAY..MAX_DUE_DAY).forEach { day ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.list_detail_item_due_day_option, day)) },
                    onClick = {
                        onDueDayChange(day)
                        isMenuExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

/** "Repete?": only this month, every month, or a number of months chosen with − / +. */
@Composable
private fun RepetitionField(
    choice: RepetitionChoice,
    onChoiceChange: (RepetitionChoice) -> Unit,
    monthCount: Int,
    onMonthCountChange: (Int) -> Unit,
) {
    Text(
        text = stringResource(R.string.bill_repeat_question),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 8.dp),
    )
    Column(modifier = Modifier.selectableGroup()) {
        RepetitionChoiceRow(stringResource(R.string.bill_repeat_once), choice == RepetitionChoice.ONCE) {
            onChoiceChange(RepetitionChoice.ONCE)
        }
        RepetitionChoiceRow(stringResource(R.string.bill_repeat_monthly), choice == RepetitionChoice.MONTHLY) {
            onChoiceChange(RepetitionChoice.MONTHLY)
        }
        RepetitionChoiceRow(stringResource(R.string.bill_repeat_for_months), choice == RepetitionChoice.FOR_MONTHS) {
            onChoiceChange(RepetitionChoice.FOR_MONTHS)
        }
    }
    if (choice == RepetitionChoice.FOR_MONTHS) {
        Row(
            modifier = Modifier.padding(start = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilledTonalIconButton(
                onClick = { onMonthCountChange(monthCount - 1) },
                enabled = monthCount > MIN_REPEAT_MONTHS,
            ) {
                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.bill_repeat_fewer))
            }
            Text(
                text = pluralStringResource(R.plurals.bill_repeat_months_count, monthCount, monthCount),
                style = MaterialTheme.typography.titleMedium,
            )
            FilledTonalIconButton(
                onClick = { onMonthCountChange(monthCount + 1) },
                enabled = monthCount < MAX_REPEAT_MONTHS,
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.bill_repeat_more))
            }
        }
    }
}

@Composable
private fun RepetitionChoiceRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
    }
}

/** Which months the bill will be in: "Só em out/2026", "out/2026 → mar/2027" or "A partir de out/2026...". */
@Composable
private fun PeriodText(
    startMonth: YearMonth,
    endMonth: YearMonth?,
) {
    Text(
        text =
            when (endMonth) {
                null -> stringResource(R.string.bill_period_from, startMonth.shortLabel())
                startMonth -> stringResource(R.string.bill_period_once, startMonth.shortLabel())
                else -> stringResource(R.string.bill_period_range, startMonth.shortLabel(), endMonth.shortLabel())
            },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

private fun BillRepetition.toChoice(): RepetitionChoice =
    when (this) {
        BillRepetition.Once -> RepetitionChoice.ONCE
        BillRepetition.Monthly -> RepetitionChoice.MONTHLY
        is BillRepetition.ForMonths -> RepetitionChoice.FOR_MONTHS
    }

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun BillBottomSheetPreview() {
    FinCheckTheme {
        BillBottomSheet(startMonth = YearMonth(2026, 10), onConfirm = { _, _, _, _ -> }, onDismiss = {})
    }
}
