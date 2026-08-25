package com.mbd.cmsstudent.feature.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.NotificationControllerWorkspace

@Composable
fun NotificationsScreen(
    refreshVersion: Int = 0,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val controller = viewModel.controller

    LaunchedEffect(refreshVersion) {
        if (refreshVersion > 0) controller.refresh()
    }

    NotificationControllerWorkspace(controller)
}
