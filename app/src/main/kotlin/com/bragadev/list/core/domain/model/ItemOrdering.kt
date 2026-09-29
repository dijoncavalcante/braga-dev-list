package com.bragadev.list.core.domain.model

import java.text.Collator
import java.util.Locale

/**
 * Order in which the items of a list are shown (and shared), following the list's
 * "Ordenar por" choice. See [ItemSortOrder] for each order.
 *
 * Names are compared with Portuguese rules, ignoring case and accents, so
 * "água", "Arroz" and "Açúcar" sort as a user expects.
 */
fun List<ShoppingListItem>.sortedForDisplay(sortOrder: ItemSortOrder): List<ShoppingListItem> {
    val byName = compareBy<ShoppingListItem, String>(portugueseCollator()) { it.name }
    return when (sortOrder) {
        ItemSortOrder.ADDED -> sortedBy { it.createdAt }
        ItemSortOrder.ALPHABETICAL -> sortedWith(byName)
        ItemSortOrder.DUE_DAY -> sortedWith(
            compareBy<ShoppingListItem> { it.dueDay == null } // without due day → end
                .thenBy { it.dueDay }
                .then(byName),
        )
    }
}

private fun portugueseCollator(): Collator =
    Collator.getInstance(Locale.forLanguageTag("pt-BR")).apply { strength = Collator.PRIMARY }
