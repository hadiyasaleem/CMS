package com.mbd.cmscommon.ui.documents

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mbd.cmscommon.domain.model.DocumentUploadFile
import com.mbd.cmscommon.domain.model.DocumentViewerRole
import com.mbd.cmscommon.ui.components.DocumentWorkspace

@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val controller = viewModel.controller
    val documents by controller.docs.collectAsState()
    val loading by controller.loading.collectAsState()
    val busy by controller.busy.collectAsState()
    val downloadingId by controller.downloadingId.collectAsState()
    val error by controller.error.collectAsState()
    val actionMessage by controller.actionMessage.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val viewer by viewModel.resolvedViewer.collectAsState()

    var pickedFile by remember { mutableStateOf<DocumentUploadFile?>(null) }
    var fileSelectionError by remember { mutableStateOf<String?>(null) }

    val currentViewer = viewer
    if (currentViewer == null) {
        androidx.compose.foundation.layout.Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    DocumentWorkspace(
        documents = documents,
        viewer = currentViewer,
        departments = departments,
        canManage = currentViewer.role == DocumentViewerRole.ADMIN || currentViewer.role == DocumentViewerRole.TEACHER,
        loading = loading,
        busy = busy,
        downloadingId = downloadingId,
        errorMessage = error,
        actionMessage = actionMessage,
        pickedFile = pickedFile,
        fileSelectionError = fileSelectionError,
        onRetry = controller::load,
        onChooseFile = { fileSelectionError = "File picking is handled by the host screen." },
        onClearPickedFile = { pickedFile = null },
        onCreate = { draft, file ->
            if (file != null) {
                controller.uploadDocument(draft, file, publishedBy = viewModel.accountKey)
            } else {
                controller.createText(draft, publishedBy = viewModel.accountKey)
            }
            pickedFile = null
        },
        onOpenFile = { document -> viewModel.open(context, document) },
        onSetPublished = controller::setPublished,
        onDelete = controller::delete,
        modifier = modifier,
    )
}
