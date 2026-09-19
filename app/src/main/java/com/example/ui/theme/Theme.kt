package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VsCodeDarkColorScheme = darkColorScheme(
    primary = VsCodePrimary,
    onPrimary = Color.White,
    secondary = VsCodeCyan,
    onSecondary = Color.Black,
    tertiary = VsCodeOrange,
    background = VsCodeBg,
    onBackground = VsCodeTextPrimary,
    surface = VsCodeSidebar,
    onSurface = VsCodeTextPrimary,
    surfaceVariant = VsCodeTabsBar,
    onSurfaceVariant = VsCodeTextSecondary,
    outline = VsCodeBorder
)

val VsCodeLightColorScheme = lightColorScheme(
    primary = VsCodePrimary,
    onPrimary = Color.White,
    secondary = VsCodeCyan,
    background = VsCodeLightBg,
    onBackground = VsCodeLightText,
    surface = VsCodeLightSidebar,
    onSurface = VsCodeLightText,
    surfaceVariant = VsCodeLightBorder,
    outline = VsCodeLightBorder
)

@Composable
fun CodeForgeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VsCodeDarkColorScheme else VsCodeLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

