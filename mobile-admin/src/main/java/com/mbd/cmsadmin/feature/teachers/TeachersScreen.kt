package com.mbd.cmsadmin.feature.teachers

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.ui.components.TeacherDirectoryWorkspace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingPhotoTeacher by remember { mutableStateOf<Teacher?>(null) }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val teacher = pendingPhotoTeacher
        if (uri != null && teacher != null) {
            scope.launch {
                val resolver = context.contentResolver
                val mimeType = resolver.getType(uri) ?: "image/jpeg"
                val bytes = withContext(Dispatchers.IO) { resolver.openInputStream(uri)?.use { it.readBytes() } }
                if (bytes != null) viewModel.uploadPhoto(teacher, bytes, mimeType)
            }
        }
    }

    TeacherDirectoryWorkspace(
        teachers = teachers,
        departments = departments,
        assignments = assignments,
        loading = loading,
        busy = creating,
        busyTeacherId = busyTeacherId,
        notice = notice,
        errorMessage = errorMessage,
        onRefresh = viewModel::refresh,
        onCreate = viewModel::createTeacher,
        onUpdate = viewModel::updateTeacher,
        onSetStatus = viewModel::setStatus,
        onDelete = viewModel::deleteTeacher,
        onPickPhoto = { teacher -> pendingPhotoTeacher = teacher; pickPhoto.launch("image/*") },
        onLoadPhoto = viewModel::loadPhoto,
        onConsumeNotice = viewModel::consumeNotice,
        onClearError = viewModel::clearError,
    )
}
