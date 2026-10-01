package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetAmountsHiddenUseCase(
    private val repository: AppPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeAmountsHidden()
}
