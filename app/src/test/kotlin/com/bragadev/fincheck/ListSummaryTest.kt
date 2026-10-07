package com.bragadev.fincheck

import com.bragadev.fincheck.core.domain.model.ItemsTotal
import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.core.domain.model.toSummary
import kotlin.test.Test
import kotlin.test.assertEquals

class ListSummaryTest {

    @Test
    fun `totals split unchecked and checked items and multiply price by quantity`() {
        val items = listOf(
            item(id = 1, priceInCents = 18_990, quantity = 1, isChecked = true),
            item(id = 2, priceInCents = 2_500, quantity = 2, isChecked = false),
            item(id = 3, priceInCents = 0, quantity = 3, isChecked = false),
        )

        val summary = items.toSummary()

        assertEquals(ItemsTotal(count = 2, amountInCents = 5_000), summary.unchecked)
        assertEquals(ItemsTotal(count = 1, amountInCents = 18_990), summary.checked)
        assertEquals(ItemsTotal(count = 3, amountInCents = 23_990), summary.total)
    }

    @Test
    fun `empty list has zero everywhere`() {
        val summary = emptyList<ShoppingListItem>().toSummary()

        assertEquals(ItemsTotal(0, 0), summary.unchecked)
        assertEquals(ItemsTotal(0, 0), summary.checked)
        assertEquals(ItemsTotal(0, 0), summary.total)
    }

    private fun item(id: Long, priceInCents: Long, quantity: Int, isChecked: Boolean) = ShoppingListItem(
        id = id,
        listId = 1,
        name = "Item $id",
        quantity = quantity,
        priceInCents = priceInCents,
        isChecked = isChecked,
        createdAt = id,
    )
}
