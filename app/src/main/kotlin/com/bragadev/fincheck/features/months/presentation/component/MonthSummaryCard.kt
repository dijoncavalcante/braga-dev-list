package com.bragadev.fincheck.features.months.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.MonthOverview
import com.bragadev.fincheck.core.domain.model.MonthTiming
import com.bragadev.fincheck.ui.components.toDisplayAmount

/**
 * Top of the month in the "Todos" and "Quinzenas" views: what should be left at the end of the
 * month (income minus bills), how much is paid, and at most one alert, only for bills already due.
 * Without income set up it shows the bills' totals and an invitation to set it up.
 */
@Composable
fun MonthSummaryCard(
    overview: MonthOverview,
    hasIncome: Boolean,
    onEditIncomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totals = overview.totals
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text =
                            stringResource(
                                if (hasIncome) R.string.month_summary_title else R.string.month_summary_title_no_income,
                            ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val headline = if (hasIncome) overview.projectedBalanceInCents else totals.totalInCents
                    Text(
                        text = headline.toDisplayAmount(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasIncome) balanceColor(headline) else MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = onEditIncomeClick) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(R.string.cycle_edit_income))
                }
            }

            if (totals.totalInCents > 0) {
                LinearProgressIndicator(
                    progress = { (totals.paidInCents.toFloat() / totals.totalInCents).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (hasIncome) {
                AmountRow(stringResource(R.string.month_summary_income), "+ ${overview.incomeInCents.toDisplayAmount()}")
                AmountRow(stringResource(R.string.month_summary_bills), "- ${totals.totalInCents.toDisplayAmount()}")
                HorizontalDivider()
            }
            AmountRow(stringResource(R.string.month_summary_paid), totals.paidInCents.toDisplayAmount())
            AmountRow(
                label = stringResource(R.string.month_summary_to_pay),
                text = totals.unpaidInCents.toDisplayAmount(),
                isHighlighted = true,
            )

            if (!hasIncome) {
                Text(
                    text = stringResource(R.string.month_summary_setup_income),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onEditIncomeClick, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.cycle_setup_button))
                }
            }

            val overdue = overview.overdueBills
            when {
                overdue.isNotEmpty() ->
                    AlertBanner(
                        pluralStringResource(
                            R.plurals.month_summary_overdue,
                            overdue.size,
                            overdue.size,
                            overdue.sumOf { it.amountInCents }.toDisplayAmount(),
                        ),
                    )
                overview.timing == MonthTiming.FUTURE && overview.bills.isNotEmpty() ->
                    Text(
                        text = stringResource(R.string.month_summary_future),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
            }
        }
    }
}

