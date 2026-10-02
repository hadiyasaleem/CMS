package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.controller.inScope
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.controller.departmentScopeOptions
import com.mbd.cmscommon.domain.model.shiftForRoll
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val LinkGreen = ModSuccess
private val LinkGold = ModWarn
private val LinkRed = ModAccent
private val LinkClaimDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

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
    onApprove: (StudentLinkRequest, Boolean) -> Unit,
    onReject: (StudentLinkRequest, String) -> Unit,
    onConsumeNotice: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Department -> Session -> Shift filter over the claimed session and roll number.
    var filterScope by remember { mutableStateOf(ShiftScope.ALL) }
    val requests = requests.inScope(filterScope, sessions)
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(LinkRequestSort.NEWEST) }
    var approvalTarget by remember { mutableStateOf<StudentLinkRequest?>(null) }
    var rejectionTarget by remember { mutableStateOf<StudentLinkRequest?>(null) }

    if (access != LinkRequestAccess.GRANTED) {
        LinkRequestAccessState(access)
        return
    }

    // The claimed roll number's serial decides the shift (Morning block first, Evening above it).
    fun sessionLabel(request: StudentLinkRequest): String {
        val session = sessions.firstOrNull { it.sessionId == request.sessionIdClaimed }
        val dept = departments.firstOrNull { it.deptId == session?.deptId }?.name
        val shift = session?.let { shiftForRoll(it, request.rollNumberClaimed)?.label ?: it.shiftMode.label }
        return if (session != null) "${dept ?: session.deptId} ${session.label} · $shift" else "No session selected"
    }

    val filtered = requests.filter { request ->
        query.isBlank() ||
            (request.nameClaimed ?: "").contains(query, ignoreCase = true) ||
            request.rollNumberClaimed.contains(query, ignoreCase = true) ||
            request.requestedByUid.contains(query, ignoreCase = true)
    }

    val visible = when (sort) {
        LinkRequestSort.OLDEST -> filtered.sortedBy { it.createdAt }
        LinkRequestSort.NEWEST -> filtered.sortedByDescending { it.createdAt }
        LinkRequestSort.ATTEMPTS -> filtered.sortedByDescending { it.attemptCount }
    }

    val listState = rememberLazyListState()
    WithVerticalScrollbar(listState) {
    LazyColumn( state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ShiftScopeSelector(filterScope, departmentScopeOptions(departments), sessions, { filterScope = it }) }

        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, onDismiss = onClearError) }
        }
        if (!notice.isNullOrBlank()) {
            item { CmsNotice(notice, tone = NoticeTone.Success, onDismiss = onConsumeNotice) }
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
                Text("SORT", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LinkRequestSort.entries.forEach { option ->
                        CmsChip(option.label, selected = sort == option, onClick = { sort = option })
                    }
                }
            }
        }

        when {
            loading -> items(3) { SkeletonRow() }
            requests.isEmpty() -> item { LinkRequestEmptyState(filtered = false, onClearFilters = {}) }
            visible.isEmpty() -> item { LinkRequestEmptyState(filtered = true, onClearFilters = { query = "" }) }
            else -> items(visible, key = { it.requestId }) { request ->
                val key = linkRequestVerificationKey(request)
                LinkRequestCard(
                    request = request,
                    sessionLabel = sessionLabel(request),
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
    }

    approvalTarget?.let { request ->
        val verification = verifications[linkRequestVerificationKey(request)]
        val hasMismatch = verification?.state == RosterVerificationState.IDENTITY_MISMATCH
        AlertDialog(
            onDismissRequest = { approvalTarget = null },
            title = { Text("Approve student link?", style = MaterialTheme.typography.headlineSmall) },
            text = { DialogScrollBody {
                Column {
                    if (hasMismatch) {
                        Text(
                            "The claimed details don't fully match the official record -- review the identity claim below before approving.",
                            color = ModMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (verification.identityComparisons.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            IdentityCheckSummary(verification)
                        }
                    } else {
                        Text(
                            if (verification?.linkedEmail.isNullOrBlank()) {
                                "This connects ${request.requestedByUid} to roll ${request.rollNumberClaimed}."
                            } else {
                                "This will replace the existing link (${verification?.linkedEmail}) with ${request.requestedByUid}."
                            },
                        )
                    }
                    if (!verification?.linkedEmail.isNullOrBlank() && hasMismatch) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "This roll number is currently linked to ${verification?.linkedEmail}. That account will be delinked and replaced with ${request.requestedByUid}.",
                            color = LinkRed,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }},
            confirmButton = {
                TextButton(onClick = { onApprove(request, hasMismatch); approvalTarget = null }) {
                    Text("Approve link", color = if (hasMismatch) LinkRed else Color.Unspecified)
                }
            },
            dismissButton = { TextButton(onClick = { approvalTarget = null }) { Text("Cancel") } },
        )
    }

    rejectionTarget?.let { request ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectionTarget = null },
            title = { Text("Reject request", style = MaterialTheme.typography.headlineSmall) },
            text = { DialogScrollBody {
                Column {
                    Text("The reason is shown to the student.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            }},
            confirmButton = {
                TextButton(onClick = { onReject(request, reason); rejectionTarget = null }, enabled = reason.trim().length >= 4) { Text("Reject request") }
            },
            dismissButton = { TextButton(onClick = { rejectionTarget = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun LinkRequestAccessState(access: LinkRequestAccess) {
    Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
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
                color = ModMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
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
    val canApprove = verification?.state == RosterVerificationState.MATCHED ||
        verification?.state == RosterVerificationState.RELINK ||
        verification?.state == RosterVerificationState.IDENTITY_MISMATCH

    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(request.nameClaimed?.takeIf { it.isNotBlank() } ?: "Name not provided", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Roll ${request.rollNumberClaimed} · $sessionLabel", color = ModMuted, style = MaterialTheme.typography.bodySmall)
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
                quality.issues.forEach { issue -> Text("· $issue", color = ModMuted, style = MaterialTheme.typography.bodySmall) }
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
        Text(label, modifier = Modifier.weight(1f), color = ModMuted, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun IdentityCheckSummary(verification: LinkRequestVerification) {
    Column {
        Text("IDENTITY CLAIM", color = ModMuted, style = CmsTextStyles.eyebrow)
        verification.identityComparisons.forEach { comparison ->
            val tone = when (comparison.status) {
                IdentityClaimStatus.MATCHED -> LinkGreen
                IdentityClaimStatus.MISMATCHED -> LinkRed
                IdentityClaimStatus.OFFICIAL_MISSING -> LinkGold
                IdentityClaimStatus.NOT_CLAIMED -> ModMuted
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
private fun LinkRequestEmptyState(filtered: Boolean, onClearFilters: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (filtered) "No matching requests" else "Review queue is clear", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (filtered) "Try another search." else "There are no pending student account claims.",
                color = ModMuted,
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
