package com.bragadev.list

import com.bragadev.list.core.data.mapper.toDomain
import com.bragadev.list.core.database.ShoppingListEntity
import com.bragadev.list.core.database.ShoppingListItemEntity
import com.bragadev.list.core.database.ShoppingListWithItemCount
import com.bragadev.list.core.domain.model.ItemSortOrder
import kotlin.test.Test
import kotlin.test.assertEquals

class ShoppingListMapperTest {

    @Test
    fun `ShoppingListWithItemCount maps every field into the domain model`() {
        val projection = ShoppingListWithItemCount(
            id = 1,
            name = "Compras",
            createdAt = 1_000,
            sortOrder = 2,
            showPrices = false,
            groupByFortnight = true,
            itemCount = 4,
        )

        val domain = projection.toDomain()

        assertEquals(1, domain.id)
        assertEquals("Compras", domain.name)
        assertEquals(1_000, domain.createdAt)
        assertEquals(4, domain.itemCount)
        assertEquals(ItemSortOrder.DUE_DAY, domain.sortOrder)
        assertEquals(false, domain.showPrices)
        assertEquals(true, domain.groupByCycle)
    }

    @Test
    fun `ShoppingListEntity maps with the given item count defaulting to zero`() {
        val entity = ShoppingListEntity(id = 2, name = "Contas", createdAt = 2_000)

        val domain = entity.toDomain()

        assertEquals(0, domain.itemCount)
        assertEquals("Contas", domain.name)
    }

    @Test
    fun `ShoppingListItemEntity maps every field into the domain model`() {
        val entity = ShoppingListItemEntity(
            id = 5,
            listId = 1,
            name = "Arroz",
            quantity = 2,
            priceInCents = 1_250,
            dueDay = 10,
            isChecked = true,
            createdAt = 3_000,
        )

        val domain = entity.toDomain()

        assertEquals(5, domain.id)
        assertEquals(1, domain.listId)
        assertEquals("Arroz", domain.name)
        assertEquals(2, domain.quantity)
        assertEquals(1_250, domain.priceInCents)
        assertEquals(10, domain.dueDay)
        assertEquals(true, domain.isChecked)
    }
}
