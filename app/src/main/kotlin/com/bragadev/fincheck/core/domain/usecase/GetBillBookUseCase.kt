package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.BillBook
import com.bragadev.fincheck.core.domain.repository.BillRepository
import kotlinx.coroutines.flow.Flow

class GetBillBookUseCase(
    private val repository: BillRepository,
) {
    operator fun invoke(): Flow<BillBook> = repository.observeBillBook()
}
