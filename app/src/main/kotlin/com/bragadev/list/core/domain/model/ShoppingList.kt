package com.bragadev.list.core.domain.model

/**
 * Pure domain model: no Android framework, no persistence annotations.
 */
data class ShoppingList(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val itemCount: Int = 0,
    /** "Ordenar por": order in which the items are shown and shared. */
    val sortOrder: ItemSortOrder = ItemSortOrder.ADDED,
    /** Prices are shown in the items and in the totals. */
    val showPrices: Boolean = true,
    /** Items are shown by financial cycle (pay day → day before the next pay day) instead of all together. */
    val groupByCycle: Boolean = false,
)
