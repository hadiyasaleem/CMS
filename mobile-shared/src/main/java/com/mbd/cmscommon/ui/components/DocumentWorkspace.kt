package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Document
import com.mbd.cmscommon.domain.model.DocumentDraft
import com.mbd.cmscommon.domain.model.DocumentUploadFile
import com.mbd.cmscommon.domain.model.DocumentViewerContext
import com.mbd.cmscommon.domain.model.activityAt
import com.mbd.cmscommon.domain.model.documentLibrarySummary
import com.mbd.cmscommon.domain.model.isVisibleTo
import com.mbd.cmscommon.domain.model.normalizedDocumentTags
import com.mbd.cmscommon.domain.model.reviewReasons
import com.mbd.cmscommon.domain.model.validationMessage
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DocumentBlue = Color(0xFF24577A)
private val DocumentDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val DOCUMENT_KINDS = listOf("PROSPECTUS", "RULES", "REPORT", "OTHER")
private val DOCUMENT_AUDIENCES = listOf("ALL", "ADMIN", "TEACHER", "STUDENT")

@Composable
fun DocumentWorkspace(
    documents: List<Document>,
    viewer: DocumentViewerContext,
    departments: List<Department>,
    canManage: Boolean,
    loading: Boolean,
    busy: Boolean,
    downloadingId: String?,
    errorMessage: String?,
    actionMessage: String?,
    pickedFile: DocumentUploadFile?,
    fileSelectionError: String?,
    onRetry: () -> Unit,
    onChooseFile: () -> Unit,
    onClearPickedFile: () -> Unit,
    onCreate: (DocumentDraft, DocumentUploadFile?) -> Unit,
    onOpenFile: (Document) -> Unit,
    onSetPublished: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Document?>(null) }

    val visibleToViewer = documents.filter { isVisibleTo(it, viewer) }
    val summary = documentLibrarySummary(visibleToViewer, viewer)
    val validDeptIds = departments.map { it.deptId }.toSet()
    val reviewCount = if (canManage) documents.count { reviewReasons(it, validDeptIds).isNotEmpty() } else 0

    val filtered = visibleToViewer
        .filter { selectedKind == null || it.kind.equals(selectedKind, ignoreCase = true) }
        .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
        .sortedByDescending { activityAt(it) }

    CardGrid(modifier.fillMaxWidth()) {
        fullSpanItem { DocumentHeader(canManage) { showCreate = true } }

        if (!errorMessage.isNullOrBlank()) {
            fullSpanItem { DocumentNotice(errorMessage, CmsTheme.colors.accent, action = "Retry", onAction = onRetry) }
        }
        if (!actionMessage.isNullOrBlank()) {
            fullSpanItem { DocumentNotice(actionMessage, Color(0xFF2F6B4F), action = null, onAction = null) }
        }

        fullSpanItem { DocumentSummary(summary.totalResources, summary.pdfResources, summary.recentlyUpdatedResources, reviewCount) }

        fullSpanItem {
            DocumentFilters(
                query = query,
                onQueryChange = { query = it },
                selectedKind = selectedKind,
                onKindChange = { selectedKind = it },
            )
        }

        when {
            loading -> fullSpanItems(3) { SkeletonRow() }
            visibleToViewer.isEmpty() -> fullSpanItem { DocumentEmptyState(filtered = false, canManage = canManage, onAdd = { showCreate = true }, onClear = {}) }
            filtered.isEmpty() -> fullSpanItem { DocumentEmptyState(filtered = true, canManage = false, onAdd = {}, onClear = { query = ""; selectedKind = null }) }
            else -> items(filtered, key = { it.id }) { document ->
                DocumentResourceCard(
                    document = document,
                    departments = departments,
                    canManage = canManage,
                    downloading = downloadingId == document.id,
                    onOpen = { onOpenFile(document) },
                    onTogglePublished = { onSetPublished(document.id, !document.published) },
                    onDelete = { pendingDelete = document },
                )
            }
        }

        fullSpanItem { Spacer(Modifier.height(72.dp)) }
    }

    if (showCreate) {
        CreateDocumentDialog(
            departments = departments,
            busy = busy,
            pickedFile = pickedFile,
            fileSelectionError = fileSelectionError,
            onChooseFile = onChooseFile,
            onClearPickedFile = onClearPickedFile,
            onDismiss = { showCreate = false; onClearPickedFile() },
            onConfirm = { draft, file -> onCreate(draft, file); showCreate = false },
        )
    }

    pendingDelete?.let { document ->
        ConfirmDestructiveActionDialog(
            title = "Delete document",
            dependentSummary = "\"${document.title}\" will be permanently removed.",
            onConfirm = { onDelete(document.id); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun DocumentHeader(canManage: Boolean, onAdd: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Documents", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Prospectus, rules, reports and official reference material",
                    color = CmsTheme.colors.onInkMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (canManage) {
                CmsPrimaryButton(text = "New document", onClick = onAdd)
            }
        }
    }
}

@Composable
private fun DocumentSummary(total: Int, pdfCount: Int, recent: Int, reviewCount: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DocumentMetric("Total", total.toString(), "documents", Modifier.weight(1f))
        DocumentMetric("PDF / text", pdfCount.toString(), "with attached files", Modifier.weight(1f))
        DocumentMetric("Recent", recent.toString(), "last 30 days", Modifier.weight(1f))
        DocumentMetric("Needs review", reviewCount.toString(), "records to check", Modifier.weight(1f))
    }
}

@Composable
private fun DocumentMetric(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(Locale.ROOT), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
            Text(detail, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DocumentFilters(query: String, onQueryChange: (String) -> Unit, selectedKind: String?, onKindChange: (String?) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search documents") },
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CmsChip("All types", selected = selectedKind == null, onClick = { onKindChange(null) })
            DOCUMENT_KINDS.forEach { kind ->
                CmsChip(kind, selected = selectedKind == kind, onClick = { onKindChange(kind) })
            }
        }
    }
}

