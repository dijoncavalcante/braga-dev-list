package com.bragadev.list

import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.sortedForDisplay
import kotlin.test.Test
import kotlin.test.assertEquals

class ItemOrderingTest {

    private val items = listOf(
        item(id = 1, name = "arroz"),
        item(id = 2, name = "Água"),
        item(id = 3, name = "Luz"),
        item(id = 4, name = "Açúcar"),
        item(id = 5, name = "banana"),
    )

    @Test
    fun `alphabetical order ignores case and accents`() {
        val names = items.shuffled().sortedForDisplay(sortAlphabetically = true).map { it.name }

        assertEquals(listOf("Açúcar", "Água", "arroz", "banana", "Luz"), names)
    }

    @Test
    fun `without alphabetical order items keep the order they were added`() {
        val ids = items.shuffled().sortedForDisplay(sortAlphabetically = false).map { it.id }

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), ids)
    }

    private fun item(id: Long, name: String) = ShoppingListItem(
        id = id,
        listId = 1,
        name = name,
        quantity = 1,
        isChecked = false,
        createdAt = id,
    )
}
