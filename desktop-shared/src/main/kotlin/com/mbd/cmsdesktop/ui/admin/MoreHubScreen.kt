package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.MoreHubController
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.ui.components.MoreDestination
import com.mbd.cmscommon.ui.components.MoreHubWorkspace

@Composable
fun MoreHubScreen(
    sessionManager: SessionManager,
    administratorRepository: AdministratorRepository,
    notificationRepository: NotificationRepository,
    onOpen: (MoreDestination) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val accountKey = sessionManager.accountKey.orEmpty()
    val controller = remember(accountKey, administratorRepository, notificationRepository) {
        MoreHubController(accountKey, administratorRepository, notificationRepository, scope)
    }
    val snapshot by controller.snapshot.collectAsState()
    val loading by controller.loading.collectAsState()
    val errorMessage by controller.loadError.collectAsState()

    MoreHubWorkspace(
        heroPainter = painterResource("admin-more-hero.jpg"),
        snapshot = snapshot,
        loading = loading,
        errorMessage = errorMessage,
        onRetry = controller::refresh,
        onOpen = onOpen,
    )
}
