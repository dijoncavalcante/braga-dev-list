package com.bragadev.list.features.home.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.usecase.MAX_LIST_NAME_LENGTH

/**
 * Asks for the new name of [list]. The name is only saved on "Salvar"; "Cancelar" keeps the old one.
 */
@Composable
fun RenameListDialog(
    list: ShoppingList,
    showError: Boolean,
    onNameChanged: () -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(list.id) { mutableStateOf(list.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_rename_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(MAX_LIST_NAME_LENGTH)
                        onNameChanged()
                    },
                    label = { Text(stringResource(R.string.create_list_name_label)) },
                    isError = showError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showError) {
                    Text(
                        text = stringResource(R.string.create_list_error_empty_name),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text(stringResource(R.string.list_detail_dialog_save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.list_detail_dialog_cancel))
            }
        },
    )
}
