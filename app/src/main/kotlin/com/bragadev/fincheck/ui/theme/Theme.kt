package com.bragadev.fincheck.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = Emerald400,
        onPrimary = Emerald950,
        primaryContainer = Emerald800,
        onPrimaryContainer = Emerald100,
        inversePrimary = Emerald700,
        secondary = Slate400,
        onSecondary = Navy900,
        secondaryContainer = Slate700,
        onSecondaryContainer = Slate200,
        tertiary = Cyan300,
        onTertiary = Cyan900,
        tertiaryContainer = Cyan900,
        onTertiaryContainer = Cyan100,
        background = Navy950,
        onBackground = Slate200,
        surface = Navy950,
        onSurface = Slate200,
        surfaceVariant = Slate800,
        onSurfaceVariant = Slate400,
        surfaceTint = Emerald400,
        inverseSurface = Slate200,
        inverseOnSurface = Navy900,
        outline = Slate600,
        outlineVariant = Slate700,
        surfaceDim = Navy950,
        surfaceBright = Slate700,
        surfaceContainerLowest = Color(0xFF070B16),
        surfaceContainerLow = Navy900,
        surfaceContainer = Navy850,
        surfaceContainerHigh = Navy800,
        surfaceContainerHighest = Slate800,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Emerald700,
        onPrimary = Color.White,
        primaryContainer = Emerald200,
        onPrimaryContainer = Emerald950,
        inversePrimary = Emerald400,
        secondary = Slate600,
        onSecondary = Color.White,
        secondaryContainer = Slate200,
        onSecondaryContainer = Navy900,
        tertiary = Cyan700,
        onTertiary = Color.White,
        tertiaryContainer = Cyan100,
        onTertiaryContainer = Cyan900,
        background = Slate50,
        onBackground = Navy900,
        surface = Slate50,
        onSurface = Navy900,
        surfaceVariant = Slate200,
        onSurfaceVariant = Slate600,
        surfaceTint = Emerald700,
        inverseSurface = Navy900,
        inverseOnSurface = Slate100,
        outline = Slate400,
        outlineVariant = Slate300,
        surfaceDim = Slate200,
        surfaceBright = Slate50,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Slate100,
        surfaceContainer = Slate150,
        surfaceContainerHigh = Slate200,
        surfaceContainerHighest = Slate300,
    )

@Composable
fun BragadevlistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default: the FinCheck brand colors (same as the app icon) win over the
    // wallpaper-based dynamic colors of Android 12+, so icon and app look like one product.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
