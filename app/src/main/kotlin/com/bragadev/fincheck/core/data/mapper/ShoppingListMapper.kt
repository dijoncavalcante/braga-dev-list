package com.bragadev.fincheck.core.data.mapper

import com.bragadev.fincheck.core.database.ShoppingListEntity
import com.bragadev.fincheck.core.database.ShoppingListItemEntity
import com.bragadev.fincheck.core.database.ShoppingListWithItemCount
import com.bragadev.fincheck.core.domain.model.ItemSortOrder
import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.model.ShoppingListItem

/** Converts a list read together with its item count into the domain [ShoppingList]. */
fun ShoppingListWithItemCount.toDomain(): ShoppingList = ShoppingList(
    id = id,
    name = name,
    createdAt = createdAt,
    itemCount = itemCount,
    sortOrder = ItemSortOrder.fromCode(sortOrder),
    showPrices = showPrices,
    viewMode = ItemViewMode.fromCode(viewMode),
)

/** Converts a stored list into the domain [ShoppingList]; [itemCount] is given by the caller when known. */
fun ShoppingListEntity.toDomain(itemCount: Int = 0): ShoppingList = ShoppingList(
    id = id,
    name = name,
    createdAt = createdAt,
    itemCount = itemCount,
    sortOrder = ItemSortOrder.fromCode(sortOrder),
    showPrices = showPrices,
    viewMode = ItemViewMode.fromCode(viewMode),
)

/** Converts a stored item into the domain [ShoppingListItem]. */
fun ShoppingListItemEntity.toDomain(): ShoppingListItem = ShoppingListItem(
    id = id,
    listId = listId,
    name = name,
    quantity = quantity,
    priceInCents = priceInCents,
    dueDay = dueDay,
    isChecked = isChecked,
    createdAt = createdAt,
    description = description,
)
