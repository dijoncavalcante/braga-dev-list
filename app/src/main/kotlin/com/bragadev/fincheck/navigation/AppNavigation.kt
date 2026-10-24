package com.bragadev.fincheck.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bragadev.fincheck.features.about.presentation.AboutScreen
import com.bragadev.fincheck.features.about.presentation.PrivacyPolicyScreen
import com.bragadev.fincheck.features.create.presentation.screen.CreateNewListScreen
import com.bragadev.fincheck.features.home.presentation.HomeScreen
import com.bragadev.fincheck.features.listdetail.presentation.screen.ListDetailScreen
import com.bragadev.fincheck.features.listdetail.presentation.screen.ListSettingsScreen
import com.bragadev.fincheck.features.months.presentation.screen.MonthScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/** Navigation graph of the app: the "Meses" and "Listas" tabs and every screen reached from them. */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val bottomBar: @Composable (TopLevelTab) -> Unit = { selected ->
        AppBottomBar(selected = selected, onTabSelected = { tab -> navController.navigateToTab(tab) })
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Months.route,
    ) {
        composable(Screen.Months.route) {
            MonthScreen(
                onAboutClick = { navController.navigate(Screen.About.route) },
                onPrivacyPolicyClick = { navController.navigate(Screen.PrivacyPolicy.route) },
                bottomBar = { bottomBar(TopLevelTab.MONTHS) },
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onCreateListClick = { navController.navigate(Screen.CreateNewList.route) },
                onListClick = { listId -> navController.navigate(Screen.ListDetail.createRoute(listId)) },
                bottomBar = { bottomBar(TopLevelTab.LISTS) },
            )
        }
        composable(Screen.CreateNewList.route) {
            CreateNewListScreen(
                onListCreated = { listId ->
                    navController.navigate(Screen.ListDetail.createRoute(listId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.ListDetail.route,
            arguments = listOf(navArgument(Screen.ListDetail.ARG_LIST_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong(Screen.ListDetail.ARG_LIST_ID)
            if (listId != null) {
                ListDetailScreen(
                    listId = listId,
                    onBackClick = { navController.popBackStack() },
                    onSettingsClick = { navController.navigate(Screen.ListSettings.createRoute(listId)) },
                )
            }
        }
        composable(
            route = Screen.ListSettings.route,
            arguments = listOf(navArgument(Screen.ListDetail.ARG_LIST_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong(Screen.ListDetail.ARG_LIST_ID) ?: return@composable
            // Same ViewModel as the list screen below it: changes show on the list right away,
            // and its dialogs (rename, delete items, turn into bills) work from here too.
            val listEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.ListDetail.route) }
            ListSettingsScreen(
                viewModel = koinViewModel(viewModelStoreOwner = listEntry, parameters = { parametersOf(listId) }),
                onBackClick = { navController.popBackStack() },
                onAboutClick = { navController.navigate(Screen.About.route) },
                onPrivacyPolicyClick = { navController.navigate(Screen.PrivacyPolicy.route) },
            )
        }
        composable(Screen.About.route) {
            AboutScreen(
                onBackClick = { navController.popBackStack() },
                onPrivacyPolicyClick = { navController.navigate(Screen.PrivacyPolicy.route) },
            )
        }
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

/**
 * Switches tab like the Material bottom navigation does: one copy of each tab on the back stack,
 * each keeping its own state (scroll, month on screen), and back from "Listas" returns to "Meses".
 */
private fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.screen.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
