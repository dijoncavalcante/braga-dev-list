package com.bragadev.fincheck.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("listId")],
)
/** An item as stored in the `shopping_list_items` table; deleted together with its list. */
data class ShoppingListItemEntity(
    /** Generated id; 0 before the item is saved. */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** The list the item belongs to. */
    val listId: Long,
    /** Name typed by the user, such as "Arroz". */
    val name: String,
    /** How many units; at least 1. */
    val quantity: Int,
    /** Unit price in cents (R$ 12,50 -> 1250) to avoid floating point rounding issues. */
    @ColumnInfo(defaultValue = "0") val priceInCents: Long = 0,
    /** Day of the month the bill is due (1..31); null = no due day. */
    val dueDay: Int? = null,
    /** Whether the item is checked in the list. */
    val isChecked: Boolean,
    /** Creation time in epoch milliseconds; the default item order. */
    val createdAt: Long,
    /** Free note about the item; null = none. */
    val description: String? = null,
)
