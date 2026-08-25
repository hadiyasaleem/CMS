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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.mbd.cmscommon.domain.model.AdministratorAccount
import com.mbd.cmscommon.domain.model.AdministratorDirectorySnapshot
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Fine
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ProfileCanvas = Color(0xFFF7F5F0)
private val ProfileBlue = Color(0xFF24577A)
private val ProfileGreen = Color(0xFF2F6B4F)
private val ProfileRed = Color(0xFFB43A31)
private val ProfileDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

private data class ProfileMetric(val label: String, val value: String, val detail: String)

@Composable
fun AdministratorProfileWorkspace(
    accountKey: String,
    account: AdministratorAccount?,
    directory: AdministratorDirectorySnapshot?,
    loading: Boolean,
    errorMessage: String?,
    actionMessage: String?,
    onRetry: () -> Unit,
    onResetPassword: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmReset by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }

    val active = account?.status.equals("ACTIVE", ignoreCase = true)
    val metrics = listOf(
        ProfileMetric("Admins", directory?.accounts?.size?.toString() ?: "--", "Directory accounts"),
        ProfileMetric("Active", directory?.activeCount?.toString() ?: "--", "Available accounts"),
        ProfileMetric("Recent", directory?.recentlyActiveCount?.toString() ?: "--", "Signed in within 30 days"),
    )

    LazyColumn(modifier.fillMaxWidth().background(ProfileCanvas), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ProfileHero(account?.email ?: accountKey, "Administrator", if (active) "ACTIVE" else "INACTIVE", if (active) BadgeTone.Success else BadgeTone.Neutral, "Verified administrator account") }
        if (!errorMessage.isNullOrBlank()) item { ProfileNotice(errorMessage, ProfileRed, showProgress = false, action = "Retry", onAction = onRetry) }
        if (!actionMessage.isNullOrBlank()) item { ProfileNotice(actionMessage, ProfileGreen, showProgress = false, action = null, onAction = null) }
        if (loading) item { SkeletonRow() }
        item { ProfileMetrics(metrics) }
        item {
            ProfileSectionCard("Account identity", "Your verified college identity, access and record details") {
                ProfileInfoRow("Signed-in email", account?.email ?: accountKey)
                ProfileInfoRow("Account status", account?.status?.uppercase() ?: "UNKNOWN")
                ProfileInfoRow("Last sign in", account?.lastLoginAt?.let { formatDate(it) } ?: "Never signed in")
                ProfileInfoRow("Created", account?.createdAt?.let { formatDate(it) } ?: "Not recorded")
            }
        }
        item {
            ProfileSectionCard("Account security", "Recovery and session controls") {
                ProfileActionCard(account?.email ?: accountKey, onResetPassword = { confirmReset = true }, onSignOut = { confirmSignOut = true })
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }

    if (confirmReset) {
        ConfirmDestructiveActionDialog(
            title = "Send password reset",
            dependentSummary = "GGC-MBD will email a secure reset link to ${account?.email ?: accountKey}.",
            onConfirm = { onResetPassword(); confirmReset = false },
            onDismiss = { confirmReset = false },
        )
    }
    if (confirmSignOut) {
        ConfirmDestructiveActionDialog(
            title = "Sign out",
            dependentSummary = "You will need to sign in again to access this account.",
            onConfirm = { onSignOut(); confirmSignOut = false },
            onDismiss = { confirmSignOut = false },
        )
    }
}

