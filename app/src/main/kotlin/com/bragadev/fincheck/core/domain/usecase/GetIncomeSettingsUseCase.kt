package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow

class GetIncomeSettingsUseCase(
    private val repository: IncomeRepository,
) {
    operator fun invoke(): Flow<IncomeSettings?> = repository.observeSettings()
}
