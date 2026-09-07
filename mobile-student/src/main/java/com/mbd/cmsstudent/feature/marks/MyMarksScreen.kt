package com.mbd.cmsstudent.feature.marks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentMarksWorkspace

@Composable
fun MyMarksScreen(viewModel: MyMarksViewModel = hiltViewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    StudentMarksWorkspace(
        snapshot = snapshot,
        loading = snapshot == null,
        errorMessage = errorMessage,
        onRetry = viewModel::refresh,
    )
}
