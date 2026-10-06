package com.bragadev.list.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bragadev.list.features.about.presentation.AboutScreen
import com.bragadev.list.features.create.presentation.screen.CreateNewListScreen
import com.bragadev.list.features.home.presentation.HomeScreen
import com.bragadev.list.features.listdetail.presentation.screen.ListDetailScreen
import com.bragadev.list.features.listdetail.presentation.screen.ListSettingsScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onCreateListClick = { navController.navigate(Screen.CreateNewList.route) },
                onListClick = { listId -> navController.navigate(Screen.ListDetail.createRoute(listId)) },
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
            // and its dialogs (rename, delete items, income) work from here too.
            val listEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.ListDetail.route) }
            ListSettingsScreen(
                viewModel = koinViewModel(viewModelStoreOwner = listEntry, parameters = { parametersOf(listId) }),
                onBackClick = { navController.popBackStack() },
                onAboutClick = { navController.navigate(Screen.About.route) },
            )
        }
        composable(Screen.About.route) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
