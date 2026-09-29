package com.bragadev.list

import com.bragadev.list.core.domain.model.ItemSortOrder
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.groupByFortnight
import com.bragadev.list.core.domain.model.sortedForDisplay
import kotlin.test.Test
import kotlin.test.assertEquals

class ItemOrderingTest {

    private val items = listOf(
        item(id = 1, name = "arroz"),
        item(id = 2, name = "Luz", dueDay = 10),
        item(id = 3, name = "Água", dueDay = 10),
        item(id = 4, name = "Açúcar"),
        item(id = 5, name = "Internet", dueDay = 20),
        item(id = 6, name = "Aluguel", dueDay = 5),
    )

    @Test
    fun `order added keeps the order the items were created`() {
        val ids = items.shuffled().sortedForDisplay(ItemSortOrder.ADDED).map { it.id }

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L), ids)
    }

    @Test
    fun `alphabetical order ignores case and accents`() {
        val names = items.shuffled().sortedForDisplay(ItemSortOrder.ALPHABETICAL).map { it.name }

        assertEquals(listOf("Açúcar", "Água", "Aluguel", "arroz", "Internet", "Luz"), names)
    }

    @Test
    fun `due day order puts the closest day first, ties A to Z and no due day at the end`() {
        val names = items.shuffled().sortedForDisplay(ItemSortOrder.DUE_DAY).map { it.name }

        assertEquals(listOf("Aluguel", "Água", "Luz", "Internet", "Açúcar", "arroz"), names)
    }

    @Test
    fun `due day order inside each fortnight`() {
        val groups = items.sortedForDisplay(ItemSortOrder.DUE_DAY).groupByFortnight()

        assertEquals(listOf("Aluguel", "Água", "Luz"), groups.firstFortnight.map { it.name })
        assertEquals(listOf("Internet"), groups.secondFortnight.map { it.name })
    }

    @Test
    fun `unknown stored code falls back to order added`() {
        assertEquals(ItemSortOrder.ADDED, ItemSortOrder.fromCode(99))
        assertEquals(ItemSortOrder.DUE_DAY, ItemSortOrder.fromCode(2))
    }

    private fun item(id: Long, name: String, dueDay: Int? = null) = ShoppingListItem(
        id = id,
        listId = 1,
        name = name,
        quantity = 1,
        dueDay = dueDay,
        isChecked = false,
        createdAt = id,
    )
}
