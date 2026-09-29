package com.bragadev.list.core.domain.model

/**
 * Pure domain model: no Android framework, no persistence annotations.
 */
data class ShoppingList(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val itemCount: Int = 0,
)
