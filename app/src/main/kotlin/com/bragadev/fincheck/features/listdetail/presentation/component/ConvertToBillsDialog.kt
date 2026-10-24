package com.bragadev.fincheck.features.listdetail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.domain.model.BillRepetition
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.ui.components.label

/**
 * "Transformar em contas do mês": picks the month the list's items start as bills and whether
 * they repeat every month (the usual case for monthly bills) or stay in that month only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertToBillsDialog(
    itemCount: Int,
    monthOptions: List<YearMonth>,
    onConfirm: (startMonth: YearMonth, repetition: BillRepetition) -> Unit,
    onDismiss: () -> Unit,
) {
    var monthIndex by rememberSaveable { mutableIntStateOf(0) }
    var everyMonth by rememberSaveable { mutableStateOf(true) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val startMonth = monthOptions.getOrNull(monthIndex) ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.convert_to_bills_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(pluralStringResource(R.plurals.convert_to_bills_message, itemCount, itemCount))
                ExposedDropdownMenuBox(expanded = isMenuExpanded, onExpandedChange = { isMenuExpanded = it }) {
                    OutlinedTextField(
                        value = startMonth.label(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.convert_to_bills_start_month)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
                        singleLine = true,
                        modifier =
                            Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = isMenuExpanded, onDismissRequest = { isMenuExpanded = false }) {
                        monthOptions.forEachIndexed { index, month ->
                            DropdownMenuItem(
                                text = { Text(month.label()) },
                                onClick = {
                                    monthIndex = index
                                    isMenuExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                Column(modifier = Modifier.selectableGroup()) {
                    RepetitionOption(stringResource(R.string.bill_repeat_monthly), selected = everyMonth) { everyMonth = true }
                    RepetitionOption(stringResource(R.string.bill_repeat_once), selected = !everyMonth) { everyMonth = false }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(startMonth, if (everyMonth) BillRepetition.Monthly else BillRepetition.Once)
            }) {
                Text(stringResource(R.string.convert_to_bills_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.list_detail_dialog_cancel)) }
        },
    )
}

/** One radio option of "Repete?". */
@Composable
private fun RepetitionOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
    }
}
