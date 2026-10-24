package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

private const val COPY_PREFIX = "Cópia de "

/** Month names in calendar order; "marco" is accepted without the cedilla when reading a name. */
private val MONTH_NAMES = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
)

/** A month name as a whole word ("Contas Outubro", "outubro/2026"), in any case. */
private val MONTH_REGEX = Regex(
    "(?iu)(?<!\\p{L})(${(MONTH_NAMES + "Marco").joinToString("|")})(?!\\p{L})",
)

/** A four-digit year on its own, e.g. the "2026" of "Dezembro 2026". */
private val YEAR_REGEX = Regex("(?<!\\d)\\d{4}(?!\\d)")

/**
 * Makes a copy of a list with all of its items, e.g. to reuse last month's bills.
 *
 * When the name has a month, the copy is named after the next one ("Contas Outubro" →
 * "Contas Novembro"; "Dezembro 2026" → "Janeiro 2027"), so a monthly list needs no renaming.
 * Otherwise it is named "Cópia de <name>". Either way the name is cut to
 * [MAX_LIST_NAME_LENGTH] and every item starts unchecked.
 */
class DuplicateShoppingListUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(list: ShoppingList): AppResult<ShoppingList> {
        val copyName = (nextMonthName(list.name) ?: (COPY_PREFIX + list.name)).take(MAX_LIST_NAME_LENGTH).trim()
        return repository.duplicateList(listId = list.id, newName = copyName)
    }
}

/**
 * [name] with its first month moved to the next month, keeping the case it was written in;
 * from December the year in the name (if any) goes up too. Null when [name] has no month.
 */
internal fun nextMonthName(name: String): String? {
    val match = MONTH_REGEX.find(name) ?: return null
    val written = match.value
    val index = MONTH_NAMES.indexOfFirst { it.equals(written, ignoreCase = true) }
        .takeIf { it >= 0 } ?: MONTH_NAMES.indexOf("Março")
    val next = MONTH_NAMES[(index + 1) % MONTH_NAMES.size]
    val nextWritten = when {
        written.all { it.isUpperCase() } -> next.uppercase()
        written.first().isUpperCase() -> next
        else -> next.lowercase()
    }
    val renamed = name.replaceRange(match.range, nextWritten)
    if (index != MONTH_NAMES.lastIndex) return renamed
    return YEAR_REGEX.find(renamed)?.let { year ->
        renamed.replaceRange(year.range, (year.value.toInt() + 1).toString())
    } ?: renamed
}
