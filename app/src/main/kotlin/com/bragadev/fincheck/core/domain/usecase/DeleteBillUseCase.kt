package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillEditScope
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.repository.BillRepository

/**
 * Deletes a bill while looking at [month]: only that month, or that month and the next ones.
 * The months before always stay, so the history of what was paid is kept.
 */
class DeleteBillUseCase(
    private val repository: BillRepository,
) {
    /** [scope] is ignored for a bill of a single month, which is deleted. */
    suspend operator fun invoke(
        bill: Bill,
        month: YearMonth,
        scope: BillEditScope,
    ): AppResult<Unit> =
        when {
            !bill.isRecurring -> repository.deleteBill(bill.id)
            scope == BillEditScope.ONLY_THIS_MONTH -> repository.skipMonth(bill.id, month)
            month <= bill.startMonth -> repository.deleteBill(bill.id)
            else -> repository.updateBillEndingEarlier(bill.copy(endMonth = month.plusMonths(-1)))
        }
}
