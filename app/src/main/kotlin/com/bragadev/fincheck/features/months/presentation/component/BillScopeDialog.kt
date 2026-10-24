package com.bragadev.fincheck.features.months.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.BillEditScope
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.ui.components.label

/**
 * Asked when a bill that shows up in other months is changed or deleted: "Só outubro 2026" or
 * "Outubro 2026 e os próximos". The months before are never touched, so what was paid stays.
 */
@Composable
fun BillScopeDialog(
    billName: String,
    month: YearMonth,
    isDelete: Boolean,
    onScopeSelected: (BillEditScope) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (isDelete) R.string.bill_scope_delete_title else R.string.bill_scope_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.bill_scope_message, billName))
                OutlinedButton(
                    onClick = { onScopeSelected(BillEditScope.ONLY_THIS_MONTH) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.bill_scope_only_this_month, month.label()))
                }
                Button(
                    onClick = { onScopeSelected(BillEditScope.THIS_AND_NEXT) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.bill_scope_this_and_next, month.label()))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.list_detail_dialog_cancel)) }
        },
    )
}
