package com.mbd.cmscommon.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class CmsApp(val signatureLight: Color, val signatureDark: Color) {
    ADMIN(ModAccent, ModAccent),
    TEACHER(ModAccent, ModAccent),
    STUDENT(ModAccent, ModAccent),
}

data class CmsExtendedColors(
    val accent: Color,
    val ink: Color,
    val onInk: Color,
    val onInkMuted: Color,
    val muted: Color,
    val faint: Color,
    val rule: Color,
    val track: Color,
    val redTint: Color,
    val warn: Color,
    val gold: Color,
    val goldBright: Color,
    val goldFill: Color,
    val eyebrow: Color,
    val success: Color,
    val present: Color,
    val absent: Color,
    val leave: Color,
    val liveDot: Color,
    val navy: Color,
    val onNavyMuted: Color,
    val primaryFixed: Color,
    val signature: Color,
)

val LightCmsColors = CmsExtendedColors(
    accent = ModAccent,
    ink = ModInk,
    onInk = ModOnInk,
    onInkMuted = ModOnInkMuted,
    muted = ModMuted,
    faint = ModFaint,
    rule = ModInk,
    track = ModTrack,
    redTint = ModRedTint,
    warn = ModWarn,
    gold = ModAccent,
    goldBright = ModAccent,
    goldFill = ModRedTint,
    eyebrow = ModAccent,
    success = ModSuccess,
    present = ModSuccess,
    absent = ModAccent,
    leave = ModWarn,
    liveDot = ModAccent,
    navy = ModInk,
    onNavyMuted = ModOnInkMuted,
    primaryFixed = ModTrack,
    signature = ModAccent,
)

val DarkCmsColors = LightCmsColors

val LocalCmsColors: ProvidableCompositionLocal<CmsExtendedColors> = staticCompositionLocalOf { LightCmsColors }

object CmsTheme {
    val colors: CmsExtendedColors
        @Composable get() = LocalCmsColors.current
}
