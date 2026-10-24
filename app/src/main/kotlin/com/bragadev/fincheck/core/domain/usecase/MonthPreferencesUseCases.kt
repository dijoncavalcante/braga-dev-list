package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetMonthViewModeUseCase(
    private val repository: AppPreferencesRepository,
) {
    operator fun invoke(): Flow<ItemViewMode> = repository.observeMonthViewMode()
}

class SetMonthViewModeUseCase(
    private val repository: AppPreferencesRepository,
) {
    suspend operator fun invoke(viewMode: ItemViewMode) = repository.setMonthViewMode(viewMode)
}

/** First month of the "Meses" tab: the month the user first opened it. */
class GetFirstMonthUseCase(
    private val repository: AppPreferencesRepository,
) {
    suspend operator fun invoke(currentMonth: YearMonth): YearMonth = repository.getOrInitFirstMonth(currentMonth)
}
