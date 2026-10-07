package com.bragadev.fincheck.features.listdetail.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.util.extensions.toBrlCurrency

/** What is shown in place of an amount while amounts are hidden (the eye of the toolbar). */
const val HIDDEN_AMOUNT = "R$ ••••"

/**
 * Whether the amounts of the screen are hidden. Provided once at the top of the list screen so
 * every card, row and total follows the toolbar eye without passing a flag down each composable.
 */
val LocalAmountsHidden = compositionLocalOf { false }

/**
 * Formats an amount for display: "R$ 12,50", or [HIDDEN_AMOUNT] while amounts are hidden.
 * Forms where the user types amounts keep using [toBrlCurrency], so they always show the real value.
 */
@Composable
@ReadOnlyComposable
fun Long.toDisplayAmount(): String = if (LocalAmountsHidden.current) HIDDEN_AMOUNT else toBrlCurrency()

/** Toolbar eye: open while amounts are visible, crossed out while they are hidden. */
@Composable
fun AmountsVisibilityButton(amountsHidden: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (amountsHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            // Describes what a tap does.
            contentDescription = stringResource(if (amountsHidden) R.string.amounts_show else R.string.amounts_hide),
        )
    }
}
