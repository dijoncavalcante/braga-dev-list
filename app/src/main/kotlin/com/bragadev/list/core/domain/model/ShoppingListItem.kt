package com.bragadev.list.core.domain.model

data class ShoppingListItem(
    val id: Long,
    val listId: Long,
    val name: String,
    val quantity: Int,
    /** Unit price in cents (R$ 12,50 -> 1250). */
    val priceInCents: Long = 0,
    /** Due date as UTC midnight epoch millis; null = no due date. */
    val dueDateMillis: Long? = null,
    val isChecked: Boolean,
    val createdAt: Long,
)
