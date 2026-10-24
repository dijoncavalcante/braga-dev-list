package com.bragadev.fincheck.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R

/**
 * Open/closed state of the collapsible sections of a screen. Sections of things to do start open
 * and those of things done start closed; [toggled] holds the keys the user flipped from that default.
 */
class SectionExpansion(
    private val toggled: Set<String>,
    private val onToggle: (key: String) -> Unit,
) {
    /** Whether section [key] is open, given whether it starts open ([expandedByDefault]). */
    fun isExpanded(
        key: String,
        expandedByDefault: Boolean,
    ): Boolean = expandedByDefault != (key in toggled)

    /** Opens section [key] if closed, or closes it if open. */
    fun toggle(key: String) = onToggle(key)
}

/** [SectionExpansion] kept across configuration changes: only the sections the user flipped are saved. */
@Composable
fun rememberSectionExpansion(): SectionExpansion {
    var toggledSections by rememberSaveable { mutableStateOf(emptySet<String>()) }
    return SectionExpansion(toggledSections) { key ->
        toggledSections = if (key in toggledSections) toggledSections - key else toggledSections + key
    }
}

/** Tappable header of a collapsible section, with an arrow showing whether it is open. */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 0f else -90f, label = "arrowRotation")
    val stateDescription =
        stringResource(
            if (isExpanded) R.string.list_detail_section_collapse else R.string.list_detail_section_expand,
        )
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClickLabel = stateDescription, onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.rotate(arrowRotation),
        )
    }
}

/** A short line inside a section, e.g. "Nenhuma conta nesta quinzena". */
@Composable
fun SectionMessage(
    @StringRes textRes: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
