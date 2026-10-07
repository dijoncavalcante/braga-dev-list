package com.bragadev.fincheck.core.domain.model

/** How many items a group has and how much they add up to (unit price × quantity), in cents. */
data class ItemsTotal(
    val count: Int,
    val amountInCents: Long,
)

/** Totals shown at the end of a list: unchecked, checked and everything together. */
data class ListSummary(
    val unchecked: ItemsTotal,
    val checked: ItemsTotal,
    val total: ItemsTotal,
)

fun List<ShoppingListItem>.toSummary(): ListSummary {
    val (checkedItems, uncheckedItems) = partition { it.isChecked }
    return ListSummary(
        unchecked = uncheckedItems.toItemsTotal(),
        checked = checkedItems.toItemsTotal(),
        total = toItemsTotal(),
    )
}

private fun List<ShoppingListItem>.toItemsTotal(): ItemsTotal = ItemsTotal(
    count = size,
    amountInCents = sumOf { it.priceInCents * it.quantity },
)
