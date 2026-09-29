package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import com.bragadev.list.core.util.extensions.toBrlCurrency

/**
 * Builds the plain-text version of a list used by "Compartilhar" (WhatsApp, e-mail...):
 *
 * ```
 * Contas de casa
 *
 * ☐ Luz (1) - R$ 189,90 - vence dia 10
 * ☑ Água (1) - R$ 50,00
 *
 * Total: R$ 239,90
 * ```
 *
 * The total (unit price × quantity) is only added when at least one item has a price.
 */
class GetListShareTextUseCase(
    private val repository: ShoppingListRepository,
) {
    suspend operator fun invoke(list: ShoppingList): AppResult<String> =
        when (val result = repository.getItems(list.id)) {
            is AppResult.Success -> AppResult.Success(buildShareText(list.name, result.data))
            is AppResult.Error -> result
        }

    private fun buildShareText(listName: String, items: List<ShoppingListItem>): String = buildString {
        append(listName)
        if (items.isEmpty()) return@buildString
        appendLine()
        appendLine()
        items.forEach { item -> appendLine(item.toShareLine()) }
        val total = items.sumOf { it.priceInCents * it.quantity }
        if (items.any { it.priceInCents > 0 }) {
            appendLine()
            append("Total: ").append(total.toBrlCurrency())
        }
    }.trimEnd()

    private fun ShoppingListItem.toShareLine(): String = buildString {
        append(if (isChecked) "☑ " else "☐ ")
        append(name).append(" (").append(quantity).append(')')
        if (priceInCents > 0) append(" - ").append(priceInCents.toBrlCurrency())
        dueDay?.let { append(" - vence dia ").append(it) }
    }
}
