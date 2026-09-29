package com.bragadev.list.core.database

/**
 * Query projection used to bring back each list together with how many items it
 * has, in a single round trip, instead of N+1 queries.
 */
data class ShoppingListWithItemCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val sortAlphabetically: Boolean,
    val showPrices: Boolean,
    val itemCount: Int,
)
