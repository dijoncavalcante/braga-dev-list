package com.bragadev.list.features.listdetail.presentation.component

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.CalendarDate
import com.bragadev.list.core.domain.model.DayOfMonthSchedule
import com.bragadev.list.core.domain.model.ExtraIncome
import com.bragadev.list.core.domain.model.ExtraIncomeRecurrence
import com.bragadev.list.core.domain.model.IncomeFrequency
import com.bragadev.list.core.domain.model.IncomeSettings
import com.bragadev.list.core.domain.model.PayDay
import com.bragadev.list.core.util.extensions.toBrlCurrency
import com.bragadev.list.ui.theme.BragadevlistTheme
import kotlinx.coroutines.launch

/** How far ahead the next payment is looked for when suggesting its date. */
private const val SUGGESTION_WINDOW_DAYS = 62L

/** Day and amount being typed for one payment; the day is null until chosen. */
private data class PaymentInput(val day: Int? = null, val amountInCents: Long = 0)

/**
 * "Como você recebe?": monthly (one day and amount) or twice a month (two days, each with its
 * own amount), plus the date of the next payment and, optionally, the extra incomes.
 *
 * The next payment date is suggested from the chosen days (first one from today on) and follows
 * them whenever the days or the frequency change. The user can pick another date, e.g. when the
 * money arrives on Monday because the usual day is a Sunday.
 *
 * Like [AddItemBottomSheet], the sheet is not hidden on save: the caller removes it once saving
 * succeeds, so on failure it stays open with the user's input. Extra incomes are saved on their
 * own sheet ([ExtraIncomeBottomSheet]), so they do not depend on this sheet's save button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeSettingsBottomSheet(
    initialSettings: IncomeSettings?,
    today: CalendarDate,
    extraIncomes: List<ExtraIncome>,
    onConfirm: (IncomeSettings) -> Unit,
    onDismiss: () -> Unit,
    onAddExtraIncomeClick: () -> Unit,
    onExtraIncomeClick: (ExtraIncome) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    var frequency by remember { mutableStateOf(initialSettings?.frequency ?: IncomeFrequency.MONTHLY) }
    var first by remember { mutableStateOf(initialSettings?.payDays?.getOrNull(0).toInput()) }
    var second by remember { mutableStateOf(initialSettings?.payDays?.getOrNull(1).toInput()) }
    // A saved date that already passed is no longer "the next payment": suggest a new one instead.
    var pickedNextDate by remember { mutableStateOf(initialSettings?.nextPaymentDate?.takeIf { it >= today }) }
    var showErrors by remember { mutableStateOf(false) }

    val inputs = if (frequency == IncomeFrequency.MONTHLY) listOf(first) else listOf(first, second)
    val hasSameDays = frequency == IncomeFrequency.TWICE_A_MONTH && first.day != null && first.day == second.day
    val nextDate = pickedNextDate ?: suggestNextPaymentDate(inputs, today)

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
            Text(text = stringResource(R.string.income_title), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(R.string.income_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FrequencyChoice(
                selected = frequency,
                onSelect = {
                    if (it != frequency) pickedNextDate = null
                    frequency = it
                },
            )

            PaymentFields(
                title = if (frequency == IncomeFrequency.MONTHLY) {
                    stringResource(R.string.income_monthly_payment)
                } else {
                    stringResource(R.string.income_first_payment)
                },
                input = first,
                onInputChange = {
                    if (it.day != first.day) pickedNextDate = null
                    first = it
                },
                showErrors = showErrors,
            )
            if (frequency == IncomeFrequency.TWICE_A_MONTH) {
                PaymentFields(
                    title = stringResource(R.string.income_second_payment),
                    input = second,
                    onInputChange = {
                        if (it.day != second.day) pickedNextDate = null
                        second = it
                    },
                    showErrors = showErrors,
                )
            }
            if (showErrors && hasSameDays) {
                Text(
                    text = stringResource(R.string.income_error_same_day),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                text = stringResource(R.string.income_day_rule),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            DatePickerField(
                date = nextDate,
                label = stringResource(R.string.income_next_payment_label),
                supportingText = stringResource(R.string.income_next_payment_hint),
                onDateSelected = { pickedNextDate = it },
                initialPickerDate = today,
                modifier = Modifier.fillMaxWidth(),
            )

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
                    val isValid = inputs.all { it.day != null && it.amountInCents > 0 } && !hasSameDays
                    if (!isValid || nextDate == null) {
                        showErrors = true
                        return@Button
                    }
                    onConfirm(
                        IncomeSettings(
                            frequency = frequency,
                            payDays = inputs.map { PayDay(dayOfMonth = it.day!!, amountInCents = it.amountInCents) },
                            nextPaymentDate = nextDate,
                        ),
                    )
                }) {
                    Text(stringResource(R.string.list_detail_dialog_save_changes))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ExtraIncomesSection(
                extraIncomes = extraIncomes,
                onAddClick = onAddExtraIncomeClick,
                onIncomeClick = onExtraIncomeClick,
            )
        }
    }
}

/** "Outras entradas (opcional)": the saved extra incomes, each one opening its own sheet. */
@Composable
private fun ExtraIncomesSection(
    extraIncomes: List<ExtraIncome>,
    onAddClick: () -> Unit,
    onIncomeClick: (ExtraIncome) -> Unit,
) {
    Text(text = stringResource(R.string.extra_income_section_title), style = MaterialTheme.typography.titleMedium)
    Text(
        text = stringResource(R.string.extra_income_section_description),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    extraIncomes.forEach { income ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onIncomeClick(income) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = income.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = extraIncomeRecurrenceText(income.recurrence),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(text = income.amountInCents.toBrlCurrency(), style = MaterialTheme.typography.bodyMedium)
        }
    }
    OutlinedButton(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
        Text(text = stringResource(R.string.extra_income_add), modifier = Modifier.padding(start = 8.dp))
    }
}

