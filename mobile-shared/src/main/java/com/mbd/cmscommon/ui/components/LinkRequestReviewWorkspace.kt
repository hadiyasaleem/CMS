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
import com.mbd.cmscommon.controller.LinkRequestAccess
import com.mbd.cmscommon.controller.LinkRequestVerification
import com.mbd.cmscommon.controller.RosterVerificationState
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.IdentityClaimStatus
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.model.linkRequestClaimQuality
import com.mbd.cmscommon.domain.model.linkRequestVerificationKey
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val LinkGreen = Color(0xFF2F6B4F)
private val LinkGold = Color(0xFF9A651B)
private val LinkRed = Color(0xFFB43A31)
private val LinkClaimDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

enum class LinkRequestFilter(val label: String) {
    ALL("All requests"),
    READY("Ready"),
    NEEDS_ROSTER("Needs roster"),
    IDENTITY_MISMATCH("Identity conflict"),
    CLAIM_ISSUE("Claim issue"),
    RELINK("Relink"),
    RETRIES("Repeat attempts"),
}

enum class LinkRequestSort(val label: String) {
    OLDEST("Oldest first"),
    NEWEST("Newest first"),
    ATTEMPTS("Most attempts"),
}

@Composable
fun LinkRequestReviewWorkspace(
    requests: List<StudentLinkRequest>,
    sessions: List<AcademicSession>,
    departments: List<Department>,
    verifications: Map<String, LinkRequestVerification>,
    access: LinkRequestAccess,
    loading: Boolean,
    busyRequestId: String?,
    rowErrors: Map<String, String>,
    notice: String?,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onApprove: (StudentLinkRequest) -> Unit,
    onReject: (StudentLinkRequest, String) -> Unit,
    onConsumeNotice: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(LinkRequestFilter.ALL) }
    var sort by remember { mutableStateOf(LinkRequestSort.NEWEST) }
    var approvalTarget by remember { mutableStateOf<StudentLinkRequest?>(null) }
    var rejectionTarget by remember { mutableStateOf<StudentLinkRequest?>(null) }

    if (access != LinkRequestAccess.GRANTED) {
        LinkRequestAccessState(access)
        return
    }

    fun sessionLabel(sessionId: String?): String {
        val session = sessions.firstOrNull { it.sessionId == sessionId }
        val dept = departments.firstOrNull { it.deptId == session?.deptId }?.name
        return if (session != null) "${dept ?: session.deptId} ${session.label} ${session.shift}" else "No session selected"
    }

    val filtered = requests.filter { request ->
        val key = linkRequestVerificationKey(request)
        val verification = verifications[key]
        val matchesQuery = query.isBlank() ||
            (request.nameClaimed ?: "").contains(query, ignoreCase = true) ||
            request.rollNumberClaimed.contains(query, ignoreCase = true) ||
            request.requestedByUid.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            LinkRequestFilter.ALL -> true
            LinkRequestFilter.READY -> verification?.state == RosterVerificationState.MATCHED
            LinkRequestFilter.NEEDS_ROSTER -> verification?.state == RosterVerificationState.MISSING
            LinkRequestFilter.IDENTITY_MISMATCH -> verification?.state == RosterVerificationState.IDENTITY_MISMATCH
            LinkRequestFilter.CLAIM_ISSUE -> linkRequestClaimQuality(request).issues.isNotEmpty()
            LinkRequestFilter.RELINK -> verification?.state == RosterVerificationState.RELINK
            LinkRequestFilter.RETRIES -> request.attemptCount > 1
        }
        matchesQuery && matchesFilter
    }

    val visible = when (sort) {
        LinkRequestSort.OLDEST -> filtered.sortedBy { it.createdAt }
        LinkRequestSort.NEWEST -> filtered.sortedByDescending { it.createdAt }
        LinkRequestSort.ATTEMPTS -> filtered.sortedByDescending { it.attemptCount }
    }

    val ready = requests.count { verifications[linkRequestVerificationKey(it)]?.state == RosterVerificationState.MATCHED }
    val blocked = requests.count {
        val state = verifications[linkRequestVerificationKey(it)]?.state
        state == RosterVerificationState.MISSING || state == RosterVerificationState.IDENTITY_MISMATCH || state == RosterVerificationState.FAILED
    }
    val relinks = requests.count { verifications[linkRequestVerificationKey(it)]?.state == RosterVerificationState.RELINK }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { LinkRequestHero(requests.size) }

        if (!errorMessage.isNullOrBlank()) {
            item { LinkRequestNotice(errorMessage, LinkRed, onClearError) }
        }
        if (!notice.isNullOrBlank()) {
            item { LinkRequestNotice(notice, LinkGreen, onConsumeNotice) }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LinkSummaryTile("Pending", requests.size.toString(), Modifier.weight(1f))
                LinkSummaryTile("Ready", ready.toString(), Modifier.weight(1f))
                LinkSummaryTile("Blocked", blocked.toString(), Modifier.weight(1f), alert = blocked > 0)
                LinkSummaryTile("Relinks", relinks.toString(), Modifier.weight(1f))
            }
        }

        item {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name, roll number, or email") },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Text("SHOW", color = Color(0xFF716B64), style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinkRequestFilter.entries.forEach { option ->
                        CmsChip(option.label, selected = filter == option, onClick = { filter = option })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("SORT", color = Color(0xFF716B64), style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinkRequestSort.entries.forEach { option ->
                        CmsChip(option.label, selected = sort == option, onClick = { sort = option })
                    }
                }
            }
        }

        when {
            loading -> items(3) { SkeletonRow() }
            requests.isEmpty() -> item { LinkRequestEmptyState(filtered = false, onClearFilters = {}) }
            visible.isEmpty() -> item { LinkRequestEmptyState(filtered = true, onClearFilters = { query = ""; filter = LinkRequestFilter.ALL }) }
            else -> items(visible, key = { it.requestId }) { request ->
                val key = linkRequestVerificationKey(request)
                LinkRequestCard(
                    request = request,
                    sessionLabel = sessionLabel(request.sessionIdClaimed),
                    verification = verifications[key],
                    busy = busyRequestId == key,
                    rowError = rowErrors[key],
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
            title = { Text("Approve student link?", style = MaterialTheme.typography.headlineSmall) },
            text = {
                val verification = verifications[linkRequestVerificationKey(request)]
                Text(
                    if (verification?.linkedEmail.isNullOrBlank()) {
                        "This connects ${request.requestedByUid} to roll ${request.rollNumberClaimed}."
                    } else {
                        "This will replace the existing link (${verification?.linkedEmail}) with ${request.requestedByUid}."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { onApprove(request); approvalTarget = null }) { Text("Approve link") }
            },
            dismissButton = { TextButton(onClick = { approvalTarget = null }) { Text("Cancel") } },
        )
    }

    rejectionTarget?.let { request ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectionTarget = null },
            title = { Text("Reject request", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column {
                    Text("The reason is shown to the student.", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            },
            confirmButton = {
                TextButton(onClick = { onReject(request, reason); rejectionTarget = null }, enabled = reason.trim().length >= 4) { Text("Reject request") }
            },
            dismissButton = { TextButton(onClick = { rejectionTarget = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun LinkRequestAccessState(access: LinkRequestAccess) {
    Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(24.dp)) {
            Text(
                if (access == LinkRequestAccess.CHECKING) "Checking review permission" else "Review permission required",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (access == LinkRequestAccess.CHECKING) {
                    "Verifying whether this account can approve student link requests."
                } else {
                    "An Admin must grant the Approve link requests permission before this queue can be reviewed."
                },
                color = Color(0xFF77716A),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun LinkRequestHero(count: Int) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Column(Modifier.padding(20.dp)) {
            Text("ACCOUNT VERIFICATION", color = LinkGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Student link review", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("$count pending claim(s) awaiting a reviewer", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun LinkSummaryTile(label: String, value: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) LinkRed else Color(0xFF252321), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

private fun verificationTone(state: RosterVerificationState?): Pair<String, BadgeTone> = when (state) {
    RosterVerificationState.MATCHED -> "ROSTER MATCH" to BadgeTone.Success
    RosterVerificationState.RELINK -> "RELINK REQUIRED" to BadgeTone.Warning
    RosterVerificationState.IDENTITY_MISMATCH -> "IDENTITY CONFLICT" to BadgeTone.Error
    RosterVerificationState.MISSING -> "NO ROSTER MATCH" to BadgeTone.Error
    RosterVerificationState.FAILED -> "CHECK FAILED" to BadgeTone.Error
    RosterVerificationState.CHECKING, null -> "CHECKING" to BadgeTone.Neutral
}

@Composable
private fun LinkRequestCard(
    request: StudentLinkRequest,
    sessionLabel: String,
    verification: LinkRequestVerification?,
    busy: Boolean,
    rowError: String?,
    now: Instant,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    val (badgeLabel, badgeTone) = verificationTone(verification?.state)
    val quality = linkRequestClaimQuality(request)
    val canApprove = verification?.state == RosterVerificationState.MATCHED || verification?.state == RosterVerificationState.RELINK

    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(request.nameClaimed?.takeIf { it.isNotBlank() } ?: "Name not provided", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Roll ${request.rollNumberClaimed} · $sessionLabel", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge(badgeLabel, badgeTone)
            }
            Spacer(Modifier.height(8.dp))
            LinkRequestDetail("Requested account", request.requestedByUid)
            LinkRequestDetail("Submitted", relativeRequestAge(request.createdAt, now))
            if (request.attemptCount > 1) LinkRequestDetail("Attempts", "ATTEMPT ${request.attemptCount}")
            if (!verification?.linkedEmail.isNullOrBlank()) {
                LinkRequestDetail("Currently linked to", verification?.linkedEmail ?: "")
            }
            if (quality.issues.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("CLAIM ISSUE", color = LinkRed, style = CmsTextStyles.eyebrow)
                quality.issues.forEach { issue -> Text("· $issue", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall) }
            }
            if (verification != null && verification.identityComparisons.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                IdentityCheckSummary(verification)
            }
            if (!rowError.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(rowError, color = LinkRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onApprove, enabled = canApprove && !busy) { Text(if (busy) "Working..." else "Approve link") }
                TextButton(onClick = onReject, enabled = !busy) { Text("Reject request", color = CmsTheme.colors.accent) }
            }
        }
    }
}

@Composable
private fun LinkRequestDetail(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun IdentityCheckSummary(verification: LinkRequestVerification) {
    Column {
        Text("IDENTITY CLAIM", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        verification.identityComparisons.forEach { comparison ->
            val tone = when (comparison.status) {
                IdentityClaimStatus.MATCHED -> LinkGreen
                IdentityClaimStatus.MISMATCHED -> LinkRed
                IdentityClaimStatus.OFFICIAL_MISSING -> LinkGold
                IdentityClaimStatus.NOT_CLAIMED -> Color(0xFF77716A)
            }
            Text(
                "${comparison.field.label}: ${comparison.claimedValue ?: "not provided"} → ${comparison.officialValue ?: "not provided"}",
                color = tone,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun LinkRequestNotice(message: String, color: Color, onDismiss: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = color, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onDismiss) { Text("Dismiss", color = color) }
        }
    }
}

@Composable
private fun LinkRequestEmptyState(filtered: Boolean, onClearFilters: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (filtered) "No matching requests" else "Review queue is clear", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (filtered) "Try another search or review filter." else "There are no pending student account claims.",
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

private fun relativeRequestAge(createdAt: Instant, now: Instant): String {
    val hours = Duration.between(createdAt, now).toHours()
    return when {
        hours < 1 -> "Less than an hour ago"
        hours < 24 -> "$hours hour${if (hours == 1L) "" else "s"} ago"
        hours < 48 -> "Yesterday"
        else -> createdAt.atZone(ZoneId.systemDefault()).format(LinkClaimDateFormat)
    }
}
