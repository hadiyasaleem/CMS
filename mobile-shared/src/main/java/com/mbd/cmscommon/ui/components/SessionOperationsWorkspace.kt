package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.controller.promotionConfirmText
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.controller.capacityError
import com.mbd.cmscommon.controller.capacityForShiftSelection
import com.mbd.cmscommon.controller.studentCountsByShift
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
import com.mbd.cmscommon.domain.model.SemesterTerm
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
    currentSemesterTerm: SemesterTerm?,
    canPromote: Boolean,
    errorMessage: String?,
    notice: String?,
    teachers: List<Teacher>,
    onPromoteSession: () -> Unit,
    onUpdateDetails: (String, String, Int, ShiftMode) -> Unit,
    onOpenStudents: () -> Unit,
    onOpenTimetable: () -> Unit,
    onOpenSemester: (Int) -> Unit,
    onOpenFees: () -> Unit,
    onDeleteSession: () -> Unit,
    onClearError: () -> Unit,
    onConsumeNotice: () -> Unit,
    modifier: Modifier = Modifier,
    shiftCounts: Map<Session, Int> = studentCountsByShift(students),
) {
    var showEditDetails by remember { mutableStateOf(false) }
    var showPromoteConfirm by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val gpaRecorded = students.count { it.gpa != null }
    val configuredSemesters = subjectCounts.count { it.value > 0 }

    val actions = listOf(
        SessionAction("Students", "Roster, profiles, imports, and account links", shiftEnrolmentLine(session, students.size, shiftCounts), Icons.Outlined.School, onOpenStudents),
        SessionAction("Timetable", "Weekly periods, subjects, rooms, and teachers", "${periods.size} period(s) configured", Icons.Outlined.CalendarMonth, onOpenTimetable),
        SessionAction("Fee structure", "Fee heads and payment instructions for this intake", if (fee != null) "Rs ${fee.totalAmount}" else "Fee structure not configured", Icons.Outlined.Payments, onOpenFees),
    )

    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SessionIdentityCard(session, onEdit = { showEditDetails = true }) }

        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, onDismiss = onClearError) }
        }
        if (!notice.isNullOrBlank()) {
            item { CmsNotice(notice, tone = NoticeTone.Success, onDismiss = onConsumeNotice) }
        }

        item {
            SessionProgressCard(
                session,
                students.size,
                shiftCounts,
                gpaRecorded,
                configuredSemesters,
                currentSemesterTerm = currentSemesterTerm,
                canPromote = canPromote,
                onPromoteClick = { showPromoteConfirm = true },
            )
        }

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
        EditSessionDetailsDialog(
            session,
            teachers,
            enrolled = students.size,
            onDismiss = { showEditDetails = false },
            onSave = { program, incharge, capacity, mode -> onUpdateDetails(program, incharge, capacity, mode); showEditDetails = false },
        )
    }

    if (showPromoteConfirm) {
        val currentSemester = session?.currentSemester ?: 1
        val graduating = currentSemester >= 8
        AlertDialog(
            onDismissRequest = { showPromoteConfirm = false },
            title = { Text(if (graduating) "Graduate this class" else "Promote to semester ${currentSemester + 1}", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(promotionConfirmText(session))
            },
            confirmButton = { TextButton(onClick = { onPromoteSession(); showPromoteConfirm = false }) { Text(if (graduating) "Graduate" else "Promote") } },
            dismissButton = { TextButton(onClick = { showPromoteConfirm = false }) { Text("Cancel") } },
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
                if (session != null) {
                    Spacer(Modifier.height(6.dp))
                    StatusBadge(session.shiftMode.label.uppercase(), if (session.shiftMode == ShiftMode.EVENING) BadgeTone.Gold else BadgeTone.Navy)
                }
            }
            StatusBadge(if (session?.isActive == true) "ACTIVE" else "ARCHIVED", if (session?.isActive == true) BadgeTone.Success else BadgeTone.Neutral)
            TextButton(onClick = onEdit) { Text("Edit", color = CmsTheme.colors.onInk) }
        }
    }
}

