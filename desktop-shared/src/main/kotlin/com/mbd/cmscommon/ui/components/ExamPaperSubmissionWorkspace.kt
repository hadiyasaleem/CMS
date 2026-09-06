package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.ExamPaperReviewStatus
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.util.Outcome
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val PaperCanvas = ModGround
private val PaperBlue = ModInk
private val PaperGreen = ModSuccess
private val PaperGold = ModWarn
private val PaperRed = ModAccent
private val PaperDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

@Composable
fun ExamPaperSubmissionWorkspace(
    assignments: List<ResolvedAssignment>,
    selected: ResolvedAssignment?,
    examType: ExamType,
    submissions: List<ExamPaperSubmission>,
    outcome: Outcome<Unit>?,
    onSelect: (ResolvedAssignment) -> Unit,
    onExamType: (ExamType) -> Unit,
    onChooseFile: () -> Unit,
    onOpen: (ExamPaperSubmission) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var deleteTarget by remember { mutableStateOf<ExamPaperSubmission?>(null) }

    val forThisType = submissions.filter { it.examType == examType }
    val latest = forThisType.maxByOrNull { it.uploadedAt }
    val typesCovered = submissions.map { it.examType }.distinct().size

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(PaperCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PaperHeader(selected, examType) }
        item { AssignmentPicker(assignments, selected, onSelect) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExamType.entries.forEach { type ->
                    CmsChip(type.name, selected = examType == type, onClick = { onExamType(type) })
                }
            }
        }
        item { PaperMetrics(submissions.size, forThisType.size, typesCovered, latest?.uploadedAt) }
        item { UploadPaperCard(examType, outcome, onChooseFile) }

        if (forThisType.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                    Text("No papers uploaded for $examType yet.", modifier = Modifier.padding(24.dp), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(forThisType.sortedByDescending { it.uploadedAt }, key = { it.submissionId }) { submission ->
                SubmissionCard(submission, onOpen = { onOpen(submission) }, onDelete = { deleteTarget = submission })
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    deleteTarget?.let { submission ->
        ConfirmDestructiveActionDialog(
            title = "Remove paper",
            dependentSummary = "\"${submission.fileName}\" will be permanently removed.",
            onConfirm = { onDelete(submission.submissionId); deleteTarget = null },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun PaperHeader(selected: ResolvedAssignment?, examType: ExamType) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("ASSESSMENT WORKSPACE", color = PaperGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Exam papers", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                selected?.let { "${it.subjectLabel} · ${it.sessionLabel} · $examType" } ?: "Select a class",
                color = CmsTheme.colors.onInkMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AssignmentPicker(assignments: List<ResolvedAssignment>, selected: ResolvedAssignment?, onSelect: (ResolvedAssignment) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                selected?.let { "${it.subjectLabel} (${it.courseCode})" } ?: "Select a class",
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            assignments.forEach { assignment ->
                DropdownMenuItem(
                    text = { Text("${assignment.subjectLabel} · ${assignment.sessionLabel}") },
                    onClick = { onSelect(assignment); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun PaperMetrics(total: Int, forType: Int, covered: Int, latest: java.time.Instant?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PaperMetric("This type", forType.toString(), Modifier.weight(1f))
        PaperMetric("All files", total.toString(), Modifier.weight(1f))
        PaperMetric("Types covered", covered.toString(), Modifier.weight(1f))
        PaperMetric("Latest upload", latest?.atZone(ZoneId.systemDefault())?.format(PaperDateFormat) ?: "None", Modifier.weight(1f))
    }
}

@Composable
private fun PaperMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun UploadPaperCard(examType: ExamType, outcome: Outcome<Unit>?, onChooseFile: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text("Upload $examType paper", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("PDF or DOCX, stored securely with this class", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            when (outcome) {
                // null = idle (nothing uploaded yet): show only the picker, not a false "success".
                null -> CmsPrimaryButton(text = "Choose file", onClick = onChooseFile)
                is Outcome.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Uploading...", color = PaperBlue)
                }
                is Outcome.Success -> {
                    Text("Paper uploaded successfully.", color = PaperGreen, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    CmsPrimaryButton(text = "Choose file", onClick = onChooseFile)
                }
                is Outcome.Error -> {
                    Text(outcome.message, color = PaperRed, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    CmsPrimaryButton(text = "Choose file", onClick = onChooseFile)
                }
            }
        }
    }
}

@Composable
private fun SubmissionCard(submission: ExamPaperSubmission, onOpen: () -> Unit, onDelete: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(submission.fileName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Uploaded ${submission.uploadedAt.atZone(ZoneId.systemDefault()).format(PaperDateFormat)}",
                        color = ModMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                StatusBadge(submission.examType.name, BadgeTone.Navy)
                TextButton(onClick = onOpen) { Text("Open") }
                TextButton(onClick = onDelete) { Text("Remove", color = CmsTheme.colors.accent) }
            }
            if (submission.reviewStatus == ExamPaperReviewStatus.REVIEWED) {
                Spacer(Modifier.height(6.dp))
                StatusBadge("REVIEWED", BadgeTone.Success)
                if (!submission.teacherNotes.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Admin feedback: ${submission.teacherNotes}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
