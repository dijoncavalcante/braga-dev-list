package com.bragadev.list.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bragadev.list.features.create.presentation.screen.CreateItemScreen
import com.bragadev.list.features.home.presentation.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.CreateItem.route) {
            CreateItemScreen(navController)
        }
//         Define a tela de Profile com um argumento (userId)
//        composable(
//            route = Screen.Profile.route, // Usa a rota base de Screen.Profile
//            arguments = listOf(navArgument("userId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            // Extrai o argumento userId da backStackEntry
//            val userId = backStackEntry.arguments?.getString("userId")
//            ProfileScreen(navController = navController, userId = userId)
//        }
    }
}