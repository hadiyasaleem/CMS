package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.StagedPaperFile
import com.mbd.cmscommon.controller.TeacherPaperSlot
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.datesheetLabel
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModRedTint
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.util.Outcome
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val PaperCanvas = ModGround
private val PaperGold = ModWarn
private val PaperGreen = ModSuccess
private val PaperRed = ModAccent
private val PaperDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamPaperSubmissionWorkspace(
    slots: List<TeacherPaperSlot>,
    sessions: List<AcademicSession>,
    selected: TeacherPaperSlot?,
    stagedFile: StagedPaperFile?,
    uploadState: Outcome<Unit>?,
    onSelectSlot: (TeacherPaperSlot?) -> Unit,
    onChooseFile: () -> Unit,
    onClearStagedFile: () -> Unit,
    onConfirmUpload: (fileName: String, description: String?) -> Unit,
    onOpen: (ExamPaperSubmission) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var deleteTarget by remember { mutableStateOf<ExamPaperSubmission?>(null) }

    fun sessionOf(sessionId: String): AcademicSession? = sessions.firstOrNull { it.sessionId == sessionId }

    val grouped = slots.groupBy { it.datesheet.id }
    val submittedCount = slots.count { it.isSubmitted }

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(PaperCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PaperHeader(slots.size, submittedCount) }

        if (slots.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                    Text(
                        "No published datesheet has an exam slot for a subject you teach yet.",
                        modifier = Modifier.padding(24.dp),
                        color = ModMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            grouped.forEach { (_, group) ->
                val sheet = group.first().datesheet
                item {
                    Text(
                        datesheetLabel(sheet, sessionOf(sheet.sessionId), department = null),
                        color = ModMuted,
                        style = CmsTextStyles.eyebrow,
                    )
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        group.forEach { paperSlot -> SubjectTile(paperSlot, onClick = { onSelectSlot(paperSlot) }) }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    selected?.let { target ->
        SubjectPaperDialog(
            target = target,
            stagedFile = stagedFile,
            uploadState = uploadState,
            onDismiss = { onSelectSlot(null) },
            onChooseFile = onChooseFile,
            onClearStagedFile = onClearStagedFile,
            onConfirmUpload = onConfirmUpload,
            onOpen = { target.submission?.let(onOpen) },
            onDelete = { target.submission?.let { deleteTarget = it } },
        )
    }

    deleteTarget?.let { submission ->
        ConfirmDestructiveActionDialog(
            title = "Remove paper",
            dependentSummary = "\"${submission.fileName}\" will be permanently removed.",
            onConfirm = { onDelete(submission.submissionId); deleteTarget = null; onSelectSlot(null) },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun PaperHeader(total: Int, submitted: Int) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("ASSESSMENT WORKSPACE", color = PaperGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Exam papers", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                if (total == 0) "No exam slots yet" else "$submitted of $total papers submitted",
                color = CmsTheme.colors.onInkMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SubjectTile(paperSlot: TeacherPaperSlot, onClick: () -> Unit) {
    val tone = if (paperSlot.isSubmitted) PaperGreen else PaperRed
    val icon: ImageVector = if (paperSlot.isSubmitted) Icons.Filled.CheckCircle else Icons.Filled.Warning
    Surface(
        modifier = Modifier.width(180.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (paperSlot.isSubmitted) ModSurface else ModRedTint,
        border = BorderStroke(1.dp, tone.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(paperSlot.slot.subjectName, fontWeight = FontWeight.Bold, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
                Text(paperSlot.slot.courseCode, color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            Icon(icon, contentDescription = if (paperSlot.isSubmitted) "Submitted" else "Not submitted", tint = tone, modifier = Modifier.width(20.dp))
        }
    }
}

@Composable
private fun SubjectPaperDialog(
    target: TeacherPaperSlot,
    stagedFile: StagedPaperFile?,
    uploadState: Outcome<Unit>?,
    onDismiss: () -> Unit,
    onChooseFile: () -> Unit,
    onClearStagedFile: () -> Unit,
    onConfirmUpload: (fileName: String, description: String?) -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    var fileName by remember(stagedFile) { mutableStateOf(stagedFile?.fileName ?: "") }
    var description by remember(stagedFile) { mutableStateOf("") }
    val busy = uploadState is Outcome.Loading

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(target.slot.subjectName) },
        text = {
            Column {
                Text(target.slot.courseCode, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))

                val submission = target.submission
                if (submission != null) {
                    Text("Currently submitted", color = ModMuted, style = CmsTextStyles.eyebrow)
                    Text(submission.fileName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Uploaded ${submission.uploadedAt.atZone(ZoneId.systemDefault()).format(PaperDateFormat)}",
                        color = ModMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    val submissionDescription = submission.description
                    if (!submissionDescription.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(submissionDescription, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onOpen) { Text("Open") }
                        TextButton(onClick = onDelete) { Text("Remove", color = CmsTheme.colors.accent) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Reuploading below replaces this file.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                }

                val sizeError = stagedFile?.sizeError
                when {
                    stagedFile == null -> CmsPrimaryButton(text = "Choose file", onClick = onChooseFile, enabled = !busy)
                    sizeError != null -> {
                        CmsNotice(sizeError, tone = NoticeTone.Error)
                        Spacer(Modifier.height(8.dp))
                        CmsPrimaryButton(text = "Choose a different file", onClick = onChooseFile)
                    }
                    else -> {
                        Text("Selected: ${stagedFile.fileName}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = fileName,
                            onValueChange = { fileName = it },
                            label = { Text("File name (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !busy,
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Title or description (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            enabled = !busy,
                        )
                        Spacer(Modifier.height(10.dp))
                        when (uploadState) {
                            is Outcome.Loading -> CmsNotice("Uploading...", tone = NoticeTone.Info, showProgress = true)
                            is Outcome.Error -> CmsNotice(uploadState.message, tone = NoticeTone.Error)
                            is Outcome.Success -> CmsNotice("Paper uploaded successfully.", tone = NoticeTone.Success)
                            null -> {}
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (stagedFile != null && stagedFile.sizeError == null) {
                TextButton(
                    onClick = { onConfirmUpload(fileName.trim().ifBlank { stagedFile.fileName }, description.trim().ifBlank { null }) },
                    enabled = !busy,
                ) { Text(if (busy) "Uploading..." else "Upload") }
            } else {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {
            if (stagedFile != null) {
                TextButton(onClick = onClearStagedFile, enabled = !busy) { Text("Cancel") }
            } else {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
    )
}
