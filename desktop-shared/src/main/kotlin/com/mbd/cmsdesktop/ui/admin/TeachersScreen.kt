package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.TeachersController
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.TeacherDirectoryWorkspace

@Composable
fun TeachersScreen(
    repository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    createdBy: String?,
    assignmentsProvider: TeacherAssignmentsProvider,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository, departmentRepository, assignmentsProvider, createdBy) {
        TeachersController(repository, departmentRepository, assignmentsProvider, createdBy.orEmpty(), scope)
    }
    val teachers by controller.teachers.collectAsState()
    val departments by controller.departments.collectAsState()
    val assignments by controller.assignments.collectAsState()
    val loading by controller.loading.collectAsState()
    val creating by controller.creating.collectAsState()
    val busyTeacherId by controller.busyTeacherId.collectAsState()
    val notice by controller.notice.collectAsState()
    val errorMessage by controller.error.collectAsState()

    TeacherDirectoryWorkspace(
        teachers = teachers,
        departments = departments,
        assignments = assignments,
        loading = loading,
        busy = creating,
        busyTeacherId = busyTeacherId,
        notice = notice,
        errorMessage = errorMessage,
        onRefresh = controller::refresh,
        onCreate = controller::createTeacher,
        onUpdate = controller::updateTeacher,
        onSetStatus = controller::setStatus,
        onDelete = controller::deleteTeacher,
        onConsumeNotice = controller::consumeNotice,
        onClearError = controller::clearError,
    )
}
