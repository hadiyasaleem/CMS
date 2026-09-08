package com.mbd.cmsstudent.feature.datesheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentDatesheetWorkspace

@Composable
fun StudentDatesheetsScreen(viewModel: StudentDatesheetsViewModel = hiltViewModel()) {
    val context by viewModel.context.collectAsState()
    val sheet by viewModel.sheet.collectAsState()
    val slots by viewModel.slots.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    StudentDatesheetWorkspace(
        sheet = sheet,
        session = context?.session,
        slots = slots,
        loading = loading,
        errorMessage = error,
        onRetry = viewModel::refresh,
    )
}
