package com.mbd.cmscommon.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

val Archivo = FontFamily(
    Font("fonts/archivo_regular.ttf", FontWeight.Normal),
    Font("fonts/archivo_medium.ttf", FontWeight.Medium),
    Font("fonts/archivo_semibold.ttf", FontWeight.SemiBold),
    Font("fonts/archivo_bold.ttf", FontWeight.Bold),
    Font("fonts/archivo_extrabold.ttf", FontWeight.ExtraBold),
)
val Newsreader = Archivo
val PublicSans = Archivo

val CmsTypography = Typography(
    displayLarge = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 38.sp, lineHeight = 42.sp, letterSpacing = (-0.6).sp),
    displayMedium = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    displaySmall = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = (-0.4).sp),
    headlineLarge = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 24.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, lineHeight = 22.sp),
    titleLarge = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp),
    titleMedium = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp),
    titleSmall = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 17.sp),
    bodyLarge = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    bodySmall = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 15.sp),
    labelLarge = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
    labelMedium = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.0.sp),
    labelSmall = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Bold, fontSize = 9.sp, lineHeight = 12.sp, letterSpacing = 1.0.sp),
)

object CmsTextStyles {
    val eyebrow = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 2.0.sp)
    val serifItalic = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp)
}
