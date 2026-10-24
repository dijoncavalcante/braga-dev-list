package com.bragadev.fincheck.core.domain.repository

import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.YearMonth
import kotlinx.coroutines.flow.Flow

/** User preferences that apply to the whole app, not to a single list. */
interface AppPreferencesRepository {
    /** Amounts hidden by the eye of the toolbar; false until the user hides them. */
    fun observeAmountsHidden(): Flow<Boolean>

    /** Saves whether amounts are hidden. */
    suspend fun setAmountsHidden(hidden: Boolean)

    /** "Visualizar" of the "Meses" tab; "Todos" until the user picks another view. */
    fun observeMonthViewMode(): Flow<ItemViewMode>

    /** Saves the "Visualizar" choice of the "Meses" tab. */
    suspend fun setMonthViewMode(viewMode: ItemViewMode)

    /**
     * First month the user had the "Meses" tab: months before it have nothing to show. Saved as
     * [currentMonth] the first time it is asked for.
     */
    suspend fun getOrInitFirstMonth(currentMonth: YearMonth): YearMonth
}
