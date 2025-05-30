package com.bragadev.list.navigation

// Sealed class/object para definir as rotas como constantes

sealed class Screen(
    val route: String,
) {
    object Home : Screen("home_screen")

    object CreateItem : Screen("create_item_screen")

    object DetailItem : Screen("detail_item/{itemId}") {
        fun createRoute(itemId: Int) = "detail_item/$itemId"
    }
}
