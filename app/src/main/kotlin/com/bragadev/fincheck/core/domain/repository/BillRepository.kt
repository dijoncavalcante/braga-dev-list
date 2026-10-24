package com.bragadev.fincheck.core.domain.repository

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillBook
import com.bragadev.fincheck.core.domain.model.YearMonth
import kotlinx.coroutines.flow.Flow

/** Bills of the "Meses" tab and what each one did in each month. */
interface BillRepository {
    /** Every bill and month status; emits again on every change. */
    fun observeBillBook(): Flow<BillBook>

    /** Saves a new bill. */
    suspend fun addBill(bill: Bill): AppResult<Unit>

    /** Saves several new bills at once. */
    suspend fun addBills(bills: List<Bill>): AppResult<Unit>

    /** Replaces the whole rule of [bill], in every month. */
    suspend fun updateBill(bill: Bill): AppResult<Unit>

    /** [bill] ends earlier than before; statuses of the months it loses are dropped. */
    suspend fun updateBillEndingEarlier(bill: Bill): AppResult<Unit>

    /** "Este e os próximos": [original] keeps the months before [newBill] starts, [newBill] the rest. */
    suspend fun splitBill(
        original: Bill,
        newBill: Bill,
    ): AppResult<Unit>

    /** "Só este mês": [month] of bill [originalId] is replaced by the one-month bill [newBill]. */
    suspend fun replaceBillInMonth(
        originalId: Long,
        month: YearMonth,
        newBill: Bill,
    ): AppResult<Unit>

    /** Deletes bill [billId] from every month, with its statuses. */
    suspend fun deleteBill(billId: Long): AppResult<Unit>

    /** Marks bill [billId] as paid or unpaid in [month]. */
    suspend fun setPaid(
        billId: Long,
        month: YearMonth,
        isPaid: Boolean,
    ): AppResult<Unit>

    /** "Excluir só este mês": the bill no longer shows up in [month]. */
    suspend fun skipMonth(
        billId: Long,
        month: YearMonth,
    ): AppResult<Unit>
}
