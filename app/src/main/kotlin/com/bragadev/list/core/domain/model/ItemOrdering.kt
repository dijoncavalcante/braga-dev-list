package com.bragadev.list.core.domain.model

import java.text.Collator
import java.util.Locale

/**
 * Order in which the items of a list are shown (and shared): A→Z when the list has
 * "Ordem alfabética" on, otherwise the order they were added.
 *
 * The comparison follows Portuguese rules and ignores case and accents, so
 * "água", "Arroz" and "Açúcar" sort as a user expects.
 */
fun List<ShoppingListItem>.sortedForDisplay(sortAlphabetically: Boolean): List<ShoppingListItem> {
    if (!sortAlphabetically) return sortedBy { it.createdAt }
    val collator = Collator.getInstance(Locale.forLanguageTag("pt-BR")).apply { strength = Collator.PRIMARY }
    return sortedWith(compareBy(collator) { it.name })
}
