package com.mbd.cmscommon.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavTab(
    val navLabel: String,
    val icon: ImageVector?,
    val route: String,
)