/** "Todo mês, dia 15" or "Uma vez, em 20/10/2026". */
@Composable
internal fun extraIncomeRecurrenceText(recurrence: ExtraIncomeRecurrence): String = when (recurrence) {
    is ExtraIncomeRecurrence.Monthly -> stringResource(R.string.extra_income_monthly_on_day, recurrence.dayOfMonth)
    is ExtraIncomeRecurrence.Once -> stringResource(R.string.extra_income_once_on_date, recurrence.date.toDayMonthYear())
}

/** First payment from today on, following the days typed so far; null while no day is chosen. */
private fun suggestNextPaymentDate(inputs: List<PaymentInput>, today: CalendarDate): CalendarDate? {
    val payDays = inputs.mapNotNull { input -> input.day?.let { PayDay(it, input.amountInCents) } }
    if (payDays.isEmpty()) return null
    return DayOfMonthSchedule(payDays, adjustedNextPayment = null)
        .paymentsBetween(today, today.plusDays(SUGGESTION_WINDOW_DAYS))
        .firstOrNull()
        ?.date
}

private fun PayDay?.toInput(): PaymentInput =
    if (this == null) PaymentInput() else PaymentInput(day = dayOfMonth, amountInCents = amountInCents)

@Composable
private fun FrequencyChoice(selected: IncomeFrequency, onSelect: (IncomeFrequency) -> Unit) {
    Column(modifier = Modifier.selectableGroup()) {
        IncomeFrequency.entries.forEach { frequency ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = frequency == selected, role = Role.RadioButton) { onSelect(frequency) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // The whole row selects; the radio button only mirrors the state.
                RadioButton(selected = frequency == selected, onClick = null)
                Text(
                    text = stringResource(
                        when (frequency) {
                            IncomeFrequency.MONTHLY -> R.string.income_frequency_monthly
                            IncomeFrequency.TWICE_A_MONTH -> R.string.income_frequency_twice_a_month
                        },
                    ),
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}

/** Day (1..31) and amount of one payment, side by side. */
@Composable
private fun PaymentFields(
    title: String,
    input: PaymentInput,
    onInputChange: (PaymentInput) -> Unit,
    showErrors: Boolean,
) {
    Text(text = title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DayDropdownField(
            day = input.day,
            onDaySelected = { onInputChange(input.copy(day = it)) },
            isError = showErrors && input.day == null,
            modifier = Modifier.weight(0.4f),
        )
        CurrencyField(
            amountInCents = input.amountInCents,
            onAmountChange = { onInputChange(input.copy(amountInCents = it)) },
            isError = showErrors && input.amountInCents <= 0,
            modifier = Modifier.weight(0.6f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun IncomeSettingsBottomSheetPreview() {
    BragadevlistTheme {
        IncomeSettingsBottomSheet(
            initialSettings = IncomeSettings(
                frequency = IncomeFrequency.TWICE_A_MONTH,
                payDays = listOf(PayDay(10, 200_000), PayDay(25, 250_000)),
                nextPaymentDate = CalendarDate(2026, 10, 10),
            ),
            today = CalendarDate(2026, 10, 1),
            extraIncomes = listOf(ExtraIncome(1, "Aluguel", 120_000, ExtraIncomeRecurrence.Monthly(15))),
            onConfirm = {},
            onDismiss = {},
            onAddExtraIncomeClick = {},
            onExtraIncomeClick = {},
        )
    }
}
