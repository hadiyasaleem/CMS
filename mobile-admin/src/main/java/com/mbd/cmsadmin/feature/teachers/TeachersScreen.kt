package com.mbd.cmsadmin.feature.teachers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.TeacherDirectoryWorkspace

@Composable
fun TeachersScreen(viewModel: TeachersViewModel = hiltViewModel()) {
    val teachers by viewModel.teachers.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val creating by viewModel.creating.collectAsState()
    val busyTeacherId by viewModel.busyTeacherId.collectAsState()
    val notice by viewModel.notice.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    TeacherDirectoryWorkspace(
        teachers = teachers,
        departments = departments,
        assignments = assignments,
        loading = loading,
        creating = creating,
        busyTeacherId = busyTeacherId,
        notice = notice,
        errorMessage = errorMessage,
        onRefresh = viewModel::refresh,
        onCreate = viewModel::createTeacher,
        onUpdate = viewModel::updateTeacher,
        onSetStatus = viewModel::setStatus,
        onDelete = viewModel::deleteTeacher,
        onConsumeNotice = viewModel::consumeNotice,
        onClearError = viewModel::clearError,
    )
}
