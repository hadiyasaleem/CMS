package com.mbd.cmsdesktopstudent

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.ui.components.BrandedSplashScreen

@Composable
fun StudentSplashScreen(statusText: String) {
    BrandedSplashScreen(
        background = painterResource("splash_postgraduate_block.jpg"),
        logo = painterResource("splash_app_logo.png"),
        portalLabel = "Student Portal",
        statusText = statusText,
    )
}
