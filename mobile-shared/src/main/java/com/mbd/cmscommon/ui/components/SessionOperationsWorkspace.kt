package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.ui.theme.ModRedTint

private val SessionGreen = ModSuccess
private val SessionGold = ModWarn
private val SessionRed = ModAccent
private val SessionBlue = ModInk

private data class SessionAction(val title: String, val subtitle: String, val detail: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun SessionOperationsWorkspace(
    session: AcademicSession?,
    students: List<SessionStudent>,
    subjectCounts: Map<Int, Int>,
    periods: List<SessionPeriod>,
    fee: SessionFeeStructure?,
    feeLoading: Boolean,
    errorMessage: String?,
    teachers: List<Teacher>,
    onSetSemester: (Int) -> Unit,
    onUpdateDetails: (String, String, Int) -> Unit,
    onOpenStudents: () -> Unit,
    onOpenTimetable: () -> Unit,
    onOpenSemester: (Int) -> Unit,
    onOpenFees: () -> Unit,
    onDeleteSession: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEditDetails by remember { mutableStateOf(false) }
    var pendingSemester by remember { mutableStateOf<Int?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    val gpaRecorded = students.count { it.gpa != null }
    val configuredSemesters = subjectCounts.count { it.value > 0 }

    val actions = listOf(
        SessionAction("Students", "Roster, profiles, imports, and account links", "${students.size} enrolled", Icons.Outlined.School, onOpenStudents),
        SessionAction("Timetable", "Weekly periods, subjects, rooms, and teachers", "${periods.size} period(s) configured", Icons.Outlined.CalendarMonth, onOpenTimetable),
        SessionAction("Fee structure", "Fee heads and payment instructions for this intake", if (fee != null) "Rs ${fee.totalAmount}" else "Fee structure not configured", Icons.Outlined.Payments, onOpenFees),
    )

    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SessionIdentityCard(session, onEdit = { showEditDetails = true }) }

        if (!errorMessage.isNullOrBlank()) {
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = SessionRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, SessionRed.copy(alpha = 0.25f))) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(errorMessage, modifier = Modifier.weight(1f), color = SessionRed, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onClearError) { Text("Dismiss") }
                    }
                }
            }
        }

        item { SessionProgressCard(session, students.size, gpaRecorded, configuredSemesters, onSelectSemester = { pendingSemester = it }) }

        item { WorkspaceSection("Operational areas", "Roster, timetable, and fee tools for this intake") }
        items(actions) { action -> SessionActionCard(action) }

        item { WorkspaceSection("Eight-semester curriculum", "Curriculum coverage across the eight-semester program") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..8).chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { sem ->
                            CurriculumSemesterCard(
                                semester = sem,
                                subjectCount = subjectCounts[sem] ?: 0,
                                isCurrent = session?.currentSemester == sem,
                                onClick = { onOpenSemester(sem) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        item { WorkspaceSection("Danger zone", "Irreversible changes to this session") }
        item { DangerZoneCard(students.size, onDelete = { confirmDelete = true }) }

        item { Spacer(Modifier.height(72.dp)) }
    }

    if (showEditDetails && session != null) {
        EditSessionDetailsDialog(session, onDismiss = { showEditDetails = false }, onSave = { program, incharge, capacity -> onUpdateDetails(program, incharge, capacity); showEditDetails = false })
    }

    pendingSemester?.let { semester ->
        val moveForward = session != null && semester > session.currentSemester
        AlertDialog(
            onDismissRequest = { pendingSemester = null },
            title = { Text("Change to semester $semester", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    if (moveForward) "This promotes the whole class from semester ${session?.currentSemester} to $semester." else "This moves the whole class back from semester ${session?.currentSemester} to $semester.",
                )
            },
            confirmButton = { TextButton(onClick = { onSetSemester(semester); pendingSemester = null }) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { pendingSemester = null }) { Text("Cancel") } },
        )
    }

    if (confirmDelete) {
        ConfirmDestructiveActionDialog(
            title = "Delete this session",
            dependentSummary = "This permanently removes ${session?.label ?: "this session"} and its ${students.size} enrolled student(s).",
            onConfirm = { onDeleteSession(); confirmDelete = false },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun SessionIdentityCard(session: AcademicSession?, onEdit: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ACADEMIC SESSION", color = SessionGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text(session?.label ?: "Session", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(session?.programName?.takeIf { it.isNotBlank() } ?: "Program name not configured", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
                Text(session?.inchargeEmail?.takeIf { it.isNotBlank() } ?: "Session in-charge not assigned", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
            StatusBadge(if (session?.isActive == true) "ACTIVE" else "ARCHIVED", if (session?.isActive == true) BadgeTone.Success else BadgeTone.Neutral)
            TextButton(onClick = onEdit) { Text("Edit", color = CmsTheme.colors.onInk) }
        }
    }
}

@Composable
private fun SessionProgressCard(session: AcademicSession?, studentCount: Int, gpaRecorded: Int, configuredSemesters: Int, onSelectSemester: (Int) -> Unit) {
    val maxStudents = session?.maxStudents ?: 0
    val capacityUsed = if (maxStudents == 0) 0f else (studentCount.toFloat() / maxStudents).coerceIn(0f, 1f)
    val gpaPercent = if (studentCount == 0) 0f else gpaRecorded.toFloat() / studentCount
    val curriculumPercent = configuredSemesters / 8f

    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text("CURRENT ACADEMIC POSITION", color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Semester ${session?.currentSemester ?: 1}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            ProgressLine("Student capacity", capacityUsed, if (maxStudents > 0) "$studentCount / $maxStudents enrolled" else "Seats remaining unknown")
            Spacer(Modifier.height(10.dp))
            ProgressLine("GPA coverage", gpaPercent, "$gpaRecorded / $studentCount recorded")
            Spacer(Modifier.height(10.dp))
            ProgressLine("Curriculum coverage", curriculumPercent, "$configuredSemesters / 8 semesters configured")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = { (session?.currentSemester ?: 1).let { if (it > 1) onSelectSemester(it - 1) } }) { Text("Previous semester") }
                TextButton(onClick = { (session?.currentSemester ?: 1).let { if (it < 8) onSelectSemester(it + 1) } }) { Text("Next semester") }
            }
        }
    }
}

