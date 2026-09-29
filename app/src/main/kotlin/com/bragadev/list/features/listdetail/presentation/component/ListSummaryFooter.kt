package com.bragadev.list.features.listdetail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.ItemsTotal
import com.bragadev.list.core.domain.model.ListSummary
import com.bragadev.list.core.util.extensions.toBrlCurrency
import com.bragadev.list.ui.theme.BragadevlistTheme

/**
 * Totals at the end of the list: how many items are unchecked, checked and in total,
 * with how much each group adds up to (unit price × quantity).
 * With [showAmounts] off ("Mostrar valor" disabled) only the counts are shown.
 */
@Composable
fun ListSummaryFooter(summary: ListSummary, modifier: Modifier = Modifier, showAmounts: Boolean = true) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryRow(
                label = stringResource(R.string.list_detail_summary_unchecked, summary.unchecked.count),
                total = summary.unchecked,
                showAmount = showAmounts,
            )
            SummaryRow(
                label = stringResource(R.string.list_detail_summary_checked, summary.checked.count),
                total = summary.checked,
                showAmount = showAmounts,
            )
            HorizontalDivider()
            SummaryRow(
                label = stringResource(R.string.list_detail_summary_total, summary.total.count),
                total = summary.total,
                showAmount = showAmounts,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    total: ItemsTotal,
    showAmount: Boolean,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = style)
        if (showAmount) {
            Text(text = total.amountInCents.toBrlCurrency(), style = style)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListSummaryFooterPreview() {
    BragadevlistTheme {
        ListSummaryFooter(
            summary = ListSummary(
                unchecked = ItemsTotal(count = 3, amountInCents = 25_000),
                checked = ItemsTotal(count = 2, amountInCents = 18_990),
                total = ItemsTotal(count = 5, amountInCents = 43_990),
            ),
        )
    }
}
