package com.bragadev.list.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bragadev.list.core.domain.usecase.GetShoppingListsUseCase
import com.bragadev.list.features.home.presentation.state.HomeUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val getShoppingListsUseCase: GetShoppingListsUseCase,
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

    private fun observeLists() {
        observeJob?.cancel()
        observeJob = getShoppingListsUseCase()
            .onEach { lists -> _uiState.update { it.copy(isLoading = false, lists = lists, error = null) } }
            .catch { _uiState.update { it.copy(isLoading = false, error = "home_lists_load_failed") } }
            .launchIn(viewModelScope)
    }
}
