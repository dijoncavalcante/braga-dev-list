package com.bragadev.fincheck.core.data.repository

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.data.mapper.toDomain
import com.bragadev.fincheck.core.data.mapper.toEntity
import com.bragadev.fincheck.core.database.ExtraIncomeDao
import com.bragadev.fincheck.core.database.IncomeSettingsDao
import com.bragadev.fincheck.core.domain.model.ExtraIncome
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.repository.IncomeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class IncomeRepositoryImpl(
    private val dao: IncomeSettingsDao,
    private val extraIncomeDao: ExtraIncomeDao,
    private val ioDispatcher: CoroutineDispatcher,
) : IncomeRepository {

    override fun observeSettings(): Flow<IncomeSettings?> = dao.observe().map { it?.toDomain() }

    override suspend fun saveSettings(settings: IncomeSettings): AppResult<Unit> = runCatchingToResult {
        dao.upsert(settings.toEntity())
    }

    override fun observeExtraIncomes(): Flow<List<ExtraIncome>> =
        extraIncomeDao.observeAll().map { incomes -> incomes.mapNotNull { it.toDomain() } }

    override suspend fun saveExtraIncome(income: ExtraIncome): AppResult<Unit> = runCatchingToResult {
        extraIncomeDao.upsert(income.toEntity())
    }

    override suspend fun deleteExtraIncome(id: Long): AppResult<Unit> = runCatchingToResult {
        extraIncomeDao.deleteById(id)
    }

    private suspend fun runCatchingToResult(block: suspend () -> Unit): AppResult<Unit> = try {
        withContext(ioDispatcher) { block() }
        AppResult.Success(Unit)
    } catch (exception: Exception) {
        AppResult.Error(AppError.Database)
    }
}
