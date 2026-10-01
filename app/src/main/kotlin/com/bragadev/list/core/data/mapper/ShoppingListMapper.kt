package com.bragadev.list.core.data.mapper

import com.bragadev.list.core.database.ShoppingListEntity
import com.bragadev.list.core.database.ShoppingListItemEntity
import com.bragadev.list.core.database.ShoppingListWithItemCount
import com.bragadev.list.core.domain.model.ItemSortOrder
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem

fun ShoppingListWithItemCount.toDomain(): ShoppingList = ShoppingList(
    id = id,
    name = name,
    createdAt = createdAt,
    itemCount = itemCount,
    sortOrder = ItemSortOrder.fromCode(sortOrder),
    showPrices = showPrices,
    groupByCycle = groupByFortnight,
)

fun ShoppingListEntity.toDomain(itemCount: Int = 0): ShoppingList = ShoppingList(
    id = id,
    name = name,
    createdAt = createdAt,
    itemCount = itemCount,
    sortOrder = ItemSortOrder.fromCode(sortOrder),
    showPrices = showPrices,
    groupByCycle = groupByFortnight,
)

fun ShoppingListItemEntity.toDomain(): ShoppingListItem = ShoppingListItem(
    id = id,
    listId = listId,
    name = name,
    quantity = quantity,
    priceInCents = priceInCents,
    dueDay = dueDay,
    isChecked = isChecked,
    createdAt = createdAt,
)
