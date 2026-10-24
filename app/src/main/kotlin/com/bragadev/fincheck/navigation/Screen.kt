package com.bragadev.fincheck.navigation

/**
 * Centralized, typed navigation routes. Screens are reached with simple
 * arguments (an id) rather than passing complex objects across destinations.
 */
sealed class Screen(val route: String) {
    /** "Meses": where the app opens. */
    data object Months : Screen(route = "months")

    /** "Listas": the standalone lists. */
    data object Home : Screen(route = "home")

    /** "Nova lista". */
    data object CreateNewList : Screen(route = "create_new_list")

    /** A list with its items; takes the list id. */
    data object ListDetail : Screen(route = "list_detail/{listId}") {
        /** Name of the list id argument. */
        const val ARG_LIST_ID = "listId"

        /** Route that opens list [listId]. */
        fun createRoute(listId: Long) = "list_detail/$listId"
    }

    /** Settings of a list; always opened on top of that list's [ListDetail]. */
    data object ListSettings : Screen(route = "list_detail/{listId}/settings") {
        /** Route that opens the settings of list [listId]. */
        fun createRoute(listId: Long) = "list_detail/$listId/settings"
    }

    /** "Sobre o app". */
    data object About : Screen(route = "about")

    /** "Política de privacidade". */
    data object PrivacyPolicy : Screen(route = "privacy_policy")
}
