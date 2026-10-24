package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow

/** The "Visualizar" choice of the "Meses" tab. */
class GetMonthViewModeUseCase(
    private val repository: AppPreferencesRepository,
) {
    /** Emits the chosen view; "Todos" until the user picks another. */
    operator fun invoke(): Flow<ItemViewMode> = repository.observeMonthViewMode()
}

/** Saves the "Visualizar" choice of the "Meses" tab. */
class SetMonthViewModeUseCase(
    private val repository: AppPreferencesRepository,
) {
    /** Saves [viewMode]. */
    suspend operator fun invoke(viewMode: ItemViewMode) = repository.setMonthViewMode(viewMode)
}

/** First month of the "Meses" tab: the month the user first opened it. */
class GetFirstMonthUseCase(
    private val repository: AppPreferencesRepository,
) {
    /** The saved first month, or [currentMonth] saved as the first one when there is none yet. */
    suspend operator fun invoke(currentMonth: YearMonth): YearMonth = repository.getOrInitFirstMonth(currentMonth)
}
