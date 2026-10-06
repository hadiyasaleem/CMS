package com.mbd.cmscommon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

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
    // These screens are laid out tightly (fixed-height top bars, icon rows with no overflow
    // handling) around the app's own type scale, not whatever the device's accessibility font-size
    // setting happens to be -- left alone, a large system font scale grows title/label text enough
    // that fixed-size rows run out of room and later elements (e.g. the import/export icons on
    // Session Students) get pushed outside the row's bounds and clipped. Pinning fontScale to 1 here
    // makes every screen render at the same size regardless of that device setting; screen-density
    // scaling (different screen sizes/resolutions) is untouched since only fontScale is overridden.
    val fixedFontScale = LocalDensity.current.let { Density(it.density, fontScale = 1f) }
    CompositionLocalProvider(LocalCmsColors provides LightCmsColors, LocalDensity provides fixedFontScale) {
        MaterialTheme(
            colorScheme = ModernistColors,
            shapes = CmsShapes,
            typography = CmsTypography,
            content = content,
        )
    }
}
