package com.bragadev.fincheck

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.data.mapper.toDomain
import com.bragadev.fincheck.core.data.mapper.toEntity
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.IncomeFrequency
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.model.PayDay
import com.bragadev.fincheck.core.domain.repository.IncomeRepository
import com.bragadev.fincheck.core.domain.usecase.SaveIncomeSettingsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class IncomeSettingsTest {

    private val repository: IncomeRepository = mockk {
        coEvery { saveSettings(any()) } returns AppResult.Success(Unit)
    }
    private val saveIncomeSettings = SaveIncomeSettingsUseCase(repository)

    private val twiceAMonth = IncomeSettings(
        frequency = IncomeFrequency.TWICE_A_MONTH,
        payDays = listOf(PayDay(10, 200_000), PayDay(25, 250_000)),
        nextPaymentDate = CalendarDate(2026, 10, 10),
    )

    @Test
    fun `valid settings are saved`() = runTest {
        assertIs<AppResult.Success<Unit>>(saveIncomeSettings(twiceAMonth))
        coVerify(exactly = 1) { repository.saveSettings(twiceAMonth) }
    }

    @Test
    fun `invalid settings are rejected without saving`() = runTest {
        val invalid = mapOf(
            "invalid_pay_day_count" to twiceAMonth.copy(payDays = listOf(PayDay(10, 200_000))),
            "invalid_pay_day" to twiceAMonth.copy(payDays = listOf(PayDay(0, 200_000), PayDay(25, 250_000))),
            "invalid_income_amount" to twiceAMonth.copy(payDays = listOf(PayDay(10, 0), PayDay(25, 250_000))),
            "repeated_pay_day" to twiceAMonth.copy(payDays = listOf(PayDay(10, 200_000), PayDay(10, 250_000))),
        )
        invalid.forEach { (reason, settings) ->
            val error = assertIs<AppResult.Error>(saveIncomeSettings(settings))
            assertEquals(AppError.Validation(reason), error.error)
        }
        coVerify(exactly = 0) { repository.saveSettings(any()) }
    }

    @Test
    fun `settings survive the round trip to the database row`() {
        val monthly = IncomeSettings(IncomeFrequency.MONTHLY, listOf(PayDay(5, 500_000)), CalendarDate(2026, 10, 6))

        assertEquals(twiceAMonth, twiceAMonth.toEntity().toDomain())
        assertEquals(monthly, monthly.toEntity().toDomain())
    }
}
