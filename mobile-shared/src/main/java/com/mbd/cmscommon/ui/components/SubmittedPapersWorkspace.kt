package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.SubmittedPapersFilters
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val PapersCanvas = ModGround
private val PapersDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

/** Admin's browse/download screen for teacher-submitted exam papers -- collected to print, not
 * graded, so this lists and filters rather than reviewing/approving. */
@Composable
fun SubmittedPapersWorkspace(
    grouped: Map<String, List<ExamPaperSubmission>>,
    teachers: List<Teacher>,
    departments: List<Department>,
    filters: SubmittedPapersFilters,
    loading: Boolean,
    notice: String?,
    onSetTeacherFilter: (String?) -> Unit,
    onSetDeptFilter: (String?) -> Unit,
    onSetSemesterFilter: (Int?) -> Unit,
    onSetShiftFilter: (Session?) -> Unit,
    onClearFilters: () -> Unit,
    onDownload: (ExamPaperSubmission) -> Unit,
    onConsumeNotice: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalCount = grouped.values.sumOf { it.size }

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(PapersCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PapersHeader(totalCount, onRefresh) }

        notice?.let { message ->
            item { CmsNotice(message, tone = NoticeTone.Success, onDismiss = onConsumeNotice) }
        }

        item {
            PapersFilterBar(
                teachers = teachers,
                departments = departments,
                filters = filters,
                onSetTeacherFilter = onSetTeacherFilter,
                onSetDeptFilter = onSetDeptFilter,
                onSetSemesterFilter = onSetSemesterFilter,
                onSetShiftFilter = onSetShiftFilter,
                onClearFilters = onClearFilters,
            )
        }

        if (loading && grouped.isEmpty()) {
            item {
                Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp), strokeWidth = 2.dp)
                }
            }
        } else if (grouped.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                    Text(
                        "No submitted papers match this filter.",
                        modifier = Modifier.padding(24.dp),
                        color = ModMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            grouped.forEach { (groupLabel, papers) ->
                item { Text(groupLabel.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow) }
                items(papers, key = { it.submissionId }) { submission ->
                    SubmittedPaperCard(submission, onDownload = { onDownload(submission) })
                }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun PapersHeader(totalCount: Int, onRefresh: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("PRINT QUEUE", color = ModAccent, style = CmsTextStyles.eyebrow)
                TextButton(onClick = onRefresh) { Text("Refresh") }
            }
            Spacer(Modifier.height(6.dp))
            Text("Submitted exam papers", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("$totalCount submitted", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PapersFilterBar(
    teachers: List<Teacher>,
    departments: List<Department>,
    filters: SubmittedPapersFilters,
    onSetTeacherFilter: (String?) -> Unit,
    onSetDeptFilter: (String?) -> Unit,
    onSetSemesterFilter: (Int?) -> Unit,
    onSetShiftFilter: (Session?) -> Unit,
    onClearFilters: () -> Unit,
) {
    val teacherOptions = teachers.sortedBy { it.name }.map { CmsEntityOption(it.email, it.name) }
    val deptOptions = departments.sortedBy { it.name }.map { CmsEntityOption(it.deptId, "${it.code} · ${it.name}") }
    val semesterOptions = (1..8).map { CmsEntityOption(it.toString(), "Semester $it") }
    val shiftOptions = Session.entries.map { CmsEntityOption(it.name, it.name) }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("FILTER", color = ModMuted, style = CmsTextStyles.eyebrow)
            if (!filters.isEmpty) {
                TextButton(onClick = onClearFilters) { Text("Clear filters") }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DropdownChip(
                selectedLabel = teacherOptions.firstOrNull { it.id == filters.teacherEmail }?.label,
                emptyLabel = "All teachers",
                options = teacherOptions,
                onSelected = onSetTeacherFilter,
            )
            DropdownChip(
                selectedLabel = deptOptions.firstOrNull { it.id == filters.deptId }?.label,
                emptyLabel = "All departments",
                options = deptOptions,
                onSelected = onSetDeptFilter,
            )
            DropdownChip(
                selectedLabel = semesterOptions.firstOrNull { it.id == filters.semester?.toString() }?.label,
                emptyLabel = "All semesters",
                options = semesterOptions,
                onSelected = { onSetSemesterFilter(it?.toIntOrNull()) },
            )
            DropdownChip(
                selectedLabel = shiftOptions.firstOrNull { it.id == filters.shift?.name }?.label,
                emptyLabel = "All shifts",
                options = shiftOptions,
                onSelected = { onSetShiftFilter(it?.let(Session::valueOf)) },
            )
        }
    }
}

@Composable
private fun SubmittedPaperCard(submission: ExamPaperSubmission, onDownload: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(submission.fileName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${submission.subjectId} · Semester ${submission.semester}",
                        color = ModMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "Uploaded ${submission.uploadedAt.atZone(ZoneId.systemDefault()).format(PapersDateFormat)}" +
                            (submission.fileSizeBytes?.let { " · ${formatFileSize(it)}" } ?: ""),
                        color = ModMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = onDownload) { Text("Download") }
            }
            val submissionDescription = submission.description
            if (!submissionDescription.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(submissionDescription, color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> "%.0f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}
