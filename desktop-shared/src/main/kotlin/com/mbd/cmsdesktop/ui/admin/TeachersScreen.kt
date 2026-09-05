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
import com.mbd.cmscommon.domain.model.TEACHER_PHOTO_COMPRESSED_TARGET_BYTES
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.TeacherDirectoryWorkspace
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import java.io.File
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
    val photoCacheDir = remember { File(System.getProperty("java.io.tmpdir"), "cms_teacher_photos").apply { mkdirs() } }

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
                // The remote storage path is deterministic (teachers/{email}.jpg), so pre-warm the
                // local cache with what we just uploaded instead of waiting to re-download it.
                withContext(Dispatchers.IO) {
                    runCatching { cacheFileFor(photoCacheDir, "teachers/${teacher.teacherId}.jpg").writeBytes(bytes) }
                }
            }
        },
        onLoadPhoto = { path -> loadPhotoCached(photoCacheDir, path, repository) },
        onConsumeNotice = controller::consumeNotice,
        onClearError = controller::clearError,
    )
}

private fun cacheFileFor(cacheDir: File, photoPath: String): File = File(cacheDir, photoPath.replace('/', '_'))

/** Local-first photo load: serves the cached file if present, otherwise downloads once and caches it. */
private suspend fun loadPhotoCached(cacheDir: File, photoPath: String, repository: TeacherRepository): ImageBitmap? {
    val cacheFile = cacheFileFor(cacheDir, photoPath)
    val bytes = withContext(Dispatchers.IO) {
        if (cacheFile.exists()) {
            cacheFile.readBytes()
        } else {
            repository.downloadPhoto(photoPath)?.also { runCatching { cacheFile.writeBytes(it) } }
        }
    } ?: return null
    return runCatching { Image.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull()
}

/** Re-encodes the cropped avatar as JPEG, stepping quality down until it's comfortably under 100 KB. */
private fun compressToJpeg(bitmap: ImageBitmap): ByteArray {
    val image = Image.makeFromBitmap(bitmap.asSkiaBitmap())
    var quality = 90
    var bytes: ByteArray
    do {
        bytes = image.encodeToData(EncodedImageFormat.JPEG, quality)?.bytes ?: ByteArray(0)
        quality -= 15
    } while (bytes.size > TEACHER_PHOTO_COMPRESSED_TARGET_BYTES && quality > 10)
    return bytes
}
