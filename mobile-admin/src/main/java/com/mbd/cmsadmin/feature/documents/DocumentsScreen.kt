package com.mbd.cmsadmin.feature.documents

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.DocumentsController
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Document
import com.mbd.cmscommon.domain.model.DocumentDraft
import com.mbd.cmscommon.domain.model.DocumentUploadFile
import com.mbd.cmscommon.domain.model.DocumentViewerContext
import com.mbd.cmscommon.domain.model.DocumentViewerRole
import com.mbd.cmscommon.domain.model.MAX_DOCUMENT_UPLOAD_BYTES
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.DocumentRepository
import com.mbd.cmscommon.ui.components.DocumentWorkspace
import com.mbd.cmscommon.util.FileOpener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    repo: DocumentRepository,
    departmentRepository: DepartmentRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    val controller = DocumentsController(repo, viewModelScope)
    val departments: StateFlow<List<Department>> = departmentRepository.observeActiveDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun readPickedFile(uri: Uri, onResult: (Result<DocumentUploadFile>) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val (name, reportedSize) = resolveFileMetadata(uri)
                    require(name.endsWith(".pdf", ignoreCase = true)) { "Only PDF files can be uploaded to Documents." }
                    require(reportedSize == null || reportedSize <= MAX_DOCUMENT_UPLOAD_BYTES) { "The selected PDF exceeds the 10 MB limit." }
                    val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("The selected file could not be read.")
                    DocumentUploadFile(name, bytes)
                }
            }
            onResult(result)
        }
    }

    fun create(draft: DocumentDraft, file: DocumentUploadFile?) {
        val publisher = sessionManager.accountKey.orEmpty()
        if (file == null) controller.createText(draft, publisher)
        else controller.uploadDocument(draft, file, publisher)
    }

    fun open(context: Context, document: Document) {
        controller.openDocument(document, appContext.cacheDir) { file ->
            FileOpener.open(context, file, "application/pdf")
        }
    }

    private fun resolveFileMetadata(uri: Uri): Pair<String, Long?> {
        appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                val name = if (nameIndex >= 0) cursor.getString(nameIndex) else null
                val size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else null
                if (!name.isNullOrBlank()) return name to size
            }
        }
        return (uri.lastPathSegment ?: "document.pdf") to null
    }
}

@Composable
fun DocumentsScreen(viewModel: DocumentsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val documents by viewModel.controller.docs.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val loading by viewModel.controller.loading.collectAsState()
    val busy by viewModel.controller.busy.collectAsState()
    val downloadingId by viewModel.controller.downloadingId.collectAsState()
    val error by viewModel.controller.error.collectAsState()
    val actionMessage by viewModel.controller.actionMessage.collectAsState()
    var pickedFile by remember { mutableStateOf<DocumentUploadFile?>(null) }
    var fileError by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            fileError = null
            viewModel.readPickedFile(it) { result ->
                result.onSuccess { file -> pickedFile = file }
                    .onFailure { failure -> fileError = failure.message ?: "The selected file could not be read." }
            }
        }
    }

    DocumentWorkspace(
        documents = documents,
        viewer = DocumentViewerContext(DocumentViewerRole.ADMIN),
        departments = departments,
        canManage = true,
        loading = loading,
        busy = busy,
        downloadingId = downloadingId,
        errorMessage = error,
        actionMessage = actionMessage,
        pickedFile = pickedFile,
        fileSelectionError = fileError,
        onRetry = viewModel.controller::load,
        onChooseFile = { picker.launch(arrayOf("application/pdf")) },
        onClearPickedFile = { pickedFile = null; fileError = null },
        onCreate = viewModel::create,
        onOpenFile = { viewModel.open(context, it) },
        onSetPublished = viewModel.controller::setPublished,
        onDelete = viewModel.controller::delete,
    )
}
