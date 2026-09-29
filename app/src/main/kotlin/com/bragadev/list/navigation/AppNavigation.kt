package com.bragadev.list.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bragadev.list.features.create.presentation.screen.CreateNewListScreen
import com.bragadev.list.features.home.presentation.HomeScreen
import com.bragadev.list.features.listdetail.presentation.screen.ListDetailScreen

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
                )
            }
        }
    }
}
