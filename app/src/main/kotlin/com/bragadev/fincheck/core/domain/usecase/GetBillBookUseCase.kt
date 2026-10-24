package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.BillBook
import com.bragadev.fincheck.core.domain.repository.BillRepository
import kotlinx.coroutines.flow.Flow

/** Every bill of the "Meses" tab and what each one did in each month. */
class GetBillBookUseCase(
    private val repository: BillRepository,
) {
    /** Emits the bills and month statuses again on every change. */
    operator fun invoke(): Flow<BillBook> = repository.observeBillBook()
}
