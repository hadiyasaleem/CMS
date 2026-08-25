package com.mbd.cmsdesktop.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.controller.InsightsController
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.InsightsRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.ui.components.InsightsViewer
import com.mbd.cmscommon.ui.components.InsightsWorkspace

/**
 * Self-contained Insights screen shared by admin and teacher desktop roles: builds its own
 * [InsightsController] and resolves the session/department lists [InsightsWorkspace] needs for its
 * filters. [assignments] scopes a teacher viewer's insights to their own assigned sessions (see
 * `scopeTeacherInsights`); admins pass nothing and see every session.
 */
@Composable
fun InsightsScreen(
    repository: InsightsRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    viewer: InsightsViewer,
    assignments: List<ResolvedAssignment>? = null,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository) { InsightsController(repository, scope) }

    val overviews by controller.overviews.collectAsState()
    val atRisk by controller.atRisk.collectAsState()
    val examStats by controller.examStats.collectAsState()
    val refreshing by controller.refreshing.collectAsState()

    var sessions by remember { mutableStateOf<List<AcademicSession>>(emptyList()) }
    var departments by remember { mutableStateOf<List<Department>>(emptyList()) }
    LaunchedEffect(sessionRepository) { sessionRepository.observeAllSessions().collect { sessions = it } }
    LaunchedEffect(departmentRepository) { departmentRepository.observeActiveDepartments().collect { departments = it } }

    InsightsWorkspace(
        overviews = overviews.orEmpty(),
        atRisk = atRisk.orEmpty(),
        examStats = examStats.orEmpty(),
        sessions = sessions,
        departments = departments,
        viewer = viewer,
        assignments = assignments.orEmpty(),
        loading = refreshing && overviews == null,
        errorMessage = null,
        onRetry = controller::refresh,
    )
}