@Composable
private fun ProgressLine(label: String, percent: Float, detail: String) {
    Column {
        Row {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("${(percent * 100).toInt()}%", color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = { percent }, modifier = Modifier.fillMaxWidth().height(6.dp), color = SessionBlue, trackColor = ModTrack)
        Spacer(Modifier.height(2.dp))
        Text(detail, color = ModMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun WorkspaceSection(title: String, subtitle: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, color = ModMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SessionActionCard(action: SessionAction) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = action.onClick),
        shape = RoundedCornerShape(16.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(action.icon, contentDescription = null, tint = SessionBlue)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(action.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(action.subtitle, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                Text(action.detail, color = SessionBlue, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun CurriculumSemesterCard(semester: Int, subjectCount: Int, isCurrent: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrent) SessionBlue.copy(alpha = 0.08f) else ModSurface,
        border = BorderStroke(1.dp, if (isCurrent) SessionBlue.copy(alpha = 0.4f) else ModTrack),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Semester $semester", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                if (isCurrent) StatusBadge("CURRENT", BadgeTone.Navy)
            }
            Text(if (subjectCount > 0) "$subjectCount subject(s)" else "Subjects not configured", color = if (subjectCount > 0) ModMuted else SessionGold, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DangerZoneCard(studentCount: Int, onDelete: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModRedTint, border = BorderStroke(1.dp, SessionRed.copy(alpha = 0.3f))) {
        Column(Modifier.padding(16.dp)) {
            Text("DANGER ZONE", color = SessionRed, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Delete this session", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("This permanently removes the session and its $studentCount enrolled student(s).", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onDelete) { Text("Delete session", color = SessionRed) }
        }
    }
}

@Composable
private fun EditSessionDetailsDialog(session: AcademicSession, onDismiss: () -> Unit, onSave: (String, String, Int) -> Unit) {
    var programName by remember { mutableStateOf(session.programName ?: "") }
    var inchargeEmail by remember { mutableStateOf(session.inchargeEmail ?: "") }
    var maxStudents by remember { mutableStateOf(session.maxStudents.toString()) }

    val parsedCapacity = maxStudents.toIntOrNull()
    val minimumCapacity = session.maxStudents.let { 1 }
    val error = if (parsedCapacity == null || parsedCapacity < minimumCapacity) "Capacity must be between $minimumCapacity and 500." else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Academic session", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(value = programName, onValueChange = { programName = it }, label = { Text("Program name (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = inchargeEmail, onValueChange = { inchargeEmail = it }, label = { Text("Session in-charge") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = maxStudents, onValueChange = { maxStudents = it }, label = { Text("Student capacity") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = SessionRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { parsedCapacity?.let { onSave(programName.trim(), inchargeEmail.trim(), it) } }, enabled = error == null) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
