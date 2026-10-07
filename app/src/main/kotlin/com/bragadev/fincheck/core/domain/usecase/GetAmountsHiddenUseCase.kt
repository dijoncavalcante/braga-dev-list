package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetAmountsHiddenUseCase(
    private val repository: AppPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeAmountsHidden()
}