@Composable
fun TeacherProfileWorkspace(
    profile: Teacher?,
    accountKey: String,
    departmentName: String?,
    assignments: List<ResolvedAssignment>,
    loading: Boolean,
    errorMessage: String?,
    actionMessage: String?,
    onResetPassword: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmReset by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }
    val permissions = profile?.permissions

    val metrics = listOf(
        ProfileMetric("Classes", assignments.distinctBy { it.sessionId to it.courseCode }.size.toString(), "Current timetable-derived workload"),
        ProfileMetric("Sessions", assignments.map { it.sessionId }.distinct().size.toString(), "Active cohorts"),
    )

    LazyColumn(modifier.fillMaxWidth().background(ProfileCanvas), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ProfileHero(profile?.name ?: accountKey, "Teacher", (profile?.status?.name ?: "ACTIVE"), BadgeTone.Success, "Faculty member") }
        if (!errorMessage.isNullOrBlank()) item { ProfileNotice(errorMessage, ProfileRed, showProgress = false, action = null, onAction = null) }
        if (!actionMessage.isNullOrBlank()) item { ProfileNotice(actionMessage, ProfileGreen, showProgress = false, action = null, onAction = null) }
        if (loading) item { SkeletonRow() }
        item { ProfileMetrics(metrics) }
        item {
            ProfileSectionCard("Professional details", "Faculty role and expertise") {
                ProfileInfoRow("Designation", profile?.designation ?: "Not recorded")
                ProfileInfoRow("Department", departmentName ?: "Not recorded")
                ProfileInfoRow("Qualification", profile?.qualification ?: "Not recorded")
                ProfileInfoRow("Specialization", profile?.specialization ?: "Not recorded")
                ProfileInfoRow("Office", profile?.officeRoom ?: "Not recorded")
            }
        }
        item {
            ProfileSectionCard("Contact", "How the college can reach you") {
                ProfileInfoRow("College email", profile?.email ?: accountKey)
                ProfileInfoRow("Phone", profile?.phone ?: "Not recorded")
            }
        }
        item {
            ProfileSectionCard("Permissions", "Capabilities assigned by an administrator") {
                PermissionLine("Approve student link requests", permissions?.canApproveLinkRequests == true)
                PermissionLine("Edit class timetables", permissions?.canEditTimetable == true)
                PermissionLine("Send notifications", permissions?.canSendNotifications == true)
                PermissionLine("Manage datesheets", permissions?.canManageDatesheets == true)
            }
        }
        item {
            ProfileSectionCard("Teaching assignments", "Linked access and student responsibilities") {
                if (assignments.isEmpty()) {
                    ProfileEmptyLine("No teaching assignments are currently linked to this account.")
                } else {
                    assignments.forEach { AssignmentCard(it) }
                }
            }
        }
        item {
            ProfileSectionCard("Account security", "Recovery and session controls") {
                ProfileActionCard(profile?.email ?: accountKey, onResetPassword = { confirmReset = true }, onSignOut = { confirmSignOut = true })
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }

    if (confirmReset) {
        ConfirmDestructiveActionDialog(
            title = "Send password reset",
            dependentSummary = "GGC-MBD will email a secure reset link to ${profile?.email ?: accountKey}.",
            onConfirm = { onResetPassword(); confirmReset = false },
            onDismiss = { confirmReset = false },
        )
    }
    if (confirmSignOut) {
        ConfirmDestructiveActionDialog(
            title = "Sign out",
            dependentSummary = "You will need to sign in again to access this account.",
            onConfirm = { onSignOut(); confirmSignOut = false },
            onDismiss = { confirmSignOut = false },
        )
    }
}

