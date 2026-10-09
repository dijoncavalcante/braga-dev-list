package com.bragadev.fincheck.navigation

/**
 * Centralized, typed navigation routes. Screens are reached with simple
 * arguments (an id) rather than passing complex objects across destinations.
 */
sealed class Screen(val route: String) {
    data object Home : Screen(route = "home")

    data object CreateNewList : Screen(route = "create_new_list")

    data object ListDetail : Screen(route = "list_detail/{listId}") {
        const val ARG_LIST_ID = "listId"

        fun createRoute(listId: Long) = "list_detail/$listId"
    }

    /** Settings of a list; always opened on top of that list's [ListDetail]. */
    data object ListSettings : Screen(route = "list_detail/{listId}/settings") {
        fun createRoute(listId: Long) = "list_detail/$listId/settings"
    }

    data object About : Screen(route = "about")

    data object PrivacyPolicy : Screen(route = "privacy_policy")
}
