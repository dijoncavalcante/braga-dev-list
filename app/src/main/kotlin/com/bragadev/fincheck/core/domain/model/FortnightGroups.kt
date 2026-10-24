package com.bragadev.fincheck.core.domain.model

/** Last due day of the 1ª quinzena; days after it belong to the 2ª quinzena. */
const val FIRST_FORTNIGHT_LAST_DAY = 15

/**
 * Bills of a month split by due day, for the "Visualizar: Quinzenas" view:
 * 1ª quinzena = due days 1–15, 2ª quinzena = 16–31, and bills without a due day
 * apart so they never disappear from the screen.
 *
 * Each group keeps the order of the list it came from.
 */
data class FortnightGroups<T>(
    val firstFortnight: List<T>,
    val secondFortnight: List<T>,
    val withoutDueDay: List<T>,
)

/** Splits these entries by the due day [dueDay] reads from each one (null = no due day). */
fun <T> List<T>.groupByFortnight(dueDay: (T) -> Int?): FortnightGroups<T> {
    val (withDueDay, withoutDueDay) = partition { dueDay(it) != null }
    val (first, second) = withDueDay.partition { dueDay(it)!! <= FIRST_FORTNIGHT_LAST_DAY }
    return FortnightGroups(firstFortnight = first, secondFortnight = second, withoutDueDay = withoutDueDay)
}
