package com.bragadev.list.features.listdetail.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bragadev.list.R

/**
 * "Excluir itens": lets the user remove only the checked items or every item.
 * Nothing is deleted until one of the two options is tapped.
 */
@Composable
fun DeleteItemsDialog(
    checkedCount: Int,
    totalCount: Int,
    onDeleteChecked: () -> Unit,
    onDeleteAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val destructive = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.list_detail_delete_items_title)) },
        text = { Text(stringResource(R.string.list_detail_delete_items_message)) },
        confirmButton = {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(onClick = onDeleteChecked, enabled = checkedCount > 0, colors = destructive) {
                    Text(stringResource(R.string.list_detail_delete_items_checked, checkedCount))
                }
                TextButton(onClick = onDeleteAll, colors = destructive) {
                    Text(stringResource(R.string.list_detail_delete_items_all, totalCount))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.list_detail_dialog_cancel))
                }
            }
        },
    )
}
