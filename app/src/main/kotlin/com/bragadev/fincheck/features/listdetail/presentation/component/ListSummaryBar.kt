package com.bragadev.fincheck.features.listdetail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.ItemsTotal
import com.bragadev.fincheck.core.domain.model.ListSummary
import com.bragadev.fincheck.ui.components.adaptiveContentWidth
import com.bragadev.fincheck.ui.theme.FinCheckTheme

/**
 * Totals pinned to the bottom of the screen, side by side: how many items are unchecked,
 * checked and in total, with how much each group adds up to (unit price × quantity).
 * Being horizontal keeps it short enough to stay always visible while the list scrolls.
 *
 * With [showAmounts] off ("Mostrar valor" disabled) the counts take the place of the amounts.
 */
@Composable
fun ListSummaryBar(summary: ListSummary, modifier: Modifier = Modifier, showAmounts: Boolean = true) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            // The bar spans the screen; its three columns line up with the centered content on tablets.
            modifier = Modifier
                .adaptiveContentWidth()
                .navigationBarsPadding()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryColumn(
                label = if (showAmounts) {
                    stringResource(R.string.list_detail_summary_unchecked, summary.unchecked.count)
                } else {
                    stringResource(R.string.list_detail_summary_unchecked_label)
                },
                total = summary.unchecked,
                showAmount = showAmounts,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.fillMaxHeight())
            SummaryColumn(
                label = if (showAmounts) {
                    stringResource(R.string.list_detail_summary_checked, summary.checked.count)
                } else {
                    stringResource(R.string.list_detail_summary_checked_label)
                },
                total = summary.checked,
                showAmount = showAmounts,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.fillMaxHeight())
            SummaryColumn(
                label = if (showAmounts) {
                    stringResource(R.string.list_detail_summary_total, summary.total.count)
                } else {
                    stringResource(R.string.list_detail_summary_total_label)
                },
                total = summary.total,
                showAmount = showAmounts,
                isHighlighted = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    total: ItemsTotal,
    showAmount: Boolean,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        ShrinkToFitText(
            text = if (showAmount) total.amountInCents.toDisplayAmount() else total.count.toString(),
            style = if (isHighlighted) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Single-line text that lowers its font size until it fits, so large amounts
 * (e.g. "R$ 12.345,67") never get cut in the narrow columns of the bar.
 */
@Composable
internal fun ShrinkToFitText(text: String, style: TextStyle, color: Color) {
    var fittedStyle by remember(text, style) { mutableStateOf(style) }
    var isFitted by remember(text, style) { mutableStateOf(false) }
    Text(
        text = text,
        style = fittedStyle,
        color = color,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.drawWithContent { if (isFitted) drawContent() },
        onTextLayout = { result ->
            if (result.didOverflowWidth && fittedStyle.fontSize > MIN_AMOUNT_FONT_SIZE) {
                fittedStyle = fittedStyle.copy(fontSize = fittedStyle.fontSize * 0.9f)
            } else {
                isFitted = true
            }
        },
    )
}

private val MIN_AMOUNT_FONT_SIZE = 10.sp

@Preview(showBackground = true)
@Composable
private fun ListSummaryBarPreview() {
    FinCheckTheme {
        ListSummaryBar(
            summary = ListSummary(
                unchecked = ItemsTotal(count = 3, amountInCents = 25_000),
                checked = ItemsTotal(count = 2, amountInCents = 1_218_990),
                total = ItemsTotal(count = 5, amountInCents = 1_243_990),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ListSummaryBarWithoutAmountsPreview() {
    FinCheckTheme {
        ListSummaryBar(
            summary = ListSummary(
                unchecked = ItemsTotal(count = 3, amountInCents = 25_000),
                checked = ItemsTotal(count = 2, amountInCents = 18_990),
                total = ItemsTotal(count = 5, amountInCents = 43_990),
            ),
            showAmounts = false,
        )
    }
}
