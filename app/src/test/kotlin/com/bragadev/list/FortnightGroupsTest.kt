package com.bragadev.list

import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.groupByFortnight
import com.bragadev.list.core.domain.model.toSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FortnightGroupsTest {

    private val items = listOf(
        item(id = 1, name = "Aluguel", dueDay = 5, isChecked = true),
        item(id = 2, name = "Luz", dueDay = 15),
        item(id = 3, name = "Internet", dueDay = 16, isChecked = true),
        item(id = 4, name = "Cartão", dueDay = 31),
        item(id = 5, name = "Pão", dueDay = null),
    )

    @Test
    fun `days 1 to 15 go to the first fortnight and 16 to 31 to the second`() {
        val groups = items.groupByFortnight()

        assertEquals(listOf("Aluguel", "Luz"), groups.firstFortnight.map { it.name })
        assertEquals(listOf("Internet", "Cartão"), groups.secondFortnight.map { it.name })
    }

    @Test
    fun `items without due day are kept apart instead of disappearing`() {
        val groups = items.groupByFortnight()

        assertEquals(listOf("Pão"), groups.withoutDueDay.map { it.name })
        assertEquals(
            items.size,
            groups.firstFortnight.size + groups.secondFortnight.size + groups.withoutDueDay.size,
        )
    }

    @Test
    fun `each group keeps the order it received and the checked state of its items`() {
        val groups = items.reversed().groupByFortnight()

        assertEquals(listOf("Luz", "Aluguel"), groups.firstFortnight.map { it.name })
        assertTrue(groups.firstFortnight.single { it.name == "Aluguel" }.isChecked)
    }

    @Test
    fun `totals of the whole list do not change with the fortnight view`() {
        val summary = items.toSummary()

        assertEquals(2, summary.checked.count)
        assertEquals(3, summary.unchecked.count)
        assertEquals(5, summary.total.count)
    }

    private fun item(id: Long, name: String, dueDay: Int?, isChecked: Boolean = false) = ShoppingListItem(
        id = id,
        listId = 1,
        name = name,
        quantity = 1,
        dueDay = dueDay,
        isChecked = isChecked,
        createdAt = id,
    )
}
