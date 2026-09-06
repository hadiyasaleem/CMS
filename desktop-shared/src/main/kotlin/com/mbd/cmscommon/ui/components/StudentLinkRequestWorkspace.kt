package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.LinkRequestStatus
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.ui.theme.ModRedTint
import com.mbd.cmscommon.util.Outcome
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val LinkCanvas = ModGround
private val LinkGold = ModWarn
private val LinkRed = ModAccent
private val LinkDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

data class StudentLinkRequestUiState(
    val departments: List<Department> = emptyList(),
    val sessions: List<AcademicSession> = emptyList(),
    val latestRequest: StudentLinkRequest? = null,
    val submitState: Outcome<Unit> = Outcome.Success(Unit),
    val refreshing: Boolean = false,
    val refreshError: String? = null,
)

data class StudentLinkRequestActions(
    val onRefresh: () -> Unit,
    val onSubmit: (String, String, String, String, String, String, String, String) -> Unit,
)

@Composable
fun StudentLinkRequestWorkspace(state: StudentLinkRequestUiState, actions: StudentLinkRequestActions, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(LinkCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { LinkHeader(state.refreshing, actions.onRefresh) }

        if (!state.refreshError.isNullOrBlank()) {
            item { CmsNotice(state.refreshError, tone = NoticeTone.Error, actionLabel = "Retry", onAction = actions.onRefresh) }
        }

        item { LinkGuidanceCard() }

        val request = state.latestRequest
        when (request?.status) {
            LinkRequestStatus.PENDING -> item { PendingRequestCard(request) }
            LinkRequestStatus.REJECTED -> {
                item { RejectedRequestCard(request) }
                item { LinkRequestForm(state, actions.onSubmit) }
            }
            LinkRequestStatus.APPROVED -> item { CmsNotice("Your request was approved. Refreshing your account access now.", tone = NoticeTone.Success) }
            null -> item { LinkRequestForm(state, actions.onSubmit) }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun LinkHeader(refreshing: Boolean, onRefresh: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ACCOUNT LINKING", color = LinkGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("Connect your college record", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            }
            TextButton(onClick = onRefresh, enabled = !refreshing) { Text(if (refreshing) "Checking" else "Refresh", color = CmsTheme.colors.onInk) }
        }
    }
}

@Composable
private fun LinkGuidanceCard() {
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Text(
            "Submit one accurate claim so a reviewer can match your account safely. Enter values exactly as they appear in college records.",
            modifier = Modifier.padding(16.dp),
            color = ModMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PendingRequestCard(request: StudentLinkRequest) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, LinkGold.copy(alpha = 0.3f))) {
        Column(Modifier.padding(16.dp)) {
            StatusBadge("REQUEST PENDING", BadgeTone.Gold)
            Spacer(Modifier.height(8.dp))
            Text("Submitted ${request.createdAt.atZone(ZoneId.systemDefault()).format(LinkDateFormat)}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            if (request.attemptCount > 1) {
                Text("Attempt ${request.attemptCount}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(10.dp))
            ClaimGrid(request)
        }
    }
}

@Composable
private fun RejectedRequestCard(request: StudentLinkRequest) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModRedTint, border = BorderStroke(1.dp, LinkRed.copy(alpha = 0.3f))) {
        Column(Modifier.padding(16.dp)) {
            StatusBadge("CORRECTION REQUIRED", BadgeTone.Error)
            Spacer(Modifier.height(8.dp))
            Text(request.rejectionReason?.takeIf { it.isNotBlank() } ?: "Not provided", color = LinkRed, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text("Review your details and submit a corrected request.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ClaimGrid(request: StudentLinkRequest) {
    Column {
        ClaimLine("Roll number", request.rollNumberClaimed)
        ClaimLine("Full name", request.nameClaimed ?: "Not provided")
        ClaimLine("CNIC / B-Form", request.cnicClaimed ?: "Not provided")
        ClaimLine("Date of birth", request.dobClaimed ?: "Not provided")
        ClaimLine("University roll", request.universityRollClaimed ?: "Not provided")
        ClaimLine("Registration", request.registrationNoClaimed ?: "Not provided")
    }
}

@Composable
private fun ClaimLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = ModMuted, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LinkRequestForm(
    state: StudentLinkRequestUiState,
    onSubmit: (String, String, String, String, String, String, String, String) -> Unit,
) {
    var deptId by remember { mutableStateOf<String?>(null) }
    var sessionId by remember { mutableStateOf<String?>(null) }
    var rollNumber by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var universityRoll by remember { mutableStateOf("") }
    var registrationNo by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val sessionsForDept = state.sessions.filter { deptId == null || it.deptId == deptId }
    val busy = state.submitState is Outcome.Loading
    val valid = sessionId != null && rollNumber.isNotBlank() && name.isNotBlank() && cnic.isNotBlank()

    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text("Choose the class record a reviewer should search.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            if (state.departments.isEmpty()) {
                Text("No departments are available yet. Refresh to try again.", color = LinkRed, style = MaterialTheme.typography.bodySmall)
            } else {
                CmsEntityPicker(
                    label = "Department",
                    selectedId = deptId,
                    options = state.departments.map { CmsEntityOption(it.deptId, it.name) },
                    onSelected = { deptId = it; sessionId = null },
                )
                Spacer(Modifier.height(10.dp))
                if (deptId == null || sessionsForDept.isEmpty()) {
                    Text("Select a department and shift first.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                } else {
                    CmsEntityPicker(
                        label = "Academic session",
                        selectedId = sessionId,
                        options = sessionsForDept.map { CmsEntityOption(it.sessionId, "${it.label} · ${it.shift}") },
                        onSelected = { sessionId = it },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            LinkFieldLabel("Class roll number")
            OutlinedTextField(value = rollNumber, onValueChange = { rollNumber = it }, placeholder = { Text("IT-21-09") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            LinkFieldLabel("Student name")
            OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("As on college record") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            LinkFieldLabel("CNIC / B-Form")
            OutlinedTextField(value = cnic, onValueChange = { cnic = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            CmsDateField(value = dob, onValueChange = { dob = it }, label = "Date of birth", optional = true)
            Spacer(Modifier.height(10.dp))
            Text("Optional identifiers can speed up a difficult match.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = universityRoll, onValueChange = { universityRoll = it }, label = { Text("University roll number (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = registrationNo, onValueChange = { registrationNo = it }, label = { Text("Registration number (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message to reviewer (optional)") }, placeholder = { Text("Add context only if needed") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(6.dp))
            Text("Session, class roll number, full name, and CNIC / B-Form are required.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            if (state.submitState is Outcome.Error) {
                Spacer(Modifier.height(8.dp))
                Text(state.submitState.message, color = LinkRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
            CmsPrimaryButton(
                text = if (busy) "Submitting..." else if (state.latestRequest != null) "Submit corrected request" else "Submit for verification",
                onClick = { onSubmit(sessionId ?: "", rollNumber.trim(), name.trim(), cnic.trim(), dob.trim(), universityRoll.trim(), registrationNo.trim(), message.trim()) },
                enabled = valid && !busy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LinkFieldLabel(text: String) {
    Text(text, color = ModMuted, style = CmsTextStyles.eyebrow)
    Spacer(Modifier.height(4.dp))
}

