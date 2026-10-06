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
     * "Visualizar" choice of the list screen menu, as [com.bragadev.list.core.domain.model.ItemViewMode.code].
     * The column keeps its
     * original name (and INTEGER type) from when it was a true/false flag, to avoid a table rebuild;
     * the old values 0 and 1 are the codes of "Todos" and "Ciclos financeiros".
     */
    @ColumnInfo(name = "groupByFortnight", defaultValue = "0") val viewMode: Int = 0,
)
