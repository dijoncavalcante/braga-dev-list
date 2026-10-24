package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillEditScope
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.repository.BillRepository
import com.bragadev.fincheck.core.util.extensions.MAX_DUE_DAY
import com.bragadev.fincheck.core.util.extensions.MIN_DUE_DAY

/**
 * Adds a bill, or saves the changes made to one while looking at a given month.
 *
 * A change to a recurring bill applies to the months the user chose ([BillEditScope]); the months
 * outside it keep exactly what they had, paid flags included.
 */
class SaveBillUseCase(
    private val repository: BillRepository,
) {
    suspend fun add(bill: Bill): AppResult<Unit> {
        validate(bill)?.let { return it }
        return repository.addBill(bill.normalized())
    }

    /**
     * @param original the bill as saved.
     * @param edited the form's values: same start month as [original], end month from its "Repete?".
     * @param month the month the user is looking at.
     * @param scope ignored for a bill of a single month, which is simply replaced.
     */
    suspend fun edit(
        original: Bill,
        edited: Bill,
        month: YearMonth,
        scope: BillEditScope,
    ): AppResult<Unit> {
        validate(edited)?.let { return it }
        val changed =
            edited.normalized().copy(
                id = original.id,
                startMonth = original.startMonth,
                createdAt = original.createdAt,
            )
        val editedEnd = changed.endMonth
        return when {
            !original.isRecurring -> repository.updateBill(changed)
            scope == BillEditScope.ONLY_THIS_MONTH ->
                repository.replaceBillInMonth(original.id, month, changed.copy(startMonth = month, endMonth = month))
            // From its first month on: the whole bill changes.
            month == original.startMonth ->
                if (changed.endsBefore(original)) {
                    repository.updateBillEndingEarlier(changed)
                } else {
                    repository.updateBill(changed)
                }
            // The new end is behind the month on screen: the bill just ends there, as it was.
            editedEnd != null && editedEnd < month -> repository.updateBillEndingEarlier(original.copy(endMonth = editedEnd))
            else ->
                repository.splitBill(
                    original = original.copy(endMonth = month.plusMonths(-1)),
                    newBill = changed.copy(startMonth = month),
                )
        }
    }

    private fun validate(bill: Bill): AppResult.Error? =
        when {
            bill.name.isBlank() -> AppResult.Error(AppError.Validation(reason = "invalid_bill_name"))
            bill.dueDay != null && bill.dueDay !in MIN_DUE_DAY..MAX_DUE_DAY ->
                AppResult.Error(AppError.Validation(reason = "invalid_due_day"))
            bill.endMonth != null && bill.endMonth < bill.startMonth ->
                AppResult.Error(AppError.Validation(reason = "invalid_bill_end"))
            else -> null
        }

    private fun Bill.normalized() = copy(name = name.trim(), priceInCents = priceInCents.coerceAtLeast(0))

    private fun Bill.endsBefore(other: Bill): Boolean {
        val end = endMonth ?: return false
        return other.endMonth == null || end < other.endMonth
    }
}
