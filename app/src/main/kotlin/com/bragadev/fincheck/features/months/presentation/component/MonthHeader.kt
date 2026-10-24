package com.bragadev.fincheck.features.months.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.features.months.presentation.state.MonthStripEntry
import com.bragadev.fincheck.ui.components.label
import com.bragadev.fincheck.ui.components.monthAbbreviation
import com.bragadev.fincheck.ui.components.toDisplayAmount

/**
 * "‹ Outubro 2026 ›" plus the strip of the current month and the next ones (out, nov, dez, jan...),
 * each with its balance, so the user sees the months ahead at a glance and jumps to any of them.
 */
@Composable
fun MonthHeader(
    selectedMonth: YearMonth,
    currentMonth: YearMonth,
    canGoBack: Boolean,
    strip: List<MonthStripEntry>,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onCurrentMonthClick: () -> Unit,
    onMonthClick: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onPreviousClick, enabled = canGoBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.months_previous),
                )
            }
            Text(
                text = selectedMonth.label(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.months_next),
                )
            }
        }
        if (selectedMonth != currentMonth) {
            Surface(
                onClick = onCurrentMonthClick,
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.Today, contentDescription = null, modifier = Modifier.padding(0.dp))
                    Text(stringResource(R.string.months_back_to_current), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        MonthStrip(strip = strip, selectedMonth = selectedMonth, onMonthClick = onMonthClick)
    }
}

@Composable
private fun MonthStrip(
    strip: List<MonthStripEntry>,
    selectedMonth: YearMonth,
    onMonthClick: (YearMonth) -> Unit,
) {
    val listState = rememberLazyListState()
    // Keeps the month on screen in view when it is changed with the arrows.
    LaunchedEffect(selectedMonth, strip.size) {
        val index = strip.indexOfFirst { it.month == selectedMonth }
        if (index >= 0) listState.animateScrollToItem((index - 1).coerceAtLeast(0))
    }
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = strip, key = { it.month.index }) { entry ->
            MonthChip(entry = entry, isSelected = entry.month == selectedMonth, onClick = { onMonthClick(entry.month) })
        }
    }
}

@Composable
private fun MonthChip(
    entry: MonthStripEntry,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) colors.primaryContainer else colors.surfaceContainerLow,
        border = if (isSelected) BorderStroke(1.dp, colors.primary) else null,
        modifier =
            Modifier
                .widthIn(min = 76.dp)
                .semantics { selected = isSelected },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = entry.month.monthAbbreviation(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = entry.month.year.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
            val amount = entry.balanceInCents ?: entry.billsInCents
            Text(
                text = amount.toDisplayAmount(),
                style = MaterialTheme.typography.labelSmall,
                color =
                    when {
                        entry.balanceInCents == null -> colors.onSurfaceVariant
                        entry.balanceInCents < 0 -> colors.error
                        else -> colors.primary
                    },
            )
        }
    }
}
