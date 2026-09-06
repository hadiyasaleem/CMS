package com.mbd.cmsstudent.feature.linkrequest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentLinkRequestActions
import com.mbd.cmscommon.ui.components.StudentLinkRequestWorkspace

@Composable
fun LinkRequestScreen(viewModel: LinkRequestViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    StudentLinkRequestWorkspace(
        state = state,
        actions = StudentLinkRequestActions(
            onRefresh = viewModel::refresh,
            onSubmit = viewModel::submit,
        ),
    )
}
