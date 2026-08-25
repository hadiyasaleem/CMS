package com.mbd.cmsdesktop.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.controller.DocumentsController
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.DocumentUploadFile
import com.mbd.cmscommon.domain.model.DocumentViewerContext
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.DocumentRepository
import com.mbd.cmscommon.ui.components.DocumentWorkspace
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * Self-contained Documents (library) screen shared by every desktop role: builds its own
 * [DocumentsController], resolves the department list for the create form's audience picker, and
 * wires the platform PDF file-chooser/opener straight to AWT (mirrors the exam-paper picker in
 * `TeacherNavHost`) instead of routing through a platform-services abstraction.
 */
@Composable
fun DocumentsScreen(
    repository: DocumentRepository,
    departmentRepository: DepartmentRepository,
    viewer: DocumentViewerContext,
    canUpload: Boolean,
    window: ComposeWindow,
    publishedBy: String? = null,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository) { DocumentsController(repository, scope) }

    val docs by controller.docs.collectAsState()
    val loading by controller.loading.collectAsState()
    val busy by controller.busy.collectAsState()
    val downloadingId by controller.downloadingId.collectAsState()
    val actionMessage by controller.actionMessage.collectAsState()

    var departments by remember { mutableStateOf<List<Department>>(emptyList()) }
    LaunchedEffect(departmentRepository) {
        departmentRepository.observeActiveDepartments().collect { departments = it }
    }

    var pickedFile by remember { mutableStateOf<DocumentUploadFile?>(null) }
    var fileSelectionError by remember { mutableStateOf<String?>(null) }

    DocumentWorkspace(
        documents = docs,
        viewer = viewer,
        departments = departments,
        canManage = canUpload,
        loading = loading,
        busy = busy,
        downloadingId = downloadingId,
        errorMessage = null,
        actionMessage = actionMessage,
        pickedFile = pickedFile,
        fileSelectionError = fileSelectionError,
        onRetry = controller::load,
        onChooseFile = {
            fileSelectionError = null
            val dialog = FileDialog(window as? Frame, "Choose a PDF", FileDialog.LOAD)
            dialog.file = "*.pdf"
            dialog.isVisible = true
            val dir = dialog.directory
            val name = dialog.file
            if (dir != null && name != null) {
                runCatching { File(dir, name).readBytes() }
                    .onSuccess { pickedFile = DocumentUploadFile(name, it) }
                    .onFailure { fileSelectionError = it.message ?: "Could not read the selected file." }
            }
        },
        onClearPickedFile = { pickedFile = null; fileSelectionError = null },
        onCreate = { draft, file ->
            val by = publishedBy.orEmpty()
            if (file != null) controller.uploadDocument(draft, file, by) else controller.createText(draft, by)
            pickedFile = null
        },
        onOpenFile = { document ->
            controller.openDocument(document, File(System.getProperty("java.io.tmpdir"))) { file ->
                runCatching { Desktop.getDesktop().open(file) }
            }
        },
        onSetPublished = controller::setPublished,
        onDelete = controller::delete,
    )
}
