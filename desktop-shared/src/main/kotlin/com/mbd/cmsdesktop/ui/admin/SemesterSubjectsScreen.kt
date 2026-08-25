package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.SemesterSubjectsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.ui.components.SemesterCurriculumWorkspace

@Composable
fun SemesterSubjectsScreen(
    sessionId: String,
    semester: Int,
    curriculumRepository: CurriculumRepository,
    sessionRepository: AcademicSessionRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, semester, curriculumRepository, sessionRepository) {
        SemesterSubjectsController(sessionId, semester, curriculumRepository, sessionRepository, scope)
    }
    val subjects by controller.subjects.collectAsState()
    val session by controller.session.collectAsState()
    val term by controller.term.collectAsState()
    val loading by controller.loading.collectAsState()
    val errorMessage by controller.error.collectAsState()

    SemesterCurriculumWorkspace(
        sessionId = sessionId,
        session = session,
        semester = semester,
        subjects = subjects,
        term = term,
        loading = loading,
        errorMessage = errorMessage,
        onSaveSubject = controller::saveSubject,
        onRemoveSubject = controller::removeSubject,
        onSaveTerm = controller::saveTerm,
        onClearError = controller::clearError,
    )
}
