package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ExtraIncome
import com.bragadev.list.core.domain.model.ExtraIncomeRecurrence
import com.bragadev.list.core.domain.model.MAX_PAY_DAY
import com.bragadev.list.core.domain.model.MIN_PAY_DAY
import com.bragadev.list.core.domain.repository.IncomeRepository

/**
 * Adds or updates an extra income. Rules: a name (trimmed, up to [MAX_LIST_NAME_LENGTH] chars,
 * like a list name), an amount above zero and, when it repeats every month, a day in 1..31.
 */
class SaveExtraIncomeUseCase(
    private val repository: IncomeRepository,
) {
    suspend operator fun invoke(income: ExtraIncome): AppResult<Unit> {
        val name = income.name.trim()
        val recurrence = income.recurrence
        val reason = when {
            name.isEmpty() || name.length > MAX_LIST_NAME_LENGTH -> "invalid_extra_income_name"
            income.amountInCents <= 0 -> "invalid_extra_income_amount"
            recurrence is ExtraIncomeRecurrence.Monthly && recurrence.dayOfMonth !in MIN_PAY_DAY..MAX_PAY_DAY ->
                "invalid_extra_income_day"
            else -> null
        }
        return if (reason != null) {
            AppResult.Error(AppError.Validation(reason))
        } else {
            repository.saveExtraIncome(income.copy(name = name))
        }
    }
}
