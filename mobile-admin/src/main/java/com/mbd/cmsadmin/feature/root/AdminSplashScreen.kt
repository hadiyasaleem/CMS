package com.mbd.cmsadmin.feature.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.mbd.cmsadmin.R
import com.mbd.cmscommon.ui.components.BrandedSplashScreen

@Composable
fun AdminSplashScreen(
    statusText: String = "VERIFYING SECURE SESSION",
    modifier: Modifier = Modifier,
) {
    BrandedSplashScreen(
        background = painterResource(R.drawable.splash_postgraduate_block),
        logo = painterResource(R.drawable.splash_app_logo),
        portalLabel = "Admin Portal",
        statusText = statusText,
        modifier = modifier,
    )
}
