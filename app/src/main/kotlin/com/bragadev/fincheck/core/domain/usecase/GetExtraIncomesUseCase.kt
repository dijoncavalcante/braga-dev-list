package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.ExtraIncome
import com.bragadev.fincheck.core.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow

class GetExtraIncomesUseCase(
    private val repository: IncomeRepository,
) {
    operator fun invoke(): Flow<List<ExtraIncome>> = repository.observeExtraIncomes()
}