private fun documentKindIcon(kind: String): ImageVector = when (kind.uppercase(Locale.ROOT)) {
    "PROSPECTUS" -> Icons.Outlined.Summarize
    "RULES" -> Icons.Outlined.Gavel
    "REPORT" -> Icons.Outlined.Description
    else -> Icons.Outlined.InsertDriveFile
}

private fun documentKindTone(kind: String): BadgeTone = when (kind.uppercase(Locale.ROOT)) {
    "PROSPECTUS" -> BadgeTone.Navy
    "RULES" -> BadgeTone.Warning
    "REPORT" -> BadgeTone.Success
    else -> BadgeTone.Neutral
}

@Composable
private fun DocumentResourceCard(
    document: Document,
    departments: List<Department>,
    canManage: Boolean,
    downloading: Boolean,
    onOpen: () -> Unit,
    onTogglePublished: () -> Unit,
    onDelete: () -> Unit,
) {
    val departmentLabel = departments.firstOrNull { it.deptId == document.deptId }?.name
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(documentKindIcon(document.kind), contentDescription = null, tint = DocumentBlue)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(document.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        activityAt(document).takeIf { it != Instant.EPOCH }
                            ?.let { it.atZone(ZoneId.systemDefault()).format(DocumentDateFormat) } ?: "Not recorded",
                        color = Color(0xFF77716A),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                StatusBadge(document.kind.uppercase(Locale.ROOT), documentKindTone(document.kind))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(if (document.published) "PUBLISHED" else "DRAFT", if (document.published) BadgeTone.Success else BadgeTone.Neutral)
                StatusBadge(document.audience.uppercase(Locale.ROOT), BadgeTone.Neutral)
                if (departmentLabel != null) StatusBadge(departmentLabel, BadgeTone.Neutral)
            }
            if (document.tags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(normalizedDocumentTags(document.tags).joinToString(" · "), color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (document.hasFile || document.hasBody) {
                    TextButton(onClick = onOpen, enabled = !downloading) {
                        if (downloading) {
                            CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (document.hasFile) "Open PDF" else "Read")
                        }
                    }
                }
                if (canManage) {
                    TextButton(onClick = onTogglePublished) { Text(if (document.published) "Move to drafts" else "Publish now") }
                    TextButton(onClick = onDelete) { Text("Delete", color = CmsTheme.colors.accent) }
                }
            }
        }
    }
}

@Composable
private fun DocumentNotice(message: String, color: Color, action: String?, onAction: (() -> Unit)?) {
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = color, style = MaterialTheme.typography.bodyMedium)
            if (action != null && onAction != null) {
                TextButton(onClick = onAction) { Text(action, color = color) }
            }
        }
    }
}

@Composable
private fun DocumentEmptyState(filtered: Boolean, canManage: Boolean, onAdd: () -> Unit, onClear: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (filtered) "No matching documents" else "No documents are available", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (filtered) "Try a different search or type filter." else "Published resources will appear here for this audience.",
                color = Color(0xFF77716A),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            if (filtered) {
                CmsPrimaryButton(text = "Clear filters", onClick = onClear)
            } else if (canManage) {
                CmsPrimaryButton(text = "Create first document", onClick = onAdd)
            }
        }
    }
}

@Composable
private fun CreateDocumentDialog(
    departments: List<Department>,
    busy: Boolean,
    pickedFile: DocumentUploadFile?,
    fileSelectionError: String?,
    onChooseFile: () -> Unit,
    onClearPickedFile: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (DocumentDraft, DocumentUploadFile?) -> Unit,
) {
    var kind by remember { mutableStateOf("PROSPECTUS") }
    var title by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("ALL") }
    var deptId by remember { mutableStateOf<String?>(null) }
    var body by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }
    var published by remember { mutableStateOf(true) }

    val draft = DocumentDraft(
        kind = kind,
        title = title.trim(),
        body = body,
        audience = audience,
        published = published,
        departmentId = deptId,
        tags = normalizedDocumentTags(tagsText.split(",")),
    )
    val error = validationMessage(draft, pickedFile)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create document", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text("Available formats", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DOCUMENT_KINDS.forEach { option ->
                        CmsChip(option, selected = kind == option, onClick = { kind = option })
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                Text("Audience", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DOCUMENT_AUDIENCES.forEach { option ->
                        CmsChip(option, selected = audience == option, onClick = { audience = option })
                    }
                }
                Spacer(Modifier.height(10.dp))
                CmsEntityPicker(
                    label = "Department scope",
                    selectedId = deptId,
                    options = departments.map { CmsEntityOption(it.deptId, it.name) },
                    onSelected = { deptId = it },
                    optional = true,
                    emptyLabel = "College wide",
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onChooseFile) { Text(if (pickedFile != null) pickedFile.name else "Choose PDF") }
                    if (pickedFile != null) {
                        TextButton(onClick = onClearPickedFile) { Text("Clear") }
                    }
                }
                Text("PDF only, maximum 10 MB", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                if (!fileSelectionError.isNullOrBlank()) {
                    Text(fileSelectionError, color = CmsTheme.colors.accent, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Document text") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = tagsText, onValueChange = { tagsText = it }, label = { Text("Tags (comma separated)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Publish immediately", modifier = Modifier.weight(1f))
                    Switch(checked = published, onCheckedChange = { published = it })
                }
                if (error != null && title.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = CmsTheme.colors.accent, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft, pickedFile) }, enabled = error == null && !busy) {
                Text(if (published) "Publish document" else "Save as draft")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } },
    )
}
