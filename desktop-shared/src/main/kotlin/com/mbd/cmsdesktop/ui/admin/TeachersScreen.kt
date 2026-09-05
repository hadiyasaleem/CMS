package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.mbd.cmscommon.controller.TeachersController
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.TeacherDirectoryWorkspace
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image

@Composable
fun TeachersScreen(
    repository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    createdBy: String?,
    assignmentsProvider: TeacherAssignmentsProvider,
    window: ComposeWindow,
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
        onPickPhoto = { teacher ->
            val file = AwtDesktopPlatformServices.pickFile(window, "Choose a photo (JPEG/PNG/WebP)")
            if (file != null) {
                val mimeType = when (file.extension.lowercase()) {
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    else -> "image/jpeg"
                }
                scope.launch { controller.uploadPhoto(teacher, file.readBytes(), mimeType) }
            }
        },
        onLoadPhoto = { path -> loadPhotoAsImageBitmap(repository, path) },
        onConsumeNotice = controller::consumeNotice,
        onClearError = controller::clearError,
    )
}

private suspend fun loadPhotoAsImageBitmap(repository: TeacherRepository, photoPath: String): ImageBitmap? {
    val bytes = repository.downloadPhoto(photoPath) ?: return null
    return runCatching { Image.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()
}
