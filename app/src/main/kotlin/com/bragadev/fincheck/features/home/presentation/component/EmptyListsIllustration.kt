package com.bragadev.fincheck.features.home.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bragadev.fincheck.ui.components.EmptyStateIllustration
import com.bragadev.fincheck.ui.theme.FinCheckTheme

/** "No lists yet" picture of the home screen: an empty box. */
@Composable
fun EmptyListsIllustration(modifier: Modifier = Modifier) {
    EmptyStateIllustration(icon = Icons.Outlined.Inventory2, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun EmptyListsIllustrationPreview() {
    FinCheckTheme {
        EmptyListsIllustration()
    }
}
