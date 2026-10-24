package com.bragadev.fincheck.core.domain.model

/**
 * Pure domain model: no Android framework, no persistence annotations.
 */
data class ShoppingList(
    /** Generated id of the list. */
    val id: Long,
    /** Name typed by the user. */
    val name: String,
    /** Creation time in epoch milliseconds; newer lists come first. */
    val createdAt: Long,
    /** How many items the list has; 0 when not loaded with the list. */
    val itemCount: Int = 0,
    /** "Ordenar por": order in which the items are shown and shared. */
    val sortOrder: ItemSortOrder = ItemSortOrder.ADDED,
    /** Prices are shown in the items and in the totals. */
    val showPrices: Boolean = true,
    /** "Visualizar": all items together, by financial cycle or by fortnight of the due day. */
    val viewMode: ItemViewMode = ItemViewMode.ALL,
)
