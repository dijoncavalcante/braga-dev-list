package com.bragadev.fincheck.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Room access to the `bills` table and to `bill_month_status`, including the multi-step edits of a recurring bill. */
@Dao
interface BillDao {
    /** Every bill, oldest first; emits again whenever the table changes. */
    @Query("SELECT * FROM bills ORDER BY createdAt ASC, id ASC")
    fun observeAll(): Flow<List<BillEntity>>

    /** Every stored month status; only months the user touched have one. */
    @Query("SELECT * FROM bill_month_status")
    fun observeStatuses(): Flow<List<BillMonthStatusEntity>>

    /** Saves a new bill and returns its generated id. */
    @Insert
    suspend fun insert(bill: BillEntity): Long

    /** Saves several new bills at once (e.g. a list turned into bills). */
    @Insert
    suspend fun insertAll(bills: List<BillEntity>)

    /** Replaces every column of an existing bill. */
    @Update
    suspend fun update(bill: BillEntity)

    /** Its month statuses go with it (foreign key cascade). */
    @Query("DELETE FROM bills WHERE id = :billId")
    suspend fun deleteById(billId: Long)

    /**
     * What bill [billId] did in [month] (a [com.bragadev.fincheck.core.domain.model.YearMonth.index]); null when untouched.
     */
    @Query("SELECT * FROM bill_month_status WHERE billId = :billId AND month = :month")
    suspend fun getStatus(
        billId: Long,
        month: Int,
    ): BillMonthStatusEntity?

    /** Inserts or replaces the status of one bill in one month. */
    @Upsert
    suspend fun upsertStatus(status: BillMonthStatusEntity)

    /** Hands the statuses of [fromBillId] from [fromMonth] on to [toBillId], used when a bill is split. */
    @Query("UPDATE bill_month_status SET billId = :toBillId WHERE billId = :fromBillId AND month >= :fromMonth")
    suspend fun moveStatuses(
        fromBillId: Long,
        toBillId: Long,
        fromMonth: Int,
    )

    /** Deletes the statuses of [billId] after [month], used when a bill now ends earlier. */
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
