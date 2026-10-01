package com.bragadev.list.core.domain.repository

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ExtraIncome
import com.bragadev.list.core.domain.model.IncomeSettings
import kotlinx.coroutines.flow.Flow

/** How the user receives their income, including extra incomes; shared by every list. */
interface IncomeRepository {
    /** Null until the user sets up their income. */
    fun observeSettings(): Flow<IncomeSettings?>

    suspend fun saveSettings(settings: IncomeSettings): AppResult<Unit>

    fun observeExtraIncomes(): Flow<List<ExtraIncome>>

    /** Adds [income] when its id is 0, otherwise updates it. */
    suspend fun saveExtraIncome(income: ExtraIncome): AppResult<Unit>

    suspend fun deleteExtraIncome(id: Long): AppResult<Unit>
}
