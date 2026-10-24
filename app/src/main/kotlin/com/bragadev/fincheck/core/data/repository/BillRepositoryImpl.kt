package com.bragadev.fincheck.core.data.repository

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.data.mapper.toDomain
import com.bragadev.fincheck.core.data.mapper.toEntity
import com.bragadev.fincheck.core.database.BillDao
import com.bragadev.fincheck.core.database.BillMonthStatusEntity
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillBook
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.repository.BillRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

/** Room implementation of [BillRepository]; new bills get their creation time here. */
class BillRepositoryImpl(
    private val dao: BillDao,
    private val ioDispatcher: CoroutineDispatcher,
    /** Current time in epoch milliseconds, for the creation time of new bills; replaceable in tests. */
    private val clock: () -> Long = System::currentTimeMillis,
) : BillRepository {
    override fun observeBillBook(): Flow<BillBook> =
        combine(dao.observeAll(), dao.observeStatuses()) { bills, statuses ->
            BillBook(bills = bills.map { it.toDomain() }, statuses = statuses.map { it.toDomain() })
        }

    override suspend fun addBill(bill: Bill): AppResult<Unit> =
        runCatchingToResult {
            dao.insert(bill.toNewEntity())
        }

    override suspend fun addBills(bills: List<Bill>): AppResult<Unit> =
        runCatchingToResult {
            dao.insertAll(bills.map { it.toNewEntity() })
        }

    override suspend fun updateBill(bill: Bill): AppResult<Unit> =
        runCatchingToResult {
            dao.update(bill.toEntity())
        }

    override suspend fun updateBillEndingEarlier(bill: Bill): AppResult<Unit> =
        runCatchingToResult {
            dao.updateEndingEarlier(bill.toEntity())
        }

    override suspend fun splitBill(
        original: Bill,
        newBill: Bill,
    ): AppResult<Unit> =
        runCatchingToResult {
            dao.split(original = original.toEntity(), newBill = newBill.toNewEntity())
        }

    override suspend fun replaceBillInMonth(
        originalId: Long,
        month: YearMonth,
        newBill: Bill,
    ): AppResult<Unit> =
        runCatchingToResult {
            dao.replaceInMonth(originalId = originalId, month = month.index, newBill = newBill.toNewEntity())
        }

    override suspend fun deleteBill(billId: Long): AppResult<Unit> =
        runCatchingToResult {
            dao.deleteById(billId)
        }

    override suspend fun setPaid(
        billId: Long,
        month: YearMonth,
        isPaid: Boolean,
    ): AppResult<Unit> =
        runCatchingToResult {
            dao.upsertStatus(BillMonthStatusEntity(billId = billId, month = month.index, isPaid = isPaid, isSkipped = false))
        }

    override suspend fun skipMonth(
        billId: Long,
        month: YearMonth,
    ): AppResult<Unit> =
        runCatchingToResult {
            dao.upsertStatus(BillMonthStatusEntity(billId = billId, month = month.index, isPaid = false, isSkipped = true))
        }

    /**
     * A bill being inserted: Room picks its id. A brand new bill is created now; one that takes over
     * part of another keeps the original's creation time, so it stays in the same place on screen.
     */
    private fun Bill.toNewEntity() = copy(id = 0, createdAt = createdAt.takeIf { it > 0 } ?: clock()).toEntity()

    private suspend fun runCatchingToResult(block: suspend () -> Unit): AppResult<Unit> =
        try {
            withContext(ioDispatcher) { block() }
            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(AppError.Database)
        }
}
