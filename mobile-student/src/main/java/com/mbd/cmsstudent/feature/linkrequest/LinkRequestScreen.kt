package com.mbd.cmsstudent.feature.linkrequest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentLinkRequestActions
import com.mbd.cmscommon.ui.components.StudentLinkRequestWorkspace
import com.mbd.cmscommon.ui.theme.CmsTheme

@Composable
fun LinkRequestScreen(viewModel: LinkRequestViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CmsTheme.colors.ink)
            .statusBarsPadding(),
    ) {
        StudentLinkRequestWorkspace(
            state = state,
            actions = StudentLinkRequestActions(
                onRefresh = viewModel::refresh,
                onSubmit = viewModel::submit,
            ),
        )
    }
}
