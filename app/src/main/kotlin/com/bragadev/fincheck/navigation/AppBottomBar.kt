package com.bragadev.fincheck.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bragadev.fincheck.R

/** The two top-level tabs: "Meses" (bills and money of each month) and "Listas" (standalone lists). */
enum class TopLevelTab(
    /** Destination the tab opens. */
    val screen: Screen,
) {
    MONTHS(Screen.Months),
    LISTS(Screen.Home),
}

/** Bottom navigation of the top-level screens; each one puts it in its own Scaffold. */
@Composable
fun AppBottomBar(
    selected: TopLevelTab,
    onTabSelected: (TopLevelTab) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == TopLevelTab.MONTHS,
            onClick = { onTabSelected(TopLevelTab.MONTHS) },
            icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_months)) },
        )
        NavigationBarItem(
            selected = selected == TopLevelTab.LISTS,
            onClick = { onTabSelected(TopLevelTab.LISTS) },
            icon = { Icon(Icons.AutoMirrored.Outlined.ListAlt, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_lists)) },
        )
    }
}
