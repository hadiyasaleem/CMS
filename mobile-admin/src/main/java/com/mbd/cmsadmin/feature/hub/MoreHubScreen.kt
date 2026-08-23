package com.mbd.cmsadmin.feature.hub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmsadmin.R
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.MoreHubController
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.ui.components.MoreDestination
import com.mbd.cmscommon.ui.components.MoreHubWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MoreHubViewModel @Inject constructor(
    sessionManager: SessionManager,
    administratorRepository: AdministratorRepository,
    notificationRepository: NotificationRepository,
) : ViewModel() {
    private val controller = MoreHubController(
        accountKey = sessionManager.accountKey.orEmpty(),
        administratorRepository = administratorRepository,
        notificationRepository = notificationRepository,
        scope = viewModelScope,
    )

    val snapshot = controller.snapshot
    val loading = controller.loading
    val error = controller.loadError
    fun refresh() = controller.refresh()
}

@Composable
fun MoreHubScreen(
    onOpen: (MoreDestination) -> Unit,
    viewModel: MoreHubViewModel = hiltViewModel(),
) {
    val snapshot by viewModel.snapshot.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    MoreHubWorkspace(
        heroPainter = painterResource(R.drawable.admin_more_hero),
        snapshot = snapshot,
        loading = loading,
        errorMessage = error,
        onRetry = viewModel::refresh,
        onOpen = onOpen,
    )
}
