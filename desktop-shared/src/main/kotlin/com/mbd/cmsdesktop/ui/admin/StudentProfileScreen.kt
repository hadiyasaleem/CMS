package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.StudentProfileEditController
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.ui.components.StudentProfileWorkspace
import com.mbd.cmscommon.util.Outcome

@Composable
fun StudentProfileScreen(
    sessionId: String,
    rollNumber: String,
    sessionRepository: AcademicSessionRepository,
    fineRepository: FineRepository,
    sessionManager: SessionManager,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber, sessionRepository, fineRepository) {
        StudentProfileEditController(sessionId, rollNumber, sessionRepository, fineRepository, sessionManager.accountKey.orEmpty(), scope)
    }
    val profile by controller.profile.collectAsState()
    val session by controller.session.collectAsState()
    val saveState by controller.saveState.collectAsState()
    val fines by controller.fines.collectAsState()
    val errorMessage by controller.error.collectAsState()

    val loadedProfile = profile ?: StudentProfile(sessionId = sessionId, rollNumber = rollNumber, name = "")

    StudentProfileWorkspace(
        loadedProfile = loadedProfile,
        session = session,
        fines = fines,
        saveOutcome = saveState ?: Outcome.Success(Unit),
        errorMessage = errorMessage,
        onSave = controller::save,
        onIssueFine = controller::issueFine,
        onDeleteFine = { controller.deleteFine(it.id) },
        onClearError = controller::clearError,
    )
}
