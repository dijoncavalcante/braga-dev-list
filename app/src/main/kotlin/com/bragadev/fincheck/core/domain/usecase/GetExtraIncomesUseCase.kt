package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.ExtraIncome
import com.bragadev.fincheck.core.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow

/** The user's extra incomes ("Outras entradas"). */
class GetExtraIncomesUseCase(
    private val repository: IncomeRepository,
) {
    /** Emits every extra income again on every change. */
    operator fun invoke(): Flow<List<ExtraIncome>> = repository.observeExtraIncomes()
}
