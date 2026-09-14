package com.mbd.cmsadmin.feature.academics

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.StudentProfileEditController
import com.mbd.cmscommon.domain.model.PROFILE_PHOTO_COMPRESSED_TARGET_BYTES
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.ui.components.StudentProfileWorkspace
import com.mbd.cmscommon.util.orLogCritical
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: AcademicSessionRepository,
    fineRepository: FineRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    private val controller = StudentProfileEditController(
        sessionId = checkNotNull(savedStateHandle["sessionId"]),
        rollNumber = checkNotNull(savedStateHandle["roll"]),
        sessionRepository = sessionRepository,
        fineRepository = fineRepository,
        issuedBy = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    val profile = controller.profile
    val session = controller.session
    val saveState = controller.saveState
    val fines = controller.fines
    val error = controller.error
    val photoBusy = controller.photoBusy

    fun issueFine(category: String, amount: Double, reason: String) = controller.issueFine(category, amount, reason)
    fun deleteFine(id: String) = controller.deleteFine(id)
    fun save(profile: StudentProfile) = controller.save(profile)
    fun delinkAccount() = controller.delinkAccount()
    fun clearError() = controller.clearError()
    fun uploadPhoto(imageBytes: ByteArray, mimeType: String) = controller.uploadPhoto(imageBytes, mimeType)
    fun reportPhotoPickFailure(t: Throwable) = controller.reportPhotoPickFailure(t)
    suspend fun downloadPhotoBytes(photoPath: String): ByteArray? = sessionRepository.downloadStudentPhoto(photoPath)
}

@Composable
fun StudentProfileScreen(viewModel: StudentProfileViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsState()
    val session by viewModel.session.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val fines by viewModel.fines.collectAsState()
    val errorMessage by viewModel.error.collectAsState()
    val photoBusy by viewModel.photoBusy.collectAsState()

    val loadedProfile = profile
    if (loadedProfile == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoCacheDir = remember(context) { File(context.cacheDir, "student_photos").apply { mkdirs() } }
    var pendingOnPicked by remember { mutableStateOf<((ImageBitmap) -> Unit)?>(null) }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val onPicked = pendingOnPicked
        if (uri != null && onPicked != null) {
            scope.launch {
                try {
                    val bytes = withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }
                        ?: throw IllegalStateException("Couldn't read the selected photo.")
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                        ?: throw IllegalStateException("Couldn't decode the selected photo.")
                    onPicked(bitmap)
                } catch (t: Throwable) {
                    viewModel.reportPhotoPickFailure(t)
                }
            }
        }
    }

    StudentProfileWorkspace(
        loadedProfile = loadedProfile,
        session = session,
        fines = fines,
        saveOutcome = saveState,
        errorMessage = errorMessage,
        onSave = viewModel::save,
        onIssueFine = viewModel::issueFine,
        onDeleteFine = { viewModel.deleteFine(it.id) },
        onDelink = viewModel::delinkAccount,
        onClearError = viewModel::clearError,
        onPickPhoto = { onPicked -> pendingOnPicked = onPicked; pickPhoto.launch("image/*") },
        onSavePhoto = { cropped ->
            scope.launch {
                val bytes = withContext(Dispatchers.Default) { compressToJpeg(cropped) }
                viewModel.uploadPhoto(bytes, "image/jpeg")
                // The remote storage path is deterministic (students/{sessionId}/{roll}.jpg), so
                // pre-warm the local cache with what we just uploaded instead of waiting to
                // re-download it.
                withContext(Dispatchers.IO) {
                    runCatching { cacheFileFor(photoCacheDir, "students/${loadedProfile.sessionId}/${loadedProfile.rollNumber}.jpg").writeBytes(bytes) }
                        .orLogCritical("StudentProfileScreen.cacheUploadedPhoto")
                }
            }
        },
        photoBusy = photoBusy,
        onLoadPhoto = { path -> loadPhotoCached(photoCacheDir, path, viewModel::downloadPhotoBytes) },
    )
}

private fun cacheFileFor(cacheDir: File, photoPath: String): File = File(cacheDir, photoPath.replace('/', '_'))

/** Local-first photo load: serves the cached file if present, otherwise downloads once and caches it. */
private suspend fun loadPhotoCached(cacheDir: File, photoPath: String, download: suspend (String) -> ByteArray?): ImageBitmap? {
    val cacheFile = cacheFileFor(cacheDir, photoPath)
    val bytes = withContext(Dispatchers.IO) {
        if (cacheFile.exists()) {
            cacheFile.readBytes()
        } else {
            download(photoPath)?.also { runCatching { cacheFile.writeBytes(it) }.orLogCritical("StudentProfileScreen.cacheDownloadedPhoto") }
        }
    } ?: return null
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
}

/** Re-encodes the cropped avatar as JPEG, stepping quality down until it's comfortably under 100 KB. */
private fun compressToJpeg(bitmap: ImageBitmap): ByteArray {
    val androidBitmap = bitmap.asAndroidBitmap()
    var quality = 90
    var bytes: ByteArray
    do {
        val stream = ByteArrayOutputStream()
        androidBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        bytes = stream.toByteArray()
        quality -= 15
    } while (bytes.size > PROFILE_PHOTO_COMPRESSED_TARGET_BYTES && quality > 10)
    return bytes
}
