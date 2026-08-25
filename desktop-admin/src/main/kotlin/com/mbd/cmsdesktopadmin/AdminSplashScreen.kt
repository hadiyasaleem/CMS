package com.mbd.cmsdesktopadmin

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.ui.components.BrandedSplashScreen

@Composable
fun AdminSplashScreen(statusText: String) {
    BrandedSplashScreen(
        background = painterResource("splash_postgraduate_block.jpg"),
        logo = painterResource("splash_app_logo.png"),
        portalLabel = "Admin Portal",
        statusText = statusText,
    )
}
