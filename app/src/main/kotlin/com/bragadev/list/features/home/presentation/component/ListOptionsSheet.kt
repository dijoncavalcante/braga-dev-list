package com.bragadev.list.features.home.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bragadev.list.R
import com.bragadev.list.core.domain.model.ShoppingList

/**
 * Bottom sheet opened by the three dots of a list: Renomear, Compartilhar, Copiar and Excluir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOptionsSheet(
    list: ShoppingList,
    onRenameClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.navigationBarsPadding().padding(bottom = 8.dp)) {
            Text(
                text = list.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            HorizontalDivider()
            OptionItem(Icons.Outlined.Edit, stringResource(R.string.home_option_rename), onRenameClick)
            OptionItem(Icons.Outlined.Share, stringResource(R.string.home_option_share), onShareClick)
            OptionItem(Icons.Outlined.ContentCopy, stringResource(R.string.home_option_copy), onCopyClick)
            OptionItem(
                icon = Icons.Outlined.Delete,
                label = stringResource(R.string.home_option_delete),
                onClick = onDeleteClick,
                isDestructive = true,
            )
        }
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    val color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(imageVector = icon, contentDescription = null) },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            headlineColor = color,
            leadingIconColor = color,
        ),
        modifier = Modifier.clickable(onClick = onClick),
    )
}
