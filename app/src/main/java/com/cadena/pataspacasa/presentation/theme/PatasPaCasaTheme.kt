package com.cadena.pataspacasa.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.TextOnDark,
    primaryContainer = AppColors.PrimaryLight,
    onPrimaryContainer = AppColors.TextOnDark,
    secondary = AppColors.Mustard,
    onSecondary = AppColors.TextOnDark,
    tertiary = AppColors.Coral,
    onTertiary = AppColors.TextOnDark,
    background = AppColors.BackgroundPrimary,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.BackgroundSurface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.BackgroundSecondary,
    onSurfaceVariant = AppColors.TextSecondary,
    error = AppColors.Error,
    onError = AppColors.TextOnDark,
    outline = AppColors.Border
)

@Composable
fun PatasPaCasaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
