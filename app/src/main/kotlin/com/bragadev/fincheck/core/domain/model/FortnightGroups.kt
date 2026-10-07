package com.bragadev.fincheck.core.domain.model

/** Last due day of the 1ª quinzena; days after it belong to the 2ª quinzena. */
const val FIRST_FORTNIGHT_LAST_DAY = 15

/**
 * Items of a list split by due day, for the "Visualizar: Quinzenas" view:
 * 1ª quinzena = due days 1–15, 2ª quinzena = 16–31, and items without a due day
 * apart so they never disappear from the screen.
 *
 * Each group keeps the order of the list it came from ("Ordenar por").
 */
data class FortnightGroups(
    val firstFortnight: List<ShoppingListItem>,
    val secondFortnight: List<ShoppingListItem>,
    val withoutDueDay: List<ShoppingListItem>,
)

fun List<ShoppingListItem>.groupByFortnight(): FortnightGroups {
    val (withDueDay, withoutDueDay) = partition { it.dueDay != null }
    val (first, second) = withDueDay.partition { it.dueDay!! <= FIRST_FORTNIGHT_LAST_DAY }
    return FortnightGroups(firstFortnight = first, secondFortnight = second, withoutDueDay = withoutDueDay)
}
