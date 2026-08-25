package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.StudentFeeChallanController
import com.mbd.cmscommon.domain.model.studentFeeSnapshot
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.ui.components.StudentFeeWorkspace
import java.time.LocalDate

@Composable
fun StudentFeeChallanScreen(
    sessionId: String,
    rollNumber: String,
    feeRepository: SessionFeeRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId) { StudentFeeChallanController(sessionId, feeRepository, scope) }
    val fee by controller.fee.collectAsState()
    val loading by controller.loading.collectAsState()

    StudentFeeWorkspace(
        snapshot = if (loading && fee == null) null else studentFeeSnapshot(fee, LocalDate.now()),
        loading = loading,
        errorMessage = null,
        onRetry = controller::refresh,
    )
}
