package com.bragadev.fincheck.features.months.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.CycleStatus
import com.bragadev.fincheck.features.listdetail.presentation.component.ShrinkToFitText
import com.bragadev.fincheck.ui.components.toDisplayAmount

/*
 * Pieces shared by the cards at the top of the "Meses" tab (cycles, fortnights and the month), so
 * every view reads the same way: tiles with a balance and a bar, amount rows and one alert.
 */

/** Percent → progress bar fraction. */
private const val PERCENT = 100f

/**
 * One period in a compact tile (a cycle or a fortnight): title, dates, its own balance and a bar
 * of how much of its money is committed. [isHighlighted] marks the period today is in, with
 * [badge] ("atual") next to the title; the open tile gets the primary border.
 */
@Composable
internal fun OverviewTile(
    title: String,
    period: String,
    balanceInCents: Long,
    committedPercent: Int?,
    status: CycleStatus,
    isOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    val arrowRotation by animateFloatAsState(targetValue = if (isOpen) 180f else 0f, label = "tileArrow")
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (badge != null) colors.secondaryContainer else colors.surfaceContainerLow,
        border = if (isOpen) BorderStroke(1.dp, colors.primary) else null,
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
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(arrowRotation),
                )
            }
            Text(
                text = if (badge != null) "$period · $badge" else period,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            ShrinkToFitText(
                text = balanceInCents.toDisplayAmount(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = balanceColor(balanceInCents),
            )
            committedPercent?.let { percent ->
                LinearProgressIndicator(
                    progress = { (percent / PERCENT).coerceIn(0f, 1f) },
                    color = statusColor(status),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                )
            }
        }
    }
}

/** The single alert of a card: one short line on a soft error background. */
@Composable
internal fun AlertBanner(text: String) {
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

/** "Label ........ R$ 0,00"; [isHighlighted] for the line that sums up the others. */
@Composable
internal fun AmountRow(
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

/** "48% das entradas comprometidas · Saudável", in the color of the status. */
@Composable
internal fun CommittedText(percent: Int, status: CycleStatus) {
    Text(
        text = stringResource(R.string.cycle_committed, percent) + " · " + stringResource(statusLabel(status)),
        style = MaterialTheme.typography.bodySmall,
        color = statusColor(status),
    )
}

/** Color of a balance: the error color when money is missing, the primary color otherwise. */
@Composable
internal fun balanceColor(amountInCents: Long): Color =
    if (amountInCents < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

/** "Saudável", "Apertado" or "Despesas acima da renda". */
private fun statusLabel(status: CycleStatus): Int = when (status) {
    CycleStatus.HEALTHY -> R.string.cycle_status_healthy
    CycleStatus.TIGHT -> R.string.cycle_status_tight
    CycleStatus.OVER_BUDGET -> R.string.cycle_status_over_budget
}

/** Color of a status: primary, tertiary or error. */
@Composable
private fun statusColor(status: CycleStatus): Color = when (status) {
    CycleStatus.HEALTHY -> MaterialTheme.colorScheme.primary
    CycleStatus.TIGHT -> MaterialTheme.colorScheme.tertiary
    CycleStatus.OVER_BUDGET -> MaterialTheme.colorScheme.error
}