@Composable
fun StudentOwnProfileWorkspace(
    session: AcademicSession?,
    studentName: String,
    rollNumber: String,
    gpa: Double?,
    cgpa: Double?,
    linkedEmail: String?,
    profile: StudentProfile?,
    departmentName: String?,
    accountKey: String,
    fines: List<Fine>,
    loading: Boolean,
    errorMessage: String?,
    actionMessage: String?,
    onRetry: () -> Unit,
    onResetPassword: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmReset by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }

    val metrics = listOf(
        ProfileMetric("Semester", session?.currentSemester?.toString() ?: "--", "Of 8"),
        ProfileMetric("GPA", gpa?.let { "%.2f".format(it) } ?: "--", "Semester GPA"),
        ProfileMetric("CGPA", cgpa?.let { "%.2f".format(it) } ?: "--", "Cumulative standing"),
    )

    LazyColumn(modifier.fillMaxWidth().background(ProfileCanvas), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ProfileHero(studentName, "Student", "ENROLLED", BadgeTone.Success, "Class roll $rollNumber") }
        if (!errorMessage.isNullOrBlank()) item { ProfileNotice(errorMessage, ProfileRed, showProgress = false, action = "Retry", onAction = onRetry) }
        if (!actionMessage.isNullOrBlank()) item { ProfileNotice(actionMessage, ProfileGreen, showProgress = false, action = null, onAction = null) }
        if (loading) item { SkeletonRow() }
        item { ProfileMetrics(metrics) }
        item {
            ProfileSectionCard("Current program and performance", "Academic standing") {
                ProfileInfoRow("Department", departmentName ?: "Not recorded")
                ProfileInfoRow("Academic session", session?.label ?: "Not recorded")
                ProfileInfoRow("Shift", session?.shift?.name ?: "Not recorded")
                ProfileInfoRow("Class roll", rollNumber)
            }
        }
        item {
            ProfileSectionCard("University identifiers", "Official enrollment references") {
                ProfileInfoRow("University roll", profile?.universityRollNo ?: "Not recorded")
                ProfileInfoRow("Registration number", profile?.registrationNo ?: "Not recorded")
            }
        }
        item {
            ProfileSectionCard("Personal details", "Identity information held in your record") {
                ProfileInfoRow("Father's name", profile?.fatherName ?: "Not recorded")
                ProfileInfoRow("Guardian", profile?.guardianName ?: "Not recorded")
                ProfileInfoRow("CNIC / B-Form", profile?.cnicBform ?: "Not recorded")
                ProfileInfoRow("Date of birth", profile?.dob ?: "Not recorded")
                ProfileInfoRow("Gender", profile?.gender ?: "Not recorded")
            }
        }
        item {
            ProfileSectionCard("Contact and address", "Student and guardian contact details") {
                ProfileInfoRow("Personal email", profile?.personalEmail ?: "Not recorded")
                ProfileInfoRow("Phone", profile?.phone ?: "Not recorded")
                ProfileInfoRow("Guardian phone", profile?.guardianPhone ?: "Not recorded")
                ProfileInfoRow("Current address", profile?.currentAddress ?: "Not recorded")
                ProfileInfoRow("Permanent address", profile?.permanentAddress ?: "Not recorded")
            }
        }
        item {
            ProfileSectionCard("Fines", "Informational amounts issued to this record") {
                if (fines.isEmpty()) {
                    ProfileEmptyLine("No fines recorded")
                } else {
                    fines.forEach { FineLine(it) }
                }
            }
        }
        item {
            ProfileSectionCard("Account security", "Recovery and session controls") {
                ProfileActionCard(linkedEmail ?: accountKey, onResetPassword = { confirmReset = true }, onSignOut = { confirmSignOut = true })
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }

    if (confirmReset) {
        ConfirmDestructiveActionDialog(
            title = "Send password reset",
            dependentSummary = "GGC-MBD will email a secure reset link to ${linkedEmail ?: accountKey}.",
            onConfirm = { onResetPassword(); confirmReset = false },
            onDismiss = { confirmReset = false },
        )
    }
    if (confirmSignOut) {
        ConfirmDestructiveActionDialog(
            title = "Sign out",
            dependentSummary = "You will need to sign in again to access this account.",
            onConfirm = { onSignOut(); confirmSignOut = false },
            onDismiss = { confirmSignOut = false },
        )
    }
}

@Composable
private fun ProfileHero(name: String, role: String, status: String, statusTone: BadgeTone, supporting: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarInitials(name, size = 52)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(role.uppercase(), color = CmsTheme.colors.onInk.copy(alpha = 0.7f), style = CmsTextStyles.eyebrow)
                Text(name, color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Text(supporting, color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
            StatusBadge(status, statusTone)
        }
    }
}

@Composable
private fun ProfileMetrics(metrics: List<ProfileMetric>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        metrics.forEach { metric ->
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
                Column(Modifier.padding(14.dp)) {
                    Text(metric.value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text(metric.label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
                    Text(metric.detail, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String, valueColor: Color = Color(0xFF252321)) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        Text(value, color = valueColor, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PermissionLine(label: String, granted: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        StatusBadge(if (granted) "GRANTED" else "NOT GRANTED", if (granted) BadgeTone.Success else BadgeTone.Neutral)
    }
}

@Composable
private fun AssignmentCard(assignment: ResolvedAssignment) {
    Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(10.dp), color = Color(0xFFF7F5F0)) {
        Column(Modifier.padding(10.dp)) {
            Text(assignment.subjectLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text("${assignment.sessionLabel} · ${assignment.courseCode}", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FineLine(fine: Fine) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.weight(1f)) {
            Text(fine.category, style = MaterialTheme.typography.bodyMedium)
            Text(fine.reason.ifBlank { "Issue details not recorded" }, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        }
        Text("Rs ${fine.amount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ProfileEmptyLine(text: String) {
    Text(text, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun ProfileNotice(message: String, color: Color, showProgress: Boolean, action: String?, onAction: (() -> Unit)?) {
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
private fun ProfileActionCard(email: String, onResetPassword: () -> Unit, onSignOut: () -> Unit) {
    Column {
        Text(email, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onResetPassword) { Text("Send password reset") }
            TextButton(onClick = onSignOut) { Text("Sign out", color = ProfileBlue) }
        }
    }
}

private fun formatDate(instant: Instant): String = instant.atZone(ZoneId.systemDefault()).format(ProfileDateFormatter)
