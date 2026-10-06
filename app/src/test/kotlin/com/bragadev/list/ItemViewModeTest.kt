package com.bragadev.list

import com.bragadev.list.core.domain.model.ItemViewMode
import kotlin.test.Test
import kotlin.test.assertEquals

class ItemViewModeTest {

    @Test
    fun `codes 0 and 1 keep meaning the old false and true of the group by cycle flag`() {
        assertEquals(ItemViewMode.ALL, ItemViewMode.fromCode(0))
        assertEquals(ItemViewMode.CYCLES, ItemViewMode.fromCode(1))
    }

    @Test
    fun `code 2 is the fortnights view`() {
        assertEquals(ItemViewMode.FORTNIGHTS, ItemViewMode.fromCode(2))
    }

    @Test
    fun `unknown code falls back to all items`() {
        assertEquals(ItemViewMode.ALL, ItemViewMode.fromCode(99))
    }
}
