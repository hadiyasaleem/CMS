package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ReviewCanvas = ModGround
private val ReviewDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

/** Admin queue of exam papers still awaiting review, across every teacher and session. */
@Composable
fun ExamPaperReviewWorkspace(
    pending: List<ExamPaperSubmission>,
    loading: Boolean,
    busySubmissionId: String?,
    notice: String?,
    errorMessage: String?,
    onOpen: (ExamPaperSubmission) -> Unit,
    onMarkReviewed: (ExamPaperSubmission, String?) -> Unit,
    onConsumeNotice: () -> Unit,
    onClearError: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var reviewTarget by remember { mutableStateOf<ExamPaperSubmission?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(ReviewCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ReviewHeader(pending.size, onRefresh) }

        errorMessage?.let { message ->
            item { ReviewBanner(message, isError = true, onDismiss = onClearError) }
        }
        notice?.let { message ->
            item { ReviewBanner(message, isError = false, onDismiss = onConsumeNotice) }
        }

        if (loading) {
            item {
                Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp), strokeWidth = 2.dp)
                }
            }
        } else if (pending.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                    Text(
                        "Nothing waiting for review.",
                        modifier = Modifier.padding(24.dp),
                        color = ModMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            items(pending.sortedBy { it.uploadedAt }, key = { it.submissionId }) { submission ->
                PendingSubmissionCard(
                    submission = submission,
                    busy = busySubmissionId == submission.submissionId,
                    onOpen = { onOpen(submission) },
                    onMarkReviewed = { reviewTarget = submission },
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    reviewTarget?.let { submission ->
        MarkReviewedDialog(
            submission = submission,
            onConfirm = { notes -> onMarkReviewed(submission, notes); reviewTarget = null },
            onDismiss = { reviewTarget = null },
        )
    }
}

@Composable
private fun ReviewHeader(pendingCount: Int, onRefresh: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("REVIEW QUEUE", color = ModAccent, style = CmsTextStyles.eyebrow)
                TextButton(onClick = onRefresh) { Text("Refresh") }
            }
            Spacer(Modifier.height(6.dp))
            Text("Exam paper review", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("$pendingCount awaiting review", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReviewBanner(message: String, isError: Boolean, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, if (isError) ModAccent else ModSuccess),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Composable
private fun PendingSubmissionCard(
    submission: ExamPaperSubmission,
    busy: Boolean,
    onOpen: () -> Unit,
    onMarkReviewed: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(submission.fileName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(
                "${submission.examType} · ${submission.subjectId} · Uploaded by ${submission.teacherId}",
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Uploaded ${submission.uploadedAt.atZone(ZoneId.systemDefault()).format(ReviewDateFormat)}",
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onOpen, enabled = !busy) { Text("Open") }
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                } else {
                    TextButton(onClick = onMarkReviewed) { Text("Mark reviewed") }
                }
            }
        }
    }
}

@Composable
private fun MarkReviewedDialog(submission: ExamPaperSubmission, onConfirm: (String?) -> Unit, onDismiss: () -> Unit) {
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark reviewed") },
        text = {
            Column {
                Text("\"${submission.fileName}\" will be marked as reviewed.")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Feedback for the teacher (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(notes.trim().ifBlank { null }) }) { Text("Mark reviewed") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
