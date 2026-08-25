package com.mbd.cmsadmin.feature.linkrequests

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.LinkRequestReviewWorkspace

@Composable
fun LinkRequestsScreen(viewModel: LinkRequestsViewModel = hiltViewModel()) {
    val requests by viewModel.requests.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val verifications by viewModel.verifications.collectAsState()
    val access by viewModel.access.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val busyRequestId by viewModel.busyRequestId.collectAsState()
    val rowErrors by viewModel.rowErrors.collectAsState()
    val notice by viewModel.notice.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    LinkRequestReviewWorkspace(
        requests = requests,
        sessions = sessions,
        departments = departments,
        verifications = verifications,
        access = access,
        loading = loading,
        busyRequestId = busyRequestId,
        rowErrors = rowErrors,
        notice = notice,
        errorMessage = errorMessage,
        onRefresh = viewModel::refresh,
        onApprove = viewModel::approve,
        onReject = viewModel::reject,
        onConsumeNotice = viewModel::consumeNotice,
        onClearError = viewModel::clearError,
    )
}
