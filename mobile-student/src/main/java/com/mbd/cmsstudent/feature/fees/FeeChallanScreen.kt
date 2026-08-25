package com.mbd.cmsstudent.feature.fees

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentFeeWorkspace

@Composable
fun FeeChallanScreen(viewModel: FeeChallanViewModel = hiltViewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()

    StudentFeeWorkspace(
        snapshot = snapshot,
        loading = snapshot == null,
        errorMessage = null,
        onRetry = viewModel::refresh,
    )
}
