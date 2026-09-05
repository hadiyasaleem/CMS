package com.mbd.cmsadmin.feature.teachers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.domain.model.MAX_TEACHER_PHOTO_BYTES
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.ui.components.TeacherDirectoryWorkspace
import java.io.ByteArrayOutputStream
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
    var pendingOnPicked by remember { mutableStateOf<((ImageBitmap) -> Unit)?>(null) }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val onPicked = pendingOnPicked
        if (uri != null && onPicked != null) {
            scope.launch {
                val bytes = withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }
                val bitmap = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
                if (bitmap != null) onPicked(bitmap)
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
        onPickPhoto = { onPicked -> pendingOnPicked = onPicked; pickPhoto.launch("image/*") },
        onUploadCroppedPhoto = { teacher: Teacher, cropped: ImageBitmap ->
            scope.launch {
                val bytes = withContext(Dispatchers.Default) { compressToJpeg(cropped) }
                viewModel.uploadPhoto(teacher, bytes, "image/jpeg")
            }
        },
        onLoadPhoto = viewModel::loadPhoto,
        onConsumeNotice = viewModel::consumeNotice,
        onClearError = viewModel::clearError,
    )
}

/** Re-encodes the cropped avatar as JPEG, stepping quality down until it fits the bucket's 1 MB cap. */
private fun compressToJpeg(bitmap: ImageBitmap): ByteArray {
    val androidBitmap = bitmap.asAndroidBitmap()
    var quality = 90
    var bytes: ByteArray
    do {
        val stream = ByteArrayOutputStream()
        androidBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        bytes = stream.toByteArray()
        quality -= 15
    } while (bytes.size > MAX_TEACHER_PHOTO_BYTES && quality > 10)
    return bytes
}
