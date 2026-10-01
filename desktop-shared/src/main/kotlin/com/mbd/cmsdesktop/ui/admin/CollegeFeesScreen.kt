package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.controller.CollegeFeesController
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.ui.components.SessionFeeWorkspace
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import com.mbd.cmsdesktop.util.FeeChallanPdfGenerator

/** The college-wide base fee structure for one shift (Morning and Evening are edited separately). */
@Composable
fun CollegeFeesScreen(
    initialShift: Session,
    feeRepository: SessionFeeRepository,
    updatedBy: String?,
    window: ComposeWindow,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(feeRepository, updatedBy) {
        CollegeFeesController(feeRepository, updatedBy.orEmpty(), scope, initialShift)
    }
    val structure by controller.structure.collectAsState()
    val shift by controller.shift.collectAsState()
    val loading by controller.loading.collectAsState()
    val saving by controller.saving.collectAsState()
    val saved by controller.saved.collectAsState()
    val errorMessage by controller.error.collectAsState()

    SessionFeeWorkspace(
        sessionId = "",
        session = null,
        department = null,
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
        shift = shift,
        shifts = controller.shifts,
        onSelectShift = controller::selectShift,
        collegeBase = true,
    )
}
