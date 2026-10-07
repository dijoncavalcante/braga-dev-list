package com.bragadev.fincheck.features.create.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.bragadev.fincheck.R
import com.bragadev.fincheck.features.create.presentation.state.CreateListError
import com.bragadev.fincheck.features.create.presentation.state.CreateNewListUiState
import com.bragadev.fincheck.features.create.presentation.viewmodel.CreateNewListViewModel
import com.bragadev.fincheck.ui.theme.BragadevlistTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateNewListScreen(
    onListCreated: (Long) -> Unit,
    onCancel: () -> Unit,
    viewModel: CreateNewListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CreateNewListContent(
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onSaveClick = { viewModel.onSaveClick(onSaved = onListCreated) },
        onCancelClick = onCancel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateNewListContent(
    uiState: CreateNewListUiState,
    onNameChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancelClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Column {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChanged,
                    label = { Text(stringResource(R.string.create_list_name_label)) },
                    placeholder = { Text(stringResource(R.string.create_list_name_placeholder)) },
                    singleLine = true,
                    isError = uiState.error != null,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                uiState.error?.let { error ->
                    Text(text = stringResource(error.toStringRes()))
                }
            }

            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                enabled = !uiState.isSaving,
                onClick = onSaveClick,
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(stringResource(R.string.create_list_save_button))
                }
            }
        }
    }
}

private fun CreateListError.toStringRes(): Int = when (this) {
    CreateListError.EmptyName -> R.string.create_list_error_empty_name
    CreateListError.Generic -> R.string.create_list_generic_error
}

@Preview(showBackground = true)
@Composable
private fun CreateNewListPreview() {
    BragadevlistTheme {
        CreateNewListContent(
            uiState = CreateNewListUiState(),
            onNameChanged = {},
            onSaveClick = {},
            onCancelClick = {},
        )
    }
}