@Composable
private fun SessionProgressCard(
    session: AcademicSession?,
    studentCount: Int,
    shiftCounts: Map<Session, Int>,
    gpaRecorded: Int,
    configuredSemesters: Int,
    currentSemesterTerm: SemesterTerm?,
    canPromote: Boolean,
    onPromoteClick: () -> Unit,
) {
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
            if (session != null && session.shifts.size > 1) {
                Spacer(Modifier.height(4.dp))
                Text(
                    session.shifts.joinToString(" · ") { "${it.label} ${shiftCounts[it] ?: 0}" },
                    color = ModMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(10.dp))
            ProgressLine("GPA coverage", gpaPercent, "$gpaRecorded / $studentCount recorded")
            Spacer(Modifier.height(10.dp))
            ProgressLine("Curriculum coverage", curriculumPercent, "$configuredSemesters / 8 semesters configured")
            Spacer(Modifier.height(10.dp))
            if (session?.isActive == true) {
                if (canPromote) {
                    TextButton(onClick = onPromoteClick) {
                        Text(if (session.currentSemester >= 8) "Mark as graduated" else "Promote semester")
                    }
                } else {
                    val hint = currentSemesterTerm?.endDate?.let { "Available after semester ends on $it." }
                        ?: "Set this semester's end date in Curriculum to enable promotion."
                    Text(hint, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
            } else if (session != null) {
                Text("This class has graduated.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
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
private fun EditSessionDetailsDialog(
    session: AcademicSession,
    teachers: List<Teacher>,
    enrolled: Int,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, ShiftMode) -> Unit,
) {
    var programName by remember { mutableStateOf(session.programName ?: "") }
    var inchargeEmail by remember { mutableStateOf(session.inchargeEmail ?: "") }
    var shifts by remember { mutableStateOf(session.shifts.toSet()) }
    var maxStudents by remember { mutableStateOf(session.maxStudents.toString()) }

    val mode = ShiftMode.of(shifts)
    val error = if (mode == null) "Tick Morning, Evening, or both." else capacityError(maxStudents, enrolled)
    val dropping = session.shifts.filterNot { it in shifts }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Academic session", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(value = programName, onValueChange = { programName = it }, label = { Text("Program name (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                CmsEntityPicker(
                    label = "Session in-charge",
                    selectedId = inchargeEmail.ifBlank { null },
                    options = teachers.sortedBy { it.name }.map { CmsEntityOption(it.email, it.name, it.email) },
                    onSelected = { inchargeEmail = it.orEmpty() },
                    emptyLabel = "In-charge not assigned",
                )
                Spacer(Modifier.height(10.dp))
                SessionShiftFields(
                    shifts = shifts,
                    onShiftsChange = { picked -> shifts = picked; maxStudents = capacityForShiftSelection(picked, maxStudents) },
                    capacity = maxStudents,
                    onCapacityChange = { maxStudents = it },
                    capacityMessage = if (mode == null) null else capacityError(maxStudents, enrolled),
                )
                if (dropping.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Removing the ${dropping.joinToString(" and ") { it.label }} shift only works once it has no students, fee structure, timetable periods or datesheet.",
                        color = SessionRed,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (mode == null) {
                    Spacer(Modifier.height(6.dp))
                    Text("Tick Morning, Evening, or both.", color = SessionRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (mode != null) maxStudents.toIntOrNull()?.let { onSave(programName.trim(), inchargeEmail.trim(), it, mode) } },
                enabled = error == null,
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** "38 enrolled · Morning 20 · Evening 18" for a two-shift session, "20 enrolled" otherwise. */
private fun shiftEnrolmentLine(session: AcademicSession?, total: Int, byShift: Map<Session, Int>): String {
    val shifts = session?.shifts.orEmpty()
    if (shifts.size < 2) return "$total enrolled"
    return "$total enrolled · " + shifts.joinToString(" · ") { "${it.label} ${byShift[it] ?: 0}" }
}
