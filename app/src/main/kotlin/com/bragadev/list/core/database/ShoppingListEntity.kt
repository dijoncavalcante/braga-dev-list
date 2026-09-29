package com.bragadev.list.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    /** "Ordem alfabética" toggle of the list screen menu. */
    @ColumnInfo(defaultValue = "0") val sortAlphabetically: Boolean = false,
    /** "Mostrar valor" toggle of the list screen menu. */
    @ColumnInfo(defaultValue = "1") val showPrices: Boolean = true,
)
