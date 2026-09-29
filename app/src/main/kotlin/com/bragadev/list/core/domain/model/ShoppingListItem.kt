package com.bragadev.list.core.domain.model

data class ShoppingListItem(
    val id: Long,
    val listId: Long,
    val name: String,
    val quantity: Int,
    val isChecked: Boolean,
    val createdAt: Long,
)
