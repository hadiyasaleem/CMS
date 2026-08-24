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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.MarkEditRequestDetails
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.markEditReviewQuality
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val MarkGreen = Color(0xFF2F6B4F)
private val MarkAmber = Color(0xFF9A651B)
private val MarkRed = Color(0xFFB43A31)
private val MarkBlue = Color(0xFF24577A)
private val MarkDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

enum class MarkRequestFilter(val label: String) {
    ALL("All"),
    INCREASES("Increases"),
    DECREASES("Decreases"),
    NO_REASON("No reason"),
    ATTENTION("Needs attention"),
    BLOCKED("Approval blocked"),
}

enum class MarkRequestSort(val label: String) {
    OLDEST("Oldest"),
    NEWEST("Newest"),
    LARGEST_CHANGE("Largest change"),
}

@Composable
fun MarkEditRequestReviewWorkspace(
    requests: List<MarkEditRequest>,
    details: Map<String, MarkEditRequestDetails>,
    sessions: List<AcademicSession>,
    departments: List<Department>,
    teachers: List<Teacher>,
    loading: Boolean,
    busyRequestId: String?,
    rowErrors: Map<String, String>,
    notice: String?,
    errorMessage: String?,
    onApprove: (MarkEditRequest) -> Unit,
    onReject: (MarkEditRequest) -> Unit,
    onRefresh: () -> Unit,
    onConsumeNotice: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(MarkRequestFilter.ALL) }
    var sort by remember { mutableStateOf(MarkRequestSort.NEWEST) }
    var approvalTarget by remember { mutableStateOf<MarkEditRequest?>(null) }
    var rejectionTarget by remember { mutableStateOf<MarkEditRequest?>(null) }

    fun sessionLabel(sessionId: String): String {
        val session = sessions.firstOrNull { it.sessionId == sessionId }
        val dept = departments.firstOrNull { it.deptId == session?.deptId }?.name
        return if (session != null) "${dept ?: session.deptId} ${session.label}" else "Session details unavailable"
    }

    fun studentName(request: MarkEditRequest): String =
        details[request.id]?.studentName?.takeIf { it.isNotBlank() } ?: "Student name unavailable"

    val filtered = requests.filter { request ->
        val quality = markEditReviewQuality(request)
        val delta = request.requestedScore - (request.currentScore ?: 0)
        val matchesQuery = query.isBlank() ||
            studentName(request).contains(query, ignoreCase = true) ||
            request.rollNumber.contains(query, ignoreCase = true) ||
            request.courseCode.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            MarkRequestFilter.ALL -> true
            MarkRequestFilter.INCREASES -> delta > 0
            MarkRequestFilter.DECREASES -> delta < 0
            MarkRequestFilter.NO_REASON -> request.reason.isNullOrBlank()
            MarkRequestFilter.ATTENTION -> quality.needsAttention
            MarkRequestFilter.BLOCKED -> quality.blocksApproval
        }
        matchesQuery && matchesFilter
    }

    val visible = when (sort) {
        MarkRequestSort.OLDEST -> filtered.sortedBy { it.requestedAt }
        MarkRequestSort.NEWEST -> filtered.sortedByDescending { it.requestedAt }
        MarkRequestSort.LARGEST_CHANGE -> filtered.sortedByDescending { kotlin.math.abs(it.requestedScore - (it.currentScore ?: 0)) }
    }

    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { MarkRequestHero(requests.size) }

        if (!errorMessage.isNullOrBlank()) {
            item { MarkRequestNotice(errorMessage, MarkRed, onClearError) }
        }
        if (!notice.isNullOrBlank()) {
            item { MarkRequestNotice(notice, MarkGreen, onConsumeNotice) }
        }

        item { MarkSummaryCard(requests) }

        item {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by student, roll, or course") },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MarkRequestFilter.entries.forEach { option ->
                        CmsChip(option.label, selected = filter == option, onClick = { filter = option })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("SORT: ${sort.label}", color = Color(0xFF716B64), style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MarkRequestSort.entries.forEach { option ->
                        CmsChip(option.label, selected = sort == option, onClick = { sort = option })
                    }
                }
            }
        }

        when {
            loading -> items(3) { SkeletonRow() }
            requests.isEmpty() -> item { MarkRequestEmpty(filtered = false, onClearFilters = {}) }
            visible.isEmpty() -> item { MarkRequestEmpty(filtered = true, onClearFilters = { query = ""; filter = MarkRequestFilter.ALL }) }
            else -> items(visible, key = { it.id }) { request ->
                MarkRequestCard(
                    request = request,
                    studentName = studentName(request),
                    subjectName = details[request.id]?.subjectName?.takeIf { it.isNotBlank() } ?: "Subject details unavailable",
                    sessionLabel = sessionLabel(request.sessionId),
                    busy = busyRequestId == request.id,
                    rowError = rowErrors[request.id],
                    now = Instant.now(),
                    onApprove = { approvalTarget = request },
                    onReject = { rejectionTarget = request },
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    approvalTarget?.let { request ->
        AlertDialog(
            onDismissRequest = { approvalTarget = null },
            title = { Text("Score change review", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Approve the requested change for ${studentName(request)} in ${request.courseCode}?") },
            confirmButton = { TextButton(onClick = { onApprove(request); approvalTarget = null }) { Text("Approve") } },
            dismissButton = { TextButton(onClick = { approvalTarget = null }) { Text("Cancel") } },
        )
    }

    rejectionTarget?.let { request ->
        AlertDialog(
            onDismissRequest = { rejectionTarget = null },
            title = { Text("Reject request", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Reject the requested score change for ${studentName(request)}?") },
            confirmButton = { TextButton(onClick = { onReject(request); rejectionTarget = null }) { Text("Reject") } },
            dismissButton = { TextButton(onClick = { rejectionTarget = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun MarkRequestHero(count: Int) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Column(Modifier.padding(20.dp)) {
            Text("ASSESSMENT CONTROL", color = MarkAmber, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Mark edit request", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("$count in the review queue", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MarkSummaryCard(requests: List<MarkEditRequest>) {
    val blocked = requests.count { markEditReviewQuality(it).blocksApproval }
    val needsAttention = requests.count { markEditReviewQuality(it).needsAttention }
    val increases = requests.count { it.requestedScore > (it.currentScore ?: 0) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MarkStatLabel("REVIEW QUEUE", requests.size.toString(), Modifier.weight(1f))
        MarkStatLabel("BLOCKED", blocked.toString(), Modifier.weight(1f), alert = blocked > 0)
        MarkStatLabel("NEEDS ATTENTION", needsAttention.toString(), Modifier.weight(1f), alert = needsAttention > 0)
        MarkStatLabel("INCREASES", increases.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun MarkStatLabel(label: String, value: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) MarkRed else Color(0xFF252321), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label, color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun MarkRequestCard(
    request: MarkEditRequest,
    studentName: String,
    subjectName: String,
    sessionLabel: String,
    busy: Boolean,
    rowError: String?,
    now: Instant,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    val quality = markEditReviewQuality(request)
    val delta = request.requestedScore - (request.currentScore ?: 0)

    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, if (quality.blocksApproval) MarkRed.copy(alpha = 0.3f) else Color(0xFFE5E0D7))) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Roll ${request.rollNumber} · $subjectName · ${request.examType}", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                    Text(sessionLabel, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
                if (quality.blocksApproval) StatusBadge("BLOCKED", BadgeTone.Error)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MarkScoreValue("CURRENT", request.currentScore?.toString() ?: "--", MarkBlue)
                MarkScoreValue("REQUESTED", request.requestedScore.toString(), if (delta >= 0) MarkGreen else MarkRed)
            }
            Spacer(Modifier.height(8.dp))
            Text("Requested ${relativeRequestAge(request.requestedAt, now)}", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            Text("TEACHER'S REASON", color = Color(0xFF716B64), style = CmsTextStyles.eyebrow)
            Text(request.reason?.takeIf { it.isNotBlank() } ?: "No reason was supplied. Confirm the change before approval.", color = Color(0xFF625E58), style = MaterialTheme.typography.bodyMedium)
            if (quality.blockingIssues.isNotEmpty() || quality.warnings.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                (quality.blockingIssues + quality.warnings).forEach { issue ->
                    Text("· $issue", color = if (quality.blocksApproval) MarkRed else MarkAmber, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (!rowError.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(rowError, color = MarkRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onApprove, enabled = !quality.blocksApproval && !busy) { Text(if (busy) "Working..." else "Approve") }
                TextButton(onClick = onReject, enabled = !busy) { Text("Reject", color = CmsTheme.colors.accent) }
            }
        }
    }
}

@Composable
private fun MarkScoreValue(label: String, score: String, tint: Color) {
    Surface(shape = RoundedCornerShape(10.dp), color = tint.copy(alpha = 0.1f), border = BorderStroke(1.dp, tint.copy(alpha = 0.25f))) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(score, color = tint, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(label, color = tint, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun MarkRequestNotice(message: String, color: Color, onDismiss: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = color, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onDismiss) { Text("Dismiss", color = color) }
        }
    }
}

@Composable
private fun MarkRequestEmpty(filtered: Boolean, onClearFilters: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (filtered) "No matching requests" else "Review queue clear", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (filtered) "Try a different search or filter." else "There are no pending score changes. New teacher requests will appear here.",
                color = Color(0xFF77716A),
                style = MaterialTheme.typography.bodySmall,
            )
            if (filtered) {
                Spacer(Modifier.height(12.dp))
                CmsPrimaryButton(text = "Clear filters", onClick = onClearFilters)
            }
        }
    }
}

private fun relativeRequestAge(requestedAt: Instant, now: Instant): String {
    val minutes = Duration.between(requestedAt, now).toMinutes()
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes minute${if (minutes == 1L) "" else "s"} ago"
        minutes < 1440 -> "${minutes / 60} hour${if (minutes / 60 == 1L) "" else "s"} ago"
        else -> "on ${requestedAt.atZone(ZoneId.systemDefault()).format(MarkDateFormat)}"
    }
}
