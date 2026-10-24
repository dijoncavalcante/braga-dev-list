package com.bragadev.fincheck.features.listdetail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.BillRepetition
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.ItemSortOrder
import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.model.nextMonths
import com.bragadev.fincheck.core.domain.model.sortedForDisplay
import com.bragadev.fincheck.core.domain.usecase.AddListItemUseCase
import com.bragadev.fincheck.core.domain.usecase.ConvertListToBillsUseCase
import com.bragadev.fincheck.core.domain.usecase.DeleteListItemUseCase
import com.bragadev.fincheck.core.domain.usecase.DeleteListItemsUseCase
import com.bragadev.fincheck.core.domain.usecase.GetAmountsHiddenUseCase
import com.bragadev.fincheck.core.domain.usecase.GetListItemsUseCase
import com.bragadev.fincheck.core.domain.usecase.GetListShareTextUseCase
import com.bragadev.fincheck.core.domain.usecase.GetShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.RenameShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.SetAllItemsCheckedUseCase
import com.bragadev.fincheck.core.domain.usecase.SetAmountsHiddenUseCase
import com.bragadev.fincheck.core.domain.usecase.SetItemCheckedUseCase
import com.bragadev.fincheck.core.domain.usecase.SetListPreferencesUseCase
import com.bragadev.fincheck.core.domain.usecase.UpdateListItemUseCase
import com.bragadev.fincheck.features.listdetail.presentation.state.ListDetailUiState
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
    private val updateListItemUseCase: UpdateListItemUseCase,
    private val deleteListItemUseCase: DeleteListItemUseCase,
    private val renameShoppingListUseCase: RenameShoppingListUseCase,
    private val getListShareTextUseCase: GetListShareTextUseCase,
    private val setListPreferencesUseCase: SetListPreferencesUseCase,
    private val setAllItemsCheckedUseCase: SetAllItemsCheckedUseCase,
    private val deleteListItemsUseCase: DeleteListItemsUseCase,
    private val getAmountsHiddenUseCase: GetAmountsHiddenUseCase,
    private val setAmountsHiddenUseCase: SetAmountsHiddenUseCase,
    private val convertListToBillsUseCase: ConvertListToBillsUseCase,
    /** "Today", for the months a list can be turned into bills from; replaceable in tests. */
    private val currentDate: () -> CalendarDate = CalendarDate::today,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListDetailUiState())
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeListDetail()
        // Kept apart from the list data: toggling the eye must not reload the list.
        getAmountsHiddenUseCase()
            .onEach { hidden -> _uiState.update { it.copy(amountsHidden = hidden) } }
            .launchIn(viewModelScope)
    }

    /** Eye of the toolbar: hides or shows the amounts of every list (an app-wide preference). */
    fun onToggleAmountsVisibility() {
        val hidden = !_uiState.value.amountsHidden
        viewModelScope.launch { setAmountsHiddenUseCase(hidden) }
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

    fun onItemClick(item: ShoppingListItem) {
        _uiState.update { it.copy(editingItem = item) }
    }

    /** Cancel: nothing was persisted while editing, so closing the dialog discards the changes. */
    fun onDismissEditItemDialog() {
        _uiState.update { it.copy(editingItem = null) }
    }

    /**
     * Save: only now are the edited values written to the database. The due day is no longer edited
     * in a list (bills live in the "Meses" tab), so an older item keeps the one it had.
     */
    fun onEditItemConfirm(name: String, quantity: Int, priceInCents: Long) {
        val item = _uiState.value.editingItem ?: return
        viewModelScope.launch {
            when (updateListItemUseCase(item.id, name, quantity, priceInCents, item.dueDay)) {
                is AppResult.Success -> _uiState.update { it.copy(editingItem = null) }
                is AppResult.Error -> Unit // dialog stays open with the user's edits
            }
        }
    }

    /** Called after the user confirms the deletion of the item open in the edit dialog. */
    fun onDeleteItemConfirm() {
        val item = _uiState.value.editingItem ?: return
        viewModelScope.launch {
            when (deleteListItemUseCase(item.id)) {
                is AppResult.Success -> _uiState.update { it.copy(editingItem = null) }
                is AppResult.Error -> Unit // dialog stays open; the item is still in the list
            }
        }
    }

    fun onItemCheckedChange(itemId: Long, isChecked: Boolean) {
        viewModelScope.launch {
            setItemCheckedUseCase(itemId, isChecked)
        }
    }

    // region Toolbar menu (three dots)

    /** "Ordenar por": only one order is active at a time. */
    fun onSortOrderSelected(sortOrder: ItemSortOrder) {
        val list = _uiState.value.list ?: return
        if (list.sortOrder == sortOrder) return
        viewModelScope.launch {
            setListPreferencesUseCase.setSortOrder(list.id, sortOrder)
        }
    }

    fun onShowPricesToggle() {
        val list = _uiState.value.list ?: return
        viewModelScope.launch {
            setListPreferencesUseCase.setShowPrices(list.id, !list.showPrices)
        }
    }

    fun onCheckAllClick() {
        viewModelScope.launch { setAllItemsCheckedUseCase(listId, isChecked = true) }
    }

    fun onUncheckAllClick() {
        viewModelScope.launch { setAllItemsCheckedUseCase(listId, isChecked = false) }
    }

    fun onDeleteItemsClick() {
        _uiState.update { it.copy(isDeleteItemsDialogVisible = true) }
    }

    /** [onlyChecked] = "Excluir marcados"; otherwise "Excluir todos". */
    fun onDeleteItemsConfirm(onlyChecked: Boolean) {
        viewModelScope.launch {
            when (deleteListItemsUseCase(listId, onlyChecked)) {
                is AppResult.Success -> _uiState.update { it.copy(isDeleteItemsDialogVisible = false) }
                is AppResult.Error -> Unit // dialog stays open; nothing was deleted
            }
        }
    }

    fun onDismissDeleteItems() {
        _uiState.update { it.copy(isDeleteItemsDialogVisible = false) }
    }

    fun onRenameListClick() {
        _uiState.update { it.copy(isRenameDialogVisible = true, showRenameError = false) }
    }

    fun onRenameNameChanged() {
        _uiState.update { it.copy(showRenameError = false) }
    }

    fun onRenameConfirm(newName: String) {
        viewModelScope.launch {
            when (val result = renameShoppingListUseCase(listId, newName)) {
                is AppResult.Success -> _uiState.update { it.copy(isRenameDialogVisible = false) }
                is AppResult.Error -> if (result.error is AppError.Validation) {
                    _uiState.update { it.copy(showRenameError = true) }
                }
            }
        }
    }

    fun onDismissRename() {
        _uiState.update { it.copy(isRenameDialogVisible = false, showRenameError = false) }
    }

    fun onShareClick() {
        val list = _uiState.value.list ?: return
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

    // region "Transformar em contas do mês"

    /** Months the list can start from: the current one and the next ones. */
    fun onConvertToBillsClick() {
        _uiState.update {
            it.copy(convertMonthOptions = YearMonth.of(currentDate()).nextMonths(), isConvertDialogVisible = true)
        }
    }

    fun onDismissConvertToBills() {
        _uiState.update { it.copy(isConvertDialogVisible = false) }
    }

    fun onConvertToBillsConfirm(startMonth: YearMonth, repetition: BillRepetition) {
        viewModelScope.launch {
            when (val result = convertListToBillsUseCase(listId, startMonth, repetition)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isConvertDialogVisible = false, convertedBillsCount = result.data)
                }
                is AppResult.Error -> Unit // dialog stays open; nothing was created
            }
        }
    }

    /** The screen showed how many bills were created; clear the one-off event. */
    fun onConvertedHandled() {
        _uiState.update { it.copy(convertedBillsCount = null) }
    }

    // endregion

    private fun observeListDetail() {
        observeJob?.cancel()
        observeJob = combine(
            getShoppingListUseCase(listId),
            getListItemsUseCase(listId),
        ) { list, items -> list to items.sortedForDisplay(list?.sortOrder ?: ItemSortOrder.ADDED) }
            .onEach { (list, items) ->
                _uiState.update { it.copy(isLoading = false, list = list, items = items, error = null) }
            }
            .catch { _uiState.update { it.copy(isLoading = false, error = "list_detail_load_failed") } }
            .launchIn(viewModelScope)
    }
}
