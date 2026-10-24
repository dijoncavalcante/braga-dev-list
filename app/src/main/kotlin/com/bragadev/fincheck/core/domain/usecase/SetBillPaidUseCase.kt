package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.repository.BillRepository

/** Marks a bill as paid (or not) in one month only: the other months keep their own state. */
class SetBillPaidUseCase(
    private val repository: BillRepository,
) {
    /** Marks bill [billId] as paid or unpaid in [month]. */
    suspend operator fun invoke(
        billId: Long,
        month: YearMonth,
        isPaid: Boolean,
    ): AppResult<Unit> = repository.setPaid(billId, month, isPaid)
}
