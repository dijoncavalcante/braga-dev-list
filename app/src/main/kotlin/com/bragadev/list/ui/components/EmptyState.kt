package com.bragadev.list.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bragadev.list.ui.theme.BragadevlistTheme

/**
 * Full-screen "nothing here yet" state shared by the app screens: an [illustration] above a
 * centred title and description. The content sits a bit above the middle, leaving room for
 * the screen's floating button.
 */
@Composable
fun EmptyStateContent(
    title: String,
    description: String,
    padding: PaddingValues,
    illustration: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 32.dp)
            // Room for the floating button, so the content looks centred in the free area.
            .padding(bottom = 72.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        illustration()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            // Keeps long lines readable on wide screens / tablets.
            modifier = Modifier.widthIn(max = 320.dp),
        )
    }
}

/**
 * Empty-state picture: [icon] inside soft circles, with a few floating dots. Built from
 * theme colors, so it follows light/dark mode and the app palette without image assets.
 */
@Composable
fun EmptyStateIllustration(icon: ImageVector, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = modifier.size(200.dp), contentAlignment = Alignment.Center) {
        // Outer halo and inner disc.
        Box(
            modifier = Modifier
                .size(184.dp)
                .background(colors.primaryContainer.copy(alpha = 0.35f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(136.dp)
                .background(colors.primaryContainer, CircleShape),
        )
        Icon(
            imageVector = icon,
            contentDescription = null, // decorative: the texts next to it explain the empty state
            tint = colors.onPrimaryContainer,
            modifier = Modifier.size(72.dp),
        )
        // Floating dots around the circle.
        Dot(size = 14.dp, color = colors.tertiary, x = 70.dp, y = (-66).dp)
        Dot(size = 10.dp, color = colors.primary, x = (-78).dp, y = (-34).dp)
        Dot(size = 8.dp, color = colors.secondary, x = (-60).dp, y = 70.dp)
        Dot(size = 12.dp, color = colors.primary.copy(alpha = 0.6f), x = 84.dp, y = 40.dp)
    }
}

@Composable
private fun Dot(size: Dp, color: Color, x: Dp, y: Dp) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(size)
            .background(color, CircleShape),
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateContentPreview() {
    BragadevlistTheme {
        EmptyStateContent(
            title = "Nenhum item nesta lista",
            description = "Toque no botão abaixo para adicionar o primeiro item.",
            padding = PaddingValues(),
        ) {
            EmptyStateIllustration(icon = Icons.AutoMirrored.Outlined.PlaylistAdd)
        }
    }
}
