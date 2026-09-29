package com.bragadev.list.features.listdetail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.usecase.AddListItemUseCase
import com.bragadev.list.core.domain.usecase.GetListItemsUseCase
import com.bragadev.list.core.domain.usecase.GetShoppingListUseCase
import com.bragadev.list.core.domain.usecase.SetItemCheckedUseCase
import com.bragadev.list.features.listdetail.presentation.state.ListDetailUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListDetailViewModel(
    private val listId: Long,
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val getListItemsUseCase: GetListItemsUseCase,
    private val addListItemUseCase: AddListItemUseCase,
    private val setItemCheckedUseCase: SetItemCheckedUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListDetailUiState())
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeListDetail()
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        observeListDetail()
    }

    fun onAddItemClick() {
        _uiState.update { it.copy(isAddItemDialogVisible = true) }
    }

    fun onDismissAddItemDialog() {
        _uiState.update { it.copy(isAddItemDialogVisible = false) }
    }

    fun onAddItemConfirm(name: String, quantity: Int, priceInCents: Long) {
        viewModelScope.launch {
            when (addListItemUseCase(listId, name, quantity, priceInCents)) {
                is AppResult.Success -> _uiState.update { it.copy(isAddItemDialogVisible = false) }
                is AppResult.Error -> Unit // dialog stays open; inline field validation is a follow-up iteration
            }
        }
    }

    fun onItemCheckedChange(itemId: Long, isChecked: Boolean) {
        viewModelScope.launch {
            setItemCheckedUseCase(itemId, isChecked)
        }
    }

    private fun observeListDetail() {
        observeJob?.cancel()
        observeJob = combine(
            getShoppingListUseCase(listId),
            getListItemsUseCase(listId),
        ) { list, items -> (list?.name.orEmpty()) to items }
            .onEach { (name, items) ->
                _uiState.update { it.copy(isLoading = false, listName = name, items = items, error = null) }
            }
            .catch { _uiState.update { it.copy(isLoading = false, error = "list_detail_load_failed") } }
            .launchIn(viewModelScope)
    }
}
