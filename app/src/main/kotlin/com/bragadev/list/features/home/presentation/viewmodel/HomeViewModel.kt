package com.bragadev.list.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.usecase.DeleteShoppingListUseCase
import com.bragadev.list.core.domain.usecase.DuplicateShoppingListUseCase
import com.bragadev.list.core.domain.usecase.GetListShareTextUseCase
import com.bragadev.list.core.domain.usecase.GetShoppingListsUseCase
import com.bragadev.list.core.domain.usecase.RenameShoppingListUseCase
import com.bragadev.list.features.home.presentation.state.HomeUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getShoppingListsUseCase: GetShoppingListsUseCase,
    private val renameShoppingListUseCase: RenameShoppingListUseCase,
    private val deleteShoppingListUseCase: DeleteShoppingListUseCase,
    private val duplicateShoppingListUseCase: DuplicateShoppingListUseCase,
    private val getListShareTextUseCase: GetListShareTextUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeLists()
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        observeLists()
    }

    // region Options sheet (three dots)

    fun onListMenuClick(list: ShoppingList) {
        _uiState.update { it.copy(menuList = list) }
    }

    fun onDismissListMenu() {
        _uiState.update { it.copy(menuList = null) }
    }

    // endregion

    // region Renomear

    fun onRenameClick() {
        _uiState.update { it.copy(renamingList = it.menuList, menuList = null, showRenameError = false) }
    }

    fun onRenameNameChanged() {
        _uiState.update { it.copy(showRenameError = false) }
    }

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

    fun onDismissRename() {
        _uiState.update { it.copy(renamingList = null, showRenameError = false) }
    }

    // endregion

    // region Compartilhar

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

    fun onCopyClick() {
        val list = _uiState.value.menuList ?: return
        _uiState.update { it.copy(menuList = null) }
        viewModelScope.launch {
            duplicateShoppingListUseCase(list) // the copy shows up at the top of the list by itself
        }
    }

    // endregion

    // region Excluir

    fun onDeleteClick() {
        _uiState.update { it.copy(deletingList = it.menuList, menuList = null) }
    }

    fun onDeleteConfirm() {
        val list = _uiState.value.deletingList ?: return
        viewModelScope.launch {
            when (deleteShoppingListUseCase(list.id)) {
                is AppResult.Success -> _uiState.update { it.copy(deletingList = null) }
                is AppResult.Error -> Unit // confirmation stays open; the list is still there
            }
        }
    }

    fun onDismissDelete() {
        _uiState.update { it.copy(deletingList = null) }
    }

    // endregion

    private fun observeLists() {
        observeJob?.cancel()
        observeJob = getShoppingListsUseCase()
            .onEach { lists -> _uiState.update { it.copy(isLoading = false, lists = lists, error = null) } }
            .catch { _uiState.update { it.copy(isLoading = false, error = "home_lists_load_failed") } }
            .launchIn(viewModelScope)
    }
}
