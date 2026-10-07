package com.bragadev.fincheck.core.domain.model

data class ShoppingListItem(
    val id: Long,
    val listId: Long,
    val name: String,
    val quantity: Int,
    /** Unit price in cents (R$ 12,50 -> 1250). */
    val priceInCents: Long = 0,
    /**
     * Day of the month the bill is due (1..31); null = no due day. In shorter months
     * a day past the end falls on the last day (see [com.bragadev.fincheck.core.util.extensions.dueDayIn]).
     */
    val dueDay: Int? = null,
    val isChecked: Boolean,
    val createdAt: Long,
)
