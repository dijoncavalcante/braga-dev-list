package com.bragadev.fincheck.core.domain.model

/** How many items a group has and how much they add up to (unit price × quantity), in cents. */
data class ItemsTotal(
    /** How many items. */
    val count: Int,
    /** Their total (unit price × quantity), in cents. */
    val amountInCents: Long,
)

/** Totals shown at the end of a list: unchecked, checked and everything together. */
data class ListSummary(
    /** Items still unchecked. */
    val unchecked: ItemsTotal,
    /** Items already checked. */
    val checked: ItemsTotal,
    /** Every item. */
    val total: ItemsTotal,
)

/** Totals of these items: unchecked, checked and all of them. */
fun List<ShoppingListItem>.toSummary(): ListSummary {
    val (checkedItems, uncheckedItems) = partition { it.isChecked }
    return ListSummary(
        unchecked = uncheckedItems.toItemsTotal(),
        checked = checkedItems.toItemsTotal(),
        total = toItemsTotal(),
    )
}

/** How many of these items and their total (unit price × quantity). */
private fun List<ShoppingListItem>.toItemsTotal(): ItemsTotal = ItemsTotal(
    count = size,
    amountInCents = sumOf { it.priceInCents * it.quantity },
)
