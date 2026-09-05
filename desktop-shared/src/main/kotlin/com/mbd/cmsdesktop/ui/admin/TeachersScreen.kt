package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.mbd.cmscommon.controller.TeachersController
import com.mbd.cmscommon.domain.model.MAX_TEACHER_PHOTO_BYTES
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.TeacherDirectoryWorkspace
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
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
        onPickPhoto = { onPicked ->
            val file = AwtDesktopPlatformServices.pickFile(window, "Choose a photo (JPEG/PNG/WebP)")
            if (file != null) {
                val bitmap = runCatching { Image.makeFromEncoded(file.readBytes()).toComposeImageBitmap() }.getOrNull()
                if (bitmap != null) onPicked(bitmap)
            }
        },
        onUploadCroppedPhoto = { teacher, cropped ->
            scope.launch {
                val bytes = withContext(Dispatchers.Default) { compressToJpeg(cropped) }
                controller.uploadPhoto(teacher, bytes, "image/jpeg")
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

/** Re-encodes the cropped avatar as JPEG, stepping quality down until it fits the bucket's 1 MB cap. */
private fun compressToJpeg(bitmap: ImageBitmap): ByteArray {
    val image = Image.makeFromBitmap(bitmap.asSkiaBitmap())
    var quality = 90
    var bytes: ByteArray
    do {
        bytes = image.encodeToData(EncodedImageFormat.JPEG, quality)?.bytes ?: ByteArray(0)
        quality -= 15
    } while (bytes.size > MAX_TEACHER_PHOTO_BYTES && quality > 10)
    return bytes
}
