package com.mbd.cmsteacher.feature.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.ui.components.BrandedSplashScreen
import com.mbd.cmsteacher.R

@Composable
fun TeacherSplashScreen(
    statusText: String = "VERIFYING SECURE SESSION",
    modifier: Modifier = Modifier,
) {
    BrandedSplashScreen(
        background = painterResource(R.drawable.splash_postgraduate_block),
        logo = painterResource(R.drawable.splash_app_logo),
        portalLabel = "Faculty Portal",
        statusText = statusText,
        modifier = modifier,
    )
}
