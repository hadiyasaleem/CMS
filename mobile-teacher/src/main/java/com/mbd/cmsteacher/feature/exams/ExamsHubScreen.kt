package com.mbd.cmsteacher.feature.exams

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.ExamsHubController
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.ExamsDestination
import com.mbd.cmscommon.ui.components.ExamsHubWorkspace
import com.mbd.cmsteacher.R
import com.mbd.cmsteacher.navigation.TeacherDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExamsHubViewModel @Inject constructor(
    sessionManager: SessionManager,
    assignmentsProvider: TeacherAssignmentsProvider,
    examPaperRepository: ExamPaperSubmissionRepository,
    datesheetRepository: DatesheetRepository,
) : ViewModel() {
    val controller = ExamsHubController(
        teacherId = sessionManager.accountKey.orEmpty(),
        assignmentsProvider = assignmentsProvider,
        examPaperRepository = examPaperRepository,
        datesheetRepository = datesheetRepository,
        scope = viewModelScope,
    )
}

@Composable
fun ExamsHubScreen(onOpen: (String) -> Unit, viewModel: ExamsHubViewModel = hiltViewModel()) {
    val controller = viewModel.controller
    val snapshot by controller.snapshot.collectAsState()
    val loading by controller.loading.collectAsState()
    val loadError by controller.loadError.collectAsState()

    ExamsHubWorkspace(
        heroPainter = painterResource(R.drawable.teacher_exams_hero),
        snapshot = snapshot,
        loading = loading,
        errorMessage = loadError,
        onRetry = controller::refresh,
        onOpen = { destination ->
            onOpen(
                when (destination) {
                    ExamsDestination.MARKS -> TeacherDestination.Marks.route
                    ExamsDestination.EXAM_PAPER -> TeacherDestination.ExamPaper.route
                    ExamsDestination.RESULTS -> TeacherDestination.SemesterResults.route
                    ExamsDestination.DATESHEETS -> TeacherDestination.Datesheets.route
                },
            )
        },
    )
}
