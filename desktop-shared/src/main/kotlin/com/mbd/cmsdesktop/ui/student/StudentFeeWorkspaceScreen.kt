package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.controller.StudentFeeChallanController
import com.mbd.cmscommon.domain.model.studentFeeSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.ui.components.StudentFeeWorkspace
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import com.mbd.cmsdesktop.util.FeeChallanPdfGenerator
import java.time.LocalDate

@Composable
fun StudentFeeChallanScreen(
    sessionId: String,
    rollNumber: String,
    feeRepository: SessionFeeRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    window: ComposeWindow,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId) {
        StudentFeeChallanController(sessionId, rollNumber, feeRepository, sessionRepository, departmentRepository, scope)
    }
    val fee by controller.fee.collectAsState()
    val header by controller.header.collectAsState()
    val loading by controller.loading.collectAsState()
    val errorMessage by controller.error.collectAsState()

    StudentFeeWorkspace(
        header = header,
        snapshot = if (loading && fee == null) null else studentFeeSnapshot(fee, LocalDate.now()),
        loading = loading,
        errorMessage = errorMessage,
        onRetry = controller::refresh,
        onDownloadPdf = {
            val currentFee = fee
            val currentHeader = header
            if (currentFee != null && currentHeader != null) {
                val target = AwtDesktopPlatformServices.chooseSaveFile(window, "Save fee challan", "${currentHeader.challanNumber}.pdf")
                if (target != null) {
                    target.writeBytes(FeeChallanPdfGenerator.generate(currentHeader, currentFee))
                    AwtDesktopPlatformServices.open(target)
                }
            }
        },
    )
}
