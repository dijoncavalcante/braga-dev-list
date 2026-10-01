package com.bragadev.list.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    /** "Ordenar por" choice of the list screen menu, as [com.bragadev.list.core.domain.model.ItemSortOrder.code]. */
    @ColumnInfo(defaultValue = "0") val sortOrder: Int = 0,
    /** "Mostrar valor" toggle of the list screen menu. */
    @ColumnInfo(defaultValue = "1") val showPrices: Boolean = true,
    /**
     * "Visualizar: Ciclos financeiros" choice of the list screen menu. The column keeps its
     * original name from when the view was a fixed 1–15 / 16–31 split, to avoid a table rebuild.
     */
    @ColumnInfo(defaultValue = "0") val groupByFortnight: Boolean = false,
)
