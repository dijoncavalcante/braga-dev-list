package com.bragadev.fincheck.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Room access to the `extra_incomes` table. */
@Dao
interface ExtraIncomeDao {

    /** Every extra income, in the order they were added. */
    @Query("SELECT * FROM extra_incomes ORDER BY id ASC")
    fun observeAll(): Flow<List<ExtraIncomeEntity>>

    /** Inserts when [income] has id 0, otherwise replaces the saved one. */
    @Upsert
    suspend fun upsert(income: ExtraIncomeEntity)

    /** Deletes the extra income with [id]. */
    @Query("DELETE FROM extra_incomes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
