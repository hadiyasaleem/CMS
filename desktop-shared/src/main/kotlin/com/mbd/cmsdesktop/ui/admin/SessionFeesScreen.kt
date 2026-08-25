package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.SessionFeesController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.ui.components.SessionFeeWorkspace

@Composable
fun SessionFeesScreen(
    sessionId: String,
    feeRepository: SessionFeeRepository,
    sessionRepository: AcademicSessionRepository,
    updatedBy: String?,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, feeRepository, sessionRepository, updatedBy) {
        SessionFeesController(sessionId, feeRepository, sessionRepository, updatedBy.orEmpty(), scope)
    }
    val structure by controller.structure.collectAsState()
    val session by controller.session.collectAsState()
    val loading by controller.loading.collectAsState()
    val saving by controller.saving.collectAsState()
    val saved by controller.saved.collectAsState()
    val errorMessage by controller.error.collectAsState()

    SessionFeeWorkspace(
        sessionId = sessionId,
        session = session,
        structure = structure,
        loading = loading,
        saving = saving,
        saved = saved,
        errorMessage = errorMessage,
        onSave = controller::save,
        onConsumeSaved = controller::consumeSaved,
        onClearError = controller::clearError,
    )
}
