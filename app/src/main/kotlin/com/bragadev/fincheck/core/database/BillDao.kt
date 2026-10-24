package com.bragadev.fincheck.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY createdAt ASC, id ASC")
    fun observeAll(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bill_month_status")
    fun observeStatuses(): Flow<List<BillMonthStatusEntity>>

    @Insert
    suspend fun insert(bill: BillEntity): Long

    @Insert
    suspend fun insertAll(bills: List<BillEntity>)

    @Update
    suspend fun update(bill: BillEntity)

    /** Its month statuses go with it (foreign key cascade). */
    @Query("DELETE FROM bills WHERE id = :billId")
    suspend fun deleteById(billId: Long)

    @Query("SELECT * FROM bill_month_status WHERE billId = :billId AND month = :month")
    suspend fun getStatus(
        billId: Long,
        month: Int,
    ): BillMonthStatusEntity?

    @Upsert
    suspend fun upsertStatus(status: BillMonthStatusEntity)

    @Query("UPDATE bill_month_status SET billId = :toBillId WHERE billId = :fromBillId AND month >= :fromMonth")
    suspend fun moveStatuses(
        fromBillId: Long,
        toBillId: Long,
        fromMonth: Int,
    )

    @Query("DELETE FROM bill_month_status WHERE billId = :billId AND month > :month")
    suspend fun deleteStatusesAfter(
        billId: Long,
        month: Int,
    )

    /**
     * "Este e os próximos": [original] (already ending the month before [newBill] starts) keeps the
     * months behind, and [newBill] takes over from its start month, with the paid flags of those months.
     */
    @Transaction
    suspend fun split(
        original: BillEntity,
        newBill: BillEntity,
    ) {
        update(original)
        val newId = insert(newBill)
        moveStatuses(fromBillId = original.id, toBillId = newId, fromMonth = newBill.startMonth)
    }

    /**
     * "Só este mês": [month] of bill [originalId] is skipped and [newBill], a one-month bill, shows
     * up in its place, already paid if the original was.
     */
    @Transaction
    suspend fun replaceInMonth(
        originalId: Long,
        month: Int,
        newBill: BillEntity,
    ) {
        val wasPaid = getStatus(originalId, month)?.isPaid == true
        val newId = insert(newBill)
        if (wasPaid) upsertStatus(BillMonthStatusEntity(billId = newId, month = month, isPaid = true, isSkipped = false))
        upsertStatus(BillMonthStatusEntity(billId = originalId, month = month, isPaid = false, isSkipped = true))
    }

    /** [bill] now ends earlier: the statuses of the months it no longer has go away. */
    @Transaction
    suspend fun updateEndingEarlier(bill: BillEntity) {
        update(bill)
        bill.endMonth?.let { deleteStatusesAfter(bill.id, it) }
    }
}
