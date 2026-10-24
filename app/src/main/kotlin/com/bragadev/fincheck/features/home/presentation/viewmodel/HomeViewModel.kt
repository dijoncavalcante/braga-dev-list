package com.bragadev.fincheck.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.usecase.DeleteShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.DuplicateShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.GetListShareTextUseCase
import com.bragadev.fincheck.core.domain.usecase.GetShoppingListsUseCase
import com.bragadev.fincheck.core.domain.usecase.RenameShoppingListUseCase
import com.bragadev.fincheck.features.home.presentation.state.HomeUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel of the "Listas" tab: observes the lists and runs the options of each one (rename, share, copy, delete). */
class HomeViewModel(
    private val getShoppingListsUseCase: GetShoppingListsUseCase,
    private val renameShoppingListUseCase: RenameShoppingListUseCase,
    private val deleteShoppingListUseCase: DeleteShoppingListUseCase,
    private val duplicateShoppingListUseCase: DuplicateShoppingListUseCase,
    private val getListShareTextUseCase: GetListShareTextUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    /** State the screen draws. */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Collection of the lists; replaced when the user retries. */
    private var observeJob: Job? = null

    init {
        observeLists()
    }

    /** Reads the lists again after an error. */
    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        observeLists()
    }

    // region Options sheet (three dots)

    /** Opens the options sheet of [list]. */
    fun onListMenuClick(list: ShoppingList) {
        _uiState.update { it.copy(menuList = list) }
    }

    /** Closes the options sheet. */
    fun onDismissListMenu() {
        _uiState.update { it.copy(menuList = null) }
    }

    // endregion

    // region Renomear

    /** "Renomear": opens the rename dialog for the list of the options sheet. */
    fun onRenameClick() {
        _uiState.update { it.copy(renamingList = it.menuList, menuList = null, showRenameError = false) }
    }

    /** The name in the rename dialog changed; hides the error. */
    fun onRenameNameChanged() {
        _uiState.update { it.copy(showRenameError = false) }
    }

    /** Saves [newName]; the dialog closes on success and shows an error for an invalid name. */
    fun onRenameConfirm(newName: String) {
        val list = _uiState.value.renamingList ?: return
        viewModelScope.launch {
            when (val result = renameShoppingListUseCase(list.id, newName)) {
                is AppResult.Success -> _uiState.update { it.copy(renamingList = null) }
                is AppResult.Error -> if (result.error is AppError.Validation) {
                    _uiState.update { it.copy(showRenameError = true) }
                }
            }
        }
    }

    /** Closes the rename dialog without saving. */
    fun onDismissRename() {
        _uiState.update { it.copy(renamingList = null, showRenameError = false) }
    }

    // endregion

    // region Compartilhar

    /** "Compartilhar": builds the text of the list of the options sheet, for the share sheet. */
    fun onShareClick() {
        val list = _uiState.value.menuList ?: return
        _uiState.update { it.copy(menuList = null) }
        viewModelScope.launch {
            val result = getListShareTextUseCase(list)
            if (result is AppResult.Success) {
                _uiState.update { it.copy(pendingShareText = result.data) }
            }
        }
    }

    /** The screen opened the share sheet; clear the one-off event. */
    fun onShareHandled() {
        _uiState.update { it.copy(pendingShareText = null) }
    }

    // endregion

    // region Copiar

    /** "Copiar": copies the list of the options sheet with its items. */
    fun onCopyClick() {
        val list = _uiState.value.menuList ?: return
        _uiState.update { it.copy(menuList = null) }
        viewModelScope.launch {
            duplicateShoppingListUseCase(list) // the copy shows up at the top of the list by itself
        }
    }

    // endregion

    // region Excluir

    /** "Excluir": asks to confirm the deletion of the list of the options sheet. */
    fun onDeleteClick() {
        _uiState.update { it.copy(deletingList = it.menuList, menuList = null) }
    }

    /** Deletes the list waiting for confirmation, with its items. */
    fun onDeleteConfirm() {
        val list = _uiState.value.deletingList ?: return
        viewModelScope.launch {
            when (deleteShoppingListUseCase(list.id)) {
                is AppResult.Success -> _uiState.update { it.copy(deletingList = null) }
                is AppResult.Error -> Unit // confirmation stays open; the list is still there
            }
        }
    }

    /** Closes the delete confirmation without deleting. */
    fun onDismissDelete() {
        _uiState.update { it.copy(deletingList = null) }
    }

    // endregion

    /** Starts (or restarts) collecting the lists into the state. */
    private fun observeLists() {
        observeJob?.cancel()
        observeJob = getShoppingListsUseCase()
            .onEach { lists -> _uiState.update { it.copy(isLoading = false, lists = lists, error = null) } }
            .catch { _uiState.update { it.copy(isLoading = false, error = "home_lists_load_failed") } }
            .launchIn(viewModelScope)
    }
}
