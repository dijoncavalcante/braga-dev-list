package com.bragadev.fincheck.features.months.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillBook
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.Fortnight
import com.bragadev.fincheck.core.domain.model.FortnightOverview
import com.bragadev.fincheck.core.domain.model.IncomeFrequency
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.model.PayDay
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.model.buildMonthOverview
import com.bragadev.fincheck.core.domain.model.toFortnights
import com.bragadev.fincheck.ui.components.toDisplayAmount
import com.bragadev.fincheck.ui.theme.FinCheckTheme

/** Which fortnight has its breakdown open in [FortnightOverviewCard]. */
private enum class OpenFortnight { FIRST, SECOND }

/**
 * Top of the "Quinzenas" view, in the same format as the cycles card:
 * 1. what should be left at the end of the month;
 * 2. each fortnight side by side, with its own balance and how much of its money is committed, the
 *    one today is in marked "atual";
 * 3. at most one alert, for bills already due and not paid.
 *
 * The 2ª quinzena carries what was left (or missing) from the 1ª. Tapping a fortnight opens where
 * its balance comes from.
 */
@Composable
fun FortnightOverviewCard(
    overview: FortnightOverview,
    onEditIncomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by rememberSaveable { mutableStateOf<OpenFortnight?>(null) }
    val toggle: (OpenFortnight) -> Unit = { fortnight -> open = if (open == fortnight) null else fortnight }
    val current = overview.current

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MonthBalanceHeader(overview = overview, onEditIncomeClick = onEditIncomeClick)

            Column {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FortnightTile(
                        title = stringResource(R.string.list_detail_first_fortnight),
                        fortnight = overview.first,
                        isCurrent = current == overview.first,
                        isOpen = open == OpenFortnight.FIRST,
                        onClick = { toggle(OpenFortnight.FIRST) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    FortnightTile(
                        title = stringResource(R.string.list_detail_second_fortnight),
                        fortnight = overview.second,
                        isCurrent = current == overview.second,
                        isOpen = open == OpenFortnight.SECOND,
                        onClick = { toggle(OpenFortnight.SECOND) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                AnimatedContent(
                    targetState = open,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "fortnightBreakdown",
                ) { target ->
                    when (target) {
                        OpenFortnight.FIRST -> FortnightBreakdown(overview.first, today = overview.overview.today)
                        OpenFortnight.SECOND -> FortnightBreakdown(overview.second, today = overview.overview.today)
                        null -> Box(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            val overdue = overview.overdueBills
            if (overdue.isNotEmpty()) {
                AlertBanner(
                    pluralStringResource(
                        R.plurals.month_summary_overdue,
                        overdue.size,
                        overdue.size,
                        overdue.sumOf { it.amountInCents }.toDisplayAmount(),
                    ),
                )
            }
        }
    }
}

/** "Saldo previsto do mês" in big type, the month's dates and its bills without a due day. */
@Composable
private fun MonthBalanceHeader(overview: FortnightOverview, onEditIncomeClick: () -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.month_summary_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = overview.projectedBalanceInCents.toDisplayAmount(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = balanceColor(overview.projectedBalanceInCents),
            )
            val period = stringResource(
                R.string.cycle_period,
                overview.first.startDate.toDayMonth(),
                overview.second.endDate.toDayMonth(),
            )
            Text(
                // Bills without a due day belong to no fortnight: they only count here, in the month.
                text = if (overview.otherExpenses.isEmpty()) {
                    period
                } else {
                    period + " · " +
                        stringResource(R.string.fortnight_other_expenses, overview.otherExpensesInCents.toDisplayAmount())
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onEditIncomeClick) {
            Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(R.string.cycle_edit_income))
        }
    }
}

/** One fortnight as a tile: title, dates, balance and bar, marked "atual" when [isCurrent]. */
@Composable
private fun FortnightTile(
    title: String,
    fortnight: Fortnight,
    isCurrent: Boolean,
    isOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OverviewTile(
        title = title,
        period = stringResource(R.string.cycle_period, fortnight.startDate.toDayMonth(), fortnight.endDate.toDayMonth()),
        balanceInCents = fortnight.balanceInCents,
        committedPercent = fortnight.committedPercent,
        status = fortnight.status,
        isOpen = isOpen,
        onClick = onClick,
        modifier = modifier,
        badge = if (isCurrent) stringResource(R.string.fortnight_current) else null,
    )
}

/**
 * Where a fortnight's balance comes from: what was left (or missing) from the 1ª quinzena, each
 * payment and extra income ("a receber" when still to come) and its bills, then its balance and
 * how much of its money they take.
 */
@Composable
private fun FortnightBreakdown(fortnight: Fortnight, today: CalendarDate) {
    Column(
        modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val carried = fortnight.carriedOverInCents
        when {
            carried > 0 -> AmountRow(
                label = stringResource(R.string.fortnight_carried_over),
                text = "+ ${carried.toDisplayAmount()}",
            )
            carried < 0 -> AmountRow(
                label = stringResource(R.string.fortnight_carried_missing),
                text = "- ${(-carried).toDisplayAmount()}",
                color = MaterialTheme.colorScheme.error,
            )
        }
        fortnight.payments.forEach { payment ->
            AmountRow(
                label = stringResource(R.string.cycle_income_on, payment.date.toDayMonth()),
                text = "+ ${payment.amountInCents.toDisplayAmount()}",
            )
        }
        fortnight.extraIncomes.forEach { entry ->
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
        AmountRow(label = stringResource(R.string.cycle_bills), text = "- ${fortnight.totals.totalInCents.toDisplayAmount()}")
        HorizontalDivider()
        AmountRow(
            label = stringResource(R.string.fortnight_balance),
            text = fortnight.balanceInCents.toDisplayAmount(),
            color = balanceColor(fortnight.balanceInCents),
            isHighlighted = true,
        )
        fortnight.committedPercent?.let { percent -> CommittedText(percent, fortnight.status) }
    }
}

@Preview(showBackground = true)
@Composable
private fun FortnightOverviewCardPreview() {
    val october = YearMonth(2026, 10)
    val bills = listOf(
        Bill(1, "Energia", 20_000, dueDay = 4, startMonth = october, endMonth = null),
        Bill(2, "Aluguel", 150_000, dueDay = 10, startMonth = october, endMonth = null),
        Bill(3, "Cartão", 520_000, dueDay = 26, startMonth = october, endMonth = null),
        Bill(4, "Mercado", 80_000, dueDay = null, startMonth = october, endMonth = null),
    )
    val settings = IncomeSettings(
        frequency = IncomeFrequency.TWICE_A_MONTH,
        payDays = listOf(PayDay(5, 500_000), PayDay(20, 300_000)),
        nextPaymentDate = CalendarDate(2026, 11, 5),
    )
    val overview = buildMonthOverview(october, CalendarDate(2026, 10, 24), BillBook(bills), settings).toFortnights()
    FinCheckTheme {
        FortnightOverviewCard(overview = overview, onEditIncomeClick = {}, modifier = Modifier.padding(16.dp))
    }
}
