package com.bragadev.list.features.listdetail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.CalendarDate
import com.bragadev.list.core.domain.model.CycleInsight
import com.bragadev.list.core.domain.model.CycleStatus
import com.bragadev.list.core.domain.model.ExtraIncome
import com.bragadev.list.core.domain.model.ExtraIncomeRecurrence
import com.bragadev.list.core.domain.model.FinancialCycle
import com.bragadev.list.core.domain.model.IncomeFrequency
import com.bragadev.list.core.domain.model.IncomeSettings
import com.bragadev.list.core.domain.model.PayDay
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.buildFinancialOverview
import com.bragadev.list.ui.theme.BragadevlistTheme

/** Percent → progress bar fraction. */
private const val PERCENT = 100f

/**
 * Health of one financial cycle: income × expenses, projected balance and how much of the
 * income is already committed. The current cycle also lists the [insights] about today and
 * offers [onEditIncomeClick]. Extra incomes dated after [today] are flagged "a receber".
 */
@Composable
fun FinancialCycleCard(
    title: String,
    cycle: FinancialCycle,
    today: CalendarDate,
    modifier: Modifier = Modifier,
    insights: List<CycleInsight> = emptyList(),
    onEditIncomeClick: (() -> Unit)? = null,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(
                            R.string.cycle_period,
                            cycle.startDate.toDayMonth(),
                            cycle.endDate.toDayMonth(),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusLabel(cycle.status)
                if (onEditIncomeClick != null) {
                    IconButton(onClick = onEditIncomeClick) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.cycle_edit_income),
                        )
                    }
                }
            }

            IncomeRows(cycle = cycle, today = today)
            AmountRow(label = stringResource(R.string.cycle_bills), text = "- ${cycle.billsInCents.toDisplayAmount()}")
            if (cycle.otherExpenses.isNotEmpty()) {
                AmountRow(
                    label = stringResource(R.string.cycle_other_expenses),
                    text = "- ${cycle.otherExpensesInCents.toDisplayAmount()}",
                )
            }
            HorizontalDivider()
            val isNegative = cycle.projectedBalanceInCents < 0
            AmountRow(
                label = stringResource(R.string.cycle_balance),
                text = cycle.projectedBalanceInCents.toDisplayAmount(),
                color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                isHighlighted = true,
            )

            cycle.committedPercent?.let { percent ->
                LinearProgressIndicator(
                    progress = { (percent / PERCENT).coerceIn(0f, 1f) },
                    color = statusColor(cycle.status),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                )
                Text(
                    text = stringResource(R.string.cycle_committed, percent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (insights.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                insights.forEach { insight ->
                    Text(text = insightText(insight), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/** Shown in the cycles view until the user tells how they receive their income. */
@Composable
fun IncomeSetupCard(onSetupClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = stringResource(R.string.cycle_setup_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.cycle_setup_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onSetupClick, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.cycle_setup_button))
            }
        }
    }
}

/**
 * The payment that opens the cycle and, below it, each extra income on its own line (so the user
 * sees where the money comes from) and their total.
 */
@Composable
private fun IncomeRows(cycle: FinancialCycle, today: CalendarDate) {
    AmountRow(
        label = stringResource(R.string.cycle_income_on, cycle.payment.date.toDayMonth()),
        text = "+ ${cycle.payment.amountInCents.toDisplayAmount()}",
    )
    if (cycle.extraIncomes.isEmpty()) return
    cycle.extraIncomes.forEach { entry ->
        AmountRow(
            label = stringResource(
                if (entry.date > today) R.string.cycle_extra_income_upcoming else R.string.cycle_extra_income,
                entry.income.name,
                entry.date.toDayMonth(),
            ),
            text = "+ ${entry.amountInCents.toDisplayAmount()}",
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
    AmountRow(
        label = stringResource(R.string.cycle_total_income),
        text = cycle.incomeInCents.toDisplayAmount(),
        isHighlighted = true,
    )
}

@Composable
private fun AmountRow(
    label: String,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    isHighlighted: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else null,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = text,
            style = if (isHighlighted) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else null,
            color = color,
        )
    }
}

@Composable
private fun StatusLabel(status: CycleStatus) {
    Text(
        text = stringResource(
            when (status) {
                CycleStatus.HEALTHY -> R.string.cycle_status_healthy
                CycleStatus.TIGHT -> R.string.cycle_status_tight
                CycleStatus.OVER_BUDGET -> R.string.cycle_status_over_budget
            },
        ),
        style = MaterialTheme.typography.labelMedium,
        color = statusColor(status),
    )
}

@Composable
private fun statusColor(status: CycleStatus): Color = when (status) {
    CycleStatus.HEALTHY -> MaterialTheme.colorScheme.primary
    CycleStatus.TIGHT -> MaterialTheme.colorScheme.tertiary
    CycleStatus.OVER_BUDGET -> MaterialTheme.colorScheme.error
}

@Composable
private fun insightText(insight: CycleInsight): String = when (insight) {
    is CycleInsight.Received -> stringResource(
        R.string.cycle_insight_received,
        insight.payment.amountInCents.toDisplayAmount(),
        insight.payment.date.toDayMonth(),
    )
    is CycleInsight.NextPayment -> pluralStringResource(
        R.plurals.cycle_insight_next_payment,
        insight.daysUntil.toInt(),
        insight.daysUntil.toInt(),
        insight.payment.amountInCents.toDisplayAmount(),
        insight.payment.date.toDayMonth(),
    )
    is CycleInsight.ExtraIncomeExpected -> {
        val entry = insight.entries.singleOrNull()
        if (entry != null) {
            stringResource(
                R.string.cycle_insight_extra_income_expected,
                entry.amountInCents.toDisplayAmount(),
                entry.income.name,
                entry.date.toDayMonth(),
            )
        } else {
            stringResource(
                R.string.cycle_insight_extra_incomes_expected,
                insight.entries.size,
                insight.entries.sumOf { it.amountInCents }.toDisplayAmount(),
            )
        }
    }
    is CycleInsight.BillsDueBeforeExtraIncome -> {
        val bill = insight.bills.singleOrNull()
        val extra = insight.extraIncome
        if (bill != null) {
            stringResource(
                R.string.cycle_insight_bill_before_extra,
                bill.item.name,
                bill.amountInCents.toDisplayAmount(),
                bill.dueDate?.toDayMonth().orEmpty(),
                extra.income.name,
                extra.date.toDayMonth(),
            )
        } else {
            stringResource(
                R.string.cycle_insight_bills_before_extra,
                insight.bills.size,
                insight.bills.sumOf { it.amountInCents }.toDisplayAmount(),
                extra.income.name,
                extra.date.toDayMonth(),
            )
        }
    }
    is CycleInsight.Balance -> balanceText(insight.cycle)
    is CycleInsight.BillsDueBeforeNextPayment -> {
        val bill = insight.bills.singleOrNull()
        if (bill != null) {
            stringResource(
                R.string.cycle_insight_bill_due,
                bill.item.name,
                bill.amountInCents.toDisplayAmount(),
                bill.dueDate?.toDayMonth().orEmpty(),
            )
        } else {
            stringResource(
                R.string.cycle_insight_bills_due,
                insight.bills.size,
                insight.bills.sumOf { it.amountInCents }.toDisplayAmount(),
            )
        }
    }
    is CycleInsight.OverdueBills -> {
        val bill = insight.bills.singleOrNull()
        if (bill != null) {
            stringResource(
                R.string.cycle_insight_bill_overdue,
                bill.item.name,
                bill.amountInCents.toDisplayAmount(),
                bill.dueDate?.toDayMonth().orEmpty(),
            )
        } else {
            stringResource(
                R.string.cycle_insight_bills_overdue,
                insight.bills.size,
                insight.bills.sumOf { it.amountInCents }.toDisplayAmount(),
            )
        }
    }
    is CycleInsight.NextCycleOverBudget -> stringResource(
        R.string.cycle_insight_next_cycle_over,
        insight.cycle.startDate.toDayMonth(),
        insight.cycle.endDate.toDayMonth(),
        insight.cycle.expensesInCents.toDisplayAmount(),
        insight.cycle.incomeInCents.toDisplayAmount(),
    )
}

@Composable
private fun balanceText(cycle: FinancialCycle): String {
    val expenses = cycle.expensesInCents.toDisplayAmount()
    val income = cycle.incomeInCents.toDisplayAmount()
    return when (cycle.status) {
        CycleStatus.HEALTHY -> stringResource(
            R.string.cycle_insight_balance_healthy,
            expenses,
            cycle.projectedBalanceInCents.toDisplayAmount(),
        )
        CycleStatus.TIGHT -> stringResource(
            R.string.cycle_insight_balance_tight,
            expenses,
            income,
            cycle.projectedBalanceInCents.toDisplayAmount(),
        )
        CycleStatus.OVER_BUDGET -> stringResource(
            R.string.cycle_insight_balance_over,
            expenses,
            income,
            (-cycle.projectedBalanceInCents).toDisplayAmount(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FinancialCycleCardPreview() {
    val items = listOf(
        previewItem(1, "Aluguel", 100_000, dueDay = 25),
        previewItem(2, "Internet", 10_000, dueDay = 28),
        previewItem(3, "Energia", 20_000, dueDay = 5),
        previewItem(4, "Mercado", 40_000, dueDay = null),
    )
    val overview = buildFinancialOverview(
        settings = IncomeSettings(
            frequency = IncomeFrequency.TWICE_A_MONTH,
            payDays = listOf(PayDay(10, 200_000), PayDay(25, 250_000)),
            nextPaymentDate = CalendarDate(2026, 10, 10),
        ),
        items = items,
        today = CalendarDate(2026, 10, 1),
        extraIncomes = listOf(ExtraIncome(1, "Aluguel", 120_000, ExtraIncomeRecurrence.Monthly(5))),
    ) ?: return
    BragadevlistTheme {
        FinancialCycleCard(
            title = "Ciclo atual",
            cycle = overview.current,
            today = overview.today,
            insights = overview.insights(),
            onEditIncomeClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

private fun previewItem(id: Long, name: String, priceInCents: Long, dueDay: Int?) = ShoppingListItem(
    id = id,
    listId = 1,
    name = name,
    quantity = 1,
    priceInCents = priceInCents,
    dueDay = dueDay,
    isChecked = false,
    createdAt = id,
)
