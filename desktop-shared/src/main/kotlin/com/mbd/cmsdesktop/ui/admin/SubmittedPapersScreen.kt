package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.SubmittedPapersController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.SubmittedPapersWorkspace
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import java.io.File

@Composable
fun SubmittedPapersScreen(
    examPaperRepository: ExamPaperSubmissionRepository,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(examPaperRepository, teacherRepository, departmentRepository, sessionRepository) {
        SubmittedPapersController(examPaperRepository, teacherRepository, departmentRepository, sessionRepository, scope)
    }
    val grouped by controller.grouped.collectAsState()
    val teachers by controller.teachers.collectAsState()
    val departments by controller.departments.collectAsState()
    val filters by controller.filters.collectAsState()
    val loading by controller.loading.collectAsState()
    val notice by controller.notice.collectAsState()

    SubmittedPapersWorkspace(
        grouped = grouped,
        teachers = teachers,
        departments = departments,
        filters = filters,
        loading = loading,
        notice = notice,
        onSetTeacherFilter = controller::setTeacherFilter,
        onSetDeptFilter = controller::setDeptFilter,
        onSetSemesterFilter = controller::setSemesterFilter,
        onSetShiftFilter = controller::setShiftFilter,
        onClearFilters = controller::clearFilters,
        onDownload = { submission ->
            controller.downloadAndOpen(submission, File(System.getProperty("java.io.tmpdir"))) { downloaded ->
                AwtDesktopPlatformServices.open(downloaded)
                Unit
            }
        },
        onConsumeNotice = controller::consumeNotice,
        onRefresh = controller::refresh,
    )
}
