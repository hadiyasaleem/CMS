package com.mbd.cmsdesktopteacher

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.ui.components.BrandedSplashScreen

@Composable
fun TeacherSplashScreen(statusText: String) {
    BrandedSplashScreen(
        background = painterResource("splash_postgraduate_block.jpg"),
        logo = painterResource("splash_app_logo.png"),
        portalLabel = "Faculty Portal",
        statusText = statusText,
    )
}
