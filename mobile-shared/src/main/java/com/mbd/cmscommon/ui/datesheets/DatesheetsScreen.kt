package com.mbd.cmscommon.ui.datesheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mbd.cmscommon.ui.components.TeacherDatesheetWorkspace

@Composable
fun DatesheetsScreen(
    viewModel: DatesheetsViewModel,
    modifier: Modifier = Modifier,
) {
    val controller = viewModel.controller
    val grids by controller.grids.collectAsState()
    val loading by controller.loading.collectAsState()
    val error by controller.error.collectAsState()

    TeacherDatesheetWorkspace(
        grids = grids,
        identityKey = viewModel.identityKey,
        loading = loading,
        errorMessage = error,
        onRetry = controller::refresh,
        modifier = modifier,
    )
}
