package com.bragadev.list.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.features.home.presentation.state.HomeUiState
import com.bragadev.list.features.home.presentation.viewmodel.HomeViewModel
import com.bragadev.list.ui.theme.BragadevlistTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Stateful entry point: obtains the ViewModel from Koin and collects [HomeUiState].
 * All rendering logic lives in the stateless [HomeContent] below (state hoisting),
 * which keeps the UI pure, previewable and easy to test in isolation.
 */
@Composable
fun HomeScreen(
    onCreateListClick: () -> Unit,
    onListClick: (Long) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onCreateListClick = onCreateListClick,
        onListClick = onListClick,
        onRetryClick = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onCreateListClick: () -> Unit,
    onListClick: (Long) -> Unit,
    onRetryClick: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateListClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.home_create_list_button)) },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(padding)
            uiState.error != null -> ErrorState(padding, onRetryClick)
            uiState.isEmpty -> EmptyState(padding)
            else -> ListsState(padding, uiState.lists, onListClick)
        }
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.home_empty_title))
        Text(text = stringResource(R.string.home_empty_description))
    }
}

@Composable
private fun ErrorState(padding: PaddingValues, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.home_error_message))
        Button(onClick = onRetryClick) {
            Text(text = stringResource(R.string.home_retry_button))
        }
    }
}

@Composable
private fun ListsState(
    padding: PaddingValues,
    lists: List<ShoppingList>,
    onListClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(items = lists, key = { it.id }) { list ->
            ShoppingListRow(list = list, onClick = { onListClick(list.id) })
        }
    }
}

@Composable
private fun ShoppingListRow(list: ShoppingList, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = list.name)
            Text(text = "${list.itemCount} itens")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    BragadevlistTheme {
        HomeContent(
            uiState = HomeUiState(isLoading = false, lists = emptyList()),
            onCreateListClick = {},
            onListClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeWithListsPreview() {
    BragadevlistTheme {
        HomeContent(
            uiState = HomeUiState(
                isLoading = false,
                lists = listOf(
                    ShoppingList(id = 1, name = "Compras do mês", createdAt = 0, itemCount = 5),
                    ShoppingList(id = 2, name = "Contas de casa", createdAt = 0, itemCount = 2),
                ),
            ),
            onCreateListClick = {},
            onListClick = {},
            onRetryClick = {},
        )
    }
}
