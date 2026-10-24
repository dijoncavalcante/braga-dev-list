package com.bragadev.fincheck.core.database

/**
 * Query projection used to bring back each list together with how many items it
 * has, in a single round trip, instead of N+1 queries.
 */
data class ShoppingListWithItemCount(
    /** Id of the list. */
    val id: Long,
    /** Name of the list. */
    val name: String,
    /** Creation time in epoch milliseconds. */
    val createdAt: Long,
    /** "Ordenar por" choice, as an [com.bragadev.fincheck.core.domain.model.ItemSortOrder.code]. */
    val sortOrder: Int,
    /** "Mostrar valor" toggle. */
    val showPrices: Boolean,
    /** "Visualizar" choice, as an [com.bragadev.fincheck.core.domain.model.ItemViewMode.code]. */
    val viewMode: Int,
    /** How many items the list has. */
    val itemCount: Int,
)
