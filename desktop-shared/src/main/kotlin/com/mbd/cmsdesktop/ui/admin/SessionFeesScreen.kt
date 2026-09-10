package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.controller.SessionFeesController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.ui.components.SessionFeeWorkspace
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import com.mbd.cmsdesktop.util.FeeChallanPdfGenerator

@Composable
fun SessionFeesScreen(
    sessionId: String,
    feeRepository: SessionFeeRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    updatedBy: String?,
    window: ComposeWindow,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, feeRepository, sessionRepository, departmentRepository, updatedBy) {
        SessionFeesController(sessionId, feeRepository, sessionRepository, departmentRepository, updatedBy.orEmpty(), scope)
    }
    val structure by controller.structure.collectAsState()
    val session by controller.session.collectAsState()
    val department by controller.department.collectAsState()
    val loading by controller.loading.collectAsState()
    val saving by controller.saving.collectAsState()
    val saved by controller.saved.collectAsState()
    val errorMessage by controller.error.collectAsState()

    SessionFeeWorkspace(
        sessionId = sessionId,
        session = session,
        department = department,
        structure = structure,
        loading = loading,
        saving = saving,
        saved = saved,
        errorMessage = errorMessage,
        onSave = controller::save,
        onConsumeSaved = controller::consumeSaved,
        onClearError = controller::clearError,
        onDownloadSamplePdf = { header, sampleStructure ->
            val target = AwtDesktopPlatformServices.chooseSaveFile(window, "Save sample fee challan", "${header.challanNumber}.pdf")
            if (target != null) {
                target.writeBytes(FeeChallanPdfGenerator.generate(header, sampleStructure))
                AwtDesktopPlatformServices.open(target)
            }
        },
    )
}
