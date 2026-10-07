package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.model.MAX_PAY_DAY
import com.bragadev.fincheck.core.domain.model.MIN_PAY_DAY
import com.bragadev.fincheck.core.domain.repository.IncomeRepository

/**
 * Saves how the user receives their income. Rules: one pay day per payment of the frequency
 * (1 monthly, 2 twice a month), every day in 1..31, every amount above zero and no repeated day.
 */
class SaveIncomeSettingsUseCase(
    private val repository: IncomeRepository,
) {
    suspend operator fun invoke(settings: IncomeSettings): AppResult<Unit> {
        val reason = when {
            settings.payDays.size != settings.frequency.paymentsPerMonth -> "invalid_pay_day_count"
            settings.payDays.any { it.dayOfMonth !in MIN_PAY_DAY..MAX_PAY_DAY } -> "invalid_pay_day"
            settings.payDays.any { it.amountInCents <= 0 } -> "invalid_income_amount"
            settings.payDays.distinctBy { it.dayOfMonth }.size != settings.payDays.size -> "repeated_pay_day"
            else -> null
        }
        return if (reason != null) {
            AppResult.Error(AppError.Validation(reason))
        } else {
            repository.saveSettings(settings)
        }
    }
}
