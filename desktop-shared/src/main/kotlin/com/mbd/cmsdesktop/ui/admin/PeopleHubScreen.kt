package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.PeopleHubController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.PeopleDestination
import com.mbd.cmscommon.ui.components.PeopleHubWorkspace

@Composable
fun PeopleHubScreen(
    administratorRepository: AdministratorRepository,
    teacherRepository: TeacherRepository,
    sessionRepository: AcademicSessionRepository,
    linkRequestRepository: StudentLinkRequestRepository,
    markEditRequestRepository: MarkEditRequestRepository,
    examPaperSubmissionRepository: ExamPaperSubmissionRepository,
    onOpen: (PeopleDestination) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(administratorRepository, teacherRepository, sessionRepository, linkRequestRepository, markEditRequestRepository, examPaperSubmissionRepository) {
        PeopleHubController(administratorRepository, teacherRepository, sessionRepository, linkRequestRepository, markEditRequestRepository, examPaperSubmissionRepository, scope)
    }
    val snapshot by controller.snapshot.collectAsState()
    val loading by controller.loading.collectAsState()
    val errorMessage by controller.loadError.collectAsState()

    PeopleHubWorkspace(
        heroPainter = painterResource("admin-people-hero.jpg"),
        snapshot = snapshot,
        loading = loading,
        errorMessage = errorMessage,
        onRetry = controller::refresh,
        onOpen = onOpen,
    )
}
