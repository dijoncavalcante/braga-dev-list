package com.bragadev.fincheck.core.domain.repository

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.ExtraIncome
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import kotlinx.coroutines.flow.Flow

/** How the user receives their income, including extra incomes; shared by every list. */
interface IncomeRepository {
    /** Null until the user sets up their income. */
    fun observeSettings(): Flow<IncomeSettings?>

    /** Creates or replaces the income settings. */
    suspend fun saveSettings(settings: IncomeSettings): AppResult<Unit>

    /** Every extra income; emits again on every change. */
    fun observeExtraIncomes(): Flow<List<ExtraIncome>>

    /** Adds [income] when its id is 0, otherwise updates it. */
    suspend fun saveExtraIncome(income: ExtraIncome): AppResult<Unit>

    /** Deletes the extra income with [id]. */
    suspend fun deleteExtraIncome(id: Long): AppResult<Unit>
}
