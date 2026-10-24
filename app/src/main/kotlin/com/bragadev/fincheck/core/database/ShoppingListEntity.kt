package com.bragadev.fincheck.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** A list as stored in the `shopping_lists` table, with the preferences of its screen. */
@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    /** Generated id; 0 before the list is saved. */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Name typed by the user, such as "Compras do mês". */
    val name: String,
    /** Creation time in epoch milliseconds; newer lists come first. */
    val createdAt: Long,
    /** "Ordenar por" choice of the list screen menu, as [com.bragadev.fincheck.core.domain.model.ItemSortOrder.code]. */
    @ColumnInfo(defaultValue = "0") val sortOrder: Int = 0,
    /** "Mostrar valor" toggle of the list screen menu. */
    @ColumnInfo(defaultValue = "1") val showPrices: Boolean = true,
    /**
     * "Visualizar" choice of the list screen menu, as [com.bragadev.fincheck.core.domain.model.ItemViewMode.code].
     * The column keeps its
     * original name (and INTEGER type) from when it was a true/false flag, to avoid a table rebuild;
     * the old values 0 and 1 are the codes of "Todos" and "Ciclos financeiros".
     */
    @ColumnInfo(name = "groupByFortnight", defaultValue = "0") val viewMode: Int = 0,
)
