package com.bragadev.fincheck.core.domain.model

/** An item of a standalone list (e.g. groceries): pure domain model, with no Android or Room types. */
data class ShoppingListItem(
    /** Generated id of the item. */
    val id: Long,
    /** The list the item belongs to. */
    val listId: Long,
    /** Name typed by the user, such as "Arroz". */
    val name: String,
    /** How many units; at least 1. */
    val quantity: Int,
    /** Unit price in cents (R$ 12,50 -> 1250). */
    val priceInCents: Long = 0,
    /**
     * Day of the month the bill is due (1..31); null = no due day. In shorter months
     * a day past the end falls on the last day (see [com.bragadev.fincheck.core.util.extensions.dueDayIn]).
     */
    val dueDay: Int? = null,
    /** Whether the item is checked in the list. */
    val isChecked: Boolean,
    /** Creation time in epoch milliseconds; the default item order. */
    val createdAt: Long,
    /** Free note about the item ("marca X, 2 L"); null = none. At most [MAX_DESCRIPTION_LENGTH] characters. */
    val description: String? = null,
)
