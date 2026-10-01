package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.domain.model.IncomeSettings
import com.bragadev.list.core.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow

class GetIncomeSettingsUseCase(
    private val repository: IncomeRepository,
) {
    operator fun invoke(): Flow<IncomeSettings?> = repository.observeSettings()
}
