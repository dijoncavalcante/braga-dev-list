package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.domain.model.ExtraIncome
import com.bragadev.list.core.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow

class GetExtraIncomesUseCase(
    private val repository: IncomeRepository,
) {
    operator fun invoke(): Flow<List<ExtraIncome>> = repository.observeExtraIncomes()
}
