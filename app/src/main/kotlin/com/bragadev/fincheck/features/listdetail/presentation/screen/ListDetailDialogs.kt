package com.bragadev.fincheck.features.listdetail.presentation.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bragadev.fincheck.R
import com.bragadev.fincheck.core.util.extensions.shareText
import com.bragadev.fincheck.features.home.presentation.component.RenameListDialog
import com.bragadev.fincheck.features.listdetail.presentation.component.ConvertToBillsDialog
import com.bragadev.fincheck.features.listdetail.presentation.component.DeleteItemsDialog
import com.bragadev.fincheck.features.listdetail.presentation.state.ListDetailUiState
import com.bragadev.fincheck.features.listdetail.presentation.viewmodel.ListDetailViewModel

/**
 * Dialogs of a list (rename, delete items, turn into bills of the month) plus the share
 * sheet. Both the list screen and its settings screen show them: they share one
 * [ListDetailViewModel], so whichever screen is on top opens what the user asked for.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListDetailDialogs(uiState: ListDetailUiState, viewModel: ListDetailViewModel) {
    // "Compartilhar": hand the list text to the Android share sheet.
    val context = LocalContext.current
    val shareChooserTitle = stringResource(R.string.home_option_share)
    LaunchedEffect(uiState.pendingShareText) {
        val text = uiState.pendingShareText ?: return@LaunchedEffect
        context.shareText(text, shareChooserTitle)
        viewModel.onShareHandled()
    }

    if (uiState.isRenameDialogVisible) {
        uiState.list?.let { list ->
            RenameListDialog(
                list = list,
                showError = uiState.showRenameError,
                onNameChanged = viewModel::onRenameNameChanged,
                onConfirm = viewModel::onRenameConfirm,
                onDismiss = viewModel::onDismissRename,
            )
        }
    }

    if (uiState.isDeleteItemsDialogVisible) {
        DeleteItemsDialog(
            checkedCount = uiState.summary.checked.count,
            totalCount = uiState.summary.total.count,
            onDeleteChecked = { viewModel.onDeleteItemsConfirm(onlyChecked = true) },
            onDeleteAll = { viewModel.onDeleteItemsConfirm(onlyChecked = false) },
            onDismiss = viewModel::onDismissDeleteItems,
        )
    }

    if (uiState.isConvertDialogVisible) {
        ConvertToBillsDialog(
            itemCount = uiState.items.size,
            monthOptions = uiState.convertMonthOptions,
            onConfirm = viewModel::onConvertToBillsConfirm,
            onDismiss = viewModel::onDismissConvertToBills,
        )
    }
}
