package com.mbd.cmscommon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val ModernistColors = lightColorScheme(
    primary = ModAccent,
    onPrimary = ModSurface,
    primaryContainer = ModRedTint,
    onPrimaryContainer = ModAccentDeep,
    inversePrimary = ModAccent,
    secondary = ModInk,
    onSecondary = ModOnInk,
    secondaryContainer = ModSurfaceAlt,
    onSecondaryContainer = ModInk,
    tertiary = ModAccent,
    onTertiary = ModSurface,
    tertiaryContainer = ModRedTint,
    onTertiaryContainer = ModAccentDeep,
    background = ModGround,
    onBackground = ModInk,
    surface = ModGround,
    onSurface = ModInk,
    surfaceVariant = ModTrack,
    onSurfaceVariant = ModMuted,
    surfaceTint = ModAccent,
    inverseSurface = ModInk,
    inverseOnSurface = ModOnInk,
    error = ModAccent,
    onError = ModSurface,
    errorContainer = ModRedTint,
    onErrorContainer = ModAccentDeep,
    outline = ModInk,
    outlineVariant = ModTrack,
    surfaceBright = ModSurface,
    surfaceContainer = ModSurfaceAlt,
    surfaceContainerHigh = ModTrack,
    surfaceContainerHighest = ModTrack,
    surfaceDim = ModSurface,
    surfaceContainerLow = ModSurface,
    surfaceContainerLowest = ModTrack,
)

@Composable
fun CmsTheme(
    app: CmsApp = CmsApp.ADMIN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalCmsColors provides LightCmsColors) {
        MaterialTheme(
            colorScheme = ModernistColors,
            shapes = CmsShapes,
            typography = CmsTypography,
            content = content,
        )
    }
}
