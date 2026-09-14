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
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.StudentProfileEditController
import com.mbd.cmscommon.domain.model.PROFILE_PHOTO_COMPRESSED_TARGET_BYTES
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.ui.components.StudentProfileWorkspace
import com.mbd.cmscommon.util.orLogCritical
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@Composable
fun StudentProfileScreen(
    sessionId: String,
    rollNumber: String,
    sessionRepository: AcademicSessionRepository,
    fineRepository: FineRepository,
    sessionManager: SessionManager,
    window: ComposeWindow,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber, sessionRepository, fineRepository) {
        StudentProfileEditController(sessionId, rollNumber, sessionRepository, fineRepository, sessionManager.accountKey.orEmpty(), scope)
    }
    val profile by controller.profile.collectAsState()
    val session by controller.session.collectAsState()
    val saveState by controller.saveState.collectAsState()
    val fines by controller.fines.collectAsState()
    val errorMessage by controller.error.collectAsState()
    val photoBusy by controller.photoBusy.collectAsState()
    val photoCacheDir = remember { File(System.getProperty("java.io.tmpdir"), "cms_student_photos").apply { mkdirs() } }

    val loadedProfile = profile ?: StudentProfile(sessionId = sessionId, rollNumber = rollNumber, name = "")

    StudentProfileWorkspace(
        loadedProfile = loadedProfile,
        session = session,
        fines = fines,
        saveOutcome = saveState,
        errorMessage = errorMessage,
        onSave = controller::save,
        onIssueFine = controller::issueFine,
        onDeleteFine = { controller.deleteFine(it.id) },
        onDelink = controller::delinkAccount,
        onClearError = controller::clearError,
        onPickPhoto = { onPicked ->
            val file = AwtDesktopPlatformServices.pickFile(window, "Choose a photo (JPEG/PNG/WebP)")
            if (file != null) {
                val result = runCatching { Image.makeFromEncoded(file.readBytes()).toComposeImageBitmap() }
                val bitmap = result.getOrNull()
                if (bitmap != null) {
                    onPicked(bitmap)
                } else {
                    controller.reportPhotoPickFailure(result.exceptionOrNull() ?: IllegalStateException("Couldn't decode the selected photo."))
                }
            }
        },
        onSavePhoto = { cropped ->
            scope.launch {
                val bytes = withContext(Dispatchers.Default) { compressToJpeg(cropped) }
                controller.uploadPhoto(bytes, "image/jpeg")
                // The remote storage path is deterministic (students/{sessionId}/{roll}.jpg), so
                // pre-warm the local cache with what we just uploaded instead of waiting to
                // re-download it.
                withContext(Dispatchers.IO) {
                    runCatching { cacheFileFor(photoCacheDir, "students/$sessionId/$rollNumber.jpg").writeBytes(bytes) }
                        .orLogCritical("StudentProfileScreen.cacheUploadedPhoto")
                }
            }
        },
        photoBusy = photoBusy,
        onLoadPhoto = { path -> loadPhotoCached(photoCacheDir, path, sessionRepository) },
    )
}

private fun cacheFileFor(cacheDir: File, photoPath: String): File = File(cacheDir, photoPath.replace('/', '_'))

/** Local-first photo load: serves the cached file if present, otherwise downloads once and caches it. */
private suspend fun loadPhotoCached(cacheDir: File, photoPath: String, repository: AcademicSessionRepository): ImageBitmap? {
    val cacheFile = cacheFileFor(cacheDir, photoPath)
    val bytes = withContext(Dispatchers.IO) {
        if (cacheFile.exists()) {
            cacheFile.readBytes()
        } else {
            repository.downloadStudentPhoto(photoPath)?.also { runCatching { cacheFile.writeBytes(it) }.orLogCritical("StudentProfileScreen.cacheDownloadedPhoto") }
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
    } while (bytes.size > PROFILE_PHOTO_COMPRESSED_TARGET_BYTES && quality > 10)
    return bytes
}
