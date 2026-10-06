package com.bragadev.list.features.listdetail.presentation.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bragadev.list.R
import com.bragadev.list.core.util.extensions.shareText
import com.bragadev.list.features.home.presentation.component.RenameListDialog
import com.bragadev.list.features.listdetail.presentation.component.DeleteItemsDialog
import com.bragadev.list.features.listdetail.presentation.component.ExtraIncomeBottomSheet
import com.bragadev.list.features.listdetail.presentation.component.IncomeSettingsBottomSheet
import com.bragadev.list.features.listdetail.presentation.state.ListDetailUiState
import com.bragadev.list.features.listdetail.presentation.viewmodel.ListDetailViewModel

/**
 * Dialogs and sheets of a list (rename, delete items, income and extra income) plus the share
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

    if (uiState.isIncomeSettingsVisible) {
        IncomeSettingsBottomSheet(
            initialSettings = uiState.incomeSettings,
            today = uiState.today,
            extraIncomes = uiState.extraIncomes,
            onConfirm = viewModel::onIncomeSettingsConfirm,
            onDismiss = viewModel::onDismissIncomeSettings,
            onAddExtraIncomeClick = viewModel::onAddExtraIncomeClick,
            onExtraIncomeClick = viewModel::onExtraIncomeClick,
        )
    }

    // After the income sheet, so it opens on top of it.
    if (uiState.isExtraIncomeSheetVisible) {
        ExtraIncomeBottomSheet(
            initialIncome = uiState.editingExtraIncome,
            today = uiState.today,
            onConfirm = viewModel::onExtraIncomeConfirm,
            onDismiss = viewModel::onDismissExtraIncome,
            onDelete = viewModel::onDeleteExtraIncomeConfirm,
        )
    }
}
