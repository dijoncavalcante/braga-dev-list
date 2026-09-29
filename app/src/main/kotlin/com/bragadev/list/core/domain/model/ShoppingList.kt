package com.bragadev.list.core.domain.model

/**
 * Pure domain model: no Android framework, no persistence annotations.
 */
data class ShoppingList(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val itemCount: Int = 0,
    /** Items are shown A→Z instead of in the order they were added. */
    val sortAlphabetically: Boolean = false,
    /** Prices are shown in the items and in the totals. */
    val showPrices: Boolean = true,
)
