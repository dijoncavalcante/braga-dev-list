package com.bragadev.list.features.listdetail.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import com.bragadev.list.core.domain.model.FinancialOverview
import com.bragadev.list.core.domain.model.IncomeFrequency
import com.bragadev.list.core.domain.model.IncomeSettings
import com.bragadev.list.core.domain.model.PayDay
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.buildFinancialOverview
import com.bragadev.list.ui.theme.BragadevlistTheme

/** Percent → progress bar fraction. */
private const val PERCENT = 100f

/** Which cycle has its breakdown open in [CycleOverviewCard]. */
private enum class OpenCycle { CURRENT, NEXT }

/**
 * Top of the "Ciclos financeiros" view, built to be read in a glance:
 * 1. the projected balance of both cycles together (the answer to "how will I end the period?");
 * 2. each cycle side by side, with its own balance and how much of its income is committed;
 * 3. at most one alert, only when something needs action.
 *
 * The breakdown of a cycle (income, extra incomes, bills, other expenses) is there on demand:
 * tapping a cycle opens it, so the screen stays light without hiding any information.
 */
@Composable
fun CycleOverviewCard(
    overview: FinancialOverview,
    onEditIncomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var openCycle by rememberSaveable { mutableStateOf<OpenCycle?>(null) }
    val toggle: (OpenCycle) -> Unit = { cycle -> openCycle = if (openCycle == cycle) null else cycle }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TotalBalanceHeader(overview = overview, onEditIncomeClick = onEditIncomeClick)

            Column {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CycleTile(
                        title = stringResource(R.string.cycle_current_title),
                        cycle = overview.current,
                        isOpen = openCycle == OpenCycle.CURRENT,
                        onClick = { toggle(OpenCycle.CURRENT) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    CycleTile(
                        title = stringResource(R.string.cycle_next_title),
                        cycle = overview.next,
                        isOpen = openCycle == OpenCycle.NEXT,
                        onClick = { toggle(OpenCycle.NEXT) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                AnimatedContent(
                    targetState = openCycle,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "cycleBreakdown",
                ) { open ->
                    when (open) {
                        OpenCycle.CURRENT -> CycleBreakdown(cycle = overview.current, today = overview.today)
                        OpenCycle.NEXT -> CycleBreakdown(cycle = overview.next, today = overview.today)
                        null -> Box(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            overview.mainAlert?.let { alert -> AlertBanner(text = alertText(alert)) }
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

/** "Saldo projetado" of both cycles in big type, the period it covers and when the money comes in next. */
@Composable
private fun TotalBalanceHeader(overview: FinancialOverview, onEditIncomeClick: () -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.cycle_overview_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = overview.projectedBalanceInCents.toDisplayAmount(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = balanceColor(overview.projectedBalanceInCents),
            )
            val days = overview.daysUntilNextPayment.toInt()
            Text(
                text = stringResource(
                    R.string.cycle_period,
                    overview.current.startDate.toDayMonth(),
                    overview.periodEndDate.toDayMonth(),
                ) + " · " + pluralStringResource(R.plurals.cycle_next_payment_in, days, days),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onEditIncomeClick) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.cycle_edit_income),
            )
        }
    }
}

/** One cycle in a compact tile: period, its own balance and a bar of how much of the income is committed. */
@Composable
private fun CycleTile(
    title: String,
    cycle: FinancialCycle,
    isOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val arrowRotation by animateFloatAsState(targetValue = if (isOpen) 180f else 0f, label = "tileArrow")
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (isOpen) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(
                        if (isOpen) R.string.list_detail_section_collapse else R.string.list_detail_section_expand,
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(arrowRotation),
                )
            }
            Text(
                text = stringResource(R.string.cycle_period, cycle.startDate.toDayMonth(), cycle.endDate.toDayMonth()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ShrinkToFitText(
                text = cycle.projectedBalanceInCents.toDisplayAmount(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = balanceColor(cycle.projectedBalanceInCents),
            )
            cycle.committedPercent?.let { percent ->
                LinearProgressIndicator(
                    progress = { (percent / PERCENT).coerceIn(0f, 1f) },
                    color = statusColor(cycle.status),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                )
            }
        }
    }
}

/**
 * Where a cycle's balance comes from: the payment and each extra income ("a receber" when still
 * to come), the bills and the other expenses, then its balance and how it is doing.
 */
@Composable
private fun CycleBreakdown(cycle: FinancialCycle, today: CalendarDate) {
    Column(
        modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AmountRow(
            label = stringResource(R.string.cycle_income_on, cycle.payment.date.toDayMonth()),
            text = "+ ${cycle.payment.amountInCents.toDisplayAmount()}",
        )
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
        AmountRow(label = stringResource(R.string.cycle_bills), text = "- ${cycle.billsInCents.toDisplayAmount()}")
        if (cycle.otherExpenses.isNotEmpty()) {
            AmountRow(
                label = stringResource(R.string.cycle_other_expenses),
                text = "- ${cycle.otherExpensesInCents.toDisplayAmount()}",
            )
        }
        HorizontalDivider()
        AmountRow(
            label = stringResource(R.string.cycle_balance),
            text = cycle.projectedBalanceInCents.toDisplayAmount(),
            color = balanceColor(cycle.projectedBalanceInCents),
            isHighlighted = true,
        )
        cycle.committedPercent?.let { percent ->
            Text(
                text = stringResource(R.string.cycle_committed, percent) + " · " + stringResource(statusLabel(cycle.status)),
                style = MaterialTheme.typography.bodySmall,
                color = statusColor(cycle.status),
            )
        }
    }
}

/** The single alert of the card: one short line on a soft error background. */
@Composable
private fun AlertBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
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
            color = if (isHighlighted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = text,
            style = if (isHighlighted) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else null,
            color = color,
        )
    }
}

@Composable
private fun balanceColor(amountInCents: Long): Color =
    if (amountInCents < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

private fun statusLabel(status: CycleStatus): Int = when (status) {
    CycleStatus.HEALTHY -> R.string.cycle_status_healthy
    CycleStatus.TIGHT -> R.string.cycle_status_tight
    CycleStatus.OVER_BUDGET -> R.string.cycle_status_over_budget
}

@Composable
private fun statusColor(status: CycleStatus): Color = when (status) {
    CycleStatus.HEALTHY -> MaterialTheme.colorScheme.primary
    CycleStatus.TIGHT -> MaterialTheme.colorScheme.tertiary
    CycleStatus.OVER_BUDGET -> MaterialTheme.colorScheme.error
}

/** Text of [FinancialOverview.mainAlert]; one bill is named, several are counted and summed. */
@Composable
private fun alertText(alert: CycleInsight): String = when (alert) {
    is CycleInsight.OverdueBills -> {
        val bill = alert.bills.singleOrNull()
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
                alert.bills.size,
                alert.bills.sumOf { it.amountInCents }.toDisplayAmount(),
            )
        }
    }
    is CycleInsight.BillsDueBeforeExtraIncome -> {
        val bill = alert.bills.singleOrNull()
        val extra = alert.extraIncome
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
                alert.bills.size,
                alert.bills.sumOf { it.amountInCents }.toDisplayAmount(),
                extra.income.name,
                extra.date.toDayMonth(),
            )
        }
    }
    // mainAlert only returns the two kinds above.
    else -> ""
}

@Preview(showBackground = true)
@Composable
private fun CycleOverviewCardPreview() {
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
        CycleOverviewCard(
            overview = overview,
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
